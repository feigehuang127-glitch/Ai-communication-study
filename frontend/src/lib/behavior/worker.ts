// worker.ts — Web Worker for behavior event aggregation
// Instantiated by BehaviorEngine via: new Worker(new URL('./worker.ts', import.meta.url), { type: 'module' })

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

const state: AggregatedState = {
  dwellTime: 0,
  scrollDepth: 0,
  tabHidden: 0,
  inactivityMs: 0,
  answerLatency: 0,
  optionChanges: 0,
  streak: 0,
  avgLatency: 0,
  totalAnswers: 0,
};

let pageEnterTime = Date.now();
let lastActivityTime = Date.now();
let tabHiddenSince: number | null = null;

// Track individual answer latencies for running average
const recentLatencies: number[] = [];

function processEvent(event: any) {
  lastActivityTime = Date.now();

  switch (event.type) {
    case 'scroll_depth':
      state.scrollDepth = Math.max(state.scrollDepth, event.data?.depth ?? 0);
      break;

    case 'visibility':
      if (event.data?.hidden) {
        tabHiddenSince = Date.now();
      } else if (tabHiddenSince) {
        state.tabHidden = 0;
        tabHiddenSince = null;
      }
      break;

    case 'answer_submit':
      state.totalAnswers++;
      state.answerLatency = event.data?.latency ?? 0;
      state.optionChanges = event.data?.changes ?? 0;

      recentLatencies.push(state.answerLatency);
      if (recentLatencies.length > 20) recentLatencies.shift();
      state.avgLatency =
        recentLatencies.reduce((a, b) => a + b, 0) / recentLatencies.length;

      if (event.data?.correct) {
        state.streak++;
      } else {
        state.streak = 0;
      }
      break;

    case 'quiz_start':
      // Reset per-quiz metrics while preserving dwell/scroll
      state.answerLatency = 0;
      state.optionChanges = 0;
      break;

    default:
      // click, text_selection, mouse_exit — just update activity timestamp
      break;
  }
}

function buildState(): AggregatedState {
  state.dwellTime = Date.now() - pageEnterTime;
  state.inactivityMs = Date.now() - lastActivityTime;
  state.tabHidden = tabHiddenSince ? (Date.now() - tabHiddenSince) / 1000 : 0;
  return { ...state };
}

self.onmessage = (e: MessageEvent) => {
  const { type, events } = e.data;

  if (type === 'process') {
    if (Array.isArray(events)) {
      for (const event of events) {
        processEvent(event);
      }
    }
    self.postMessage({ type: 'state', state: buildState() });
  }

  if (type === 'reset_page') {
    pageEnterTime = Date.now();
    state.dwellTime = 0;
    state.scrollDepth = 0;
  }
};
