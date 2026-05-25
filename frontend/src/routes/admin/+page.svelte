<script lang="ts">
  import GlassCard from '$lib/components/GlassCard.svelte';
</script>

<div class="admin">
  <h1 class="gradient-text">管理后台</h1>

  <div class="admin-grid">
    <a href="/admin/courses" class="admin-link">
      <GlassCard>
        <div class="admin-card">
          <span class="icon">📚</span>
          <h3>课程管理</h3>
          <p>管理学院、课程、章节、课时</p>
        </div>
      </GlassCard>
    </a>
    <a href="/admin/questions" class="admin-link">
      <GlassCard>
        <div class="admin-card">
          <span class="icon">📝</span>
          <h3>题库管理</h3>
          <p>新增、编辑、分类题目</p>
        </div>
      </GlassCard>
    </a>
  </div>
</div>

<style>
  .admin { max-width: 800px; margin: 0 auto; }
  .admin-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-top: 24px; }
  .admin-link { text-decoration: none; color: inherit; }
  .admin-card { text-align: center; padding: 24px 0; }
  .icon { font-size: 40px; display: block; margin-bottom: 12px; }
  .admin-card h3 { font-size: 16px; margin-bottom: 4px; }
  .admin-card p { color: var(--text-secondary); font-size: 13px; }
</style>
