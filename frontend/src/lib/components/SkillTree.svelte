<script lang="ts">
  import { onMount, tick } from 'svelte';
  import { spring } from 'svelte/motion';
  import { apiJson } from '$lib/api/client';

  interface SkillNode {
    id: string;
    name: string;
    category: string;
    status: number;
  }

  interface SkillEdge {
    from: string;
    to: string;
  }

  interface SkillData {
    nodes: SkillNode[];
    edges: SkillEdge[];
  }

  let skillData: SkillData | null = null;
  let loading = true;
  let error = '';

  // Spring-driven category label glow
  const hoveredCategory = spring<string | null>(null, { stiffness: 0.2, damping: 0.7 });

  const statusConfig: Record<number, { color: string; label: string; glow: string }> = {
    0: { color: '#555', label: '未解锁', glow: 'rgba(85,85,85,0)' },
    1: { color: '#64b4ff', label: '已解锁', glow: 'rgba(100,180,255,0.15)' },
    2: { color: '#ffb464', label: '学习中', glow: 'rgba(255,180,100,0.2)' },
    3: { color: '#64c896', label: '已掌握', glow: 'rgba(100,200,150,0.25)' },
  };

  const categories = ['基础', '提示词', '工具', 'Agent', '知识'];

  // Store node positions for connection lines
  let nodePositions: Map<string, DOMRect> = new Map();
  let treeEl: HTMLDivElement;

  onMount(async () => {
    try {
      skillData = await apiJson<SkillData>('/api/reports/skill-tree');
      await tick();
      updatePositions();
    } catch (e) {
      error = '加载技能树失败';
    } finally {
      loading = false;
    }
  });

  function updatePositions() {
    if (!treeEl) return;
    const newPositions = new Map<string, DOMRect>();
    const nodeEls = treeEl.querySelectorAll<HTMLElement>('[data-node-id]');
    nodeEls.forEach(el => {
      const id = el.dataset.nodeId;
      if (id) newPositions.set(id, el.getBoundingClientRect());
    });
    nodePositions = newPositions;
  }

  function getNodeById(id: string): SkillNode | undefined {
    return skillData?.nodes.find(n => n.id === id);
  }

  function getEdgePath(fromId: string, toId: string): string {
    const treeRect = treeEl?.getBoundingClientRect();
    if (!treeRect) return '';

    const fromEl = treeEl?.querySelector(`[data-node-id="${fromId}"]`);
    const toEl = treeEl?.querySelector(`[data-node-id="${toId}"]`);
    if (!fromEl || !toEl) return '';

    const fromRect = fromEl.getBoundingClientRect();
    const toRect = toEl.getBoundingClientRect();

    const x1 = fromRect.left - treeRect.left + fromRect.width / 2;
    const y1 = fromRect.top - treeRect.top + fromRect.height / 2;
    const x2 = toRect.left - treeRect.left + toRect.width / 2;
    const y2 = toRect.top - treeRect.top + toRect.height / 2;

    // Curved bezier path
    const dx = Math.abs(x2 - x1) * 0.5;
    return `M${x1},${y1} C${x1 + dx},${y1} ${x2 - dx},${y2} ${x2},${y2}`;
  }

  function getLineProgress(fromId: string, toId: string): number {
    const from = getNodeById(fromId);
    const to = getNodeById(toId);
    if (!from || !to) return 0;
    // Line is "active" if the source node is unlocked or beyond
    if (from.status >= 1 && to.status >= 1) return 1;
    if (from.status >= 1) return 0.4;
    return 0.15;
  }
</script>

