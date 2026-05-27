<script>
  import PremiumCard from '$lib/components/PremiumCard.svelte';
</script>

<div class="lab-page">
  <h1 class="gradient-text">实验场</h1>
  <p class="sub">动手实践，构建你的 AI 项目</p>

  <div class="lab-grid">
    <PremiumCard href="/lab/sandbox" glowColor="var(--accent-blue)">
      <div class="lab-card">
        <span class="lab-icon">🐳</span>
        <h3>代码沙箱</h3>
        <p>Docker 隔离的在线代码执行环境</p>
      </div>
    </PremiumCard>
    <PremiumCard href="/lab/prompt-playground" glowColor="var(--accent-purple)">
      <div class="lab-card">
        <span class="lab-icon">🔬</span>
        <h3>提示词实验场</h3>
        <p>多模型横向评测对比</p>
      </div>
    </PremiumCard>
    <PremiumCard href="/lab/agent-builder" glowColor="var(--accent-green)">
      <div class="lab-card">
        <span class="lab-icon">🤖</span>
        <h3>Agent 构建器</h3>
        <p>可视化拖拽构建 Agent 工作流</p>
      </div>
    </PremiumCard>
  </div>
</div>

<style>
  .lab-page { max-width: 900px; margin: 0 auto; }
  .sub { color: var(--text-secondary); margin: 8px 0 32px; }
  .lab-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
  .lab-card { text-align: center; padding: 16px 0; }
  .lab-icon { font-size: 36px; display: block; margin-bottom: 12px; }
  .lab-card h3 { font-size: 16px; margin-bottom: 4px; }
  .lab-card p { color: var(--text-secondary); font-size: 13px; }
  .badge {
    display: inline-block; padding: 2px 8px;
    background: rgba(200,150,255,0.15); color: var(--accent-purple);
    border-radius: 20px; font-size: 11px; margin-top: 10px;
  }
</style>
