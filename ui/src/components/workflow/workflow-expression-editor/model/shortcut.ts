import CodeMirror, { Editor } from 'codemirror';

/**
 * 计算起始节点空格数量
 * @param str
 * @private
 */
function calcStartSpaceAmount(str: string) {
  for (let amount = 1; amount <= str.length; amount++) {
    let spaces = '';
    for (let j = 0; j < amount; j++) {
      spaces += ' ';
    }

    if (spaces !== str.substr(0, amount)) {
      return amount - 1;
    }

    if (amount === str.length) {
      return amount;
    }
  }
  return 0;
}

/**
 * 注释
 */
export class Comment {

  static get shortcut(): string {
    const { keyMap: { default: _default, macDefault } } = (window.CodeMirror || CodeMirror) as any;
    return (_default === macDefault ? 'Cmd' : 'Ctrl') + '-/';
  }

  static command(cm: Editor): void {
    const symbol = Comment.symbol(cm);
    const doc = cm.getDoc();
    const { anchor, head } = doc.listSelections()[0];

    const startLine = head.line < anchor.line ? head.line : anchor.line;
    const endLine = head.line > anchor.line ? head.line : anchor.line;

    const lineVals = cm.getValue().split(cm.lineSeparator());
    const anchorOriVal = lineVals[anchor.line];
    const headOriVal = lineVals[head.line];

    for (let i = startLine; i <= endLine; i++) {
      const lineVal = lineVals[i];
      const spaceAmount = calcStartSpaceAmount(lineVal);
      const spaceStr = lineVal.substr(0, spaceAmount);
      const noSpaceStr = lineVal.substring(spaceAmount);
      const commentRemoveFlag = noSpaceStr.startsWith(symbol);
      lineVals[i] = commentRemoveFlag ?
        (spaceStr + noSpaceStr.substring(symbol.length + (noSpaceStr.startsWith(`${symbol} `) ? 1 : 0)))
        :
        (spaceStr + `${symbol} ` + noSpaceStr);
    }

    anchor.ch += (lineVals[anchor.line].length - anchorOriVal.length);
    head.ch += (anchor === head ? 0 : 1) * (lineVals[head.line].length - headOriVal.length);

    cm.setValue(lineVals.join(cm.lineSeparator()));
    doc.setSelection(anchor, head);

    const { scrollLeft, scrollTop } = doc as any;
    setTimeout(() => cm.scrollTo(scrollLeft, scrollTop));
  }

  private static symbol(cm: Editor): string {
    switch (cm.getMode().name) {
      case 'javascript':
        return '//';
      case 'python':
      case 'yaml':
        return '#';
    }

    return '';
  }
}

/**
 * 制表符
 */
export class Tab {

  static get shortcut(): string {
    return 'Tab';
  }

  static command(cm: Editor): void {
    if (cm.somethingSelected()) {
      cm.indentSelection('add');
      return;
    }

    cm.replaceSelection(Array(cm.getOption('indentUnit')! + 1).join(' '));
  }
}
