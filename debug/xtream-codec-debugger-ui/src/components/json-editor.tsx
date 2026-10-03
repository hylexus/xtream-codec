import { useMemo } from 'react';
import CodeMirror, { EditorView } from '@uiw/react-codemirror';
import { json } from '@codemirror/lang-json';
import { autocompletion, type CompletionContext } from '@codemirror/autocomplete';
import { completeDefinition } from '../completion.js';
import { schemaDocument } from '../model';

interface Props {
  value: string;
  onChange?: (text: string) => void;
  theme: 'dark' | 'light';
  label: string;
  schema?: boolean;
  height?: string;
  readOnly?: boolean;
}

export function JsonEditor({
  value,
  onChange,
  theme,
  label,
  schema = false,
  height = '220px',
  readOnly = false,
}: Props) {
  const extensions = useMemo(() => {
    const colors =
      theme === 'dark'
        ? { text: '#d8deee', gutter: '#5b6581', line: '#ffffff05' }
        : { text: '#26334d', gutter: '#8993a7', line: '#00000003' };
    const editorTheme = EditorView.theme(
      {
        '&': {
          backgroundColor: 'transparent',
          color: colors.text,
          fontSize: 'var(--studio-font-body)',
        },
        '.cm-content': { padding: '12px 0', caretColor: colors.text },
        '.cm-gutters': { backgroundColor: 'transparent', color: colors.gutter, border: 'none' },
        '.cm-activeLine': { backgroundColor: colors.line },
        '.cm-activeLineGutter': { backgroundColor: 'transparent' },
        '.cm-scroller': { fontFamily: 'var(--font-mono)', lineHeight: '1.6' },
        '&.cm-focused': { outline: 'none' },
        '.cm-selectionBackground': {
          backgroundColor: 'color-mix(in srgb, var(--studio-accent) 24%, transparent) !important',
        },
        '.cm-cursor': { borderLeftColor: colors.text },
      },
      { dark: theme === 'dark' },
    );
    const result = [
      json(),
      EditorView.lineWrapping,
      editorTheme,
      EditorView.contentAttributes.of({ 'aria-label': label }),
    ];
    if (schema)
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
  }, [theme, schema, label]);
  return (
    <div className="json-editor">
      <CodeMirror
        value={value}
        onChange={onChange}
        extensions={extensions}
        height={height}
        readOnly={readOnly}
        basicSetup={{
          foldGutter: true,
          highlightActiveLine: !readOnly,
          highlightActiveLineGutter: false,
        }}
        theme={theme === 'dark' ? 'dark' : 'light'}
      />
    </div>
  );
}
