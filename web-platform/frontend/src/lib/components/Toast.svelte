<script>
  import { toasts } from '$lib/stores/toast';
</script>

<div class="toast-container">
  {#each $toasts as toast (toast.id)}
    <div class="toast glass" class:toast-error={toast.type === 'error'} class:toast-success={toast.type === 'success'}>
      {toast.message}
    </div>
  {/each}
</div>

<style>
  .toast-container {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .toast {
    padding: 12px 20px;
    font-size: 13px;
    animation: slideIn 0.3s ease;
  }
  .toast-error { border-color: var(--accent-red); }
  .toast-success { border-color: var(--accent-green); }
  @keyframes slideIn {
    from { transform: translateX(100%); opacity: 0; }
    to { transform: translateX(0); opacity: 1; }
  }
</style>
