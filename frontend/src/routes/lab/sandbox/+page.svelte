<script lang="ts">
  import { apiJson } from '$lib/api/client';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let code = `print("Hello, AI Academy!")`;
  let output = '';
  let language = 'python';
  let running = false;
  let containerId = '';

  async function createSandbox() {
    try {
      const result = await apiJson<any>('/api/ai/sandbox/create', {
        method: 'POST',
        body: { userId: 1, image: 'python:3.12-slim' },
      });
      containerId = result.container_id;
    } catch {}
  }

  async function runCode() {
    running = true;
    output = '';
    try {
      if (!containerId) await createSandbox();
      const result = await apiJson<any>('/api/ai/sandbox/execute', {
        method: 'POST',
        body: { container_id: containerId, code, language },
      });
      output = result.stdout || result.stderr || result.error || '(无输出)';
    } catch (e) {
      output = '执行失败：沙箱服务未连接';
    } finally {
      running = false;
    }
  }
</script>

<div class="sandbox">
  <h1 class="gradient-text">代码沙箱</h1>
  <p class="sub">Docker 隔离环境，支持 Python 和 JavaScript</p>

  <div class="sandbox-layout">
    <GlassCard>
      <div class="editor-header">
        <select bind:value={language}>
          <option value="python">Python</option>
          <option value="javascript">JavaScript</option>
        </select>
        <button class="run-btn" on:click={runCode} disabled={running}>
          {running ? '运行中...' : '▶ 运行'}
        </button>
      </div>
      <textarea
        bind:value={code}
        class="code-editor"
        spellcheck="false"
        placeholder="在此编写代码..."
      ></textarea>
    </GlassCard>

    <GlassCard>
      <div class="output-header">输出</div>
      <pre class="output-area">{output || '点击「运行」查看结果'}</pre>
    </GlassCard>
  </div>
</div>

<style>
  .sandbox { max-width: 1000px; margin: 0 auto; }
  .sub { color: var(--text-secondary); margin: 8px 0 24px; }
  .sandbox-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .editor-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
  }
  .editor-header select {
    padding: 6px 12px;
    background: rgba(255,255,255,0.05);
    border: 1px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-primary);
    font-size: 13px;
    outline: none;
  }
  .run-btn {
    padding: 6px 18px;
    background: var(--accent-green);
    border: none;
    border-radius: 8px;
    color: white;
    font-weight: 600;
    cursor: pointer;
    font-size: 13px;
  }
  .run-btn:disabled { opacity: 0.5; cursor: not-allowed; }
  .code-editor {
    width: 100%;
    height: 300px;
    background: rgba(0,0,0,0.3);
    border: 1px solid var(--glass-border);
    border-radius: 10px;
    padding: 16px;
    color: var(--text-primary);
    font-family: 'Fira Code', 'Cascadia Code', monospace;
    font-size: 13px;
    resize: none;
    outline: none;
    line-height: 1.6;
  }
  .output-header { margin-bottom: 8px; font-size: 14px; font-weight: 600; }
  .output-area {
    min-height: 300px;
    background: rgba(0,0,0,0.3);
    border-radius: 10px;
    padding: 16px;
    font-family: monospace;
    font-size: 13px;
    color: var(--text-secondary);
    white-space: pre-wrap;
    word-break: break-all;
  }
</style>
