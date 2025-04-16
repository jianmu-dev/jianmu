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
