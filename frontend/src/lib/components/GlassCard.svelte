<script>
  export let href = undefined;
  export let onClick = undefined;
</script>

{#if href}
  <a {href} class="glass card-link">
    <slot />
  </a>
{:else if onClick}
  <button class="glass card-button" on:click={onClick}>
    <slot />
  </button>
{:else}
  <div class="glass card">
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
    transition: transform 0.2s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  }
  .card-link:hover { text-decoration: none; }
  .card-button {
    width: 100%;
    border: none;
    font: inherit;
    text-align: left;
  }
  .card-link:hover, .card-button:hover {
    transform: translateY(-2px);
    box-shadow:
      0 0 0 1px rgba(100, 180, 255, 0.1),
      0 4px 20px rgba(100, 180, 255, 0.06),
      0 12px 40px rgba(0, 0, 0, 0.4);
  }
</style>
