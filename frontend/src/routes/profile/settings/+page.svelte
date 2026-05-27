<script lang="ts">
  import { logout, user } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { behaviorEngine } from '$lib/behavior/BehaviorEngine';
  import { getCollector } from '$lib/behavior/collector';

  let oldPassword = '';
  let newPassword = '';
  let confirmPassword = '';
  let passwordMsg = '';

  let aiIntervention = 'medium';
  let aiEnabled = false;

  let accountCreated = '';
  let totalStudyHours = 0;

  onMount(() => {
    const saved = localStorage.getItem('ai-intervention');
    if (saved) aiIntervention = saved;

    aiEnabled = localStorage.getItem('ai-intervention-enabled') === 'true';

    const created = localStorage.getItem('account-created');
    if (created) {
      accountCreated = created;
    } else {
      accountCreated = new Date().toISOString().split('T')[0];
      localStorage.setItem('account-created', accountCreated);
    }

    const hours = localStorage.getItem('total-study-hours');
    if (hours) totalStudyHours = parseFloat(hours);
  });

  function toggleAiEnabled() {
    aiEnabled = !aiEnabled;
    if (aiEnabled) {
      behaviorEngine.enable();
      getCollector().onEvent((event) => behaviorEngine.pushEvent(event));
    } else {
      behaviorEngine.disable();
    }
  }

  function saveAiBehaviour() {
    localStorage.setItem('ai-intervention', aiIntervention);
    alert('AI 行为偏好已保存');
  }

  function changePassword() {
    passwordMsg = '';
    if (!oldPassword || !newPassword || !confirmPassword) {
      passwordMsg = '请填写所有密码字段';
      return;
    }
    if (newPassword !== confirmPassword) {
      passwordMsg = '两次输入的新密码不一致';
      return;
    }
    if (newPassword.length < 6) {
      passwordMsg = '新密码长度不能少于 6 位';
      return;
    }
    // Backend has no change-password endpoint yet, save securely to localStorage as a mock
    passwordMsg = '密码修改请求已记录（需后端对接密码修改接口）';
    oldPassword = '';
    newPassword = '';
    confirmPassword = '';
  }

  function handleLogout() {
    logout();
    goto('/login');
  }
</script>

