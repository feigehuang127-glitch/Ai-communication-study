<script lang="ts">
  import { onMount } from 'svelte';
  import { apiJson } from '$lib/api/client';
  import GlassCard from '$lib/components/GlassCard.svelte';
  import SkillTree from '$lib/components/SkillTree.svelte';

  interface LearningReport {
    id: number;
    userId: number;
    courseId: number;
    reportType: string;
    content: string; // JSON string
    generatedAt: string;
  }

  interface ReportContent {
    generatedAt: string;
    reportType: string;
    courseProgress: any;
    skills: { total: number; unlocked: number; mastered: number };
    wrongQuestionsPending: number;
    stats: {
      totalScore: number;
      totalTimeMinutes: number;
      completedLessons: number;
      totalLessons: number;
    };
    streak: number;
    recommendations: string[];
  }

  let reports: LearningReport[] = [];
  let loading = true;
  let expandedId: number | null = null;
  let generating = false;
  let courseIdInput = 1;
  let error = '';

  let parsedContents: Record<number, ReportContent> = {};

  onMount(async () => {
    await loadReports();
  });

  async function loadReports() {
    loading = true;
    error = '';
    try {
      reports = await apiJson<LearningReport[]>('/api/reports');
      // Parse content JSON for each report
      for (const r of reports) {
        try {
          parsedContents[r.id] = JSON.parse(r.content);
        } catch {
          parsedContents[r.id] = {} as ReportContent;
        }
      }
    } catch (e: any) {
      error = e.message || '加载报告失败';
    } finally {
      loading = false;
    }
  }

  async function generateReport() {
    generating = true;
    error = '';
    try {
      await apiJson<LearningReport>('/api/reports/generate', {
        method: 'POST',
        body: {
          courseId: courseIdInput,
          reportType: 'weekly',
        },
      });
      await loadReports();
      expandedId = null;
    } catch (e: any) {
      error = e.message || '生成报告失败';
    } finally {
      generating = false;
    }
  }

  function toggleExpand(id: number) {
    expandedId = expandedId === id ? null : id;
  }

  function formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  function reportTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      weekly: '周报',
      monthly: '月报',
      course: '课程报告',
    };
    return labels[type] || type;
  }

  function progressPercent(content: ReportContent): string {
    const cp = content.courseProgress;
    if (cp && cp.totalLessons > 0) {
      return Math.round((cp.completedLessons / cp.totalLessons) * 100) + '%';
    }
    return '0%';
  }
</script>

