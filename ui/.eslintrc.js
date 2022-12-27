/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution');

module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es6: true,
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript/recommended',
    '@vue/eslint-config-prettier',
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
    parser: '@typescript-eslint/parser',
  },
  ignorePatterns: ['auto-imports.d.ts', 'components.d.ts'],
  rules: {
    /**
     * “off” or 0 - 关闭(禁用)规则
     * “warn” or 1 - 将规则视为一个警告（并不会导致检查不通过）
     * “error” or 2 - 将规则视为一个错误 (退出码为1，检查不通过)
     */
    'prettier/prettier': 0,
    // 缩进风格
    indent: [1, 2, { SwitchCase: 1 }],
    // 禁止修改const声明的变量
    'no-const-assign': 2,
    // 花括号开始{ 和 }结束前要有空格
    'object-curly-spacing': [1, 'always'],
    // 空行最多不能超过2行
    'no-multiple-empty-lines': [1, { max: 2 }],
    // 在逗号后使用一个或多个空格 after:false禁止在逗号后使用空格.
    'comma-spacing': [1, { after: true }],
    // 语句末尾是否需要分号
    semi: [1, 'always'],
    // 在创建对象字面量时不允许键重复 {a:1,a:1}
    'no-dupe-keys': 2,
    // 函数参数不能重复
    'no-dupe-args': 2,
    // 命名检测
    'id-match': 2,
    // 强制 行注释放到代码上方
    'line-comment-position': [1, { position: 'above' }],
    // 注释 // /*前后有一个空格
    'spaced-comment': [1, 'always'],
    // 使用 === 替代 ==
    eqeqeq: [2, 'always'],
    // 不能有声明后未被使用的变量或参数
    'no-unused-vars': 0,
    '@typescript-eslint/no-unused-vars': [1, { vars: 'all', args: 'after-used' }],
    // 未定义前不能使用
    'no-use-before-define': 2,
    // 不能有无法执行的代码
    'no-unreachable': 2,
    // 不能使用未定义的变量
    'no-undef': 2,
    // 禁止在 return 语句中使用赋值语句
    'no-return-assign': 2,
    // 数组和对象键值对最后一个逗号， never参数：不能带末尾的逗号, always参数：必须带末尾的逗号，
    // always-multiline：多行模式必须带逗号，单行模式不能带逗号
    'comma-dangle': [1, 'always-multiline'],
    // 强制行的最大长度
    'max-len': [
      1,
      {
        // 强制换行的最大长度
        code: 200,
        // 忽略含有链接的行
        ignoreUrls: true,
        // 忽略含有单双引号的行
        ignoreStrings: true,
        // 忽略包含摸板字面量的行
        ignoreTemplateLiterals: true,
        // 忽略包含正则表达式的行
        ignoreRegExpLiterals: true,
      },
    ],
    // 尽可能地使用单引号
    quotes: [1, 'single'],
    // 箭头函数只有一个参数，省略括号
    'arrow-parens': [
      1,
      'as-needed',
      {
        requireForBlockBody: false,
      },
    ],
    // 禁止使用console
    'no-console': process.env.NODE_ENV === 'production' ? 2 : 0,
    // 禁止使用debugger
    'no-debugger': process.env.NODE_ENV === 'production' ? 2 : 0,
    // interface为空对象不报错
    '@typescript-eslint/no-empty-interface': 0,
    // 解决.vue文件多个单词命名的报错提示
    'vue/multi-word-component-names': 'off',
  },
};
