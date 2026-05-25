interface BehaviorEvent {
  type: string;
  timestamp: number;
  data: Record<string, unknown>;
}

type EventCallback = (event: BehaviorEvent) => void;

class BehaviorCollector {
  private listeners: EventCallback[] = [];
  private events: BehaviorEvent[] = [];
  private flushTimer: ReturnType<typeof setInterval> | null = null;
  private _enabled = true;

  constructor() {
    this.setupListeners();
    this.flushTimer = setInterval(() => this.flush(), 3000);
  }

  enable() { this._enabled = true; }
  disable() { this._enabled = false; }

  onEvent(cb: EventCallback) {
    this.listeners.push(cb);
    return () => {
      this.listeners = this.listeners.filter(l => l !== cb);
    };
  }

  private emit(event: BehaviorEvent) {
    if (!this._enabled) return;
    this.events.push(event);
    this.listeners.forEach(cb => cb(event));
  }

  private flush() {
    if (this.events.length === 0) return;
    const batch = [...this.events];
    this.events = [];
    const payload = batch.map(e => ({
      ...e,
      url: window.location.pathname,
    }));
    if (navigator.sendBeacon) {
      navigator.sendBeacon('/api/behavior/events', JSON.stringify(payload));
    }
  }

  private setupListeners() {
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
    window.addEventListener('scroll', () => {
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
    }, { passive: true });

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

export const collector = new BehaviorCollector();
