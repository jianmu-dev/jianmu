module.exports = {
  // 一行最多 120 字符<int>
  printWidth: 120,
  // 使用 2 个空格缩进<int>
  tabWidth: 2,
  // 不使用缩进符，而使用空格<bool>
  useTabs: false,
  // 行尾需要有分号<bool>
  semi: true,
  // 使用单引号<bool>
  singleQuote: true,
  // 对象的 key 仅在必要时用引号"<as-needed|consistent|preserve>"
  quoteProps: 'as-needed',
  // jsx 不使用单引号，而使用双引号<bool>
  jsxSingleQuote: false,
  // 末尾需要逗号"<es5|none|all>"
  trailingComma: 'all',
  // 大括号内的首尾需要空格<bool>
  bracketSpacing: true,
  // jsx 标签的反尖括号需要换行<bool>
  jsxBracketSameLine: false,
  // 箭头函数，只有一个参数的时候，需要括号<always|avoid>
  arrowParens: 'avoid',
  // 每个文件格式化的范围是文件的全部内容<int>
  rangeStart: 0,
  rangeEnd: Infinity,
  // 不需要写文件开头的 @prettier <bool>
  requirePragma: false,
  // 不需要自动在文件开头插入 @prettier <bool>
  insertPragma: false,
  // 使用默认的折行标准<always|never|preserve>
  proseWrap: 'preserve',
  // 根据显示样式决定 html 要不要折行<css|strict|ignore>
  htmlWhitespaceSensitivity: 'css',
  // 换行符使用 lf<lf|crlf|cr|auto>
  endOfLine: 'lf',
  // 在HTML、Vue和JSX中每行强制一个属性<bool>
  singleAttributePerLine: false,
};
