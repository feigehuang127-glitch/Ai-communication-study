import { writable, get } from 'svelte/store';

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
  autoOpened?: boolean;
}

function createChatStore() {
  const store = writable<ChatState>({
    isOpen: false,
    personaId: 'lecturer',
    personaName: '讲解老师',
    messages: [],
    isLoading: false,
  });

  const { subscribe, update } = store;

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

  function autoOpen(personaId: string, contextMessage: string) {
    update(s => {
      if (s.isOpen) return s;
      const name = getPersonaName(personaId);
      return {
        ...s,
        isOpen: true,
        personaId,
        personaName: name,
        autoOpened: true,
        messages: [
          ...s.messages,
          { role: 'assistant' as const, content: contextMessage, personaName: name },
        ],
      };
    });
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

  function updateState(partial: Partial<ChatState>) {
    update((s) => ({ ...s, ...partial }));
  }

  async function sendMessage(message: string, contextPage: string) {
    const state = get(store);
    if (!message.trim() || state.isLoading) return;

    addMessage({ role: 'user', content: message });
    setLoading(true);

    try {
      const token = typeof localStorage !== 'undefined' ? localStorage.getItem('token') : null;
      const res = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          message: message.trim(),
          context_page: contextPage,
          conversation_history: state.messages.slice(-10).map(m => ({
            role: m.role,
            content: m.content,
          })),
          model: 'claude-sonnet-4-6',
        }),
      });

      const reader = res.body?.getReader();
      if (!reader) {
        addMessage({ role: 'assistant', content: '抱歉，无法获取响应。' });
        return;
      }

      const decoder = new TextDecoder();
      let buffer = '';
      let dataBuffer = '';
      let assistantContent = '';
      let personaName: string | undefined;

      // Proper SSE state machine — handles multi-line data fields and
      // network chunk boundaries correctly per the SSE spec (W3C).
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });

        // Process complete SSE events delimited by double-newline
        while (true) {
          const idx = buffer.indexOf('\n\n');
          if (idx === -1) break;
          const eventBlock = buffer.slice(0, idx);
          buffer = buffer.slice(idx + 2);

          // Extract data: lines from this event block
          const lines = eventBlock.split('\n');
          dataBuffer = '';
          for (const line of lines) {
            if (line.startsWith('data: ')) {
              dataBuffer += (dataBuffer ? '\n' : '') + line.slice(6);
            }
          }

          const data = dataBuffer.trim();
          if (!data || data === '[DONE]') continue;

          if (data.includes('|') && !assistantContent && !data.startsWith('{')) {
            const [pid, pname] = data.split('|');
            personaName = pname;
            setPersona(pid);
          } else if (data.startsWith('{')) {
            try {
              const delta = JSON.parse(data);
              assistantContent += delta.content || '';
            } catch { /* skip malformed JSON */ }
          }
        }
      }

      if (assistantContent) {
        addMessage({ role: 'assistant', content: assistantContent, personaName });
      } else {
        addMessage({ role: 'assistant', content: '收到回复，但内容为空。' });
      }
    } catch (e: any) {
      addMessage({ role: 'assistant', content: '抱歉，连接出现问题，请重试。' });
    } finally {
      setLoading(false);
    }
  }

  return { subscribe, toggle, open, close, autoOpen, addMessage, setLoading, setPersona, updateState, sendMessage };
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
