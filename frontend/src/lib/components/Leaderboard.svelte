<script lang="ts">
  import { onMount } from 'svelte';
  import { apiJson } from '$lib/api/client';

  interface LeaderboardEntry {
    rank: number;
    userId: number;
    score: number;
  }

  let entries: LeaderboardEntry[] = [];
  let loading = true;

  onMount(async () => {
    try {
      entries = await apiJson<LeaderboardEntry[]>('/api/leaderboard?count=10');
    } catch { /* fallback */ }
    loading = false;
  });
</script>

<div class="leaderboard glass spotlight-overlay" style="--spot-x: 50%; --spot-y: 0%;">
  <h3 class="lb-title">天梯排行榜</h3>
  {#if loading}
    <p class="lb-empty">加载中...</p>
  {:else if entries.length === 0}
    <p class="lb-empty">暂无排名数据</p>
  {:else}
    <div class="lb-table">
      <div class="lb-header">
        <span class="lb-col-rank">#</span>
        <span class="lb-col-player">玩家</span>
        <span class="lb-col-score">积分</span>
      </div>
      {#each entries as entry}
        <div class="lb-row spring-hover" class:lb-top3={entry.rank <= 3}>
          <span class="lb-col-rank">
            {#if entry.rank === 1}🥇
            {:else if entry.rank === 2}🥈
            {:else if entry.rank === 3}🥉
            {:else}{entry.rank}
            {/if}
          </span>
          <span class="lb-col-player">玩家 #{entry.userId}</span>
          <span class="lb-col-score">{entry.score.toFixed(0)}</span>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .leaderboard {
    padding: 20px 24px;
    border-radius: 16px;
  }
  .lb-title {
    font-size: 16px;
    margin-bottom: 16px;
    text-align: center;
  }
  .lb-empty {
    text-align: center;
    color: var(--text-secondary);
    font-size: 13px;
    padding: 16px 0;
  }
  .lb-table {
    display: flex;
    flex-direction: column;
  }
  .lb-header {
    display: flex;
    padding: 6px 0;
    font-size: 12px;
    color: var(--text-secondary);
    border-bottom: 1px solid rgba(255,255,255,0.06);
    margin-bottom: 4px;
  }
  .lb-row {
    display: flex;
    padding: 10px 0;
    font-size: 14px;
    border-bottom: 1px solid rgba(255,255,255,0.04);
  }
  .lb-top3 {
    font-weight: 600;
  }
  .lb-col-rank {
    width: 40px;
    text-align: center;
    flex-shrink: 0;
  }
  .lb-col-player {
    flex: 1;
  }
  .lb-col-score {
    width: 60px;
    text-align: right;
    font-weight: 600;
    flex-shrink: 0;
  }
</style>
