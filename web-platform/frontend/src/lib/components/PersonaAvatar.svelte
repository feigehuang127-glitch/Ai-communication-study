<script lang="ts">
  export let personaId: string = 'lecturer';
  export let name: string = '讲解老师';

  const colors: Record<string, string> = {
    lecturer: '#ffb464',
    code_mentor: '#64b4ff',
    study_buddy: '#64c896',
    analyst: '#ff6464',
  };

  const emojis: Record<string, string> = {
    lecturer: '👨‍🏫',
    code_mentor: '👨‍💻',
    study_buddy: '🤝',
    analyst: '📊',
  };

  $: color = colors[personaId] || '#64b4ff';
  $: emoji = emojis[personaId] || '🤖';
</script>

<div class="persona-avatar" style="--persona-color: {color}">
  <span class="avatar-emoji">{emoji}</span>
  <div class="avatar-info">
    <span class="avatar-name">{name}</span>
    <span class="avatar-badge" style="background: {color}20; color: {color}">AI</span>
  </div>
</div>

<style>
  .persona-avatar {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px;
  }
  .avatar-emoji { font-size: 28px; }
  .avatar-info { display: flex; flex-direction: column; gap: 2px; }
  .avatar-name { font-size: 14px; font-weight: 600; }
  .avatar-badge {
    display: inline-block;
    padding: 1px 8px;
    border-radius: 10px;
    font-size: 10px;
    font-weight: 600;
    width: fit-content;
  }
</style>
