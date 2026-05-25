<script>
  import { onMount } from 'svelte';
  import { apiJson } from '$lib/api/client';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let questions: any[] = [];

  onMount(async () => {
    try {
      questions = await apiJson('/api/wrongbook?status=0');
    } catch { /* handle */ }
  });
</script>

<div class="wrongbook">
  <h1 class="gradient-text">错题本</h1>
  <p class="sub">待消灭的错题：{questions.length} 道</p>

  {#if questions.length === 0}
    <div class="empty glass">
      <p>暂无错题</p>
    </div>
  {:else}
    <div class="list">
      {#each questions as wq}
        <GlassCard>
          <span class="tag">{wq.college}</span>
          <span class="error-count">错 {wq.errorCount} 次</span>
        </GlassCard>
      {/each}
    </div>
  {/if}

  <a href="/game/play?mode=practice&source=wrongbook" class="btn-primary">一键挑战错题</a>
</div>

<style>
  .wrongbook { max-width: 700px; margin: 0 auto; }
  .sub { color: var(--text-secondary); margin-bottom: 24px; }
  .empty { padding: 60px; text-align: center; color: var(--text-secondary); }
  .list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
  .tag {
    display: inline-block; padding: 2px 8px;
    background: rgba(100,180,255,0.1); color: var(--accent-blue);
    border-radius: 4px; font-size: 11px;
  }
  .error-count { float: right; color: var(--accent-red); font-size: 13px; }
  .btn-primary {
    display: block; width: 100%; padding: 14px; text-align: center;
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border-radius: 12px; color: white; font-size: 15px;
    font-weight: 600; text-decoration: none;
  }
</style>
