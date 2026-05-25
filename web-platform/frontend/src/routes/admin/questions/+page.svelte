<script lang="ts">
  import { onMount } from 'svelte';
  import { apiJson } from '$lib/api/client';
  import GlassCard from '$lib/components/GlassCard.svelte';

  interface Question {
    id: number;
    content: string;
    type: string;
    optionA: string;
    optionB: string;
    optionC: string;
    optionD: string;
    answer: string;
    explanation: string;
    category: string;
    college: string;
    difficulty: number;
  }

  let questions: Question[] = [];
  let editing: Question | null = null;

  let form = {
    content: '',
    type: 'single',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    answer: '',
    explanation: '',
    category: '',
    college: 'comm',
    difficulty: 1
  };

  let message = '';

  onMount(async () => {
    try {
      questions = await apiJson<Question[]>('/api/admin/questions');
    } catch (e: any) {
      message = '加载失败: ' + e.message;
    }
  });

  function resetForm() {
    editing = null;
    form = {
      content: '',
      type: 'single',
      optionA: '',
      optionB: '',
      optionC: '',
      optionD: '',
      answer: '',
      explanation: '',
      category: '',
      college: 'comm',
      difficulty: 1
    };
  }

  function editQuestion(q: Question) {
    editing = q;
    form = {
      content: q.content,
      type: q.type || 'single',
      optionA: q.optionA,
      optionB: q.optionB,
      optionC: q.optionC || '',
      optionD: q.optionD || '',
      answer: q.answer,
      explanation: q.explanation || '',
      category: q.category || '',
      college: q.college || 'comm',
      difficulty: q.difficulty || 1
    };
  }

  async function save() {
    if (!form.content || !form.optionA || !form.optionB || !form.answer) {
      message = '请填写题干、选项A、选项B和正确答案';
      return;
    }
    try {
      if (editing) {
        const updated = await apiJson<Question>('/api/admin/questions/' + editing.id, {
          method: 'PUT',
          body: { ...form, id: editing.id }
        });
        questions = questions.map(q => q.id === updated.id ? updated : q);
        message = '题目已更新';
      } else {
        const created = await apiJson<Question>('/api/admin/questions', {
          method: 'POST',
          body: form
        });
        questions = [...questions, created];
        message = '题目已创建';
      }
      resetForm();
    } catch (e: any) {
      message = '保存失败: ' + e.message;
    }
  }

  async function deleteQuestion(q: Question) {
    if (!confirm('确定删除题目 #' + q.id + '？')) return;
    try {
      await apiJson('/api/admin/questions/' + q.id, { method: 'DELETE' });
      questions = questions.filter(item => item.id !== q.id);
      message = '题目已删除';
    } catch (e: any) {
      message = '删除失败: ' + e.message;
    }
  }
</script>

<div class="admin-page">
  <div class="header-row">
    <h1 class="gradient-text">题库管理</h1>
    <a href="/admin" class="back-link">← 返回后台</a>
  </div>

  {#if message}
    <div class="msg">{message}</div>
  {/if}

  <GlassCard>
    <h2>{editing ? '编辑题目' : '新增题目'}</h2>
    <form on:submit|preventDefault={save} class="question-form">
      <div class="form-row">
        <label>
          所属学院
          <select bind:value={form.college}>
            <option value="comm">通信学院</option>
            <option value="ai">AI 学院</option>
          </select>
        </label>
        <label>
          题目类型
          <select bind:value={form.type}>
            <option value="single">单选题</option>
            <option value="multi">多选题</option>
          </select>
        </label>
        <label>
          难度
          <select bind:value={form.difficulty}>
            <option value={1}>1 - 入门</option>
            <option value={2}>2 - 初级</option>
            <option value={3}>3 - 中级</option>
            <option value={4}>4 - 高级</option>
            <option value={5}>5 - 专家</option>
          </select>
        </label>
        <label>
          分类
          <input type="text" bind:value={form.category} placeholder="例如：prompt-engineering" />
        </label>
      </div>
      <label>
        题干
        <textarea bind:value={form.content} placeholder="题目内容" rows="3"></textarea>
      </label>
      <div class="form-row">
        <label>
          选项 A
          <input type="text" bind:value={form.optionA} placeholder="选项 A" />
        </label>
        <label>
          选项 B
          <input type="text" bind:value={form.optionB} placeholder="选项 B" />
        </label>
      </div>
      <div class="form-row">
        <label>
          选项 C
          <input type="text" bind:value={form.optionC} placeholder="选项 C（可选）" />
        </label>
        <label>
          选项 D
          <input type="text" bind:value={form.optionD} placeholder="选项 D（可选）" />
        </label>
      </div>
      <div class="form-row">
        <label>
          正确答案
          <input type="text" bind:value={form.answer} placeholder="例如：A 或 AB" />
        </label>
      </div>
      <label>
        解析
        <textarea bind:value={form.explanation} placeholder="答案解析（可选）" rows="2"></textarea>
      </label>
      <div class="form-actions">
        <button type="submit" class="btn-primary">{editing ? '更新' : '创建'}</button>
        {#if editing}
          <button type="button" on:click={resetForm}>取消</button>
        {/if}
      </div>
    </form>
  </GlassCard>

  <section class="list-section">
    <h2>已有题目 ({questions.length})</h2>
    {#if questions.length === 0}
      <p class="empty">暂无题目</p>
    {:else}
      <div class="question-list">
        {#each questions as q (q.id)}
          <GlassCard>
            <div class="question-item">
              <div class="question-info">
                <p class="q-content">{q.content}</p>
                <p class="q-meta">
                  <span>{q.college}</span>
                  <span>{q.type === 'multi' ? '多选' : '单选'}</span>
                  <span>难度 {q.difficulty}</span>
                  <span>{q.category || '未分类'}</span>
                  <span>答案: {q.answer}</span>
                </p>
              </div>
              <div class="q-actions">
                <button class="btn-sm" on:click={() => editQuestion(q)}>编辑</button>
                <button class="btn-sm btn-danger" on:click={() => deleteQuestion(q)}>删除</button>
              </div>
            </div>
          </GlassCard>
        {/each}
      </div>
    {/if}
  </section>
</div>

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
  .question-form { display: flex; flex-direction: column; gap: 12px; }
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
  .question-list { display: flex; flex-direction: column; gap: 12px; }
  .question-item { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
  .question-info { flex: 1; min-width: 0; }
  .q-content {
    font-size: 14px;
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .q-meta { font-size: 12px; color: var(--text-secondary); display: flex; gap: 12px; flex-wrap: wrap; }
  .q-actions { display: flex; gap: 8px; flex-shrink: 0; }
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
