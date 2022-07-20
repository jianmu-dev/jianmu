// 引入全局实例
import CodeMirror, { Editor, EditorFromTextArea } from 'codemirror';
import { Comment, Tab } from './shortcut';

// 核心样式
import 'codemirror/lib/codemirror.css';
// 引入主题后，还需要在 options 中指定主题才会生效
import 'codemirror/theme/idea.css';

// 需要引入具体的语法高亮库才会有对应的语法高亮效果
// codemirror 官方其实支持通过 /addon/mode/loadmode.js 和 /mode/meta.js 来实现动态加载对应语法高亮库
// 但 vue 貌似没有无法在实例初始化后再动态加载对应 JS ，所以此处才把对应的 JS 提前引入
import 'codemirror/mode/javascript/javascript';

// placeholder插件
import 'codemirror/addon/display/placeholder';

// 尝试获取全局实例
const codemirror = window.CodeMirror || CodeMirror;

type EventCallbackFnType = (e: Event) => void;

export class ExpressionEditor {
  private readonly instance: EditorFromTextArea;

  constructor(textareaEl: HTMLTextAreaElement, placeholder: string,
    focusCallbackFn: EventCallbackFnType, blurCallbackFn: EventCallbackFnType) {

    this.instance = codemirror.fromTextArea(textareaEl, {
      placeholder,
      // 模式
      mode: 'javascript',
      // 缩进格式
      tabSize: 2,
      // 主题，对应主题库 JS 需要提前引入
      theme: 'idea',
      // 强制换行
      lineWrapping: true,
      // 显示行号
      lineNumbers: true,
      // 是否只读
      readOnly: false,
      // 快捷键
      extraKeys: {
        // 注释
        [Comment.shortcut]: Comment.command,
        // 制表符
        [Tab.shortcut]: Tab.command,
      },
    });

    this.instance.on('focus',
      (_: Editor, event: FocusEvent) => focusCallbackFn(event));
    this.instance.on('blur',
      (_: Editor, event: FocusEvent) => blurCallbackFn(event));
    // Copy the content of the editor into the textarea.
    this.instance.on('change', () => this.instance.save());
  }

  insertParam(arr: string[]): void {
    this.instance.replaceSelection(arr.join('.'));
  }
}