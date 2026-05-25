<script>
  import { auth } from '$lib/stores/auth';
  import { page } from '$app/stores';
</script>

<nav class="glass navbar">
  <a href="/" class="logo gradient-text">AI Academy</a>
  <div class="nav-links">
    <a href="/college/comm" class:active={$page.url.pathname.startsWith('/college')}>学院</a>
    <a href="/lab" class:active={$page.url.pathname.startsWith('/lab')}>实验场</a>
    <a href="/game" class:active={$page.url.pathname.startsWith('/game')}>竞技</a>
    <a href="/profile" class:active={$page.url.pathname.startsWith('/profile')}>
      {$auth.user?.username || '个人中心'}
    </a>
  </div>
</nav>

<style>
  .navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    height: 60px;
    border-radius: 0;
    border-top: none;
    border-left: none;
    border-right: none;
  }
  .logo {
    font-size: 20px;
    font-weight: 700;
    text-decoration: none;
  }
  .nav-links {
    display: flex;
    gap: 24px;
  }
  .nav-links a {
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 14px;
    font-weight: 500;
    transition: color 0.2s;
  }
  .nav-links a:hover, .nav-links a.active {
    color: var(--text-primary);
  }
</style>
