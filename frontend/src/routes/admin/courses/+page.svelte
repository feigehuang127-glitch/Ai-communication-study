<script lang="ts">
  import { onMount } from 'svelte';
  import { user } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import { apiJson } from '$lib/api/client';
  import PremiumCard from '$lib/components/PremiumCard.svelte';
  import LoadingSpinner from '$lib/components/LoadingSpinner.svelte';

  interface College {
    id: number;
    name: string;
    slug: string;
  }

  interface Course {
    id: number;
    collegeId: number;
    title: string;
    slug: string;
    level: string;
    order: number;
    description: string;
  }

  let colleges: College[] = [];
  let courses: Course[] = [];
  let editing: Course | null = null;

  let form = {
    title: '',
    slug: '',
    level: 'L1',
    description: '',
    collegeId: 0
  };

  let message = '';
  let loading = true;

  onMount(async () => {
    const currentUser = $user;
    if (!currentUser || currentUser.role !== 'ADMIN') {
      goto('/login');
      return;
    }
    try {
      colleges = await apiJson<College[]>('/api/admin/colleges');
      courses = await apiJson<Course[]>('/api/admin/courses');
    } catch (e: any) {
      message = '加载失败: ' + e.message;
    } finally {
      loading = false;
    }
  });

  function resetForm() {
    editing = null;
    form = { title: '', slug: '', level: 'L1', description: '', collegeId: 0 };
  }

  function editCourse(course: Course) {
    editing = course;
    form = {
      title: course.title,
      slug: course.slug,
      level: course.level || 'L1',
      description: course.description || '',
      collegeId: course.collegeId
    };
  }

  async function save() {
    if (!form.title || !form.slug || !form.collegeId) {
      message = '请填写标题、标识和所属学院';
      return;
    }
    try {
      if (editing) {
        const updated = await apiJson<Course>('/api/admin/courses/' + editing.id, {
          method: 'PUT',
          body: { ...form, id: editing.id, order: editing.order }
        });
        courses = courses.map(c => c.id === updated.id ? updated : c);
        message = '课程已更新';
      } else {
        const created = await apiJson<Course>('/api/admin/courses', {
          method: 'POST',
          body: { ...form, order: 0 }
        });
        courses = [...courses, created];
        message = '课程已创建';
      }
      resetForm();
    } catch (e: any) {
      message = '保存失败: ' + e.message;
    }
  }

  async function deleteCourse(course: Course) {
    if (!confirm('确定删除课程 "' + course.title + '"？')) return;
    try {
      await apiJson('/api/admin/courses/' + course.id, { method: 'DELETE' });
      courses = courses.filter(c => c.id !== course.id);
      message = '课程已删除';
    } catch (e: any) {
      message = '删除失败: ' + e.message;
    }
  }
</script>

