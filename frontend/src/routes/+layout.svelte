<script>
  import { onMount } from 'svelte';
  import { fly, fade } from 'svelte/transition';
  import { page } from '$app/stores';
  import { onNavigate } from '$app/navigation';
  import GlassNavbar from '$lib/components/GlassNavbar.svelte';
  import ParticleBackground from '$lib/components/ParticleBackground.svelte';
  import Toast from '$lib/components/Toast.svelte';
  import AIWidget from '$lib/components/AIWidget.svelte';
  import CommandPalette from '$lib/components/CommandPalette.svelte';
  import SlidePanel from '$lib/components/SlidePanel.svelte';
  import InterventionDispatcher from '$lib/components/InterventionDispatcher.svelte';
  import InterventionSidebar from '$lib/components/InterventionSidebar.svelte';
  import { checkAuth } from '$lib/stores/auth';
  import { behaviorEngine } from '$lib/behavior/BehaviorEngine';
  import { getCollector } from '$lib/behavior/collector';

  onMount(() => {
    const token = localStorage.getItem('token');
    if (token) {
      checkAuth();
    }

    // Activate behavior pipeline if user opted in
    if (localStorage.getItem('ai-intervention-enabled') === 'true') {
      behaviorEngine.enable();
      getCollector().setupListeners();
      getCollector().onEvent((event) => behaviorEngine.pushEvent(event));
    }
  });

  // View Transitions API: seamless cross-page element morphing
  onNavigate((navigation) => {
    if (!document.startViewTransition) return;
    return new Promise((resolve) => {
      document.startViewTransition(async () => {
        resolve();
        await navigation.complete;
      });
    });
  });
</script>

<div class="app-shell">
  <ParticleBackground />
  <GlassNavbar />
  {#key $page.url.pathname}
    <main class="main-content" in:fly={{ x: 0, y: 20, duration: 300 }} out:fade={{ duration: 150 }}>
      <slot />
    </main>
  {/key}
  <Toast />
  <AIWidget />
  <CommandPalette />
  <SlidePanel />
  <InterventionDispatcher />
  <InterventionSidebar />
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
    view-transition-name: main-content;
  }
</style>