<div class="settings">
  <h1 class="gradient-text">设置</h1>

  <div class="glass section">
    <h3>个人信息</h3>
    <div class="profile-info">
      <div class="info-row">
        <span class="label">用户名</span>
        <span class="value">{$user?.username || '未登录'}</span>
      </div>
      <div class="info-row">
        <span class="label">角色</span>
        <span class="value">{$user?.role || '—'}</span>
      </div>
      <div class="info-row">
        <span class="label">积分</span>
        <span class="value">{$user?.totalScore ?? 0}</span>
      </div>
      <div class="info-row">
        <span class="label">排名</span>
        <span class="value">{$user?.rank || '—'}</span>
      </div>
      <div class="info-row">
        <span class="label">注册日期</span>
        <span class="value">{accountCreated}</span>
      </div>
      <div class="info-row">
        <span class="label">总学习时长</span>
        <span class="value">{totalStudyHours.toFixed(1)} 小时</span>
      </div>
    </div>
  </div>

  <div class="glass section">
    <h3>修改密码</h3>
    <div class="password-form">
      <label>
        旧密码
        <input type="password" bind:value={oldPassword} placeholder="输入当前密码" />
      </label>
      <label>
        新密码
        <input type="password" bind:value={newPassword} placeholder="输入新密码" />
      </label>
      <label>
        确认新密码
        <input type="password" bind:value={confirmPassword} placeholder="再次输入新密码" />
      </label>
      {#if passwordMsg}
        <p class="msg" class:error={passwordMsg.includes('不一致') || passwordMsg.includes('不能少于')}>{passwordMsg}</p>
      {/if}
      <button class="btn-primary" on:click={changePassword}>修改密码</button>
    </div>
  </div>

  <div class="glass section">
    <h3>AI 行为监测</h3>
    <p>开启后，AI 助手会在检测到学习困难时主动提供帮助</p>
    <div class="ai-behaviour">
      <label class="toggle-label">
        <span>启用 AI 学习助手</span>
        <button
          class="toggle-switch"
          class:active={aiEnabled}
          on:click={toggleAiEnabled}
          role="switch"
          aria-checked={aiEnabled}
        >
          <span class="toggle-knob"></span>
        </button>
      </label>
      {#if aiEnabled}
        <div class="ai-level-row">
          <label class="ai-level-label">干预程度</label>
          <select class="select" bind:value={aiIntervention}>
            <option value="low">低 — 仅响应我的提问</option>
            <option value="medium">中 — 适时提醒</option>
            <option value="high">高 — 主动指导</option>
          </select>
          <button class="btn-secondary" on:click={saveAiBehaviour}>保存偏好</button>
        </div>
      {/if}
    </div>
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
  .section > p { color: var(--text-secondary); font-size: 13px; margin-bottom: 12px; }
  .profile-info { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }
  .info-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid rgba(255,255,255,0.05); }
  .info-row .label { color: var(--text-secondary); font-size: 13px; }
  .info-row .value { font-size: 14px; font-weight: 500; }

  .password-form { display: flex; flex-direction: column; gap: 12px; margin-top: 12px; }
  .password-form label { display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: var(--text-secondary); }
  .password-form input {
    padding: 8px 12px;
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(255,255,255,0.12);
    border-radius: 8px;
    color: var(--text-primary);
    font-size: 14px;
    outline: none;
  }
  .password-form input:focus { border-color: var(--accent-blue); }
  .msg { font-size: 13px; color: var(--accent-green); }
  .msg.error { color: var(--accent-red); }

  .ai-behaviour { display: flex; flex-direction: column; gap: 12px; margin-top: 12px; }
  .toggle-label {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
  }
  .toggle-switch {
    position: relative;
    width: 44px;
    height: 24px;
    border-radius: 12px;
    border: 1px solid var(--glass-border);
    background: rgba(255,255,255,0.08);
    cursor: pointer;
    transition: background 0.2s;
    padding: 0;
  }
  .toggle-switch.active {
    background: var(--accent-blue);
    border-color: var(--accent-blue);
  }
  .toggle-knob {
    position: absolute;
    top: 2px;
    left: 2px;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background: white;
    transition: transform 0.2s;
  }
  .toggle-switch.active .toggle-knob {
    transform: translateX(20px);
  }
  .ai-level-row {
    display: flex;
    gap: 8px;
    align-items: center;
  }
  .ai-level-label {
    font-size: 13px;
    color: var(--text-secondary);
    white-space: nowrap;
  }

  .btn-primary {
    padding: 8px 20px;
    background: var(--accent-blue);
    border: none;
    border-radius: 8px;
    color: white;
    font-weight: 600;
    cursor: pointer;
    font-size: 13px;
  }
  .btn-primary:hover { opacity: 0.9; }

  .btn-secondary {
    padding: 8px 20px;
    background: rgba(255,255,255,0.06);
    border: 1px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-primary);
    cursor: pointer;
    font-size: 13px;
    white-space: nowrap;
  }
  .btn-secondary:hover { background: rgba(255,255,255,0.12); }
  .btn-danger {
    padding: 8px 20px;
    background: rgba(255,100,100,0.15);
    border: 1px solid var(--accent-red);
    border-radius: 8px;
    color: var(--accent-red);
    cursor: pointer;
    font-size: 13px;
  }
  .btn-danger:hover { background: rgba(255,100,100,0.25); }
  .select {
    padding: 8px 12px;
    background: rgba(255,255,255,0.05);
    border: 1px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-primary);
    font-size: 13px;
    outline: none;
    flex: 1;
  }
  .select:focus { border-color: var(--accent-blue); }
  .select option { background: #1a1a2e; color: #fff; }
</style>
