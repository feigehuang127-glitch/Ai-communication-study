<script>
  import GlassCard from '$lib/components/GlassCard.svelte';

  const colleges = [
    { name: '通信学院', slug: 'comm', desc: '通信原理 · 数据通信网 · 信号与系统', icon: '📡' },
    { name: 'AI 学院', slug: 'ai', desc: '提示词工程 · Skills · Agent 开发', icon: '🤖', coming: true }
  ];

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
    <div class="college-grid">
      {#each colleges as college}
        <GlassCard href={college.coming ? undefined : `/college/${college.slug}`}>
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
        </GlassCard>
      {/each}
    </div>
  </section>

  <section class="quick-actions">
    <h2>快速入口</h2>
    <div class="actions-row">
      {#each quickActions as action}
        <GlassCard href={action.href}>
          <span>{action.label}</span>
        </GlassCard>
      {/each}
    </div>
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
</style>
