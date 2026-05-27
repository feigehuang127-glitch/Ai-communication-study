<script lang="ts">
  import { spring } from 'svelte/motion';
  import { onMount } from 'svelte';
  import type { InterventionItem } from '$lib/stores/intervention';

  export let intervention: InterventionItem;
  export let onDismiss: () => void;
  export let onAction: () => void;

  let exiting = false;

  const cardScale = spring(0.92, { stiffness: 0.15, damping: 0.5 });
  const cardOpacity = spring(0, { stiffness: 0.15, damping: 0.5 });
  const cardTranslateX = spring(40, { stiffness: 0.15, damping: 0.5 });
  const glowOpacity = spring(0, { stiffness: 0.12, damping: 0.48 });

  const severityColors: Record<string, string> = {
    info: 'var(--intervention-info, var(--accent-blue))',
    warning: 'var(--intervention-warning, var(--accent-gold))',
    critical: 'var(--intervention-critical, var(--accent-red))',
  };

  const severityGlow: Record<string, string> = {
    info: 'rgba(100, 180, 255, 0.25)',
    warning: 'rgba(255, 180, 100, 0.35)',
    critical: 'rgba(255, 100, 100, 0.4)',
  };

  $: glow = severityGlow[intervention.severity] || severityGlow.info;
  $: borderColor = severityColors[intervention.severity] || severityColors.info;

  onMount(() => {
    cardScale.set(1);
    cardOpacity.set(1);
    cardTranslateX.set(0);
    glowOpacity.set(1);

    if (intervention.severity === 'info') {
      const t = setTimeout(() => triggerDismiss(), 10000);
      return () => clearTimeout(t);
    }
    if (intervention.severity === 'warning') {
      const t = setTimeout(() => triggerDismiss(), 15000);
      return () => clearTimeout(t);
    }
  });

  function triggerDismiss() {
    if (exiting) return;
    exiting = true;
    cardTranslateX.set(120);
    cardOpacity.set(0);
    cardScale.set(0.92);
    setTimeout(() => onDismiss(), 300);
  }

  function handleAction() {
    if (exiting) return;
    exiting = true;
    cardTranslateX.set(120);
    cardOpacity.set(0);
    setTimeout(() => onAction(), 250);
  }
</script>

<div
  class="intervention-card"
  style="
    transform: scale({$cardScale}) translateX({$cardTranslateX}px);
    opacity: {$cardOpacity};
    box-shadow: 0 4px 16px rgba(0,0,0,0.2), 0 0 14px {$glow * $glowOpacity};
    --border-color: {borderColor};
  "
>
  <div class="severity-bar" style="background: {borderColor}"></div>
  <div class="card-body">
    {#if intervention.title}
      <h4 class="card-title">{intervention.title}</h4>
    {/if}
    <p class="card-message">{intervention.message}</p>
    <div class="card-actions">
      {#if intervention.actionLabel}
        <button class="action-btn" on:click={handleAction}>
          {intervention.actionLabel}
        </button>
      {/if}
      <button class="dismiss-btn" on:click={triggerDismiss} aria-label="关闭">
        <span class="x-icon">&#10005;</span>
      </button>
    </div>
  </div>
</div>

<style>
  .intervention-card {
    position: relative;
    display: flex;
    background: var(--glass-bg, rgba(255,255,255,0.06));
    backdrop-filter: blur(16px);
    border: 1px solid var(--glass-border, rgba(255,255,255,0.12));
    border-radius: 12px;
    overflow: hidden;
    will-change: transform, opacity;
  }
  .severity-bar {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    border-radius: 3px 0 0 3px;
  }
  .card-body {
    flex: 1;
    padding: 14px 16px;
    min-width: 0;
  }
  .card-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 6px;
    color: var(--text-primary);
  }
  .card-message {
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.5;
    margin-bottom: 10px;
  }
  .card-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
  }
  .action-btn {
    padding: 6px 16px;
    font-size: 12px;
    font-weight: 600;
    background: var(--accent-blue);
    border: none;
    border-radius: 6px;
    color: #fff;
    cursor: pointer;
    transition: transform 0.2s var(--ease-spring-bouncy);
  }
  .action-btn:hover {
    transform: scale(1.04) translateY(-2px);
  }
  .dismiss-btn {
    padding: 4px 8px;
    background: rgba(255,255,255,0.04);
    border: 1px solid rgba(255,255,255,0.1);
    border-radius: 6px;
    color: var(--text-secondary);
    cursor: pointer;
    margin-left: auto;
    flex-shrink: 0;
  }
  .dismiss-btn:hover {
    background: rgba(255,255,255,0.1);
    color: var(--text-primary);
  }
  .x-icon {
    font-size: 11px;
    line-height: 1;
  }
</style>
