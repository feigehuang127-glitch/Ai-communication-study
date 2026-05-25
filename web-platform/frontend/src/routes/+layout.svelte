<script>
  import { onMount } from 'svelte';
  import GlassNavbar from '$lib/components/GlassNavbar.svelte';
  import ParticleBackground from '$lib/components/ParticleBackground.svelte';
  import Toast from '$lib/components/Toast.svelte';
  import { checkAuth } from '$lib/stores/auth';

  onMount(() => {
    const token = localStorage.getItem('token');
    if (token) {
      checkAuth();
    }
  });
</script>

<div class="app-shell">
  <ParticleBackground />
  <GlassNavbar />
  <main class="main-content">
    <slot />
  </main>
  <Toast />
</div>

<style>
  .app-shell {
    position: relative;
    min-height: 100vh;
  }
  .main-content {
    position: relative;
    z-index: 1;
    max-width: 1280px;
    margin: 0 auto;
    padding: 80px 24px 40px;
  }
</style>
