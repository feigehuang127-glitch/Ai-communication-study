<script lang="ts">
  import { createEventDispatcher } from 'svelte';

  export let zoom: number = 1;

  const dispatch = createEventDispatcher();
</script>

<div class="flow-toolbar-inner">
  <button class="tb-btn" title="缩小" on:click={() => dispatch('zoomOut')}>
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
      <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5" />
      <line x1="4" y1="7" x2="10" y2="7" stroke="currentColor" stroke-width="1.5" />
      <line x1="10.5" y1="10.5" x2="14" y2="14" stroke="currentColor" stroke-width="1.5" />
    </svg>
  </button>

  <span class="tb-zoom-label">{Math.round(zoom * 100)}%</span>

  <button class="tb-btn" title="放大" on:click={() => dispatch('zoomIn')}>
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
      <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5" />
      <line x1="4" y1="7" x2="10" y2="7" stroke="currentColor" stroke-width="1.5" />
      <line x1="7" y1="4" x2="7" y2="10" stroke="currentColor" stroke-width="1.5" />
      <line x1="10.5" y1="10.5" x2="14" y2="14" stroke="currentColor" stroke-width="1.5" />
    </svg>
  </button>

  <div class="tb-divider" />

  <button class="tb-btn" title="适配视图" on:click={() => dispatch('fitView')}>
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
      <rect x="2" y="2" width="12" height="12" rx="1" stroke="currentColor" stroke-width="1.5" />
      <path d="M2 6h2l-2-2 2-2" stroke="currentColor" stroke-width="1.2" fill="none" />
      <path d="M14 6h-2l2-2-2-2" stroke="currentColor" stroke-width="1.2" fill="none" />
    </svg>
  </button>

  <button class="tb-btn" title="自动布局" on:click={() => dispatch('autoLayout')}>
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
      <rect x="1" y="1" width="5" height="5" rx="1" stroke="currentColor" stroke-width="1.2" />
      <rect x="10" y="1" width="5" height="5" rx="1" stroke="currentColor" stroke-width="1.2" />
      <rect x="5.5" y="10" width="5" height="5" rx="1" stroke="currentColor" stroke-width="1.2" />
      <line x1="3.5" y1="6" x2="3.5" y2="8" stroke="currentColor" stroke-width="1" />
      <line x1="3.5" y1="8" x2="8" y2="8" stroke="currentColor" stroke-width="1" />
      <line x1="3.5" y1="8" x2="12.5" y2="8" stroke="currentColor" stroke-width="1" />
      <line x1="12.5" y1="8" x2="12.5" y2="10" stroke="currentColor" stroke-width="1" />
    </svg>
  </button>
</div>

<style>
  .flow-toolbar-inner {
    display: flex;
    align-items: center;
    gap: 4px;
  }
  .tb-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    background: none;
    border: 1px solid transparent;
    border-radius: 6px;
    color: var(--text-secondary);
    cursor: pointer;
    transition: all 0.15s;
  }
  .tb-btn:hover {
    background: rgba(255,255,255,0.08);
    border-color: var(--glass-border);
    color: var(--text-primary);
  }
  .tb-zoom-label {
    font-size: 12px;
    color: var(--text-secondary);
    min-width: 40px;
    text-align: center;
    font-variant-numeric: tabular-nums;
  }
  .tb-divider {
    width: 1px;
    height: 20px;
    background: var(--glass-border);
    margin: 0 4px;
  }
</style>
