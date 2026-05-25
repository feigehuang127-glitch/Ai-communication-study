<script>
  import { auth } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let username = '';
  let password = '';
  let error = '';
  let loading = false;

  async function handleLogin() {
    error = '';
    loading = true;
    try {
      await auth.login(username, password);
      goto('/');
    } catch (e) {
      error = '用户名或密码错误';
    } finally {
      loading = false;
    }
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') handleLogin();
  }
</script>

<div class="login-page">
  <div class="login-container">
    <h1 class="gradient-text logo-text">AI Academy</h1>
    <p class="subtitle">AI 交互式学习平台</p>

    <div class="glass login-card">
      <h2>登录</h2>
      {#if error}
        <div class="error">{error}</div>
      {/if}
      <input type="text" placeholder="用户名" bind:value={username}
             on:keydown={handleKeydown} />
      <input type="password" placeholder="密码" bind:value={password}
             on:keydown={handleKeydown} />
      <button class="btn-primary" on:click={handleLogin} disabled={loading}>
        {loading ? '登录中...' : '登录'}
      </button>
    </div>
  </div>
</div>

<style>
  .login-page {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    margin: -80px -24px -40px;
  }
  .login-container {
    text-align: center;
    width: 100%;
    max-width: 400px;
    padding: 0 24px;
  }
  .logo-text {
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 4px;
  }
  .subtitle {
    color: var(--text-secondary);
    margin-bottom: 32px;
  }
  .login-card {
    padding: 32px;
    text-align: left;
  }
  .login-card h2 {
    margin-bottom: 20px;
    font-size: 18px;
  }
  input {
    width: 100%;
    padding: 12px 16px;
    margin-bottom: 12px;
    background: rgba(255,255,255,0.05);
    border: 1px solid var(--glass-border);
    border-radius: 10px;
    color: var(--text-primary);
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;
  }
  input:focus {
    border-color: var(--accent-blue);
  }
  .btn-primary {
    width: 100%;
    padding: 12px;
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border: none;
    border-radius: 10px;
    color: white;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    margin-top: 8px;
  }
  .btn-primary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  .error {
    background: rgba(255, 100, 100, 0.15);
    color: var(--accent-red);
    padding: 10px 14px;
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 13px;
  }
</style>
