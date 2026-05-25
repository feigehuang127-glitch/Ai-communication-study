import { writable } from 'svelte/store';

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
  personaName?: string;
}

export interface ChatState {
  isOpen: boolean;
  personaId: string;
  personaName: string;
  messages: ChatMessage[];
  isLoading: boolean;
}

function createChatStore() {
  const { subscribe, update } = writable<ChatState>({
    isOpen: false,
    personaId: 'lecturer',
    personaName: '讲解老师',
    messages: [],
    isLoading: false,
  });

  function toggle() {
    update(s => ({ ...s, isOpen: !s.isOpen }));
  }

  function open(personaId?: string) {
    update(s => ({
      ...s,
      isOpen: true,
      personaId: personaId || s.personaId,
      personaName: personaId ? getPersonaName(personaId) : s.personaName,
    }));
  }

  function close() {
    update(s => ({ ...s, isOpen: false }));
  }

  function addMessage(msg: ChatMessage) {
    update(s => ({ ...s, messages: [...s.messages, msg] }));
  }

  function setLoading(loading: boolean) {
    update(s => ({ ...s, isLoading: loading }));
  }

  function setPersona(personaId: string) {
    update(s => ({ ...s, personaId, personaName: getPersonaName(personaId) }));
  }

  return { subscribe, toggle, open, close, addMessage, setLoading, setPersona };
}

function getPersonaName(id: string): string {
  const names: Record<string, string> = {
    lecturer: '讲解老师',
    code_mentor: '代码导师',
    study_buddy: '陪练同学',
    analyst: '学习分析师',
  };
  return names[id] || '讲解老师';
}

export const chat = createChatStore();
