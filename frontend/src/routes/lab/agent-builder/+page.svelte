<script lang="ts">
  import { onMount } from 'svelte';
  import FlowCanvas from '$lib/components/flow/FlowCanvas.svelte';
  import { apiJson } from '$lib/api/client';

  interface AgentNode {
    id: string;
    type: 'llm' | 'tool' | 'input' | 'output' | 'condition';
    x: number;
    y: number;
    label: string;
    config: Record<string, any>;
  }

  interface Connection {
    from: string;
    to: string;
  }

  let nodes: AgentNode[] = [];
  let connections: Connection[] = [];
  let selectedNodeId: string | null = null;
  let connectingFrom: string | null = null;
  let agentName = '未命名 Agent';
  let savedProjects: any[] = [];

  const nodeTypes: Record<string, { icon: string; color: string; defaults: any }> = {
    llm: { icon: '🤖', color: '#64b4ff', defaults: { model: 'claude-sonnet-4-6', systemPrompt: '', temperature: 0.7 } },
    tool: { icon: '🔧', color: '#64c896', defaults: { toolName: '', endpoint: '', params: '{}' } },
    input: { icon: '📥', color: '#ffb464', defaults: { inputName: '', inputType: 'text' } },
    output: { icon: '📤', color: '#ff6464', defaults: { outputName: '', format: 'text' } },
    condition: { icon: '🔀', color: '#c896ff', defaults: { expression: '' } },
  };

  $: selectedNode = nodes.find(n => n.id === selectedNodeId);

  function addNode(type: string) {
    const id = 'node_' + Date.now();
    const info = nodeTypes[type];
    nodes = [...nodes, {
      id,
      type: type as AgentNode['type'],
      x: 100 + Math.random() * 300,
      y: 80 + Math.random() * 200,
      label: info.defaults.toolName || info.defaults.inputName || info.defaults.outputName || type.toUpperCase(),
      config: { ...info.defaults },
    }];
    selectedNodeId = id;
  }

  function handleNodeClick(e: CustomEvent<{ id: string }>) {
    const { id } = e.detail;
    if (connectingFrom && connectingFrom !== id) {
      connections = [...connections, { from: connectingFrom, to: id }];
      connectingFrom = null;
    } else {
      selectedNodeId = id;
    }
  }

  function handleConnectStart(e: CustomEvent<{ id: string }>) {
    connectingFrom = e.detail.id;
  }

  function handleCanvasClick() {
    selectedNodeId = null;
    connectingFrom = null;
  }

  function handleDeleteNode(e: CustomEvent<{ id: string }>) {
    const id = e.detail.id;
    nodes = nodes.filter(n => n.id !== id);
    connections = connections.filter(c => c.from !== id && c.to !== id);
    if (selectedNodeId === id) selectedNodeId = null;
  }

  function handleNodeMoved(e: CustomEvent<{ id: string; x: number; y: number }>) {
    const { id, x, y } = e.detail;
    nodes = nodes.map(n => n.id === id ? { ...n, x, y } : n);
  }

  function handleConnectionDeleted(e: CustomEvent<{ from: string; to: string }>) {
    const { from, to } = e.detail;
    connections = connections.filter(c => !(c.from === from && c.to === to));
  }

  function updateConfig(key: string, value: any) {
    if (!selectedNodeId) return;
    nodes = nodes.map(n => n.id === selectedNodeId ? {
      ...n,
      config: { ...n.config, [key]: value },
      label: key === 'toolName' || key === 'inputName' || key === 'outputName' ? value : n.label,
    } : n);
  }

  function onKeyDown(e: KeyboardEvent) {
    if (e.key === 'Delete' && selectedNodeId) {
      nodes = nodes.filter(n => n.id !== selectedNodeId);
      connections = connections.filter(c => c.from !== selectedNodeId && c.to !== selectedNodeId);
      selectedNodeId = null;
    }
    if (e.key === 'Escape') {
      connectingFrom = null;
      selectedNodeId = null;
    }
  }

  async function saveAgent() {
    const project = { name: agentName, type: 'agent', nodes, connections };
    const json = JSON.stringify(project);
    try {
      await apiJson('/api/lab/projects', { method: 'POST', body: { type: 'agent', title: agentName, snapshot: json } });
      savedProjects = await apiJson<any[]>('/api/lab/projects');
    } catch {
      localStorage.setItem('agent_builder_draft', json);
    }
  }

  async function loadProjects() {
    try {
      savedProjects = await apiJson<any[]>('/api/lab/projects');
    } catch {
      const draft = localStorage.getItem('agent_builder_draft');
      if (draft) savedProjects = [{ title: '本地草稿', snapshot: draft }];
    }
  }

  function loadProject(project: any) {
    try {
      const data = JSON.parse(project.snapshot);
      nodes = data.nodes || [];
      connections = data.connections || [];
      agentName = data.name || project.title;
      selectedNodeId = null;
    } catch {}
  }

  function exportJSON() {
    const blob = new Blob([JSON.stringify({ name: agentName, nodes, connections }, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `${agentName}.json`; a.click();
    URL.revokeObjectURL(url);
  }

  function clearCanvas() {
    nodes = []; connections = []; selectedNodeId = null;
  }

  onMount(() => { loadProjects(); });
</script>

<svelte:window on:keydown={onKeyDown} />

<div class="agent-builder">
  <!-- Left: Palette -->
  <div class="left-panel glass">
    <h3>组件面板</h3>
    {#each Object.entries(nodeTypes) as [type, info]}
      <button class="palette-item" style="border-left: 3px solid {info.color}" on:click={() => addNode(type)}>
        <span>{info.icon}</span> {type.toUpperCase()} Node
      </button>
    {/each}
    <div class="palette-actions">
      <button class="btn-save" on:click={saveAgent}>💾 保存</button>
      <button class="btn-export" on:click={exportJSON}>📋 导出</button>
      <button class="btn-clear" on:click={clearCanvas}>🗑️ 清空</button>
    </div>
    {#if savedProjects.length > 0}
      <div class="saved-list">
        <h4>已保存</h4>
        {#each savedProjects as proj}
          <button class="saved-item" on:click={() => loadProject(proj)}>{proj.title}</button>
        {/each}
      </div>
    {/if}
  </div>

  <!-- Center: Flow Canvas -->
  <FlowCanvas
    {nodes}
    {connections}
    {nodeTypes}
    {selectedNodeId}
    {connectingFrom}
    on:nodeClick={handleNodeClick}
    on:connectStart={handleConnectStart}
    on:canvasClick={handleCanvasClick}
    on:deleteNode={handleDeleteNode}
    on:nodeMoved={handleNodeMoved}
    on:connectionDeleted={handleConnectionDeleted}
  />

  <!-- Right: Properties -->
  <div class="right-panel glass">
    {#if selectedNode}
      <h3>属性编辑 — {selectedNode.type.toUpperCase()}</h3>
      {#if selectedNode.type === 'llm'}
        <label for="prop-model">Model</label>
        <select id="prop-model" value={selectedNode.config.model} on:change={(e) => updateConfig('model', e.target.value)}>
          <option>claude-sonnet-4-6</option><option>deepseek-v3</option><option>gpt-4o</option>
        </select>
        <label for="prop-prompt">System Prompt</label>
        <textarea id="prop-prompt" rows="4" value={selectedNode.config.systemPrompt} on:input={(e) => updateConfig('systemPrompt', e.target.value)} placeholder="系统提示词..."></textarea>
        <label for="prop-temp">Temperature: {selectedNode.config.temperature}</label>
        <input id="prop-temp" type="range" min="0" max="2" step="0.1" value={selectedNode.config.temperature} on:input={(e) => updateConfig('temperature', parseFloat(e.target.value))} />
      {:else if selectedNode.type === 'tool'}
        <label for="prop-toolname">工具名称</label>
        <input id="prop-toolname" value={selectedNode.config.toolName} on:input={(e) => updateConfig('toolName', e.target.value)} placeholder="如: web_search" />
        <label for="prop-endpoint">Endpoint</label>
        <input id="prop-endpoint" value={selectedNode.config.endpoint} on:input={(e) => updateConfig('endpoint', e.target.value)} placeholder="https://..." />
        <label for="prop-params">参数 (JSON)</label>
        <textarea id="prop-params" rows="3" value={selectedNode.config.params} on:input={(e) => updateConfig('params', e.target.value)}></textarea>
      {:else if selectedNode.type === 'input'}
        <label for="prop-inputname">输入名称</label>
        <input id="prop-inputname" value={selectedNode.config.inputName} on:input={(e) => updateConfig('inputName', e.target.value)} placeholder="如: user_question" />
        <label for="prop-inputtype">类型</label>
        <select id="prop-inputtype" value={selectedNode.config.inputType} on:change={(e) => updateConfig('inputType', e.target.value)}>
          <option>text</option><option>json</option><option>image</option>
        </select>
      {:else if selectedNode.type === 'output'}
        <label for="prop-outputname">输出名称</label>
        <input id="prop-outputname" value={selectedNode.config.outputName} on:input={(e) => updateConfig('outputName', e.target.value)} placeholder="如: final_answer" />
        <label for="prop-outputfmt">格式</label>
        <select id="prop-outputfmt" value={selectedNode.config.format} on:change={(e) => updateConfig('format', e.target.value)}>
          <option>text</option><option>json</option><option>markdown</option>
        </select>
      {:else if selectedNode.type === 'condition'}
        <label for="prop-expr">条件表达式</label>
        <textarea id="prop-expr" rows="3" value={selectedNode.config.expression} on:input={(e) => updateConfig('expression', e.target.value)} placeholder="如: input.length > 100"></textarea>
      {/if}
      <button class="btn-delete-node" on:click={() => { nodes = nodes.filter(n => n.id !== selectedNodeId); connections = connections.filter(c => c.from !== selectedNodeId && c.to !== selectedNodeId); selectedNodeId = null; }}>🗑️ 删除节点</button>
    {:else}
      <div class="no-selection">
        <p>选择一个节点查看属性</p>
        {#if connectingFrom}
          <p class="hint">点击目标节点完成连接 (ESC 取消)</p>
        {/if}
      </div>
    {/if}
  </div>
</div>

<style>
  .agent-builder {
    display: grid;
    grid-template-columns: 240px 1fr 280px;
    gap: 0;
    height: calc(100vh - 140px);
    margin: -20px -24px;
    overflow: hidden;
  }

  .left-panel {
    padding: 16px;
    overflow-y: auto;
    border-right: 1px solid var(--glass-border);
    border-radius: 0;
  }
  .left-panel h3 { font-size: 14px; margin-bottom: 12px; }
  .palette-item {
    display: flex; align-items: center; gap: 8px;
    width: 100%; padding: 10px 12px;
    background: rgba(255,255,255,0.03); border: none;
    color: var(--text-primary); font-size: 13px; cursor: pointer;
    border-radius: 8px; margin-bottom: 6px; text-align: left;
  }
  .palette-item:hover { background: rgba(255,255,255,0.06); }
  .palette-actions { margin-top: 16px; display: flex; flex-direction: column; gap: 6px; }
  .btn-save, .btn-export, .btn-clear {
    padding: 8px; border: 1px solid var(--glass-border);
    border-radius: 8px; background: rgba(255,255,255,0.04);
    color: var(--text-primary); font-size: 12px; cursor: pointer;
  }
  .btn-save { background: var(--accent-blue); border-color: var(--accent-blue); }
  .saved-list { margin-top: 16px; }
  .saved-list h4 { font-size: 12px; color: var(--text-secondary); margin-bottom: 6px; }
  .saved-item {
    width: 100%; padding: 6px 10px; background: none; border: none;
    color: var(--text-secondary); cursor: pointer; text-align: left; font-size: 12px;
    border-radius: 4px;
  }
  .saved-item:hover { background: rgba(255,255,255,0.04); color: var(--text-primary); }

  .right-panel {
    padding: 16px;
    overflow-y: auto;
    border-left: 1px solid var(--glass-border);
    border-radius: 0;
  }
  .right-panel h3 { font-size: 13px; margin-bottom: 12px; color: var(--accent-blue); }
  .right-panel label { display: block; font-size: 12px; color: var(--text-secondary); margin: 10px 0 4px; }
  .right-panel input, .right-panel select, .right-panel textarea {
    width: 100%; padding: 8px 10px;
    background: rgba(255,255,255,0.04); border: 1px solid var(--glass-border);
    border-radius: 6px; color: var(--text-primary); font-size: 12px;
    outline: none; font-family: inherit; box-sizing: border-box;
  }
  .right-panel input[type="range"] { padding: 0; border: none; }
  .btn-delete-node {
    width: 100%; margin-top: 16px; padding: 8px;
    background: rgba(255,100,100,0.1); border: 1px solid var(--accent-red);
    border-radius: 8px; color: var(--accent-red); cursor: pointer; font-size: 12px;
  }
  .no-selection { text-align: center; padding: 40px 0; color: var(--text-secondary); font-size: 13px; }
  .hint { color: var(--accent-green); font-size: 12px; margin-top: 8px; }
</style>
