import type { RuleConfig } from './rules';
import { DEFAULT_RULES } from './rules';
import { chat } from '$lib/stores/chat';

interface AggregatedState {
  dwellTime: number;
  scrollDepth: number;
  tabHidden: number;
  inactivityMs: number;
  answerLatency: number;
  optionChanges: number;
  streak: number;
  avgLatency: number;
  totalAnswers: number;
}

type InterventionHandler = (rule: RuleConfig) => void;

const DEBOUNCE_MS: Record<string, number> = {
  hesitate_on_answer: 30000,
  stuck_on_page: 120000,
  about_to_quit: 60000,
  mastery_detected: 120000,
};

class BehaviorEngine {
  private worker: Worker | null = null;
  private enabled = false;
  private lastTriggered: Record<string, number> = {};
  private eventBatch: any[] = [];
  private flushTimer: ReturnType<typeof setInterval> | null = null;
  private customHandlers: Map<string, InterventionHandler> = new Map();

  get isEnabled() {
    return this.enabled;
  }

  enable() {
    if (this.enabled) return;
    this.enabled = true;
    localStorage.setItem('ai-intervention-enabled', 'true');
    this.start();
  }

  disable() {
    this.enabled = false;
    localStorage.removeItem('ai-intervention-enabled');
    this.stop();
  }

  onIntervention(ruleId: string, handler: InterventionHandler) {
    this.customHandlers.set(ruleId, handler);
  }

  private start() {
    this.initWorker();
    this.flushTimer = setInterval(() => this.flushEvents(), 2000);
  }

  private stop() {
    this.worker?.terminate();
    this.worker = null;
    if (this.flushTimer) {
      clearInterval(this.flushTimer);
      this.flushTimer = null;
    }
    this.eventBatch = [];
  }

  private initWorker() {
    try {
      this.worker = new Worker(new URL('./worker.ts', import.meta.url), { type: 'module' });
      this.worker.onmessage = (e: MessageEvent) => {
        if (e.data.type === 'state') {
          this.evaluateAndTrigger(e.data.state);
        }
      };
    } catch {
      // Worker not supported, silently fail
      this.worker = null;
    }
  }

  private evaluateAndTrigger(state: AggregatedState) {
    for (const rule of DEFAULT_RULES) {
      if (this.evaluateRule(rule, state)) {
        const now = Date.now();
        const debounce = DEBOUNCE_MS[rule.id] || 60000;
        if (now - (this.lastTriggered[rule.id] || 0) < debounce) continue;
        this.lastTriggered[rule.id] = now;
        this.handleIntervention(rule);
      }
    }
  }

  private handleIntervention(rule: RuleConfig) {
    const custom = this.customHandlers.get(rule.id);
    if (custom) {
      custom(rule);
      return;
    }
    this.defaultIntervention(rule);
  }

  private defaultIntervention(rule: RuleConfig) {
    const { intervention } = rule;

    switch (intervention.type) {
      case 'toast':
      case 'inline_card':
      case 'sidebar':
        chat.open('study_buddy');
        if (intervention.message) {
          chat.addMessage({
            role: 'assistant',
            content: intervention.message,
            personaName: '陪练同学',
          });
        }
        break;
      case 'title_flash':
        this.flashTitle(intervention.message);
        break;
    }
  }

  private flashTitle(message: string) {
    const original = document.title;
    let count = 0;
    const interval = setInterval(() => {
      document.title = count % 2 === 0 ? message : original;
      count++;
      if (count >= 6) {
        clearInterval(interval);
        document.title = original;
      }
    }, 800);
  }

  private flushEvents() {
    if (this.eventBatch.length === 0 || !this.worker) return;
    this.worker.postMessage({ type: 'process', events: [...this.eventBatch] });
    this.eventBatch = [];
  }

  // Called by collector subscription
  pushEvent(event: any) {
    if (!this.enabled) return;
    this.eventBatch.push(event);
  }

  resetPage() {
    if (this.worker) {
      this.worker.postMessage({ type: 'reset_page' });
    }
  }

  private evaluateRule(rule: RuleConfig, state: AggregatedState): boolean {
    const c = rule.conditions as any;
    if (c.all) {
      return (c.all as any[]).every((cond: any) => this.evaluateCondition(cond, state));
    }
    if (c.any) {
      return (c.any as any[]).some((cond: any) => this.evaluateCondition(cond, state));
    }
    return false;
  }

  private evaluateCondition(cond: any, state: AggregatedState): boolean {
    const factValue = (state as any)[cond.fact];
    if (factValue === undefined) return false;
    switch (cond.operator) {
      case 'greaterThan': return factValue > cond.value;
      case 'greaterThanInclusive': return factValue >= cond.value;
      case 'lessThan': return factValue < cond.value;
      case 'lessThanInclusive': return factValue <= cond.value;
      case 'equal': return factValue === cond.value;
      default: return false;
    }
  }
}

export const behaviorEngine = new BehaviorEngine();
