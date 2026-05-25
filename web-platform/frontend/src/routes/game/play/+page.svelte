<script lang="ts">
  import { game } from '$lib/stores/game';
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';

  let timeLeft = 10;
  let isLocked = false;
  let selectedAnswer = '';
  let showResult = false;
  let lastCorrect = false;
  let timer: ReturnType<typeof setInterval>;

  $: question = $game.questions[$game.currentIndex];

  onMount(() => {
    if ($game.questions.length === 0) {
      goto('/game');
      return;
    }
    startTimer();
  });

  function startTimer() {
    timeLeft = 10;
    clearInterval(timer);
    timer = setInterval(() => {
      timeLeft--;
      if (timeLeft <= 0) {
        handleTimeout();
      }
    }, 1000);
  }

  function handleTimeout() {
    clearInterval(timer);
    isLocked = true;
    lastCorrect = false;
    showResult = true;
  }

  async function selectOption(opt: string) {
    if (isLocked) return;
    isLocked = true;
    clearInterval(timer);
    selectedAnswer = opt;
    lastCorrect = await game.submitAnswer(opt);
    showResult = true;
  }

  function handleContinue() {
    showResult = false;
    selectedAnswer = '';
    isLocked = false;

    if ($game.currentIndex >= $game.totalQuestions - 1) {
      finishGame();
    } else {
      game.nextQuestion();
      startTimer();
    }
  }

  async function finishGame() {
    const result = await game.finish();
    sessionStorage.setItem('gameResult', JSON.stringify(result));
    goto('/game/result');
  }

  function optionClass(opt: string) {
    if (!showResult) return '';
    const q = $game.questions[$game.currentIndex];
    if (opt === q.answer) return 'correct';
    if (opt === selectedAnswer && !lastCorrect) return 'wrong';
    return 'dimmed';
  }
</script>

{#if $game.isLoading}
  <div class="loading">加载中...</div>
{:else if question}
  <div class="game-play">
    <div class="progress-bar">
      <div class="progress-fill" style="width:{$game.currentIndex / $game.totalQuestions * 100}%"></div>
    </div>

    <div class="timer" class:urgent={timeLeft <= 3}>
      {timeLeft}s
    </div>

    <div class="question-card glass">
      <span class="q-num">第 {$game.currentIndex + 1} / {$game.totalQuestions} 题</span>
      <h2>{question.content}</h2>

      <div class="options">
        {#each ['A', 'B', 'C', 'D'] as opt}
          {#if question['option' + opt]}
            <button
              class="option {optionClass(opt)}"
              on:click={() => selectOption(opt)}
              disabled={isLocked}
            >
              <span class="opt-letter">{opt}</span>
              <span>{question['option' + opt]}</span>
            </button>
          {/if}
        {/each}
      </div>

      {#if showResult}
        <div class="result-feedback" class:correct={lastCorrect} class:wrong={!lastCorrect}>
          {lastCorrect ? '✓ 正确!' : '✗ 错误'}
          {#if !lastCorrect && question.explanation}
            <p class="explanation">{question.explanation}</p>
          {/if}
        </div>
        <button class="btn-primary continue-btn" on:click={handleContinue}>
          {$game.currentIndex >= $game.totalQuestions - 1 ? '查看结果' : '下一题'}
        </button>
      {/if}
    </div>
  </div>
{/if}

<style>
  .game-play { max-width: 700px; margin: 0 auto; }
  .progress-bar {
    height: 4px;
    background: rgba(255,255,255,0.1);
    border-radius: 2px;
    margin-bottom: 16px;
  }
  .progress-fill {
    height: 100%;
    background: var(--accent-blue);
    border-radius: 2px;
    transition: width 0.3s;
  }
  .timer {
    text-align: center;
    font-size: 28px;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    margin-bottom: 20px;
    color: var(--text-primary);
  }
  .timer.urgent { color: var(--accent-red); animation: pulse 0.5s infinite; }
  @keyframes pulse { 50% { opacity: 0.5; } }
  .question-card { padding: 32px; }
  .q-num { font-size: 12px; color: var(--text-secondary); }
  .question-card h2 { margin: 12px 0 24px; font-size: 18px; line-height: 1.6; }
  .options { display: flex; flex-direction: column; gap: 10px; }
  .option {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 18px;
    background: rgba(255,255,255,0.04);
    border: 1px solid var(--glass-border);
    border-radius: 12px;
    color: var(--text-primary);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: left;
  }
  .option:hover:not(:disabled) {
    background: rgba(255,255,255,0.08);
    border-color: rgba(255,255,255,0.2);
  }
  .option:disabled { cursor: default; }
  .opt-letter {
    width: 28px; height: 28px;
    display: flex; align-items: center; justify-content: center;
    background: rgba(255,255,255,0.08);
    border-radius: 8px;
    font-weight: 600; font-size: 13px;
  }
  .option.correct { background: rgba(100,200,150,0.15); border-color: var(--accent-green); }
  .option.wrong { background: rgba(255,100,100,0.15); border-color: var(--accent-red); }
  .option.dimmed { opacity: 0.4; }
  .result-feedback {
    margin-top: 20px; padding: 16px; border-radius: 12px; font-weight: 600;
  }
  .result-feedback.correct { background: rgba(100,200,150,0.1); color: var(--accent-green); }
  .result-feedback.wrong { background: rgba(255,100,100,0.1); color: var(--accent-red); }
  .explanation { font-weight: 400; margin-top: 8px; font-size: 13px; color: var(--text-secondary); }
  .btn-primary {
    width: 100%; padding: 14px; margin-top: 20px;
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border: none; border-radius: 12px; color: white; font-size: 15px;
    font-weight: 600; cursor: pointer;
  }
  .loading { text-align: center; padding: 80px; color: var(--text-secondary); }
</style>
