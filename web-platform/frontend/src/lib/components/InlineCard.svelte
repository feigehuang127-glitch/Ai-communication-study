<script lang="ts">
  import { createEventDispatcher } from 'svelte';

  export let title: string = '';
  export let message: string = '';
  export let actionLabel: string = '';
  export let type: 'info' | 'warning' | 'success' = 'info';

  const dispatch = createEventDispatcher();
</script>

<div class="inline-card glass" class:warning={type === 'warning'} class:success={type === 'success'}>
  <div class="card-body">
    {#if title}
      <strong>{title}</strong>
    {/if}
    <p>{message}</p>
  </div>
  {#if actionLabel}
    <button class="card-action" on:click={() => dispatch('action')}>
      {actionLabel}
    </button>
  {/if}
</div>

<style>
  .inline-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 14px 18px;
    margin: 12px 0;
    border-left: 3px solid var(--accent-blue);
  }
  .inline-card.warning { border-left-color: var(--accent-gold); }
  .inline-card.success { border-left-color: var(--accent-green); }
  .card-body { flex: 1; }
  .card-body p { font-size: 13px; color: var(--text-secondary); margin: 0; }
  .card-body strong { font-size: 14px; display: block; margin-bottom: 4px; }
  .card-action {
    padding: 6px 14px;
    background: var(--accent-blue);
    border: none;
    border-radius: 8px;
    color: white;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    white-space: nowrap;
  }
</style>