<div class="reports-page">
  <h1 class="gradient-text">学习报告</h1>

  {#if error}
    <div class="error-toast">{error}</div>
  {/if}

  <!-- Generate section -->
  <div class="glass generate-section">
    <div class="generate-row">
      <div class="course-select">
        <label for="courseId">课程 ID</label>
        <input
          id="courseId"
          type="number"
          bind:value={courseIdInput}
          min="1"
          class="input"
        />
      </div>
      <button
        class="btn-generate"
        on:click={generateReport}
        disabled={generating}
      >
        {generating ? '生成中...' : '生成学习报告'}
      </button>
    </div>
  </div>

  <!-- Reports list -->
  <div class="reports-list">
    {#if loading}
      <p class="loading">加载中...</p>
    {:else if reports.length === 0}
      <div class="glass empty-state">
        <p>还没有学习报告</p>
        <p class="sub">完成课程后生成你的第一份学习报告</p>
      </div>
    {:else}
      {#each reports as report}
        {@const content = parsedContents[report.id]}
        <div class="glass report-card">
          <button class="report-header" on:click={() => toggleExpand(report.id)}>
            <div class="report-info">
              <span class="report-type-badge">{reportTypeLabel(report.reportType)}</span>
              <span class="report-date">{formatDate(report.generatedAt)}</span>
              {#if content?.stats}
                <span class="report-progress">进度 {progressPercent(content)}</span>
              {/if}
            </div>
            <span class="expand-icon">{expandedId === report.id ? '收起' : '展开'}</span>
          </button>

          {#if expandedId === report.id && content}
            <div class="report-detail">
              <!-- Progress overview -->
              <div class="stat-grid">
                <div class="stat-item">
                  <span class="stat-value">{content.stats?.completedLessons ?? 0}/{content.stats?.totalLessons ?? 0}</span>
                  <span class="stat-label">完成课时</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{content.stats?.totalScore ?? 0}</span>
                  <span class="stat-label">总积分</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{content.stats?.totalTimeMinutes ?? 0}分</span>
                  <span class="stat-label">学习时长</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{content.streak ?? 0}天</span>
                  <span class="stat-label">活跃天数</span>
                </div>
              </div>

              <!-- Skills overview -->
              {#if content.skills}
                <div class="skills-bar">
                  <div class="skills-bar-inner">
                    <div
                      class="skills-bar-fill"
                      style="width: {content.skills.total > 0 ? (content.skills.mastered / content.skills.total * 100) : 0}%"
                    ></div>
                  </div>
                  <span class="skills-bar-label">
                    技能掌握: {content.skills.mastered}/{content.skills.total}
                    ({content.skills.unlocked} 已解锁)
                  </span>
                </div>
              {/if}

              <!-- Wrong questions -->
              {#if content.wrongQuestionsPending != null}
                <div class="info-row">
                  <span class="info-icon">&#9888;</span>
                  待消灭错题: <strong>{content.wrongQuestionsPending}</strong> 道
                </div>
              {/if}

              <!-- Recommendations -->
              {#if content.recommendations?.length}
                <div class="recommendations">
                  <h4>学习建议</h4>
                  <ul>
                    {#each content.recommendations as rec}
                      <li>{rec}</li>
                    {/each}
                  </ul>
                </div>
              {/if}

              <!-- Chapter progress -->
              {#if content.courseProgress?.chapters}
                <div class="chapters">
                  <h4>章节进度</h4>
                  {#each content.courseProgress.chapters as ch}
                    <div class="chapter-row">
                      <span class="chapter-title">{ch.title}</span>
                      <span class="chapter-progress">{ch.completedLessons}/{ch.totalLessons}</span>
                      <div class="chapter-bar">
                        <div
                          class="chapter-bar-fill"
                          style="width: {ch.totalLessons > 0 ? (ch.completedLessons / ch.totalLessons * 100) : 0}%"
                        ></div>
                      </div>
                    </div>
                  {/each}
                </div>
              {/if}
            </div>
          {/if}
        </div>
      {/each}
    {/if}
  </div>

  <!-- Skill Tree Section -->
  <SkillTree />
</div>

<style>
  .reports-page {
    max-width: 800px;
    margin: 0 auto;
  }

  .error-toast {
    padding: 10px 16px;
    margin-bottom: 16px;
    background: rgba(255, 100, 100, 0.15);
    border: 1px solid var(--accent-red);
    border-radius: 8px;
    color: var(--accent-red);
    font-size: 13px;
  }

  .generate-section {
    padding: 20px;
    margin-bottom: 24px;
  }

  .generate-row {
    display: flex;
    align-items: flex-end;
    gap: 16px;
  }

  .course-select {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .course-select label {
    font-size: 12px;
    color: var(--text-secondary);
  }

  .input {
    width: 80px;
    padding: 8px 12px;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-primary);
    font-size: 14px;
    outline: none;
  }

  .btn-generate {
    padding: 10px 24px;
    background: linear-gradient(135deg, var(--accent-gold), #ff8c00);
    border: none;
    border-radius: 8px;
    color: #000;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: opacity 0.2s;
  }

  .btn-generate:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .btn-generate:not(:disabled):hover {
    opacity: 0.9;
  }

  .loading {
    color: var(--text-secondary);
    font-size: 13px;
    text-align: center;
    padding: 20px;
  }

  .empty-state {
    padding: 40px 20px;
    text-align: center;
  }

  .empty-state p {
    font-size: 15px;
    color: var(--text-primary);
  }

  .empty-state .sub {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 8px;
  }

  .reports-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;
  }

  .report-card {
    overflow: hidden;
  }

  .report-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding: 16px 20px;
    background: none;
    border: none;
    color: var(--text-primary);
    cursor: pointer;
    font: inherit;
  }

  .report-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .report-type-badge {
    padding: 2px 10px;
    background: linear-gradient(135deg, var(--accent-gold), #ff8c00);
    border-radius: 20px;
    font-size: 11px;
    color: #000;
    font-weight: 600;
  }

  .report-date {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .report-progress {
    font-size: 13px;
    color: var(--accent-blue-light);
  }

  .expand-icon {
    font-size: 12px;
    color: var(--text-secondary);
  }

  .report-detail {
    padding: 0 20px 20px;
    border-top: 1px solid var(--glass-border);
  }

  .stat-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-top: 16px;
  }

  .stat-item {
    text-align: center;
    padding: 12px 8px;
    background: rgba(255, 255, 255, 0.04);
    border-radius: 8px;
  }

  .stat-value {
    display: block;
    font-size: 20px;
    font-weight: 700;
    color: var(--accent-gold);
  }

  .stat-label {
    display: block;
    font-size: 11px;
    color: var(--text-secondary);
    margin-top: 4px;
  }

  .skills-bar {
    margin-top: 16px;
  }

  .skills-bar-inner {
    height: 6px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 3px;
    overflow: hidden;
  }

  .skills-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--accent-green), var(--accent-blue-light));
    border-radius: 3px;
    transition: width 0.4s ease;
  }

  .skills-bar-label {
    display: block;
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 6px;
  }

  .info-row {
    margin-top: 12px;
    font-size: 13px;
    color: var(--text-primary);
  }

  .info-row strong {
    color: var(--accent-red);
  }

  .info-icon {
    margin-right: 4px;
  }

  .recommendations {
    margin-top: 16px;
    padding: 14px;
    background: rgba(255, 255, 255, 0.03);
    border-radius: 8px;
  }

  .recommendations h4 {
    font-size: 13px;
    margin-bottom: 8px;
    color: var(--text-secondary);
  }

  .recommendations ul {
    list-style: none;
    padding: 0;
    margin: 0;
  }

  .recommendations li {
    font-size: 13px;
    color: var(--text-primary);
    padding: 4px 0;
    padding-left: 16px;
    position: relative;
  }

  .recommendations li::before {
    content: '';
    position: absolute;
    left: 0;
    top: 10px;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent-gold);
  }

  .chapters {
    margin-top: 16px;
  }

  .chapters h4 {
    font-size: 13px;
    margin-bottom: 10px;
    color: var(--text-secondary);
  }

  .chapter-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
    font-size: 13px;
  }

  .chapter-title {
    flex: 0 0 120px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .chapter-progress {
    flex: 0 0 40px;
    text-align: right;
    color: var(--text-secondary);
    font-size: 11px;
  }

  .chapter-bar {
    flex: 1;
    height: 5px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 3px;
    overflow: hidden;
  }

  .chapter-bar-fill {
    height: 100%;
    background: var(--accent-blue-light);
    border-radius: 3px;
    transition: width 0.3s ease;
  }
</style>
