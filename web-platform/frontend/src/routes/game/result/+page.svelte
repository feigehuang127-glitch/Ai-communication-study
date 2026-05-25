<script lang="ts">
  import { onMount } from 'svelte';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let result: any = null;

  onMount(() => {
    const raw = sessionStorage.getItem('gameResult');
    if (raw) result = JSON.parse(raw);
  });
</script>

{#if result}
  <div class="result-page">
    <GlassCard>
      <div class="result-header">
        <h1 class="gradient-text">
          {result.result === 'win_combo' ? '连对通关!' :
           result.result === 'win' ? '恭喜获胜!' : '挑战失败'}
        </h1>
        <div class="stats">
          <div class="stat">
            <span class="stat-value">{result.correctCount}/{result.totalQuestions}</span>
            <span class="stat-label">正确率</span>
          </div>
          <div class="stat">
            <span class="stat-value">{result.scoreEarned > 0 ? '+' : ''}{result.scoreEarned}</span>
            <span class="stat-label">积分</span>
          </div>
        </div>
      </div>

      <div class="actions">
        <a href="/game" class="btn-secondary">返回大厅</a>
        <a href="/college/comm" class="btn-primary">去学习</a>
        <a href="/profile/wrongbook" class="btn-secondary">查看错题</a>
      </div>
    </GlassCard>
  </div>
{/if}

<style>
  .result-page { max-width: 500px; margin: 0 auto; text-align: center; }
  .result-header { padding: 24px 0; }
  .result-header h1 { font-size: 28px; margin-bottom: 24px; }
  .stats { display: flex; justify-content: center; gap: 40px; }
  .stat { display: flex; flex-direction: column; }
  .stat-value { font-size: 32px; font-weight: 700; }
  .stat-label { font-size: 12px; color: var(--text-secondary); margin-top: 4px; }
  .actions { display: flex; gap: 12px; justify-content: center; padding: 16px 0; }
  .btn-primary, .btn-secondary {
    padding: 10px 24px; border-radius: 10px; font-size: 14px;
    font-weight: 500; text-decoration: none; cursor: pointer;
  }
  .btn-primary { background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple)); color: white; border: none; }
  .btn-secondary { background: rgba(255,255,255,0.06); border: 1px solid var(--glass-border); color: var(--text-primary); }
</style>
