<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { apiJson } from '$lib/api/client';
  import { chat } from '$lib/stores/chat';
  import GlassCard from '$lib/components/GlassCard.svelte';
  import PersonaAvatar from '$lib/components/PersonaAvatar.svelte';

  let course: any = null;
  let chapters: any[] = [];
  let activeLesson: any = null;

  $: slug = $page.params.slug;

  onMount(async () => {
    try {
      const data = await apiJson<any>(`/api/courses/${slug}`);
      course = data.course;
      chapters = data.chapters;
      if (chapters.length > 0 && chapters[0].lessons?.length > 0) {
        activeLesson = chapters[0].lessons[0];
      }
    } catch {}
  });

  function selectLesson(lesson: any) {
    activeLesson = lesson;
  }

  function askTeacher() {
    chat.open('lecturer');
  }

  function openInLab() {
    if (activeLesson?.labRef) {
      window.location.href = `/lab/sandbox?template=${activeLesson.labRef}&courseId=${course?.id}`;
    }
  }

  function renderMarkdown(contentJson: string): string {
    try {
      const obj = JSON.parse(contentJson);
      const body = obj.body || '';
      return body
        .replace(/^### (.*$)/gim, '<h3>$1</h3>')
        .replace(/^## (.*$)/gim, '<h2>$1</h2>')
        .replace(/^# (.*$)/gim, '<h1>$1</h1>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
        .replace(/\n\n/g, '</p><p>')
        .replace(/<p>$/, '');
    } catch {
      return contentJson.replace(/\n/g, '<br>');
    }
  }
</script>

{#if course}
  <div class="course-page">
    <div class="course-sidebar">
      <div class="course-header">
        <h2>{course.title}</h2>
        <span class="level-badge">{course.level}</span>
      </div>
      {#each chapters as ch}
        <div class="chapter">
          <h3>{ch.chapter.title}</h3>
          {#each ch.lessons as lesson}
            <button
              class="lesson-link"
              class:active={activeLesson?.id === lesson.id}
              on:click={() => selectLesson(lesson)}
            >
              {lesson.contentType === 'code' ? '💻 ' : '📖 '}
              {lesson.title}
            </button>
          {/each}
        </div>
      {/each}
    </div>

    <div class="course-content">
      {#if activeLesson}
        <GlassCard>
          <div class="lesson-header">
            <h1>{activeLesson.title}</h1>
            <div class="lesson-actions">
              <button class="action-btn" on:click={askTeacher}>
                <PersonaAvatar personaId="lecturer" name="问老师" />
              </button>
              {#if activeLesson.labRef}
                <button class="action-btn lab-btn" on:click={openInLab}>
                  🔬 在实验场打开
                </button>
              {/if}
            </div>
          </div>
          <div class="lesson-body">
            {#if activeLesson.contentType === 'text' || activeLesson.contentType === 'code'}
              {@html renderMarkdown(activeLesson.content)}
            {:else if activeLesson.contentType === 'quiz'}
              <p class="placeholder">测验内容（需后端题库对接）</p>
            {:else}
              <p class="placeholder">视频内容（需视频服务对接）</p>
            {/if}
          </div>
        </GlassCard>
      {/if}
    </div>
  </div>
{/if}

<style>
  .course-page {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 24px;
    align-items: start;
  }
  .course-sidebar {
    position: sticky;
    top: 80px;
  }
  .course-header {
    padding: 16px;
    margin-bottom: 16px;
  }
  .course-header h2 { font-size: 16px; }
  .level-badge {
    display: inline-block;
    padding: 2px 10px;
    background: rgba(100, 180, 255, 0.15);
    color: var(--accent-blue);
    border-radius: 12px;
    font-size: 11px;
    font-weight: 600;
    margin-top: 6px;
  }
  .chapter { margin-bottom: 16px; }
  .chapter h3 {
    font-size: 12px;
    color: var(--text-secondary);
    text-transform: uppercase;
    letter-spacing: 1px;
    margin-bottom: 6px;
    padding: 0 8px;
  }
  .lesson-link {
    display: block;
    width: 100%;
    text-align: left;
    padding: 8px 12px;
    background: transparent;
    border: none;
    color: var(--text-secondary);
    font-size: 13px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.15s;
  }
  .lesson-link:hover { background: rgba(255,255,255,0.04); color: var(--text-primary); }
  .lesson-link.active { background: rgba(100, 180, 255, 0.1); color: var(--accent-blue); }
  .lesson-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
  }
  .lesson-header h1 { font-size: 22px; }
  .lesson-actions { display: flex; gap: 8px; align-items: center; }
  .action-btn {
    padding: 6px 12px;
    background: rgba(255,255,255,0.04);
    border: 1px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-primary);
    font-size: 12px;
    cursor: pointer;
  }
  .lab-btn { background: rgba(100, 200, 150, 0.1); border-color: var(--accent-green); }
  .lesson-body { line-height: 1.8; font-size: 15px; }
  .lesson-body :global(h2) { margin: 20px 0 10px; font-size: 20px; }
  .lesson-body :global(h3) { margin: 16px 0 8px; font-size: 17px; }
  .lesson-body :global(p) { margin: 8px 0; color: var(--text-secondary); }
  .lesson-body :global(code) {
    background: rgba(255,255,255,0.06);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
  }
  .lesson-body :global(pre) {
    background: rgba(0,0,0,0.3);
    padding: 16px;
    border-radius: 10px;
    overflow-x: auto;
    margin: 12px 0;
  }
  .placeholder { color: var(--text-secondary); font-style: italic; }
</style>
