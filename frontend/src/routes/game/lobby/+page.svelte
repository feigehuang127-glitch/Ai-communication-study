<script lang="ts">
  import GlassCard from '$lib/components/GlassCard.svelte';
  import Leaderboard from '$lib/components/Leaderboard.svelte';
  import { apiJson } from '$lib/api/client';

  let matching = false;
  let queueStatus = '';
  let matchError = '';

  async function joinQueue() {
    matching = true;
    matchError = '';
    queueStatus = '正在匹配对手...';
    try {
      const result = await apiJson<{
        status: string;
        matchId?: number;
        questions?: any[];
      }>('/api/pvp/queue/join', { method: 'POST' });

      if (result.status === 'matched' && result.matchId) {
        queueStatus = '匹配成功！';
        window.location.href = `/game/play?mode=pvp&matchId=${result.matchId}`;
      } else if (result.status === 'queued') {
        queueStatus = '已加入匹配队列，等待对手...';
      } else if (result.status === 'already_queued') {
        queueStatus = '你已在匹配队列中';
      }
    } catch (e: any) {
      matchError = e.message || '匹配失败，请重试';
      queueStatus = '';
    }
    matching = false;
  }

  async function leaveQueue() {
    try {
      await apiJson('/api/pvp/queue/leave', { method: 'POST' });
      queueStatus = '';
    } catch { /* ignore */ }
    matching = false;
  }
</script>

<div class="lobby">
  <h1 class="gradient-text">竞技中心</h1>
  <p class="sub">选择你的挑战模式</p>

  <div class="mode-grid">
    <GlassCard href="/game/play?mode=pve">
      <div class="mode-card">
        <span class="mode-icon">⚔️</span>
        <h3>PVE 闯关</h3>
        <p>按课程进度逐关解锁，循序渐进攻克题库</p>
      </div>
    </GlassCard>

    <GlassCard onClick={joinQueue}>
      <div class="mode-card">
        <span class="mode-icon">🏆</span>
        <h3>PVP 天梯</h3>
        <p>全站题库随机对战，挑战其他玩家，冲击排行榜</p>
        {#if !matching && !queueStatus}
          <button class="mm-btn" on:click|stopPropagation={joinQueue}>开始匹配</button>
        {:else if matching}
          <button class="mm-btn loading" disabled>匹配中...</button>
        {:else if queueStatus}
          <span class="queue-status">{queueStatus}</span>
          <button class="mm-btn cancel" on:click|stopPropagation={leaveQueue}>取消排队</button>
        {/if}
        {#if matchError}
          <p class="mm-error">{matchError}</p>
        {/if}
      </div>
    </GlassCard>

    <GlassCard href="/game/play?mode=daily">
      <div class="mode-card">
        <span class="mode-icon">🔥</span>
        <h3>每日挑战</h3>
        <p>每日刷新限定题目，获取额外积分奖励</p>
      </div>
    </GlassCard>
  </div>

  <div class="lb-wrapper">
    <Leaderboard />
  </div>
</div>

<style>
  .lobby {
    max-width: 900px;
    margin: 0 auto;
    text-align: center;
  }
  .sub {
    color: var(--text-secondary);
    margin: 8px 0 32px;
  }
  .mode-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 40px;
  }
  .mode-card {
    padding: 16px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .mode-icon {
    font-size: 40px;
    display: block;
    margin-bottom: 12px;
  }
  .mode-card h3 {
    font-size: 16px;
    margin-bottom: 4px;
  }
  .mode-card p {
    color: var(--text-secondary);
    font-size: 13px;
    max-width: 200px;
    margin-bottom: 12px;
  }
  .mm-btn {
    margin-top: 8px;
    padding: 8px 24px;
    border: none;
    border-radius: 20px;
    background: linear-gradient(135deg, var(--accent-purple), #7c3aed);
    color: #fff;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: opacity 0.2s;
  }
  .mm-btn:hover {
    opacity: 0.85;
  }
  .mm-btn.loading {
    opacity: 0.6;
    cursor: not-allowed;
  }
  .mm-btn.cancel {
    background: rgba(255,255,255,0.1);
    margin-top: 4px;
  }
  .queue-status {
    display: block;
    margin-top: 8px;
    font-size: 12px;
    color: var(--accent-gold);
  }
  .mm-error {
    color: #ef4444;
    font-size: 12px;
    margin-top: 8px;
  }
  .lb-wrapper {
    max-width: 500px;
    margin: 0 auto;
  }
</style>
