<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { chat } from '$lib/stores/chat';

  let visible = false;
  let query = '';
  let inputEl: HTMLInputElement;

  const commands = [
    { label: '解释当前概念', action: 'explain', icon: '💡' },
    { label: '做个小测验', action: 'quiz', icon: '📝' },
    { label: '去实验场', action: 'lab', icon: '🔬' },
    { label: '每日挑战', action: 'daily', icon: '🔥' },
    { label: '查看学习报告', action: 'report', icon: '📊' },
    { label: '去代码沙箱', action: 'sandbox', icon: '🐳' },
  ];

  $: filtered = query
    ? commands.filter(c => c.label.includes(query))
    : commands;

  onMount(() => {
    window.addEventListener('keydown', handleGlobalKey);
  });

  function handleGlobalKey(e: KeyboardEvent) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      visible = !visible;
      if (visible) setTimeout(() => inputEl?.focus(), 50);
    }
    if (e.key === 'Escape') visible = false;
  }

  function execute(cmd: typeof commands[0]) {
    visible = false;
    query = '';
    switch (cmd.action) {
      case 'explain': chat.open('lecturer'); break;
      case 'quiz': goto('/game?mode=practice'); break;
      case 'lab': goto('/lab'); break;
      case 'daily': goto('/game?mode=daily'); break;
      case 'report': goto('/profile'); break;
      case 'sandbox': goto('/lab/sandbox'); break;
    }
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' && filtered.length > 0) execute(filtered[0]);
    if (e.key === 'Escape') visible = false;
  }
</script>

{#if visible}
  <div class="overlay" on:click={() => visible = false}>
    <div class="palette glass" on:click|stopPropagation>
      <input
        bind:this={inputEl}
        bind:value={query}
        placeholder="搜索命令或提问..."
        on:keydown={handleKeydown}
      />
      <div class="results">
        {#each filtered as cmd}
          <button class="result-item" on:click={() => execute(cmd)}>
            <span class="cmd-icon">{cmd.icon}</span>
            <span>{cmd.label}</span>
          </button>
        {/each}
        {#if filtered.length === 0}
          <div class="empty">没有匹配的命令</div>
        {/if}
      </div>
    </div>
  </div>
{/if}

<style>
  .overlay {
    position: fixed;
    inset: 0;
    z-index: 10000;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    padding-top: 15vh;
    backdrop-filter: blur(4px);
  }
  .palette {
    width: 560px;
    max-height: 400px;
    padding: 8px;
    border-radius: 16px;
    overflow: hidden;
  }
  input {
    width: 100%;
    padding: 14px 16px;
    background: transparent;
    border: none;
    border-bottom: 1px solid var(--glass-border);
    color: var(--text-primary);
    font-size: 16px;
    outline: none;
  }
  input::placeholder { color: var(--text-secondary); }
  .results { max-height: 300px; overflow-y: auto; padding: 4px 0; }
  .result-item {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 14px;
    background: transparent;
    border: none;
    color: var(--text-primary);
    font-size: 14px;
    cursor: pointer;
    border-radius: 8px;
  }
  .result-item:hover { background: rgba(255,255,255,0.06); }
  .cmd-icon { font-size: 18px; }
  .empty { padding: 20px; text-align: center; color: var(--text-secondary); }
</style>
