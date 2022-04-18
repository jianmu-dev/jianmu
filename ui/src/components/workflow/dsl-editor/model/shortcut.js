import CodeMirror from 'codemirror';

/**
 * 计算起始节点空格数量
 * @param str
 * @private
 */
function calcStartSpaceAmount(str) {
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

  static get shortcut() {
    const codemirror = window.CodeMirror || CodeMirror;
    return (codemirror.keyMap.default === codemirror.keyMap.macDefault ? 'Cmd' : 'Ctrl') + '-/';
  }

  static command(cm) {
    const doc = cm.getDoc();
    const { anchor, head } = doc.sel.ranges[0];

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
      const commentRemoveFlag = noSpaceStr.startsWith('#');
      lineVals[i] = commentRemoveFlag ?
        (spaceStr + noSpaceStr.substring(noSpaceStr.startsWith('# ') ? 2 : 1))
        :
        (spaceStr + '# ' + noSpaceStr);
    }

    anchor.ch += (lineVals[anchor.line].length - anchorOriVal.length);
    head.ch += (anchor === head ? 0 : 1) * (lineVals[head.line].length - headOriVal.length);

    cm.setValue(lineVals.join(cm.lineSeparator()));
    doc.setSelection(anchor, head);

    const { scrollLeft, scrollTop } = cm.doc;
    setTimeout(() => cm.scrollTo(scrollLeft, scrollTop));
  }
}

/**
 * 制表符
 */
export class Tab {

  static get shortcut() {
    return 'Tab';
  }

  static command(cm) {
    if (cm.somethingSelected()) {
      cm.indentSelection('add');
      return;
    }

    cm.replaceSelection(Array(cm.getOption('indentUnit') + 1).join(' '));
  }
}

/**
 * 查找
 */
export class Find {

  static get shortcut() {
    const codemirror = window.CodeMirror || CodeMirror;
    return (codemirror.keyMap.default === codemirror.keyMap.macDefault ? 'Cmd' : 'Ctrl') + '-F';
  }

  static command(cm) {
    cm.execCommand('findPersistent');
  }
}
