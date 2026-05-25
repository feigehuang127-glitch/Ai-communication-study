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
  }
  .card-link:hover { text-decoration: none; }
  .card-button {
    width: 100%;
    border: none;
    font: inherit;
    text-align: left;
  }
</style>
