<script lang="ts">
  import { createEventDispatcher, onMount, onDestroy } from 'svelte';
  import FlowToolbar from './FlowToolbar.svelte';

  export let nodes: any[] = [];
  export let connections: any[] = [];
  export let nodeTypes: Record<string, any> = {};
  export let selectedNodeId: string | null = null;
  export let connectingFrom: string | null = null;

  const dispatch = createEventDispatcher();

  let svgEl: SVGSVGElement;
  let containerEl: HTMLDivElement;
  let containerWidth = 800;
  let containerHeight = 600;

  // Selected connection (index into connections array)
  let selectedConnIndex: number | null = null;
  $: selectedConnKey = selectedConnIndex !== null ? `${connections[selectedConnIndex]?.from}→${connections[selectedConnIndex]?.to}` : null;

  // Viewport
  let viewX = 0;
  let viewY = 0;
  let zoom = 1;
  const MIN_ZOOM = 0.2;
  const MAX_ZOOM = 2.5;

  // Pan state (middle-click or shift+left-click)
  let panning = false;
  let panStart = { x: 0, y: 0 };
  let viewStart = { x: 0, y: 0 };

  // Drag state — uses offset map instead of mutating nodes for smooth rendering
  let dragId: string | null = null;
  let dragOffsets: Record<string, { dx: number; dy: number }> = {};
  let dragStartMouse = { x: 0, y: 0 };
  let dragStartPos = { x: 0, y: 0 };

  $: viewTransform = `translate(${viewX}, ${viewY}) scale(${zoom})`;

  function nodeDisplayX(node: any): number {
    return node.x + (dragOffsets[node.id]?.dx || 0);
  }
  function nodeDisplayY(node: any): number {
    return node.y + (dragOffsets[node.id]?.dy || 0);
  }

  // Zoom toward cursor
  function handleWheel(e: WheelEvent) {
    e.preventDefault();
    const step = 0.1;
    const delta = e.deltaY > 0 ? -step : step;
    const newZoom = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, zoom + delta));
    const rect = containerEl.getBoundingClientRect();
    const mx = e.clientX - rect.left;
    const my = e.clientY - rect.top;
    const wx = (mx - viewX) / zoom;
    const wy = (my - viewY) / zoom;
    zoom = newZoom;
    viewX = mx - wx * zoom;
    viewY = my - wy * zoom;
  }

  function startPan(e: MouseEvent) {
    if (e.button !== 1 && !(e.button === 0 && e.shiftKey)) return;
    e.preventDefault();
    panning = true;
    panStart = { x: e.clientX, y: e.clientY };
    viewStart = { x: viewX, y: viewY };
  }

  function startDrag(id: string, event: MouseEvent) {
    event.stopPropagation();
    const node = nodes.find((n) => n.id === id);
    if (!node) return;
    dragId = id;
    dragStartMouse = { x: event.clientX, y: event.clientY };
    dragStartPos = { x: node.x, y: node.y };
    dragOffsets = { ...dragOffsets, [id]: { dx: 0, dy: 0 } };
  }

  function onMove(e: MouseEvent) {
    if (dragId) {
      const dx = (e.clientX - dragStartMouse.x) / zoom;
      const dy = (e.clientY - dragStartMouse.y) / zoom;
      dragOffsets[dragId] = { dx, dy };
      dragOffsets = dragOffsets; // trigger reactivity
      return;
    }
    if (panning) {
      viewX = viewStart.x + (e.clientX - panStart.x);
      viewY = viewStart.y + (e.clientY - panStart.y);
    }
  }

  function onUp() {
    if (dragId) {
      const offset = dragOffsets[dragId] || { dx: 0, dy: 0 };
      const node = nodes.find((n) => n.id === dragId);
      if (node) {
        const newX = node.x + offset.dx;
        const newY = node.y + offset.dy;
        dispatch('nodeMoved', { id: dragId, x: newX, y: newY });
      }
      dragOffsets = {};
      dragId = null;
    }
    panning = false;
  }

  function onCanvasClick(e: MouseEvent) {
    if ((e.target as HTMLElement).closest('.flow-node')) return;
    if ((e.target as HTMLElement).closest('.conn-hit-area')) return;
    selectedConnIndex = null;
    dispatch('canvasClick');
  }

  function selectConnection(index: number, e: MouseEvent) {
    e.stopPropagation();
    selectedConnIndex = index;
    dispatch('connectionClick', { from: connections[index].from, to: connections[index].to, index });
  }

  function deleteSelectedConnection() {
    if (selectedConnIndex === null) return;
    const conn = connections[selectedConnIndex];
    connections = connections.filter((_, i) => i !== selectedConnIndex);
    selectedConnIndex = null;
    dispatch('connectionDeleted', { from: conn.from, to: conn.to });
  }

  function onKeyDown(e: KeyboardEvent) {
    if (e.key === 'Delete' || e.key === 'Backspace') {
      if (selectedConnIndex !== null) {
        e.preventDefault();
        deleteSelectedConnection();
      }
    }
    if (e.key === 'Escape') {
      selectedConnIndex = null;
    }
  }

  // Toolbar actions
  function zoomIn() { zoom = Math.min(MAX_ZOOM, zoom + 0.2); }
  function zoomOut() { zoom = Math.max(MIN_ZOOM, zoom - 0.2); }

  function fitView() {
    if (nodes.length === 0) { zoom = 1; viewX = 0; viewY = 0; return; }
    const pad = 80;
    const xs = nodes.map((n) => nodeDisplayX(n));
    const ys = nodes.map((n) => nodeDisplayY(n));
    const minX = Math.min(...xs);
    const minY = Math.min(...ys);
    const maxX = Math.max(...xs.map((x, i) => x + 180));
    const maxY = Math.max(...ys.map((y, i) => y + 100));
    const cw = maxX - minX + pad * 2;
    const ch = maxY - minY + pad * 2;
    zoom = Math.min(containerWidth / cw, containerHeight / ch, 1.5);
    viewX = -(minX - pad) * zoom;
    viewY = -(minY - pad) * zoom;
  }

  function autoLayout() {
    const cols = Math.ceil(Math.sqrt(nodes.length));
    nodes.forEach((node, i) => {
      node.x = 60 + (i % cols) * 220;
      node.y = 60 + Math.floor(i / cols) * 140;
    });
    nodes = nodes;
    dragOffsets = {};
    fitView();
  }

  function bezierPath(from: any, to: any): string {
    const fx = nodeDisplayX(from);
    const fy = nodeDisplayY(from);
    const tx = nodeDisplayX(to);
    const ty = nodeDisplayY(to);
    const x1 = fx + 160;
    const y1 = fy + 50;
    const x2 = tx;
    const y2 = ty + 50;
    const dx = Math.abs(x2 - x1) * 0.5;
    const cx = Math.max(dx, 40);
    return `M ${x1} ${y1} C ${x1 + cx} ${y1}, ${x2 - cx} ${y2}, ${x2} ${y2}`;
  }

  function getNodeById(id: string) {
    return nodes.find((n) => n.id === id);
  }

  let resizeObs: ResizeObserver;
  onMount(() => {
    resizeObs = new ResizeObserver(([e]) => {
      containerWidth = e.contentRect.width;
      containerHeight = e.contentRect.height;
    });
    resizeObs.observe(containerEl);
  });
  onDestroy(() => resizeObs?.disconnect());
