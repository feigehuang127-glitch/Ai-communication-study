<script lang="ts">
  import PremiumCard from '$lib/components/PremiumCard.svelte';

  let prompt = '解释一下什么是递归？';
  let results: Record<string, string> = {};
  let streaming: Record<string, string> = {};
  let loading = false;
  let selectedModels = ['claude-sonnet-4-6', 'deepseek-v3', 'gpt-4o'];
  let activeModels: Set<string> = new Set();

  async function runCompare() {
    loading = true;
    results = {};
    streaming = {};
    activeModels = new Set();
    try {
      const token = localStorage.getItem('token');
      const res = await fetch('/api/ai/compare', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ prompt, models: selectedModels }),
      });
      if (!res.ok) {
        loading = false;
        return;
      }
      const reader = res.body?.getReader();
      if (!reader) { loading = false; return; }
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        for (const line of lines) {
          if (!line.startsWith('data: ')) continue;
          if (line.includes('[DONE]')) continue;
          try {
            const data = JSON.parse(line.slice(6));
            if (data.status === 'start') {
              activeModels.add(data.model);
              streaming = { ...streaming, [data.model]: '' };
            } else if (data.status === 'done') {
              activeModels.delete(data.model);
              results = { ...results, [data.model]: data.content };
              streaming = { ...streaming };
              delete streaming[data.model];
            } else if (data.choices) {
              const delta = data.choices[0]?.delta?.content || '';
              streaming[data.model] = (streaming[data.model] || '') + delta;
              streaming = { ...streaming };
            }
          } catch {}
        }
      }
      activeModels.clear();
    } catch {} finally {
      loading = false;
    }
  }
</script>

<div class="playground">
  <h1 class="gradient-text">提示词实验场</h1>
  <p class="sub">同一个 Prompt，多个模型横向评测对比</p>

  <PremiumCard>
    <div class="input-area">
      <textarea bind:value={prompt} placeholder="输入你的 Prompt..." rows="4"></textarea>
      <div class="model-select">
        {#each ['claude-sonnet-4-6', 'deepseek-v3', 'gpt-4o'] as m}
          <label class="model-check">
            <input type="checkbox" bind:group={selectedModels} value={m} />
            {m}
          </label>
        {/each}
      </div>
      <button on:click={runCompare} disabled={loading}>
        {loading ? '评测中...' : '开始对比'}
      </button>
    </div>
  </PremiumCard>

  {#if Object.keys(results).length > 0 || activeModels.size > 0}
    <div class="compare-grid">
      {#each selectedModels as model}
        {#if results[model] || streaming[model] !== undefined}
          <PremiumCard>
            <div class="result-panel">
              <h3>
                {model}
                {#if activeModels.has(model)}
                  <span class="streaming-badge">生成中...</span>
                {/if}
              </h3>
              <div class="content">{results[model] || streaming[model] || ''}</div>
            </div>
          </PremiumCard>
        {/if}
      {/each}
    </div>
  {/if}
</div>

<style>
  .playground { max-width: 1200px; margin: 0 auto; }
  .sub { color: var(--text-secondary); margin: 8px 0 24px; }
  .input-area { padding: 8px; }
  textarea {
    width: 100%; padding: 16px; box-sizing: border-box;
    background: rgba(0,0,0,0.2); border: 1px solid var(--glass-border);
    border-radius: 12px; color: var(--text-primary); font-size: 15px;
    resize: vertical; outline: none; font-family: inherit; line-height: 1.6;
  }
  .model-select { display: flex; gap: 16px; margin: 12px 0; }
  .model-check { font-size: 13px; color: var(--text-secondary); display: flex; align-items: center; gap: 4px; cursor: pointer; }
  .input-area button {
    padding: 10px 32px; background: var(--accent-purple);
    border: none; border-radius: 10px; color: white;
    font-weight: 600; cursor: pointer;
  }
  .input-area button:disabled { opacity: 0.5; cursor: not-allowed; }
  .compare-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 16px; margin-top: 20px; }
  .result-panel { padding: 8px; }
  .result-panel h3 { font-size: 14px; color: var(--accent-blue); margin-bottom: 10px; display: flex; align-items: center; gap: 8px; }
  .streaming-badge { font-size: 11px; color: var(--accent-gold); animation: pulse 1.5s infinite; }
  @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
  .result-panel .content { font-size: 13px; line-height: 1.7; color: var(--text-secondary); white-space: pre-wrap; }
</style>
