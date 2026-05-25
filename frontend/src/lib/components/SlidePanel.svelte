<script lang="ts">
  import { chat, type ChatMessage } from '$lib/stores/chat';
  import PersonaAvatar from './PersonaAvatar.svelte';
  import { page } from '$app/stores';

  let inputText = '';
  let messagesEnd: HTMLDivElement;

  function getContextPage(): string {
    const pathname = $page.url.pathname;
    if (pathname.startsWith('/college/ai')) return `/college/ai/course/${pathname.split('/').pop()}`;
    return pathname;
  }

  async function send() {
    const text = inputText.trim();
    if (!text || $chat.isLoading) return;

    chat.addMessage({ role: 'user', content: text });
    inputText = '';
    chat.setLoading(true);

    try {
      const token = localStorage.getItem('token');
      const res = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          message: text,
          contextPage: getContextPage(),
          conversationHistory: $chat.messages.slice(-10).map(m => ({
            role: m.role,
            content: m.content,
          })),
        }),
      });

      const reader = res.body?.getReader();
      if (!reader) return;
      const decoder = new TextDecoder();
      let buffer = '';
      let assistantMsg: ChatMessage = { role: 'assistant', content: '' };

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6);
            if (data === '[DONE]') continue;
            if (data.includes('|') && !assistantMsg.content) {
              const [pid, pname] = data.split('|');
              assistantMsg.personaName = pname;
              chat.setPersona(pid);
            } else if (data.startsWith('{')) {
              try {
                const delta = JSON.parse(data);
                assistantMsg.content += delta.content || '';
              } catch {}
            }
          }
        }
      }
      chat.addMessage(assistantMsg);
    } catch (e) {
      chat.addMessage({ role: 'assistant', content: '抱歉，连接出现问题，请重试。' });
    } finally {
      chat.setLoading(false);
    }
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  }
</script>

<div class="slide-panel" class:open={$chat.isOpen}>
  <div class="panel-header glass">
    <PersonaAvatar personaId={$chat.personaId} name={$chat.personaName} />
    <button class="close-btn" on:click={chat.close}>✕</button>
  </div>

  <div class="panel-messages">
    {#each $chat.messages as msg}
      <div class="message" class:user={msg.role === 'user'} class:assistant={msg.role === 'assistant'}>
        {#if msg.role === 'assistant' && msg.personaName}
          <span class="persona-tag" style="font-size: 11px; color: var(--text-secondary);">
            {msg.personaName}
          </span>
        {/if}
        <div class="msg-bubble glass">
          {msg.content}
        </div>
      </div>
    {/each}
    {#if $chat.isLoading}
      <div class="message assistant">
        <div class="msg-bubble glass typing">...</div>
      </div>
    {/if}
    <div bind:this={messagesEnd}></div>
  </div>

  <div class="panel-input glass">
    <textarea
      bind:value={inputText}
      placeholder="向 AI 助手提问..."
      on:keydown={handleKeydown}
      rows="1"
      disabled={$chat.isLoading}
    ></textarea>
    <button class="send-btn" on:click={send} disabled={$chat.isLoading || !inputText.trim()}>
      发送
    </button>
  </div>
</div>

<style>
  .slide-panel {
    position: fixed;
    top: 0;
    right: -420px;
    width: 400px;
    height: 100vh;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    transition: right 0.3s ease;
    background: var(--bg-primary);
  }
  .slide-panel.open { right: 0; }
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-radius: 0;
    flex-shrink: 0;
  }
  .close-btn {
    background: none;
    border: none;
    color: var(--text-secondary);
    font-size: 18px;
    cursor: pointer;
    padding: 4px 8px;
  }
  .panel-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .message { max-width: 90%; }
  .message.user { align-self: flex-end; }
  .message.assistant { align-self: flex-start; }
  .msg-bubble {
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 13px;
    line-height: 1.5;
  }
  .message.user .msg-bubble {
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border: none;
  }
  .typing { animation: blink 1s infinite; }
  @keyframes blink { 50% { opacity: 0.3; } }
  .panel-input {
    display: flex;
    gap: 8px;
    padding: 12px 16px;
    border-radius: 0;
    flex-shrink: 0;
  }
  textarea {
    flex: 1;
    background: transparent;
    border: none;
    color: var(--text-primary);
    font-size: 13px;
    resize: none;
    outline: none;
    font-family: inherit;
  }
  .send-btn {
    padding: 6px 16px;
    background: var(--accent-blue);
    border: none;
    border-radius: 8px;
    color: white;
    font-weight: 600;
    cursor: pointer;
    font-size: 13px;
  }
  .send-btn:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
