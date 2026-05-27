<script>
  import SkeletonLine from './SkeletonLine.svelte';

  export let lines = 3;
</script>

<div class="glass skeleton-card">
  <SkeletonLine width="60%" height="18px" rounded="6px" />
  <div class="skeleton-lines">
    {#each Array(lines) as _}
      <SkeletonLine width="{85 + Math.random() * 15}%" />
    {/each}
  </div>
  <div class="skeleton-avatar-row">
    <div class="skeleton-shimmer skeleton-avatar" />
    <SkeletonLine width="40%" />
  </div>
</div>

<style>
  .skeleton-card {
    padding: 24px;
  }
  .skeleton-lines {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin: 16px 0;
  }
  .skeleton-avatar-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 8px;
  }
  .skeleton-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    flex-shrink: 0;
  }
</style>
