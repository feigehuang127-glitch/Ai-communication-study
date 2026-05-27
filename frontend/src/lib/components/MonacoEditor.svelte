<script lang="ts">
  import { onMount, onDestroy, createEventDispatcher } from 'svelte';

  export let language: string = 'python';
  export let value: string = '';
  export let theme: string = 'vs-dark';
  export let readonly: boolean = false;

  const dispatch = createEventDispatcher<{ change: string; ready: void }>();

  let containerEl: HTMLDivElement;
  let editor: import('monaco-editor').editor.IStandaloneCodeEditor | null = null;
  let monacoInstance: typeof import('monaco-editor') | null = null;
  let observer: ResizeObserver | null = null;
  let disposing = false;

  // Sync external value into editor (one-way from parent)
  $: if (editor && !disposing) {
    const current = editor.getValue();
    if (value !== current) {
      editor.setValue(value);
    }
  }

  // Switch language on the existing model
  $: if (editor && monacoInstance && !disposing) {
    const model = editor.getModel();
    if (model) {
      monacoInstance.editor.setModelLanguage(model, language);
    }
  }

  // Toggle readonly
  $: if (editor && !disposing) {
    editor.updateOptions({ readOnly: readonly });
  }

  onMount(async () => {
    // Load worker constructors via Vite's ?worker import
    const [editorWorker, tsWorker, jsonWorker, cssWorker, htmlWorker] =
      await Promise.all([
        import('monaco-editor/esm/vs/editor/editor.worker?worker'),
        import('monaco-editor/esm/vs/language/typescript/ts.worker?worker'),
        import('monaco-editor/esm/vs/language/json/json.worker?worker'),
        import('monaco-editor/esm/vs/language/css/css.worker?worker'),
        import('monaco-editor/esm/vs/language/html/html.worker?worker'),
      ]);

    (self as any).MonacoEnvironment = {
      getWorker(_workerId: string, label: string) {
        switch (label) {
          case 'json':
            return new (jsonWorker as any).default();
          case 'css':
          case 'scss':
          case 'less':
            return new (cssWorker as any).default();
          case 'html':
          case 'handlebars':
          case 'razor':
            return new (htmlWorker as any).default();
          case 'typescript':
          case 'javascript':
            return new (tsWorker as any).default();
          default:
            return new (editorWorker as any).default();
        }
      },
    };

    const monaco = await import('monaco-editor');
    monacoInstance = monaco;

    editor = monaco.editor.create(containerEl, {
      value,
      language,
      theme,
      readOnly: readonly,
      minimap: { enabled: false },
      fontSize: 13,
      fontFamily: "'Fira Code', 'Cascadia Code', Consolas, monospace",
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      automaticLayout: false,
      tabSize: 4,
      padding: { top: 12 },
    });

    editor.onDidChangeModelContent(() => {
      const v = editor!.getValue();
      if (v !== value) {
        value = v;
        dispatch('change', v);
      }
    });

    observer = new ResizeObserver(() => {
      editor?.layout();
    });
    observer.observe(containerEl);

    dispatch('ready');
  });

  onDestroy(() => {
    disposing = true;
    observer?.disconnect();
    editor?.dispose();
    editor = null;
  });
</script>

<div bind:this={containerEl} class="monaco-container"></div>

<style>
  .monaco-container {
    width: 100%;
    height: 300px;
    border-radius: 10px;
    overflow: hidden;
    border: 1px solid var(--glass-border);
  }
</style>
