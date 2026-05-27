<script lang="ts">
  import { chat } from '$lib/stores/chat';
  import PersonaAvatar from './PersonaAvatar.svelte';
  import { page } from '$app/stores';
  import { spring } from 'svelte/motion';
  import { tick } from 'svelte';

  let inputText = '';
  let messagesContainer: HTMLDivElement;
  let panelEl: HTMLDivElement;

  // Spring physics: stiffness and damping tuned for glass-panel elasticity
  const panelX = spring(420, { stiffness: 0.08, damping: 0.6 });
  $: panelX.set($chat.isOpen ? 0 : 420);

  // Overlay fade tied to panel position
  $: overlayAlpha = 1 - Math.abs($panelX) / 420;

  function getContextPage(): string {
    const pathname = $page.url.pathname;
    if (pathname.startsWith('/college/ai')) return `/college/ai/course/${pathname.split('/').pop()}`;
    return pathname;
  }

  async function send() {
    const text = inputText.trim();
    if (!text || $chat.isLoading) return;
    inputText = '';
    chat.sendMessage(text, getContextPage());
    await tick();
    scrollToBottom();
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  }

  $: if ($chat.messages || $chat.isLoading) {
    setTimeout(scrollToBottom, 30);
  }

  function scrollToBottom() {
    if (messagesContainer) {
      messagesContainer.scrollTo({
        top: messagesContainer.scrollHeight,
        behavior: 'smooth'
      });
    }
  }
</script>

{#if $chat.isOpen || $panelX < 419}
  <!-- Backdrop overlay -->
  <div
    class="panel-backdrop"
    style:opacity={overlayAlpha}
    style:pointer-events={$chat.isOpen ? 'auto' : 'none'}
    on:click={chat.close}
  ></div>

  <div
    bind:this={panelEl}
    class="slide-panel"
    style:transform="translateX({$panelX}px)"
  >
    <div class="panel-header glass">
      <PersonaAvatar personaId={$chat.personaId} name={$chat.personaName} />
      <button class="close-btn" on:click={chat.close} aria-label="关闭面板">✕</button>
    </div>

    <div class="panel-messages" bind:this={messagesContainer}>
      {#each $chat.messages as msg (msg.content + msg.role)}
        <div
          class="message"
          class:user={msg.role === 'user'}
          class:assistant={msg.role === 'assistant'}
        >
          {#if msg.role === 'assistant' && msg.personaName}
            <span class="persona-tag">{msg.personaName}</span>
          {/if}
          <div class="msg-bubble glass">
            <span class="stream-chunk">{msg.content}</span>
          </div>
        </div>
      {/each}

      {#if $chat.isLoading}
        <div class="message assistant">
          <div class="msg-bubble glass typing">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      {/if}
    </div>

    <div class="panel-input glass">
      <textarea
        bind:value={inputText}
        placeholder="向 AI 导师提问或探讨研究架构..."
        on:keydown={handleKeydown}
        rows="1"
        disabled={$chat.isLoading}
      ></textarea>
      <button class="send-btn" on:click={send} disabled={$chat.isLoading || !inputText.trim()}>
        发送
      </button>
    </div>
  </div>
{/if}

<style>
  .panel-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(5, 8, 20, 0.45);
    z-index: 999;
    transition: opacity 0.3s ease;
  }

  .slide-panel {
    position: fixed;
    top: 0;
    right: 0;
    width: 420px;
    height: 100vh;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    background: var(--morandi-bg-card);
    box-shadow: -10px 0 40px rgba(0, 0, 0, 0.4);
    will-change: transform;
  }
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 20px;
    border-radius: 0;
    border-top: none;
    border-left: none;
    border-right: none;
    background: rgba(17, 22, 37, 0.7);
    flex-shrink: 0;
  }
  .close-btn {
    background: none;
    border: none;
    color: var(--text-secondary);
    font-size: 16px;
    cursor: pointer;
    padding: 6px;
    border-radius: 50%;
    transition: background 0.2s, color 0.2s;
  }
  .close-btn:hover { background: rgba(255,255,255,0.05); color: var(--text-primary); }

  .panel-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    scroll-behavior: smooth;
  }
  .message { max-width: 88%; display: flex; flex-direction: column; gap: 4px; }
  .message.user { align-self: flex-end; align-items: flex-end; }
  .message.assistant { align-self: flex-start; align-items: flex-start; }

  .persona-tag {
    font-size: 11px;
    font-weight: 500;
    color: var(--morandi-blue);
    padding-left: 4px;
  }

  .msg-bubble {
    padding: 12px 16px;
    border-radius: var(--radius-xl);
    font-size: 13.5px;
    line-height: 1.6;
    word-break: break-word;
    transition: height 0.25s var(--ease-spring-snappy);
    will-change: height;
  }

  /* ─── Streaming text: each new chunk fades in smoothly ─── */
  .stream-chunk {
    animation: fade-in-up 0.3s var(--ease-spring-snappy) both;
  }

  .message.user .msg-bubble {
    background: linear-gradient(135deg, rgba(143, 164, 180, 0.25), rgba(184, 156, 156, 0.15));
    border: 1px solid rgba(143, 164, 180, 0.3);
    color: var(--text-primary);
  }
  .message.assistant .msg-bubble {
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid var(--morandi-border);
    color: #e2e8f0;
  }

  .typing { display: flex; gap: 4px; padding: 14px 20px; }
  .typing .dot {
    width: 6px;
    height: 6px;
    background: var(--morandi-blue);
    border-radius: 50%;
    animation: dot-jump 1.4s infinite ease-in-out both;
  }
  .typing .dot:nth-child(1) { animation-delay: -0.32s; }
  .typing .dot:nth-child(2) { animation-delay: -0.16s; }

  @keyframes dot-jump {
    0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
    40% { transform: scale(1.1) translateY(-4px); opacity: 1; }
  }

  .panel-input {
    display: flex;
    gap: 10px;
    align-items: flex-end;
    padding: 16px 20px;
    border-radius: 0;
    border-bottom: none;
    border-left: none;
    border-right: none;
    background: rgba(17, 22, 37, 0.8);
    flex-shrink: 0;
  }
  textarea {
    flex: 1;
    background: transparent;
    border: none;
    color: var(--text-primary);
    font-size: 13.5px;
    resize: none;
    outline: none;
    font-family: inherit;
    line-height: 1.5;
    max-height: 100px;
  }
  textarea::placeholder { color: var(--text-secondary); }

  .send-btn {
    padding: 8px 18px;
    background: var(--morandi-blue);
    border: none;
    border-radius: var(--radius-lg);
    color: #0b0f19;
    font-weight: 600;
    cursor: pointer;
    font-size: 13px;
    transition: opacity 0.2s, transform 0.2s;
  }
  .send-btn:hover:not(:disabled) { transform: translateY(-1px); opacity: 0.9; }
  .send-btn:disabled { opacity: 0.3; cursor: not-allowed; transform: none; }
</style>
