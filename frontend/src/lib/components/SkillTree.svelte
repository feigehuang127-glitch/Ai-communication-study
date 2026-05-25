<script lang="ts">
  import { onMount } from 'svelte';
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

  const statusColors: Record<number, string> = {
    0: '#555',
    1: '#64b4ff',
    2: '#ffb464',
    3: '#64c896',
  };
  const statusLabels: Record<number, string> = {
    0: '未解锁',
    1: '已解锁',
    2: '学习中',
    3: '已掌握',
  };

  const categories = ['基础', '提示词', '工具', 'Agent', '知识'];

  onMount(async () => {
    try {
      skillData = await apiJson<SkillData>('/api/reports/skill-tree');
    } catch (e) {
      error = '加载技能树失败';
    } finally {
      loading = false;
    }
  });

  function getCategoryX(category: string): number {
    const idx = categories.indexOf(category);
    return 120 + idx * 180;
  }

  // Group nodes by category for even vertical spacing
  function getNodePosition(node: SkillNode): { x: number; y: number } {
    const x = getCategoryX(node.category);
    const nodesInCategory = skillData!.nodes.filter(n => n.category === node.category);
    const indexInCategory = nodesInCategory.indexOf(node);
    const totalInCategory = nodesInCategory.length;
    const startY = 200 - ((totalInCategory - 1) * 60);
    const y = startY + indexInCategory * 120;
    return { x, y };
  }

  function getNodeById(id: string): SkillNode | undefined {
    return skillData?.nodes.find(n => n.id === id);
  }
</script>

<div class="skill-tree glass">
  <h3>技能树</h3>
  {#if loading}
    <p class="loading">加载中...</p>
  {:else if error}
    <p class="loading">{error}</p>
  {:else if skillData}
    <svg viewBox="0 0 960 460" class="skill-svg">
      <!-- Edges -->
      {#each skillData.edges as edge}
        {@const fromNode = getNodeById(edge.from)}
        {@const toNode = getNodeById(edge.to)}
        {#if fromNode && toNode}
          {@const fp = getNodePosition(fromNode)}
          {@const tp = getNodePosition(toNode)}
          <line
            x1={fp.x} y1={fp.y}
            x2={tp.x} y2={tp.y}
            stroke="#444" stroke-width="2" stroke-dasharray="5,4"
          />
        {/if}
      {/each}

      <!-- Nodes -->
      {#each skillData.nodes as node}
        {@const pos = getNodePosition(node)}
        {@const color = statusColors[node.status] || '#555'}
        {@const label = statusLabels[node.status] || '未知'}
        <!-- Glow ring for mastered skills -->
        {#if node.status === 3}
          <circle cx={pos.x} cy={pos.y} r={32} fill="none" stroke="{color}40" stroke-width="2" />
        {/if}
        <circle cx={pos.x} cy={pos.y} r={26} fill="none" stroke={color} stroke-width="3" />
        <circle cx={pos.x} cy={pos.y} r={22} fill="{color}18" />
        <!-- Status dot -->
        <circle cx={pos.x} cy={pos.y} r={4} fill={color} />
        <!-- Name label above -->
        <text x={pos.x} y={pos.y - 34} text-anchor="middle" fill="var(--text-primary)" font-size="12" font-weight="500">{node.name}</text>
        <!-- Status label inside circle -->
        <text x={pos.x} y={pos.y + 2} text-anchor="middle" fill={color} font-size="10" font-weight="600">{(label)}</text>
      {/each}

      <!-- Category labels at bottom -->
      {#each categories as cat}
        <text
          x={getCategoryX(cat)}
          y={410}
          text-anchor="middle"
          fill="var(--text-secondary)"
          font-size="11"
        >{cat}</text>
      {/each}
    </svg>
  {:else}
    <p class="empty">暂无技能数据，完成课程以解锁技能</p>
  {/if}
</div>

<style>
  .skill-tree {
    padding: 20px;
    margin: 16px 0;
  }
  .skill-tree h3 {
    margin-bottom: 12px;
    font-size: 16px;
  }
  .skill-svg {
    width: 100%;
    height: auto;
  }
  .loading,
  .empty {
    color: var(--text-secondary);
    font-size: 13px;
    text-align: center;
    padding: 20px;
  }
</style>
