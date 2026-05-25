<script>
  import { auth } from '$lib/stores/auth';
  import { goto } from '$app/navigation';

  function handleLogout() {
    auth.logout();
    goto('/login');
  }
</script>

<div class="settings">
  <h1 class="gradient-text">设置</h1>

  <div class="glass section">
    <h3>账户安全</h3>
    <p>修改密码、管理登录设备</p>
    <button class="btn-secondary" disabled>即将上线</button>
  </div>

  <div class="glass section">
    <h3>AI 行为监测</h3>
    <p>控制 AI 助手的主动干预程度</p>
    <select class="select">
      <option>低 — 仅响应我的提问</option>
      <option selected>中 — 适时提醒</option>
      <option>高 — 主动指导</option>
    </select>
  </div>

  <div class="glass section">
    <h3>退出登录</h3>
    <p>退出当前账户</p>
    <button class="btn-danger" on:click={handleLogout}>退出登录</button>
  </div>
</div>

<style>
  .settings { max-width: 600px; margin: 0 auto; }
  .section { padding: 24px; margin-top: 16px; }
  .section h3 { margin-bottom: 4px; font-size: 16px; }
  .section p { color: var(--text-secondary); font-size: 13px; margin-bottom: 12px; }
  .btn-secondary {
    padding: 8px 20px;
    background: rgba(255,255,255,0.06);
    border: 1px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-primary);
    cursor: pointer;
  }
  .btn-secondary:disabled { opacity: 0.4; cursor: not-allowed; }
  .btn-danger {
    padding: 8px 20px;
    background: rgba(255,100,100,0.15);
    border: 1px solid var(--accent-red);
    border-radius: 8px;
    color: var(--accent-red);
    cursor: pointer;
  }
  .select {
    padding: 8px 12px;
    background: rgba(255,255,255,0.05);
    border: 1px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-primary);
    font-size: 13px;
    outline: none;
  }
</style>
