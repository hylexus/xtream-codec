import { useMemo } from 'react';
import CodeMirror, { EditorView, Prec } from '@uiw/react-codemirror';
import { json } from '@codemirror/lang-json';
import { autocompletion, type CompletionContext } from '@codemirror/autocomplete';
import { completeDefinition } from '../completion.js';
import { schemaDocument } from '../model';

interface Props {
  value: string;
  onChange?: (text: string) => void;
  theme: 'dark' | 'light';
  label: string;
  language?: 'json' | 'hex';
  placeholder?: string;
  schema?: boolean;
  height?: string;
  readOnly?: boolean;
}

export function CodeEditor({
  value,
  onChange,
  theme,
  label,
  language = 'json',
  placeholder,
  schema = false,
  height = '220px',
  readOnly = false,
}: Props) {
  const extensions = useMemo(() => {
    const editorTheme = EditorView.theme(
      {
        '&': {
          backgroundColor: 'transparent',
          color: 'var(--studio-text)',
          fontSize: 'var(--studio-font-body)',
        },
        '.cm-content': {
          padding: '12px 0',
          caretColor: 'var(--studio-text)',
          color: language === 'hex' ? 'var(--studio-amber)' : 'var(--studio-text)',
        },
        '.cm-placeholder': { color: 'var(--studio-subtle)' },
        '.cm-gutters': {
          backgroundColor: 'var(--studio-input)',
          color: 'var(--studio-subtle)',
          border: 'none',
          borderRight:
            theme === 'light'
              ? '1px solid var(--studio-section-border)'
              : '1px solid var(--studio-guide)',
        },
        '.cm-activeLine': { backgroundColor: 'var(--studio-hover)' },
        '.cm-activeLineGutter': {
          backgroundColor: 'var(--studio-hover)',
          color: 'var(--studio-text)',
        },
        '.cm-scroller': { fontFamily: 'var(--font-mono)', lineHeight: '1.6' },
        '&.cm-focused': { outline: 'none' },
        '.cm-selectionBackground': {
          backgroundColor: 'color-mix(in srgb, var(--studio-accent) 24%, transparent) !important',
        },
        '.cm-cursor': { borderLeftColor: 'var(--studio-text)' },
      },
      { dark: theme === 'dark' },
    );
    const result = [
      ...(language === 'json' ? [json(), EditorView.lineWrapping] : []),
      Prec.highest(editorTheme),
      EditorView.contentAttributes.of({ 'aria-label': label }),
    ];
    if (schema && language === 'json')
      result.push(
        autocompletion({
          override: [
            (context: CompletionContext) => {
              const candidates = completeDefinition(
                context.state.doc.toString(),
                context.pos,
                schemaDocument,
              );
              if (!candidates.length) return null;
              return {
                from: candidates[0].from,
                to: candidates[0].to,
                options: candidates.map((item) => ({ label: item.label, apply: item.text })),
              };
            },
          ],
        }),
      );
    return result;
  }, [theme, schema, label, language]);
  return (
    <div className="code-editor">
      <CodeMirror
        value={value}
        onChange={onChange}
        extensions={extensions}
        height={height}
        readOnly={readOnly}
        placeholder={placeholder}
        basicSetup={{
          foldGutter: language === 'json',
          autocompletion: language === 'json',
          bracketMatching: language === 'json',
          closeBrackets: language === 'json',
          indentOnInput: language === 'json',
          highlightActiveLine: !readOnly,
          highlightActiveLineGutter: !readOnly,
        }}
        theme={theme === 'dark' ? 'dark' : 'light'}
      />
    </div>
  );
}
