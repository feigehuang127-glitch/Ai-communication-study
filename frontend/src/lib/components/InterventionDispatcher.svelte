<script lang="ts">
  import { onMount } from 'svelte';
  import { user } from '$lib/stores/auth';
  import { behaviorEngine } from '$lib/behavior/BehaviorEngine';
  import { interventions } from '$lib/stores/intervention';
  import type { RuleConfig } from '$lib/behavior/rules';

  const SEVERITY_RANK: Record<string, number> = {
    info: 1,
    warning: 2,
    critical: 3,
  };

  const SENSITIVITY_THRESHOLD: Record<string, number> = {
    low: 3,
    medium: 1,
    high: 1,
  };

  onMount(() => {
    behaviorEngine.onIntervention('hesitate_on_answer', handleRule);
    behaviorEngine.onIntervention('stuck_on_page', handleRule);
    behaviorEngine.onIntervention('about_to_quit', handleRule);
    behaviorEngine.onIntervention('mastery_detected', handleRule);
  });

  function handleRule(rule: RuleConfig) {
    const sensitivity = $user?.aiSensitivity || 'medium';
    const threshold = SENSITIVITY_THRESHOLD[sensitivity] || 1;
    const severity = rule.intervention.severity || 'info';

    if (SEVERITY_RANK[severity] < threshold) return;

    interventions.push({
      id: rule.id + '_' + Date.now(),
      ruleId: rule.id,
      severity,
      title: rule.intervention.title,
      message: rule.intervention.message,
      actionLabel: rule.intervention.actionLabel,
      actionPage: rule.intervention.actionPage,
      timestamp: Date.now(),
      dismissed: false,
    });
  }
</script>
