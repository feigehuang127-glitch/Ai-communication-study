<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { chat } from '$lib/stores/chat';

  let visible = false;
  let query = '';
  let inputEl: HTMLInputElement;
  let activeIndex = 0;

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

  $: if (filtered) activeIndex = 0;

  onMount(() => {
    window.addEventListener('keydown', handleGlobalKey);
    return () => window.removeEventListener('keydown', handleGlobalKey);
  });

  function handleGlobalKey(e: KeyboardEvent) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      visible = !visible;
      if (visible) {
        activeIndex = 0;
        setTimeout(() => inputEl?.focus(), 50);
      }
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
    if (e.key === 'Escape') {
      visible = false;
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      activeIndex = (activeIndex + 1) % filtered.length;
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      activeIndex = (activeIndex - 1 + filtered.length) % filtered.length;
    } else if (e.key === 'Enter' && filtered.length > 0) {
      e.preventDefault();
      execute(filtered[activeIndex]);
    }
  }
</script>

{#if visible}
  <div class="overlay" on:click={() => visible = false}>
    <div
      class="palette glass"
      on:click|stopPropagation
      style="animation: palette-pop var(--ease-spring-bouncy) 0.4s forwards;"
    >
      <div class="search-wrapper">
        <span class="search-icon">🔍</span>
        <input
          bind:this={inputEl}
          bind:value={query}
          placeholder="搜索命令或提问 (↑↓ 切换, Enter 执行)..."
          on:keydown={handleKeydown}
        />
      </div>

      <div class="results">
        {#if filtered.length > 0}
          <div
            class="active-slider"
            style="transform: translateY({activeIndex * 40}px); transition: transform 0.22s var(--ease-spring-snappy);"
          ></div>
        {/if}

        {#each filtered as cmd, idx}
          <button
            class="result-item"
            class:focused={idx === activeIndex}
            on:click={() => execute(cmd)}
            on:mouseenter={() => activeIndex = idx}
          >
            <span class="cmd-icon">{cmd.icon}</span>
            <span class="cmd-label">{cmd.label}</span>
            {#if idx === activeIndex}
              <span class="enter-badge" style="animation: badge-in 0.2s ease;">↵ Enter</span>
            {/if}
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
    background: rgba(5, 8, 20, 0.65);
    display: flex;
    justify-content: center;
    padding-top: 15vh;
    backdrop-filter: blur(8px);
  }
  .palette {
    width: 580px;
    max-height: 420px;
    padding: 10px;
    border-radius: 16px;
    overflow: hidden;
    border: 1px solid var(--morandi-border);
    background: var(--morandi-bg-card);
  }

  @keyframes palette-pop {
    0% { transform: scale(0.92) translateY(-10px); opacity: 0; }
    100% { transform: scale(1) translateY(0); opacity: 1; }
  }

  .search-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 6px 12px;
    border-bottom: 1px solid var(--morandi-border);
  }
  .search-icon { font-size: 16px; opacity: 0.6; }
  input {
    flex: 1;
    padding: 10px 0;
    background: transparent;
    border: none;
    color: var(--text-primary);
    font-size: 15px;
    outline: none;
  }
  input::placeholder { color: var(--text-secondary); }

  .results {
    position: relative;
    max-height: 320px;
    overflow-y: auto;
    padding: 6px 0;
  }

  .active-slider {
    position: absolute;
    left: 4px;
    right: 4px;
    top: 6px;
    height: 36px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    z-index: 1;
    pointer-events: none;
  }

  .result-item {
    position: relative;
    width: 100%;
    height: 40px;
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 0 16px;
    background: transparent;
    border: none;
    color: var(--text-secondary);
    font-size: 14px;
    cursor: pointer;
    border-radius: 8px;
    z-index: 2;
    text-align: left;
    transition: color 0.2s ease;
  }
  .result-item.focused {
    color: var(--text-primary);
  }
  .cmd-icon { font-size: 16px; }
  .cmd-label { flex: 1; }

  .enter-badge {
    font-size: 11px;
    color: var(--morandi-blue);
    background: rgba(143, 164, 180, 0.12);
    padding: 2px 6px;
    border-radius: 4px;
    font-weight: 500;
  }

  .empty { padding: 32px; text-align: center; color: var(--text-secondary); font-size: 13px; }
  @keyframes badge-in { from { opacity: 0; } to { opacity: 1; } }
</style>
