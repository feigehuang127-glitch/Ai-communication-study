// worker.ts — Web Worker for behavior event aggregation
// Usage: const worker = new Worker(new URL('./worker.ts', import.meta.url));

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

self.onmessage = (e: MessageEvent) => {
  const { type, events } = e.data;

  if (type === 'process') {
    for (const event of events) {
      lastActivityTime = Date.now();

      switch (event.data?.type) {
        case 'scroll_depth':
          state.scrollDepth = Math.max(state.scrollDepth, event.data.data.depth);
          break;
        case 'visibility':
          if (event.data.data.hidden) {
            tabHiddenSince = Date.now();
          } else if (tabHiddenSince) {
            tabHiddenSince = null;
          }
          break;
        case 'answer_submit':
          state.totalAnswers++;
          state.answerLatency = event.data.data.latency || 0;
          state.optionChanges = event.data.data.changes || 0;
          if (event.data.data.correct) {
            state.streak++;
          } else {
            state.streak = 0;
          }
          state.avgLatency =
            (state.avgLatency * (state.totalAnswers - 1) + state.answerLatency) /
            state.totalAnswers;
          break;
      }
    }

    state.dwellTime = Date.now() - pageEnterTime;
    state.inactivityMs = Date.now() - lastActivityTime;
    if (tabHiddenSince) {
      state.tabHidden = (Date.now() - tabHiddenSince) / 1000;
    } else {
      state.tabHidden = 0;
    }

    self.postMessage({ type: 'state', state: { ...state } });
  }

  if (type === 'reset_page') {
    pageEnterTime = Date.now();
    state.dwellTime = 0;
    state.scrollDepth = 0;
  }
};
