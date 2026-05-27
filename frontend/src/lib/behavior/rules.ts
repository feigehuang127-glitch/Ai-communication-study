export interface RuleConfig {
  id: string;
  name: string;
  conditions: Record<string, unknown>;
  intervention: {
    type: 'toast' | 'inline_card' | 'sidebar' | 'title_flash';
    severity?: 'info' | 'warning' | 'critical';
    title?: string;
    message: string;
    actionLabel?: string;
    actionPage?: string;
  };
}

export const DEFAULT_RULES: RuleConfig[] = [
  {
    id: 'hesitate_on_answer',
    name: '答题犹豫',
    conditions: {
      any: [
        { fact: 'answerLatency', operator: 'greaterThan', value: 8000 },
        { fact: 'optionChanges', operator: 'greaterThanInclusive', value: 2 },
      ],
    },
    intervention: {
      type: 'inline_card',
      severity: 'info',
      title: '需要帮助吗？',
      message: '看起来这题有点纠结，需要看个小提示吗？',
      actionLabel: '查看提示',
    },
  },
  {
    id: 'stuck_on_page',
    name: '页面停留过久',
    conditions: {
      all: [
        { fact: 'dwellTime', operator: 'greaterThan', value: 120000 },
        { fact: 'scrollDepth', operator: 'lessThan', value: 30 },
      ],
    },
    intervention: {
      type: 'sidebar',
      severity: 'warning',
      message: '我注意到你在这块停留很久，帮你总结一下核心概念？',
    },
  },
  {
    id: 'about_to_quit',
    name: '即将流失',
    conditions: {
      any: [
        { fact: 'tabHidden', operator: 'greaterThan', value: 60 },
        { fact: 'inactivityMs', operator: 'greaterThan', value: 90000 },
      ],
    },
    intervention: {
      type: 'title_flash',
      severity: 'critical',
      message: '还有3题就升级了，确定要走？',
    },
  },
  {
    id: 'mastery_detected',
    name: '掌握检测',
    conditions: {
      all: [
        { fact: 'streak', operator: 'greaterThanInclusive', value: 5 },
        { fact: 'avgLatency', operator: 'lessThan', value: 3000 },
      ],
    },
    intervention: {
      type: 'inline_card',
      severity: 'info',
      title: '你学得很快！',
      message: '当前难度你已经掌握了，试试跳级挑战？',
      actionLabel: '跳级测试',
    },
  },
];
