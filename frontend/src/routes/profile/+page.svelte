<script lang="ts">
  import { onMount } from 'svelte';
  import { apiJson } from '$lib/api/client';
  import PremiumCard from '$lib/components/PremiumCard.svelte';
  import LoadingSpinner from '$lib/components/LoadingSpinner.svelte';

  let profile: any = null;
  let loading = true;

  onMount(async () => {
    try {
      profile = await apiJson('/api/user/me');
    } catch { /* handle */ } finally {
      loading = false;
    }
  });
</script>

{#if loading}
  <LoadingSpinner />
{:else}
  <div class="profile">
    <div class="profile-header">
      <div class="avatar glass">{(profile?.username || '?')[0].toUpperCase()}</div>
      <div>
        <h1>{profile?.username}</h1>
        <span class="rank-badge">{profile?.rank}</span>
      </div>
      <div class="score">{profile?.totalScore ?? 0} <small>积分</small></div>
    </div>

    <div class="grid">
      <PremiumCard href="/profile/reports">
        <h3>学习报告</h3>
        <p>查看学习进度与技能树</p>
      </PremiumCard>
      <PremiumCard href="/profile/wrongbook">
        <h3>错题本</h3>
        <p>查看和复习错过的题目</p>
      </PremiumCard>
      <PremiumCard href="/profile/settings">
        <h3>设置</h3>
        <p>账户与偏好设置</p>
      </PremiumCard>
    </div>
  </div>
{/if}

<style>
  .profile { max-width: 600px; margin: 0 auto; }
  .profile-header {
    display: flex; align-items: center; gap: 16px;
    padding: 24px; margin-bottom: 24px;
  }
  .avatar {
    width: 60px; height: 60px;
    display: flex; align-items: center; justify-content: center;
    font-size: 24px; font-weight: 700;
    border-radius: 50%;
  }
  .rank-badge {
    display: inline-block;
    padding: 2px 10px;
    background: linear-gradient(135deg, var(--accent-gold), #ff8c00);
    border-radius: 20px;
    font-size: 12px;
    color: #000;
    font-weight: 600;
    margin-top: 4px;
  }
  .score { margin-left: auto; text-align: right; }
  .score { font-size: 28px; font-weight: 700; }
  .score small { font-size: 12px; color: var(--text-secondary); }
  .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
  .grid h3 { margin-bottom: 4px; }
  .grid p { color: var(--text-secondary); font-size: 13px; }
</style>