</script>

<svelte:window on:mouseup={onUp} on:mousemove={(e) => onMove(e)} />

<div bind:this={containerEl} class="flow-container" on:wheel={handleWheel}>
  <svg
    bind:this={svgEl}
    class="flow-svg"
    width="100%"
    height="100%"
    on:mousedown={startPan}
    on:click={onCanvasClick}
    on:keydown={onKeyDown}
    tabindex="0"
  >
    <defs>
      <pattern id="grid-dots" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
        <circle cx="1" cy="1" r="1" fill="rgba(255,255,255,0.06)" />
      </pattern>
      <pattern id="grid-major" x="0" y="0" width="100" height="100" patternUnits="userSpaceOnUse">
        <circle cx="1" cy="1" r="1.5" fill="rgba(255,255,255,0.1)" />
      </pattern>
      <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
        <polygon points="0 0, 8 3, 0 6" fill="#64b4ff" />
      </marker>
      <marker id="arrowhead-selected" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
        <polygon points="0 0, 8 3, 0 6" fill="#ffb464" />
      </marker>
      <filter id="conn-glow">
        <feGaussianBlur stdDeviation="2" result="blur" />
        <feMerge>
          <feMergeNode in="blur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
    </defs>

    <rect x="-5000" y="-5000" width="10000" height="10000" fill="url(#grid-dots)" />
    <rect x="-5000" y="-5000" width="10000" height="10000" fill="url(#grid-major)" />

    <g transform={viewTransform}>
      {#each connections as conn, i}
        {@const from = getNodeById(conn.from)}
        {@const to = getNodeById(conn.to)}
        {#if from && to}
          {@const d = bezierPath(from, to)}
          {@const selected = i === selectedConnIndex}
          <!-- Invisible hit area for easier clicking -->
          <path
            {d}
            stroke="transparent"
            stroke-width="14"
            fill="none"
            class="conn-hit-area"
            on:click={(e) => selectConnection(i, e)}
          />
          <!-- Visible connection line -->
          <path
            {d}
            stroke={selected ? '#ffb464' : '#64b4ff'}
            stroke-width={selected ? '3' : '2'}
            fill="none"
            marker-end={selected ? 'url(#arrowhead-selected)' : 'url(#arrowhead)'}
            class="connection-path"
            class:selected
            filter={selected ? 'url(#conn-glow)' : 'none'}
          />
        {/if}
      {/each}

      <!-- Connection delete button -->
      {#if selectedConnIndex !== null}
        {@const conn = connections[selectedConnIndex]}
        {@const from = getNodeById(conn.from)}
        {@const to = getNodeById(conn.to)}
        {#if from && to}
          {@const mx = (nodeDisplayX(from) + 160 + nodeDisplayX(to)) / 2}
          {@const my = (nodeDisplayY(from) + 50 + nodeDisplayY(to) + 50) / 2}
          <foreignObject x={mx - 12} y={my - 12} width="24" height="24" class="conn-delete-fo">
            <button
              class="conn-delete-btn"
              on:click|stopPropagation={deleteSelectedConnection}
              title="删除连线 (Delete)"
            >×</button>
          </foreignObject>
        {/if}
      {/if}

      {#each nodes as node (node.id)}
        {@const info = nodeTypes[node.type]}
        {@const nx = nodeDisplayX(node)}
        {@const ny = nodeDisplayY(node)}
        <foreignObject x={nx} y={ny} width="160" height="100" class="flow-node">
          <div
            class="agent-node glass"
            class:selected={node.id === selectedNodeId}
            class:connecting={node.id === connectingFrom}
            style="border-color: {info.color}"
            on:mousedown|stopPropagation={() => dispatch('nodeClick', { id: node.id })}
          >
            <div
              class="node-header"
              style="background: {info.color}20"
              on:mousedown|stopPropagation={(e) => startDrag(node.id, e)}
            >
              <span class="node-icon">{info.icon}</span>
              <span class="node-type">{node.label || node.type.toUpperCase()}</span>
              <button class="node-delete" on:click|stopPropagation={() => dispatch('deleteNode', { id: node.id })}>×</button>
            </div>
            <div class="node-body">
              {#if node.type === 'llm'}
                <div class="node-field">Model: {node.config.model}</div>
                <div class="node-field">Temp: {node.config.temperature}</div>
              {/if}
            </div>
            <div class="node-handles">
              <span class="handle handle-in" title="input">◀</span>
              <span class="handle handle-out" title="output"
                on:mousedown|stopPropagation={(e) => dispatch('connectStart', { id: node.id })}
              >▶</span>
            </div>
          </div>
        </foreignObject>
      {/each}
    </g>
  </svg>

  <div class="flow-toolbar glass">
    <FlowToolbar {zoom} on:zoomIn={zoomIn} on:zoomOut={zoomOut} on:fitView={fitView} on:autoLayout={autoLayout} />
  </div>

  {#if nodes.length === 0}
    <div class="canvas-empty">
      <p>从左侧面板点击组件添加到画布</p>
      <p class="hint">滚轮缩放 · Shift+拖拽平移 · 拖拽节点头部移动</p>
    </div>
  {/if}
</div>

<style>
  .flow-container {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background: rgba(0,0,0,0.2);
  }
  .flow-svg { display: block; cursor: default; }
  .flow-toolbar {
    position: absolute;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    padding: 6px 10px;
    border-radius: 12px;
    z-index: 10;
  }

  :global(.flow-node) { overflow: visible; }
  :global(.flow-node .agent-node) {
    width: 158px;
    border-radius: 10px;
    border: 2px solid var(--glass-border);
    cursor: pointer;
    user-select: none;
    padding: 0;
    background: var(--glass-bg);
    backdrop-filter: blur(12px);
    transition: box-shadow 0.15s;
    color: var(--text-primary);
  }
  :global(.flow-node .agent-node:hover) { z-index: 10; }
  :global(.flow-node .agent-node.selected) {
    box-shadow: 0 0 0 2px var(--accent-blue), 0 0 16px rgba(100,180,255,0.2);
  }
  :global(.flow-node .agent-node.connecting) {
    box-shadow: 0 0 0 2px var(--accent-green), 0 0 12px rgba(100,200,150,0.2);
  }
  :global(.flow-node .node-header) {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 10px;
    border-radius: 8px 8px 0 0;
    font-size: 11px;
    font-weight: 600;
    cursor: move;
  }
  :global(.flow-node .node-icon) { font-size: 14px; }
  :global(.flow-node .node-type) { flex: 1; text-transform: uppercase; font-size: 10px; }
  :global(.flow-node .node-delete) {
    background: none; border: none;
    color: var(--text-secondary); cursor: pointer;
    font-size: 14px; padding: 0; line-height: 1;
  }
  :global(.flow-node .node-delete:hover) { color: var(--accent-red); }
  :global(.flow-node .node-body) {
    padding: 8px 10px;
    font-size: 11px;
    color: var(--text-secondary);
  }
  :global(.flow-node .node-field) { margin-bottom: 2px; }
  :global(.flow-node .node-handles) {
    display: flex;
    justify-content: space-between;
    padding: 0 8px 8px;
  }
  :global(.flow-node .handle) {
    font-size: 10px;
    cursor: pointer;
    padding: 2px 4px;
    color: var(--text-secondary);
    border-radius: 4px;
  }
  :global(.flow-node .handle:hover) {
    color: var(--accent-blue);
    background: rgba(100,180,255,0.1);
  }

  .connection-path { transition: d 0.15s ease, stroke 0.15s ease; }
  .connection-path.selected { stroke-dasharray: none; }
  .conn-hit-area { cursor: pointer; }

  :global(.conn-delete-fo) {
    overflow: visible;
    pointer-events: all;
  }
  :global(.conn-delete-btn) {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 2px solid var(--accent-red);
    background: rgba(255,100,100,0.15);
    color: var(--accent-red);
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(8px);
    transition: all 0.15s;
    padding: 0;
    line-height: 1;
  }
  :global(.conn-delete-btn:hover) {
    background: var(--accent-red);
    color: white;
    transform: scale(1.15);
  }

  .canvas-empty {
    position: absolute;
    top: 50%; left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: var(--text-secondary);
    font-size: 14px;
    pointer-events: none;
  }
  .canvas-empty .hint {
    font-size: 12px;
    margin-top: 6px;
    opacity: 0.6;
  }
</style>
