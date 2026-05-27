<script>
  export let href = undefined;
  export let onClick = undefined;
  export let glowColor = 'rgba(100, 180, 255, 0.12)';

  let cardEl: HTMLElement;

  function handleMouseMove(e: MouseEvent) {
    if (!cardEl) return;
    const { left, top } = cardEl.getBoundingClientRect();
    cardEl.style.setProperty('--mouse-x', `${e.clientX - left}px`);
    cardEl.style.setProperty('--mouse-y', `${e.clientY - top}px`);
  }
</script>

{#if href}
  <a {href} class="glass glow-card card-link" bind:this={cardEl} on:mousemove={handleMouseMove}>
    <slot />
  </a>
{:else if onClick}
  <button class="glass glow-card card-button" bind:this={cardEl} on:mousemove={handleMouseMove} on:click={onClick}>
    <slot />
  </button>
{:else}
  <div class="glass glow-card card" bind:this={cardEl} on:mousemove={handleMouseMove}>
    <slot />
  </div>
{/if}

<style>
  .card, .card-link, .card-button {
    display: block;
    padding: 24px;
    text-decoration: none;
    color: inherit;
    cursor: pointer;
    transition: transform 0.4s var(--ease-spring-bouncy),
                box-shadow 0.35s var(--ease-spring-damped),
                border-color 0.35s var(--ease-spring-damped);
  }
  .card-link:hover { text-decoration: none; }
  .card-button {
    width: 100%;
    border: none;
    font: inherit;
    text-align: left;
  }

  /* ─── Mouse-tracking spotlight glow ─── */
  .glow-card {
    position: relative;
    overflow: hidden;
  }
  .glow-card::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(
      800px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
      rgba(100, 180, 255, 0.1),
      transparent 40%
    );
    z-index: 0;
    pointer-events: none;
    transition: opacity 0.3s ease;
    opacity: 0;
  }
  .glow-card:hover::before {
    opacity: 1;
  }
  .glow-card > :global(*) {
    position: relative;
    z-index: 1;
  }

  .card-link:hover, .card-button:hover {
    transform: translateY(-2px);
    box-shadow:
      0 0 0 1px rgba(100, 180, 255, 0.1),
      0 4px 20px rgba(100, 180, 255, 0.06),
      0 12px 40px rgba(0, 0, 0, 0.4);
  }
</style>
