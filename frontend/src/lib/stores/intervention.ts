import { writable, derived } from 'svelte/store';

export type Severity = 'info' | 'warning' | 'critical';

export interface InterventionItem {
  id: string;
  ruleId: string;
  severity: Severity;
  title?: string;
  message: string;
  actionLabel?: string;
  actionPage?: string;
  timestamp: number;
  dismissed: boolean;
}

function createInterventionStore() {
  const { subscribe, update } = writable<InterventionItem[]>([]);

  function push(item: InterventionItem) {
    update((items) => [...items, item]);
  }

  function dismiss(id: string) {
    update((items) => items.map((i) => (i.id === id ? { ...i, dismissed: true } : i)));
  }

  function remove(id: string) {
    update((items) => items.filter((i) => i.id !== id));
  }

  function dismissAll() {
    update((items) => items.map((i) => ({ ...i, dismissed: true })));
  }

  return { subscribe, push, dismiss, remove, dismissAll };
}

export const interventions = createInterventionStore();

export const activeInterventions = derived(interventions, ($items) =>
  $items.filter((i) => !i.dismissed)
);
