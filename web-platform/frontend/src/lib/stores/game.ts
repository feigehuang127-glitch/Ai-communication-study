import { writable } from 'svelte/store';
import { apiJson } from '$lib/api/client';

interface Question {
  id: number;
  content: string;
  type: string;
  optionA: string;
  optionB: string;
  optionC: string | null;
  optionD: string | null;
  explanation: string;
}

interface GameState {
  sessionId: string;
  questions: Question[];
  currentIndex: number;
  totalQuestions: number;
  timeLimit: number;
  answers: (string | null)[];
  results: (boolean | null)[];
  isFinished: boolean;
  isLoading: boolean;
}

function createGameStore() {
  const state = writable<GameState>({
    sessionId: '',
    questions: [],
    currentIndex: 0,
    totalQuestions: 10,
    timeLimit: 10,
    answers: [],
    results: [],
    isFinished: false,
    isLoading: false
  });

  async function start(college: string, category: string) {
    state.update(s => ({ ...s, isLoading: true }));
    const data = await apiJson<{
      sessionId: string;
      questions: Question[];
      timeLimit: number;
    }>('/api/game/start', {
      method: 'POST',
      body: { college, category }
    });
    state.set({
      sessionId: data.sessionId,
      questions: data.questions,
      currentIndex: 0,
      totalQuestions: data.questions.length,
      timeLimit: data.timeLimit,
      answers: new Array(data.questions.length).fill(null),
      results: new Array(data.questions.length).fill(null),
      isFinished: false,
      isLoading: false
    });
  }

  async function submitAnswer(answer: string) {
    let current: GameState = {} as GameState;
    state.update(s => { current = s; return s; });
    const data = await apiJson<{ correct: boolean }>('/api/game/answer', {
      method: 'POST',
      body: { sessionId: current.sessionId, questionIndex: current.currentIndex, answer }
    });
    state.update(s => {
      s.results[s.currentIndex] = data.correct;
      s.answers[s.currentIndex] = answer;
      return s;
    });
    return data.correct;
  }

  function nextQuestion() {
    state.update(s => {
      if (s.currentIndex < s.totalQuestions - 1) {
        s.currentIndex++;
      } else {
        s.isFinished = true;
      }
      return s;
    });
  }

  async function finish(): Promise<any> {
    let current: GameState = {} as GameState;
    state.update(s => { current = s; return s; });
    return apiJson('/api/game/finish', {
      method: 'POST',
      body: { sessionId: current.sessionId }
    });
  }

  return { state, start, submitAnswer, nextQuestion, finish };
}

export const game = createGameStore();