{#if loading}
  <LoadingSpinner />
{:else}
  <div class="admin-page">
    <div class="header-row">
      <h1 class="gradient-text">课程管理</h1>
      <a href="/admin" class="back-link">← 返回后台</a>
    </div>

    {#if message}
      <div class="msg">{message}</div>
    {/if}

    <PremiumCard>
    <h2>{editing ? '编辑课程' : '新建课程'}</h2>
    <form on:submit|preventDefault={save} class="course-form">
      <div class="form-row">
        <label>
          所属学院
          <select bind:value={form.collegeId}>
            <option value={0}>-- 选择学院 --</option>
            {#each colleges as c}
              <option value={c.id}>{c.name}</option>
            {/each}
          </select>
        </label>
        <label>
          难度等级
          <select bind:value={form.level}>
            <option value="L1">L1 - 入门</option>
            <option value="L2">L2 - 初级</option>
            <option value="L3">L3 - 中级</option>
            <option value="L4">L4 - 高级</option>
            <option value="L5">L5 - 专家</option>
          </select>
        </label>
      </div>
      <div class="form-row">
        <label>
          课程标题
          <input type="text" bind:value={form.title} placeholder="例如：提示词工程基础" />
        </label>
        <label>
          标识 (slug)
          <input type="text" bind:value={form.slug} placeholder="例如：prompt-engineering-basics" />
        </label>
      </div>
      <label>
        课程描述
        <textarea bind:value={form.description} placeholder="简要描述课程内容" rows="3"></textarea>
      </label>
      <div class="form-actions">
        <button type="submit" class="btn-primary">{editing ? '更新' : '创建'}</button>
        {#if editing}
          <button type="button" on:click={resetForm}>取消</button>
        {/if}
      </div>
    </form>
  </PremiumCard>

  <section class="list-section">
    <h2>已有课程 ({courses.length})</h2>
    {#if courses.length === 0}
      <p class="empty">暂无课程</p>
    {:else}
      <div class="course-list">
        {#each courses as course (course.id)}
          <PremiumCard>
            <div class="course-item">
              <div class="course-info">
                <h3>{course.title}</h3>
                <p class="meta">
                  <span>{course.slug}</span>
                  <span class="level">{course.level || 'L1'}</span>
                  <span>学院 #{course.collegeId}</span>
                </p>
              </div>
              <div class="course-actions">
                <button class="btn-sm" on:click={() => editCourse(course)}>编辑</button>
                <button class="btn-sm btn-danger" on:click={() => deleteCourse(course)}>删除</button>
              </div>
            </div>
          </PremiumCard>
        {/each}
      </div>
    {/if}
  </section>
</div>
{/if}

<style>
  .admin-page { max-width: 900px; margin: 0 auto; }
  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }
  .back-link { color: var(--text-secondary); font-size: 14px; text-decoration: none; }
  .back-link:hover { color: var(--accent-primary); }
  .msg {
    background: rgba(255,255,255,0.06);
    border: 1px solid rgba(255,255,255,0.1);
    border-radius: 8px;
    padding: 10px 16px;
    margin-bottom: 16px;
    font-size: 13px;
  }
  h2 { font-size: 16px; margin-bottom: 16px; }
  .course-form { display: flex; flex-direction: column; gap: 12px; }
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
  label { font-size: 13px; color: var(--text-secondary); display: flex; flex-direction: column; gap: 4px; }
  input, select, textarea {
    font: inherit;
    padding: 8px 12px;
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(255,255,255,0.12);
    border-radius: 6px;
    color: #fff;
    font-size: 14px;
  }
  input:focus, select:focus, textarea:focus {
    outline: none;
    border-color: var(--accent-primary);
  }
  select { appearance: none; }
  select option { background: #1a1a2e; color: #fff; }
  .form-actions { display: flex; gap: 8px; }
  .btn-primary {
    padding: 10px 24px;
    background: var(--accent-primary);
    border: none;
    border-radius: 6px;
    color: #fff;
    font-weight: 600;
    cursor: pointer;
  }
  .btn-primary:hover { opacity: 0.9; }
  .form-actions button[type="button"] {
    padding: 10px 16px;
    background: rgba(255,255,255,0.08);
    border: 1px solid rgba(255,255,255,0.12);
    border-radius: 6px;
    color: #fff;
    cursor: pointer;
  }
  .list-section { margin-top: 32px; }
  .empty { color: var(--text-secondary); font-size: 14px; }
  .course-list { display: flex; flex-direction: column; gap: 12px; }
  .course-item { display: flex; justify-content: space-between; align-items: center; }
  .course-info h3 { font-size: 15px; margin-bottom: 4px; }
  .meta { font-size: 12px; color: var(--text-secondary); display: flex; gap: 12px; }
  .level {
    padding: 1px 6px;
    background: rgba(100,200,255,0.15);
    border-radius: 4px;
  }
  .course-actions { display: flex; gap: 8px; flex-shrink: 0; }
  .btn-sm {
    padding: 6px 14px;
    font-size: 12px;
    background: rgba(255,255,255,0.08);
    border: 1px solid rgba(255,255,255,0.12);
    border-radius: 6px;
    color: #fff;
    cursor: pointer;
  }
  .btn-sm:hover { background: rgba(255,255,255,0.14); }
  .btn-danger:hover { border-color: rgba(255,100,100,0.5); color: #f66; }
</style>
