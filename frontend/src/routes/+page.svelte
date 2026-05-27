<script lang="ts">
  import { onMount } from 'svelte';
  import PremiumCard from '$lib/components/PremiumCard.svelte';
  import Leaderboard from '$lib/components/Leaderboard.svelte';
  import { apiJson } from '$lib/api/client';

  let colleges: Array<{ name: string; slug: string; desc: string; icon: string; coming: boolean }> = [];
  let loading = true;

  onMount(async () => {
    try {
      const data = await apiJson('/api/courses/colleges');
      colleges = data.map((c: any) => ({
        name: c.name,
        slug: c.slug,
        desc: c.description || '',
        icon: c.icon || '📚',
        coming: false,
      }));
    } catch {
      // Fallback to hardcoded list when API unavailable
      colleges = [
        { name: '通信学院', slug: 'comm', desc: '通信原理 · 数据通信网 · 信号与系统', icon: '📡', coming: true },
        { name: 'AI 学院', slug: 'ai', desc: '提示词工程 · Skills · Agent 开发', icon: '🤖', coming: true }
      ];
    } finally {
      loading = false;
    }
  });

  const quickActions = [
    { label: '每日挑战', href: '/game?mode=daily' },
    { label: '错题本', href: '/profile/wrongbook' },
    { label: '排行榜', href: '/profile' }
  ];
</script>

<div class="home">
  <section class="hero">
    <h1 class="gradient-text">掌握 AI，从第一行代码开始</h1>
    <p>交互式学习平台 — 提示词工程 · Skills 开发 · Agent 构建</p>
  </section>

  <section class="colleges">
    <h2>选择学院</h2>
    {#if loading}
      <p style="color: var(--text-secondary);">加载中...</p>
    {:else}
      <div class="college-grid">
        {#each colleges as college}
          <PremiumCard href={college.coming ? undefined : `/college/${college.slug}`}>
            <div class="college-card">
              <span class="college-icon">{college.icon}</span>
              <div>
                <h3>{college.name}</h3>
                <p>{college.desc}</p>
                {#if college.coming}
                  <span class="badge">即将上线</span>
                {/if}
              </div>
            </div>
          </PremiumCard>
        {/each}
      </div>
    {/if}
  </section>

  <section class="quick-actions">
    <h2>快速入口</h2>
    <div class="actions-row">
      {#each quickActions as action}
        <PremiumCard href={action.href}>
          <span>{action.label}</span>
        </PremiumCard>
      {/each}
    </div>
  </section>

  <section class="shortcuts">
    <h2>快速传送门</h2>
    <div class="portal-grid">
      <a href="/lab/sandbox" class="portal-link glass">
        <span>🐳</span> 代码沙箱
      </a>
      <a href="/lab/prompt-playground" class="portal-link glass">
        <span>🔬</span> 提示词实验场
      </a>
      <a href="/college/ai" class="portal-link glass">
        <span>🤖</span> AI 学院
      </a>
      <a href="/game/lobby" class="portal-link glass">
        <span>⚔️</span> 游戏大厅
      </a>
    </div>
  </section>

  <section class="leaderboard-section">
    <Leaderboard />
  </section>
</div>

<style>
  .home {
    display: flex;
    flex-direction: column;
    gap: 40px;
  }
  .hero {
    text-align: center;
    padding: 40px 0 20px;
  }
  .hero h1 {
    font-size: 36px;
    margin-bottom: 8px;
  }
  .hero p {
    color: var(--text-secondary);
    font-size: 16px;
  }
  section h2 {
    font-size: 18px;
    margin-bottom: 16px;
  }
  .college-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }
  .college-card {
    display: flex;
    gap: 16px;
    align-items: center;
  }
  .college-icon {
    font-size: 32px;
  }
  .college-card h3 {
    font-size: 16px;
    margin-bottom: 4px;
  }
  .college-card p {
    color: var(--text-secondary);
    font-size: 13px;
  }
  .badge {
    display: inline-block;
    padding: 2px 8px;
    background: rgba(200, 150, 255, 0.15);
    color: var(--accent-purple);
    border-radius: 20px;
    font-size: 11px;
    margin-top: 6px;
  }
  .actions-row {
    display: flex;
    gap: 12px;
  }
  .actions-row :global(.card) {
    padding: 16px 24px;
  }
  .shortcuts { max-width: 900px; margin: 32px auto 0; }
  .shortcuts h2 { font-size: 18px; margin-bottom: 16px; }
  .portal-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
  .portal-link { display: flex; align-items: center; gap: 8px; padding: 14px; text-decoration: none; color: var(--text-primary); font-size: 14px; border-radius: 12px; }
  .portal-link:hover { background: rgba(255,255,255,0.06); }
  .leaderboard-section { max-width: 600px; margin: 32px auto; }
</style>
