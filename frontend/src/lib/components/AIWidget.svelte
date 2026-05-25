<script lang="ts">
  import { chat } from '$lib/stores/chat';
  import { page } from '$app/stores';

  const pageToPersona: Record<string, string> = {
    '/college': 'lecturer',
    '/lab': 'code_mentor',
    '/game': 'study_buddy',
    '/profile': 'analyst',
  };

  function handleClick() {
    let pid = 'lecturer';
    for (const [prefix, id] of Object.entries(pageToPersona)) {
      if ($page.url.pathname.startsWith(prefix)) { pid = id; break; }
    }
    chat.open(pid);
  }
</script>

<button class="ai-widget glass" on:click={handleClick} title="AI 助手 (Ctrl+K)">
  <span class="widget-icon">✨</span>
</button>

<style>
  .ai-widget {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 999;
    width: 52px;
    height: 52px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 0;
    border: 1px solid var(--glass-border);
  }
  .ai-widget:hover {
    transform: scale(1.08);
    box-shadow: 0 0 24px rgba(100, 180, 255, 0.3);
  }
  .widget-icon {
    font-size: 22px;
  }
</style>
