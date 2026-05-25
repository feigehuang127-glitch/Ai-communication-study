<script>
  export let message = '加载中...';
</script>

<div class="spinner-container">
  <div class="spinner"></div>
  <p>{message}</p>
</div>

<style>
  .spinner-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 0;
    gap: 16px;
  }
  .spinner {
    width: 40px;
    height: 40px;
    border: 3px solid rgba(255, 255, 255, 0.1);
    border-top-color: var(--accent-blue);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  p { color: var(--text-secondary); font-size: 14px; }
</style>
