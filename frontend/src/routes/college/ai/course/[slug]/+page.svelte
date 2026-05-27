<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { api, apiJson } from '$lib/api/client';
  import { chat } from '$lib/stores/chat';
  import PremiumCard from '$lib/components/PremiumCard.svelte';
  import PersonaAvatar from '$lib/components/PersonaAvatar.svelte';

  let course: any = null;
  let chapters: any[] = [];
  let activeLesson: any = null;
  let courseProgress: any = null;
  let completedLessonIds: Set<number> = new Set();
  let progressMsg = '';
  let markingComplete = false;

  $: slug = $page.params.slug;

  onMount(async () => {
    try {
      const data = await apiJson<any>(`/api/courses/${slug}`);
      course = data.course;
      chapters = data.chapters;
      if (chapters.length > 0 && chapters[0].lessons?.length > 0) {
        activeLesson = chapters[0].lessons[0];
      }
      loadProgress();
    } catch {}
  });

  async function loadProgress() {
    try {
      courseProgress = await apiJson<any>(`/api/progress/course/${course.id}`);
      if (courseProgress?.completedLessonIds) {
        completedLessonIds = new Set(courseProgress.completedLessonIds);
      }
    } catch {}
  }

  function selectLesson(lesson: any) {
    activeLesson = lesson;
    // Call start endpoint to track progress
    try {
      api(`/api/progress/lesson/${lesson.id}/start`, { method: 'POST' });
    } catch {}
  }

  async function markComplete() {
    if (!activeLesson || markingComplete) return;
    markingComplete = true;
    progressMsg = '正在记录进度...';
    try {
      await api(`/api/progress/lesson/${activeLesson.id}/complete`, {
        method: 'POST',
        body: { score: 100 },
      });
      completedLessonIds.add(activeLesson.id);
      progressMsg = '已完成本课时！';
      loadProgress();
    } catch (e: any) {
      // If API fails, still mark locally
      completedLessonIds.add(activeLesson.id);
      progressMsg = '已标记为完成（离线模式）';
    } finally {
      markingComplete = false;
    }
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
        {#if courseProgress}
          <div class="progress-bar">
            <div class="progress-fill" style="width: {courseProgress.totalLessons > 0 ? courseProgress.completedLessons / courseProgress.totalLessons * 100 : 0}%"></div>
          </div>
          <span class="progress-text">{courseProgress.completedLessons}/{courseProgress.totalLessons} 课时</span>
        {/if}
      </div>
      {#each chapters as ch}
        <div class="chapter">
          <h3>{ch.chapter.title}</h3>
          {#each ch.lessons as lesson}
            <button
              class="lesson-link"
              class:active={activeLesson?.id === lesson.id}
              class:completed={completedLessonIds.has(lesson.id)}
              on:click={() => selectLesson(lesson)}
            >
              {lesson.contentType === 'code' ? '💻 ' : '📖 '}
              {lesson.title}
              {#if completedLessonIds.has(lesson.id)}
                <span class="check-mark">✓</span>
              {/if}
            </button>
          {/each}
        </div>
      {/each}
    </div>

    <div class="course-content">
      {#if activeLesson}
        <PremiumCard>
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
          <div class="lesson-footer">
            {#if progressMsg}
              <span class="progress-msg">{progressMsg}</span>
            {/if}
            {#if !completedLessonIds.has(activeLesson.id)}
              <button
                class="complete-btn"
                on:click={markComplete}
                disabled={markingComplete}
              >
                {markingComplete ? '提交中...' : '✓ 标记完成'}
              </button>
            {:else}
              <span class="completed-badge">已完成</span>
            {/if}
          </div>
        </PremiumCard>
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
  .progress-bar { height: 4px; background: rgba(255,255,255,0.08); border-radius: 2px; margin: 8px 0; overflow: hidden; }
  .progress-fill { height: 100%; background: var(--accent-blue); border-radius: 2px; transition: width 0.3s; }
  .progress-text { font-size: 11px; color: var(--text-secondary); }

  .lesson-link.completed { color: var(--accent-green); }
  .check-mark { margin-left: 6px; font-size: 12px; color: var(--accent-green); }

  .lesson-footer {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid rgba(255,255,255,0.08);
  }
  .progress-msg { font-size: 13px; color: var(--text-secondary); }
  .complete-btn {
    padding: 8px 20px;
    background: linear-gradient(135deg, var(--accent-green), #3a8);
    border: none;
    border-radius: 8px;
    color: white;
    font-weight: 600;
    cursor: pointer;
    font-size: 13px;
    transition: all 0.2s ease;
  }
  .complete-btn:hover { opacity: 0.9; transform: translateY(-1px); }
  .complete-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
  .completed-badge {
    padding: 6px 16px;
    background: rgba(100, 200, 150, 0.15);
    border: 1px solid var(--accent-green);
    border-radius: 8px;
    color: var(--accent-green);
    font-size: 13px;
    font-weight: 600;
  }
</style>
