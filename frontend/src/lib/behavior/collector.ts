interface BehaviorEvent {
  type: string;
  timestamp: number;
  data: Record<string, unknown>;
}

type EventCallback = (event: BehaviorEvent) => void;

const RATE_LIMIT_WINDOW = 100; // max events per window
const RATE_LIMIT_RESET = 1000; // reset window in ms

class BehaviorCollector {
  private listeners: EventCallback[] = [];
  private events: BehaviorEvent[] = [];
  private flushTimer: ReturnType<typeof setInterval> | null = null;
  private _enabled = true;
  private rateCount = 0;
  private rateWindowStart = Date.now();
  private optInCheck: (() => boolean) | null = null;
  private _listenersSetup = false;

  constructor() {
    // Don't access DOM in constructor — SSR-safe.
    // Call setupListeners() explicitly in browser onMount.
    if (typeof document !== 'undefined') {
      this.setupListeners();
    }
    if (typeof setInterval !== 'undefined') {
      this.flushTimer = setInterval(() => this.flush(), 3000);
    }
  }

  setOptInCheck(fn: () => boolean) {
    this.optInCheck = fn;
  }

  enable() {
    this._enabled = true;
  }

  disable() {
    this._enabled = false;
  }

  get isEnabled() {
    return this._enabled;
  }

  onEvent(cb: EventCallback) {
    this.listeners.push(cb);
    return () => {
      this.listeners = this.listeners.filter((l) => l !== cb);
    };
  }

  private emit(event: BehaviorEvent) {
    if (!this._enabled) return;

    // Rate limiting — drop events if exceeding threshold
    const now = Date.now();
    if (now - this.rateWindowStart > RATE_LIMIT_RESET) {
      this.rateCount = 0;
      this.rateWindowStart = now;
    }
    this.rateCount++;
    if (this.rateCount > RATE_LIMIT_WINDOW) return;

    this.events.push(event);
    this.listeners.forEach((cb) => cb(event));
  }

  private flush() {
    if (this.events.length === 0 || typeof window === 'undefined') return;
    const batch = [...this.events];
    this.events = [];
    const payload = batch.map((e) => ({
      ...e,
      url: window.location.pathname,
    }));
    const base = (import.meta as any).env?.VITE_API_URL || '';
    const endpoint = `${base}/api/behavior/events`;
    if (navigator.sendBeacon) {
      navigator.sendBeacon(endpoint, JSON.stringify(payload));
    }
  }

  // Public API for game/quiz pages to report domain events
  reportAnswerSubmit(latencyMs: number, optionChanges: number, correct: boolean) {
    if (!this._enabled) return;
    if (this.optInCheck && !this.optInCheck()) return;
    this.emit({
      type: 'answer_submit',
      timestamp: Date.now(),
      data: { latency: latencyMs, changes: optionChanges, correct },
    });
  }

  reportQuizStart() {
    if (!this._enabled) return;
    if (this.optInCheck && !this.optInCheck()) return;
    this.emit({
      type: 'quiz_start',
      timestamp: Date.now(),
      data: {},
    });
  }

  setupListeners() {
    if (typeof document === 'undefined' || this._listenersSetup) return;
    this._listenersSetup = true;
    // Click tracking
    document.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      this.emit({
        type: 'click',
        timestamp: Date.now(),
        data: { tag: target.tagName, class: target.className?.slice(0, 50) },
      });
    });

    // Page visibility
    document.addEventListener('visibilitychange', () => {
      this.emit({
        type: 'visibility',
        timestamp: Date.now(),
        data: { hidden: document.hidden },
      });
    });

    // Scroll depth
    let maxScrollDepth = 0;
    window.addEventListener(
      'scroll',
      () => {
        const depth = Math.round(
          (window.scrollY / (document.body.scrollHeight - window.innerHeight)) * 100
        );
        if (depth > maxScrollDepth) {
          maxScrollDepth = depth;
          if (depth % 25 === 0) {
            this.emit({
              type: 'scroll_depth',
              timestamp: Date.now(),
              data: { depth },
            });
          }
        }
      },
      { passive: true }
    );

    // Text selection
    document.addEventListener('mouseup', () => {
      const selection = window.getSelection()?.toString().trim();
      if (selection && selection.length > 10) {
        this.emit({
          type: 'text_selection',
          timestamp: Date.now(),
          data: { length: selection.length },
        });
      }
    });

    // Mouse exit (quit risk)
    document.addEventListener('mouseleave', () => {
      this.emit({
        type: 'mouse_exit',
        timestamp: Date.now(),
        data: {},
      });
    });
  }

  destroy() {
    if (this.flushTimer) clearInterval(this.flushTimer);
  }
}

let _collector: BehaviorCollector | null = null;

export function getCollector(): BehaviorCollector {
  if (!_collector) {
    _collector = new BehaviorCollector();
  }
  return _collector;
}
