<script>
  import GlassCard from '$lib/components/GlassCard.svelte';

  const modes = [
    { name: '每日挑战', desc: '每日刷新，获取额外积分', mode: 'daily', icon: '🔥' },
    { name: 'PVE 闯关', desc: '按课程进度逐关解锁', mode: 'pve', icon: '⚔️' },
    { name: 'PVP 天梯', desc: '全站题库随机对战', mode: 'pvp', icon: '🏆', coming: true }
  ];
</script>

<div class="game-lobby">
  <h1 class="gradient-text">竞技中心</h1>
  <p class="sub">选择你的挑战模式</p>

  <div class="mode-grid">
    {#each modes as mode}
      <GlassCard href={mode.coming ? undefined : `/game/play?mode=${mode.mode}`}>
        <div class="mode-card">
          <span class="mode-icon">{mode.icon}</span>
          <h3>{mode.name}</h3>
          <p>{mode.desc}</p>
          {#if mode.coming}<span class="badge">即将上线</span>{/if}
        </div>
      </GlassCard>
    {/each}
  </div>
</div>

<style>
  .game-lobby { text-align: center; }
  .sub { color: var(--text-secondary); margin: 8px 0 32px; }
  .mode-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    max-width: 700px;
    margin: 0 auto;
  }
  .mode-card { padding: 12px 0; }
  .mode-icon { font-size: 40px; display: block; margin-bottom: 8px; }
  .mode-card h3 { font-size: 16px; margin-bottom: 4px; }
  .mode-card p { color: var(--text-secondary); font-size: 13px; }
  .badge {
    display: inline-block; padding: 2px 8px;
    background: rgba(200,150,255,0.15);
    color: var(--accent-purple); border-radius: 20px;
    font-size: 11px; margin-top: 8px;
  }
</style>
