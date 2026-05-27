<script lang="ts">
  import { spring } from 'svelte/motion';

  export let href: string | undefined = undefined;
  export let onClick: (() => void) | undefined = undefined;
  export let glowColor = 'var(--accent-blue)';

  let cardEl: HTMLDivElement;
  let mouseX = 0;
  let mouseY = 0;

  // Spring physics: low stiffness + damping = smooth elastic response
  const cardScale = spring(1, { stiffness: 0.1, damping: 0.52 });
  const translateY = spring(0, { stiffness: 0.1, damping: 0.52 });
  const shadowSpread = spring(8, { stiffness: 0.08, damping: 0.5 });
  const glowOpacity = spring(0, { stiffness: 0.12, damping: 0.48 });

  function handleMouseMove(e: MouseEvent) {
    if (!cardEl) return;
    const rect = cardEl.getBoundingClientRect();
    mouseX = e.clientX - rect.left;
    mouseY = e.clientY - rect.top;
  }

  function handleMouseEnter() {
    cardScale.set(1.015);
    translateY.set(-4);
    shadowSpread.set(24);
    glowOpacity.set(1);
  }

  function handleMouseLeave() {
    cardScale.set(1);
    translateY.set(0);
    shadowSpread.set(8);
    glowOpacity.set(0);
  }

  // Scale spring value into CSS-friendly number
  $: scaleVal = $cardScale;
  $: translateYVal = $translateY;
</script>

{#if href}
  <a {href} class="premium-card-wrapper" style:transform="scale({scaleVal}) translateY({translateYVal}px)" style:box-shadow="0 {$shadowSpread / 2}px {$shadowSpread}px rgba(0,0,0,0.25), 0 0 0 1px rgba(255,255,255,{0.08 + $glowOpacity * 0.06})">
    <div
      bind:this={cardEl}
      class="premium-card"
      on:mousemove={handleMouseMove}
      on:mouseenter={handleMouseEnter}
      on:mouseleave={handleMouseLeave}
      style="--mx: {mouseX}px; --my: {mouseY}px; --glow: {glowColor};"
    >
      <div class="spotlight" style:opacity={$glowOpacity}></div>
      <div class="card-content"><slot /></div>
    </div>
  </a>
{:else if onClick}
  <button on:click={onClick} class="premium-card-wrapper premium-btn" style:transform="scale({scaleVal}) translateY({translateYVal}px)" style:box-shadow="0 {$shadowSpread / 2}px {$shadowSpread}px rgba(0,0,0,0.25), 0 0 0 1px rgba(255,255,255,{0.08 + $glowOpacity * 0.06})">
    <div
      bind:this={cardEl}
      class="premium-card"
      on:mousemove={handleMouseMove}
      on:mouseenter={handleMouseEnter}
      on:mouseleave={handleMouseLeave}
      style="--mx: {mouseX}px; --my: {mouseY}px; --glow: {glowColor};"
    >
      <div class="spotlight" style:opacity={$glowOpacity}></div>
      <div class="card-content"><slot /></div>
    </div>
  </button>
{:else}
  <div
    bind:this={cardEl}
    class="premium-card-wrapper"
    style:transform="scale({scaleVal}) translateY({translateYVal}px)"
    style:box-shadow="0 {$shadowSpread / 2}px {$shadowSpread}px rgba(0,0,0,0.25), 0 0 0 1px rgba(255,255,255,{0.08 + $glowOpacity * 0.06})"
    on:mousemove={handleMouseMove}
    on:mouseenter={handleMouseEnter}
    on:mouseleave={handleMouseLeave}
  >
    <div class="premium-card" style="--mx: {mouseX}px; --my: {mouseY}px; --glow: {glowColor};">
      <div class="spotlight" style:opacity={$glowOpacity}></div>
      <div class="card-content"><slot /></div>
    </div>
  </div>
{/if}

<style>
  .premium-card-wrapper {
    display: block;
    text-decoration: none;
    color: inherit;
    border-radius: 16px;
    cursor: pointer;
    will-change: transform;
    transition: border-color 0.4s ease;
  }

  .premium-btn {
    width: 100%;
    border: none;
    font: inherit;
    text-align: left;
    background: none;
  }

  .premium-card {
    position: relative;
    padding: 24px;
    border-radius: 16px;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    backdrop-filter: blur(20px) saturate(120%);
    -webkit-backdrop-filter: blur(20px) saturate(120%);
    overflow: hidden;
    z-index: 1;
    transition: border-color 0.4s ease;
  }

  .premium-card-wrapper:hover .premium-card {
    border-color: rgba(255, 255, 255, 0.18);
  }

  /* ─── Mouse-following spotlight ─── */
  .spotlight {
    position: absolute;
    inset: 0;
    background: radial-gradient(
      350px circle at var(--mx) var(--my),
      color-mix(in srgb, var(--glow) 18%, transparent),
      transparent 80%
    );
    pointer-events: none;
    z-index: 2;
    transition: opacity 0.3s ease;
  }

  .card-content {
    position: relative;
    z-index: 3;
  }
</style>
