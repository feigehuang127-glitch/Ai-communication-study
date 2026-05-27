<script lang="ts">
  import { game } from '$lib/stores/game';
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { getCollector } from '$lib/behavior/collector';
  import { fly, fade } from 'svelte/transition';
  import { quintOut } from 'svelte/easing';

  let timeLeft = 10;
  let isLocked = false;
  let selectedAnswer = '';
  let showResult = false;
  let lastCorrect = false;
  let timer: ReturnType<typeof setInterval>;
  let questionStart = Date.now();
  let optionChangeCount = 0;

  const RING_RADIUS = 42;
  const RING_CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS;

  $: question = $game.questions[$game.currentIndex];
  $: dashOffset = RING_CIRCUMFERENCE * (1 - timeLeft / 10);

  onMount(async () => {
    const params = $page.url.searchParams;
    const mode = params.get('mode') || 'pve';
    const source = params.get('source') || '';

    if ($game.questions.length === 0) {
      try {
        await game.start(source || 'ai', mode);
      } catch {
        goto('/game/lobby');
        return;
      }
    }
    getCollector().reportQuizStart();
    questionStart = Date.now();
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
    const latency = Date.now() - questionStart;
    getCollector().reportAnswerSubmit(latency, optionChangeCount, false);
  }

  async function selectOption(opt: string) {
    if (isLocked) return;
    if (selectedAnswer && selectedAnswer !== opt) {
      optionChangeCount++;
    }
    isLocked = true;
    clearInterval(timer);
    selectedAnswer = opt;
    const latency = Date.now() - questionStart;
    lastCorrect = await game.submitAnswer(opt);
    getCollector().reportAnswerSubmit(latency, optionChangeCount, lastCorrect);
    triggerFeedback(opt, lastCorrect);
    showResult = true;
  }

  function handleContinue() {
    showResult = false;
    selectedAnswer = '';
    isLocked = false;
    optionChangeCount = 0;

    if ($game.currentIndex >= $game.totalQuestions - 1) {
      finishGame();
    } else {
      game.nextQuestion();
      questionStart = Date.now();
      startTimer();
    }
  }

  async function finishGame() {
    const result = await game.finish();
    sessionStorage.setItem('gameResult', JSON.stringify(result));
    goto('/game/result');
  }

  let shakeTarget = '';
  let burstTarget = '';

  function optionClass(opt: string) {
    if (!showResult) return '';
    const q = $game.questions[$game.currentIndex];
    if (opt === q.answer) return 'correct';
    if (opt === selectedAnswer && !lastCorrect) return 'wrong';
    return 'dimmed';
  }

  function triggerFeedback(opt: string, correct: boolean) {
    if (correct) {
      burstTarget = opt;
      setTimeout(() => { burstTarget = ''; }, 700);
    } else {
      shakeTarget = opt;
      setTimeout(() => { shakeTarget = ''; }, 500);
    }
  }
</script>

{#if $game.isLoading}
  <div class="loading">加载中...</div>
{:else if question}
  <div class="game-play">
    <div class="progress-bar">
      <div class="progress-fill" style="width:{$game.currentIndex / $game.totalQuestions * 100}%"></div>
    </div>

    <div class="timer-ring" class:urgent={timeLeft <= 3}>
      <svg viewBox="0 0 100 100" class="ring-svg">
        <circle
          class="ring-bg"
          cx="50" cy="50" r={RING_RADIUS}
          fill="none"
          stroke="rgba(255,255,255,0.08)"
          stroke-width="4"
        />
        <circle
          class="ring-fg"
          cx="50" cy="50" r={RING_RADIUS}
          fill="none"
          stroke={timeLeft <= 3 ? 'var(--accent-red)' : 'var(--morandi-blue)'}
          stroke-width="3.5"
          stroke-linecap="round"
          stroke-dasharray={RING_CIRCUMFERENCE}
          stroke-dashoffset={dashOffset}
          style="transition: stroke-dashoffset 0.9s var(--ease-spring-damped), stroke 0.3s ease;"
        />
      </svg>
      <div class="timer-text" class:urgent-text={timeLeft <= 3}>
        {timeLeft}
      </div>
    </div>

    {#key $game.currentIndex}
      <div
        class="question-card glass"
        in:fly={{ x: 40, duration: 400, easing: quintOut }}
        out:fade={{ duration: 120 }}
      >
        <span class="q-num">第 {$game.currentIndex + 1} / {$game.totalQuestions} 题</span>
        <h2>{question.content}</h2>

        <div class="options">
          {#each ['A', 'B', 'C', 'D'] as opt}
            {#if question['option' + opt]}
              <button
                class="option {optionClass(opt)}"
                class:shake={shakeTarget === opt}
                class:burst-glow={burstTarget === opt}
                on:click={() => selectOption(opt)}
                disabled={isLocked}
              >
                <span class="opt-letter">{opt}</span>
                <span>{question['option' + opt]}</span>
                {#if burstTarget === opt}
                  <span class="burst-ring"></span>
                {/if}
              </button>
            {/if}
          {/each}
        </div>

        {#if showResult}
          <div
            class="result-feedback"
            class:correct={lastCorrect}
            class:wrong={!lastCorrect}
            in:fly={{ y: 10, duration: 300, easing: quintOut }}
          >
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
    {/key}
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

  .timer-ring {
    position: relative;
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
  }
  .timer-ring.urgent {
    animation: ring-heartbeat 0.8s ease-in-out infinite;
  }
  @keyframes ring-heartbeat {
    0%, 100% { transform: scale(1); }
    25% { transform: scale(1.06); }
    50% { transform: scale(1); }
    75% { transform: scale(1.06); }
  }
  .ring-svg {
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
  }
  .timer-text {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    color: var(--text-primary);
    transition: color 0.3s ease;
  }
  .urgent-text {
    color: var(--accent-red);
  }

  .question-card { padding: 32px; }
  .q-num { font-size: 12px; color: var(--text-secondary); }
  .question-card h2 { margin: 12px 0 24px; font-size: 18px; line-height: 1.6; }

  .options { display: flex; flex-direction: column; gap: 10px; }
  .option {
    position: relative;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 18px;
    background: rgba(255,255,255,0.04);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-xl);
    color: var(--text-primary);
    font-size: 14px;
    cursor: pointer;
    transition: background 0.2s ease, border-color 0.2s ease,
                transform 0.35s var(--ease-spring-bouncy);
    text-align: left;
    overflow: hidden;
  }
  .option:hover:not(:disabled) {
    background: rgba(255,255,255,0.08);
    border-color: rgba(255,255,255,0.2);
    transform: scale(1.01);
  }
  .option:disabled { cursor: default; }

  /* ─── Shake animation for wrong answers ─── */
  .option.shake {
    animation: shake 0.5s var(--ease-spring-damped);
  }

  /* ─── Burst glow ring for correct answers ─── */
  .burst-ring {
    position: absolute;
    inset: -2px;
    border-radius: inherit;
    border: 2px solid var(--accent-green);
    animation: correct-glow 0.7s ease-out forwards;
    pointer-events: none;
  }

  .opt-letter {
    width: 28px; height: 28px;
    display: flex; align-items: center; justify-content: center;
    background: rgba(255,255,255,0.08);
    border-radius: var(--radius-lg);
    font-weight: 600; font-size: 13px;
    transition: background 0.2s ease;
  }
  .option.correct {
    background: rgba(100,200,150,0.15);
    border-color: var(--accent-green);
  }
  .option.correct .opt-letter {
    background: rgba(100,200,150,0.25);
  }
  .option.wrong {
    background: rgba(255,100,100,0.15);
    border-color: var(--accent-red);
  }
  .option.wrong .opt-letter {
    background: rgba(255,100,100,0.25);
  }
  .option.dimmed { opacity: 0.35; }

  .result-feedback {
    margin-top: 20px; padding: 16px; border-radius: var(--radius-xl); font-weight: 600;
  }
  .result-feedback.correct { background: rgba(100,200,150,0.1); color: var(--accent-green); }
  .result-feedback.wrong { background: rgba(255,100,100,0.1); color: var(--accent-red); }
  .explanation { font-weight: 400; margin-top: 8px; font-size: 13px; color: var(--text-secondary); }

  .btn-primary {
    width: 100%; padding: 14px; margin-top: 20px;
    background: linear-gradient(135deg, var(--accent-blue), var(--accent-purple));
    border: none; border-radius: var(--radius-xl); color: white; font-size: 15px;
    font-weight: 600; cursor: pointer;
    transition: transform 0.35s var(--ease-spring-bouncy),
                box-shadow 0.3s ease;
  }
  .btn-primary:hover {
    transform: scale(1.02);
    box-shadow: 0 8px 24px rgba(100, 180, 255, 0.2);
  }
  .loading { text-align: center; padding: 80px; color: var(--text-secondary); }
</style>