<div class="skill-tree glass" bind:this={treeEl}>
  <h3>技能树</h3>

  {#if loading}
    <p class="status-msg">加载中...</p>
  {:else if error}
    <p class="status-msg">{error}</p>
  {:else if skillData}
    <div class="tree-container">
      <!-- SVG overlay for connection lines -->
      <svg class="connection-layer" viewBox="0 0 100 100" preserveAspectRatio="none">
        {#each skillData.edges as edge}
          {@const progress = getLineProgress(edge.from, edge.to)}
          <path
            d={getEdgePath(edge.from, edge.to)}
            fill="none"
            stroke="rgba(255,255,255,{0.08 + progress * 0.18})"
            stroke-width={1 + progress * 1.5}
            stroke-dasharray="{4 + progress * 8},{3}"
            class="connector"
            class:active={progress >= 1}
          />
        {/each}
      </svg>

      <!-- Category columns -->
      <div class="category-grid">
        {#each categories as cat}
          {@const catNodes = skillData.nodes.filter(n => n.category === cat)}
          <div
            class="category-col"
            on:mouseenter={() => hoveredCategory.set(cat)}
            on:mouseleave={() => hoveredCategory.set(null)}
          >
            <span
              class="category-label"
              class:highlighted={$hoveredCategory === cat}
            >
              {cat}
            </span>
            <div class="node-list">
              {#each catNodes as node, i}
                {@const cfg = statusConfig[node.status] ?? statusConfig[0]}
                <div
                  class="skill-node"
                  class:mastered={node.status === 3}
                  class:learning={node.status === 2}
                  data-node-id={node.id}
                  style="--node-color: {cfg.color}; --node-glow: {cfg.glow}; animation-delay: {i * 0.08}s;"
                >
                  <span class="node-ring" style:box-shadow="0 0 {node.status >= 2 ? 12 : 4}px {cfg.glow}"></span>
                  <span class="node-dot"></span>
                  <span class="node-name">{node.name}</span>
                  <span class="node-status">{cfg.label}</span>
                </div>
              {/each}
            </div>
          </div>
        {/each}
      </div>
    </div>
  {:else}
    <p class="status-msg">暂无技能数据，完成课程以解锁技能</p>
  {/if}
</div>

<style>
  .skill-tree {
    padding: 24px;
    margin: 16px 0;
    position: relative;
    overflow: hidden;
  }

  .skill-tree h3 {
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 600;
  }

  .status-msg {
    color: var(--text-secondary);
    font-size: 13px;
    text-align: center;
    padding: 24px;
  }

  /* ─── Tree layout ─── */
  .tree-container {
    position: relative;
    min-height: 200px;
  }

  .connection-layer {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 1;
  }

  .connector {
    transition: stroke 0.6s ease, stroke-width 0.6s ease, stroke-dasharray 0.6s ease;
  }
  .connector.active {
    stroke-dasharray: 2, 2;
    animation: connector-flow 3s linear infinite;
  }

  @keyframes connector-flow {
    to { stroke-dashoffset: -20; }
  }

  /* ─── Category grid ─── */
  .category-grid {
    display: flex;
    gap: 12px;
    justify-content: space-between;
    position: relative;
    z-index: 2;
  }

  .category-col {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  .category-label {
    font-size: 11px;
    color: var(--text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.05em;
    font-weight: 600;
    padding: 4px 12px;
    border-radius: 20px;
    transition: all 0.35s ease;
  }

  .category-label.highlighted {
    color: var(--accent-blue);
    background: rgba(100, 180, 255, 0.08);
    box-shadow: 0 0 12px rgba(100, 180, 255, 0.1);
  }

  .node-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;
  }

  /* ─── Skill node — HTML-based for spring/FLIP capability ─── */
  .skill-node {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 10px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid transparent;
    cursor: default;
    position: relative;
    transition: background 0.4s ease, border-color 0.4s ease, transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
    animation: node-appear 0.5s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  }

  .skill-node:hover {
    background: rgba(255, 255, 255, 0.06);
    border-color: rgba(255, 255, 255, 0.1);
    transform: scale(1.04);
  }

  .skill-node.mastered {
    background: rgba(100, 200, 150, 0.06);
    border-color: rgba(100, 200, 150, 0.15);
  }

  .skill-node.learning {
    animation: learning-pulse 2.5s ease-in-out infinite;
  }

  @keyframes node-appear {
    from {
      opacity: 0;
      transform: translateY(8px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes learning-pulse {
    0%, 100% { box-shadow: inset 0 0 0 rgba(255, 180, 100, 0); }
    50% { box-shadow: inset 0 0 12px var(--node-glow); }
  }

  .node-ring {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 2px solid var(--node-color);
    flex-shrink: 0;
    transition: box-shadow 0.6s ease, border-color 0.6s ease;
  }

  .node-dot {
    display: none;
  }

  .node-name {
    font-size: 12px;
    font-weight: 500;
    color: var(--text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
    min-width: 0;
  }

  .node-status {
    font-size: 10px;
    color: var(--node-color);
    font-weight: 600;
    flex-shrink: 0;
    opacity: 0.8;
  }
</style>
