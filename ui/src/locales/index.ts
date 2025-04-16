import { createI18n } from 'vue-i18n';

const messages = {
  zh: {
    top: {
      logout: '退出',
      language: '语言设置',
    },
    bottom: {
      project: '木兰社区孵化项目',
      about: '关于建木',
      manual: '使用手册',
      contact: '联系我们',
    },
    dialog: {
      needLoginTip: '未登录状态下，操作内容将会丢失',
    },
    allProject: {
      projectList: '项目列表',
      inputPlaceholder: '请输入项目名称',
      empty: '暂无项目',
      default: '默认排序',
      lastExecute: '最近触发',
      lastModify: '最近修改',
    },
    searchProject: {
      selectGroup: '请选择项目组',
      inputName: '请输入项目名称',
    },
  },

  en: {
    top: {
      logout: 'Logout',
      language: 'Language',
    },
    bottom: {
      project: 'Mulan Incubator',
      about: 'About Jianmu',
      manual: 'Documentation',
      contact: 'Contact Us',
    },
    dialog: {
      needLoginTip: 'The contents of the operation will be lost if you are not logged in',
    },
    allProject: {
      projectList: 'Project List',
      inputPlaceholder: 'Please enter project name',
      empty: 'No Projects',
      default: 'Default Sort',
      lastExecute: 'Recently triggered',
      lastModify: 'Recent changes',
    },
    searchProject: {
      selectGroup: 'Please select a project group',
      inputName: 'Please enter project name',
    },
  },
};

const getDefaultLocale = () => {
  const saved = localStorage.getItem('JianmuLang');
  if (saved) return saved;

  const browserLang = navigator.language.split('-')[0];
  const supportedLangs = ['zh', 'en'];
  const lang = supportedLangs.includes(browserLang) ? browserLang : 'zh';
  localStorage.setItem('JianmuLang', lang);
  return lang;
};

const i18n = createI18n({
  locale: getDefaultLocale(),
  fallbackLocale: 'zh',
  messages,
});

export default i18n;
