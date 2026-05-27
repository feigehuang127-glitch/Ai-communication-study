<script lang="ts">
  import { onDestroy } from 'svelte';
  import { goto } from '$app/navigation';
  import PremiumCard from '$lib/components/PremiumCard.svelte';
  import Leaderboard from '$lib/components/Leaderboard.svelte';
  import { apiJson } from '$lib/api/client';

  let queuing = false;
  let queueStatus: string | null = null;
  let matchError = '';
  let matchResult: any = null;
  let pollInterval: ReturnType<typeof setInterval> | null = null;

  async function joinPvPQueue() {
    queuing = true;
    matchError = '';
    queueStatus = '正在匹配对手...';
    try {
      const token = localStorage.getItem('token');
      const res = await apiJson<any>('/api/pvp/queue/join', { method: 'POST' });
      if (res.status === 'matched') {
        goto(`/game/play?mode=pvp&matchId=${res.matchId}`);
      } else if (res.status === 'queued') {
        queueStatus = '已加入匹配队列，等待对手...';
        // Start polling
        pollInterval = setInterval(async () => {
          try {
            const check = await apiJson<any>('/api/pvp/queue/join', { method: 'POST' });
            if (check.status === 'matched') {
              clearInterval(pollInterval!);
              goto(`/game/play?mode=pvp&matchId=${check.matchId}`);
            }
          } catch {}
        }, 3000);
      }
      matchResult = res;
    } catch (e: any) {
      queueStatus = '匹配失败，请重试';
    } finally {
      queuing = false;
    }
  }

  async function joinQueue() {
    await joinPvPQueue();
  }

  async function leaveQueue() {
    try {
      await apiJson('/api/pvp/queue/leave', { method: 'POST' });
    } catch { /* ignore */ }
    if (pollInterval) { clearInterval(pollInterval); pollInterval = null; }
    queuing = false;
    queueStatus = null;
  }

  onDestroy(() => {
    if (pollInterval) clearInterval(pollInterval);
  });
</script>

<div class="lobby">
  <h1 class="gradient-text">竞技中心</h1>
  <p class="sub">选择你的挑战模式</p>

  <div class="mode-grid">
    <PremiumCard href="/game/play?mode=pve" glowColor="var(--accent-blue)">
      <div class="mode-card">
        <span class="mode-icon">⚔️</span>
        <h3>PVE 闯关</h3>
        <p>按课程进度逐关解锁，循序渐进攻克题库</p>
      </div>
    </PremiumCard>

    <PremiumCard onClick={joinQueue} glowColor="var(--accent-gold)">
      <div class="mode-card">
        <span class="mode-icon">🏆</span>
        <h3>PVP 天梯</h3>
        <p>全站题库随机对战，挑战其他玩家，冲击排行榜</p>
        {#if !queuing && !queueStatus}
          <button class="mm-btn" on:click|stopPropagation={joinQueue}>开始匹配</button>
        {:else if queuing}
          <button class="mm-btn loading" disabled>匹配中...</button>
        {:else if queueStatus}
          <span class="queue-status">{queueStatus}</span>
          <button class="mm-btn cancel" on:click|stopPropagation={leaveQueue}>取消排队</button>
        {/if}
        {#if matchError}
          <p class="mm-error">{matchError}</p>
        {/if}
      </div>
    </PremiumCard>

    <PremiumCard href="/game/play?mode=daily" glowColor="var(--accent-red)">
      <div class="mode-card">
        <span class="mode-icon">🔥</span>
        <h3>每日挑战</h3>
        <p>每日刷新限定题目，获取额外积分奖励</p>
      </div>
    </PremiumCard>
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
