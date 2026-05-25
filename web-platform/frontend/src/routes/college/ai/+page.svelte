<script lang="ts">
  import { onMount } from 'svelte';
  import { apiJson } from '$lib/api/client';
  import GlassCard from '$lib/components/GlassCard.svelte';

  let courses: any[] = [];

  onMount(async () => {
    try {
      const college = await apiJson<any>('/api/courses/colleges/ai');
      courses = await apiJson<any[]>(`/api/courses?collegeId=${college.id}`);
    } catch {}
  });
</script>

<div class="ai-college">
  <section class="hero">
    <h1 class="gradient-text">AI 学院</h1>
    <p>从提示词工程到 Agent 开发，构建完整的 AI 开发能力</p>
  </section>

  <section class="courses">
    <h2>课程体系</h2>
    <div class="course-grid">
      {#each courses as course}
        <GlassCard href={`/college/ai/course/${course.slug}`}>
          <div class="course-card">
            <span class="level-badge">L{course.level?.replace('L', '')}</span>
            <h3>{course.title}</h3>
            <p>{course.description}</p>
          </div>
        </GlassCard>
      {/each}
    </div>
  </section>
</div>

<style>
  .ai-college { max-width: 900px; margin: 0 auto; }
  .hero { text-align: center; padding: 40px 0 20px; }
  .hero h1 { font-size: 36px; margin-bottom: 8px; }
  .hero p { color: var(--text-secondary); font-size: 16px; }
  section h2 { font-size: 18px; margin: 32px 0 16px; }
  .course-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
  .course-card { padding: 4px 0; }
  .level-badge {
    display: inline-block;
    padding: 2px 10px;
    background: rgba(100, 180, 255, 0.15);
    color: var(--accent-blue);
    border-radius: 12px;
    font-size: 11px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  .course-card h3 { font-size: 16px; margin-bottom: 4px; }
  .course-card p { color: var(--text-secondary); font-size: 13px; }
</style>
