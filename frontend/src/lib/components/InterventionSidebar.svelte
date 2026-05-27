<script lang="ts">
  import { spring } from 'svelte/motion';
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { chat } from '$lib/stores/chat';
  import { activeInterventions, interventions } from '$lib/stores/intervention';
  import type { InterventionItem } from '$lib/stores/intervention';
  import InterventionCard from './InterventionCard.svelte';

  let hasCards = false;
  const panelX = spring(100, { stiffness: 0.08, damping: 0.48 });

  $: {
    const count = $activeInterventions.length;
    if (count > 0 && !hasCards) {
      hasCards = true;
      panelX.set(0);
    } else if (count === 0 && hasCards) {
      hasCards = false;
      panelX.set(100);
    }
  }

  $: chatOpen = $chat.isOpen;
  $: rightOffset = chatOpen ? 400 : 0;

  function handleDismiss(id: string) {
    interventions.dismiss(id);
    setTimeout(() => interventions.remove(id), 350);
  }

  function handleAction(item: InterventionItem) {
    interventions.dismiss(item.id);
    setTimeout(() => interventions.remove(item.id), 350);
    if (item.actionPage) {
      goto(item.actionPage);
    }
  }
</script>

{#if hasCards || $activeInterventions.length > 0}
  <aside
    class="intervention-sidebar"
    style="
      transform: translateX({$panelX}%);
      right: {rightOffset}px;
    "
  >
    <div class="card-stack">
      {#each $activeInterventions as item, i (item.id)}
        <div class="card-wrapper" style="transition-delay: {i * 60}ms">
          <InterventionCard
            intervention={item}
            onDismiss={() => handleDismiss(item.id)}
            onAction={() => handleAction(item)}
          />
        </div>
      {/each}
    </div>
  </aside>
{/if}

<style>
  .intervention-sidebar {
    position: fixed;
    top: 60px;
    width: 360px;
    height: calc(100vh - 60px);
    z-index: 1001;
    pointer-events: none;
    transition: right 0.3s var(--ease-spring-damped);
    will-change: transform;
  }
  .card-stack {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 16px 16px 16px 0;
    height: 100%;
    overflow-y: auto;
  }
  .card-wrapper {
    pointer-events: auto;
  }
</style>
