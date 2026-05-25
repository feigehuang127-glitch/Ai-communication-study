import { writable } from 'svelte/store';

interface Toast {
  id: number;
  message: string;
  type: 'info' | 'success' | 'error';
}

let nextId = 0;

function createToastStore() {
  const { subscribe, update } = writable<Toast[]>([]);

  function add(message: string, type: 'info' | 'success' | 'error' = 'info', duration = 3000) {
    const id = nextId++;
    update(toasts => [...toasts, { id, message, type }]);
    setTimeout(() => {
      update(toasts => toasts.filter(t => t.id !== id));
    }, duration);
  }

  return { subscribe, add };
}

export const toasts = createToastStore();
