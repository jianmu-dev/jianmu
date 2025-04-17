import httpStatusVue from '@/views/error/http-status.vue';
import { createI18n } from 'vue-i18n';

const messages = {
  zh: {
    noData: 'No data',
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
    network: {
      title: '你的设备没有接入互联网',
      desc: '无法显示此页面，因为你的电脑目前已离线',
      backHome: '返回首页',
    },
    browserVersion: {
      unsupported: '抱歉，不支持此浏览器',
      backHome: '返回首页',
    },
    httpStatus: {
      backHome: '返回首页',
    },
    nodeLibrary: {
      addLocalNode: '新增本地节点',
      name: '名称',
      inputName: '请输入名称',
      description: '描述',
      inputDescription: '请输入描述',
      note: '注意事项：ref为xxx/xxx或xxx时，统一处理成local/xxx，在流程或管道dsl中以local/xxx:xxx方式引用。',
      cancel: '取消',
      confirm: '确定',
      close: '关闭',
      nodeLibraryTitle: '建木节点库',
      nodeLibraryCount: '（共有 { count } 个节点定义）',
      tooltipDeprecated:
        '由于某些原因，该节点不被推荐使用（如该节点可<br/>能会导致一些已知问题或有更好的节点可替代它）',
      sync: '同步',
      delete: '删除',
      nameRequired: '名称不能为空',
      dslRequired: 'DSL不能为空',
      createSuccess: '新增成功',
      deleteNodeTitle: '删除节点',
      deleteConfirm: '确定要删除节点吗?',
      deleteSuccess: '删除成功',
      syncTitle: '同步DSL',
      syncConfirm: '确定要同步吗?',
      syncSuccess: '同步成功',
    },
    editor: {
      cancel: '取消',
      previousStep: '上一步',
      saveAndBack: '保存并返回',
      save: '保存',
      selectProjectGroup: '选择项目组',
      pleaseSelectProjectGroup: '请选择项目组',
      dslMode: 'DSL模式',
      validationProjectGroup: '请选择项目组',
      dslEmptyError: 'DSL不能为空',
      saveSuccess: '保存成功',
      createSuccess: '新增成功',
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
    network: {
      title: 'Your device is not connected to the internet',
      desc: 'This page cannot be displayed because your computer is currently offline',
      backHome: 'Back to Home',
    },
    browserVersion: {
      unsupported: 'Sorry, this browser is not supported',
      backHome: 'Back to Home',
    },
    httpStatus: {
      backHome: 'Back to Home',
    },
    nodeLibrary: {
      addLocalNode: 'Add Local Node',
      name: 'Name',
      inputName: 'Please enter name',
      description: 'Description',
      inputDescription: 'Please enter description',
      note: 'Note: When ref is xxx/xxx or xxx, it is uniformly handled as local/xxx and referenced as local/xxx:xxx in the process or pipeline dsl.',
      cancel: 'Cancel',
      confirm: 'Confirm',
      close: 'Close',
      nodeLibraryTitle: 'Jianmu Node Library',
      nodeLibraryCount: '(Total { count } node definitions)',
      tooltipDeprecated: 'This node is not recommended due to known issues or a better alternative',
      sync: 'Sync',
      delete: 'Delete',
      nameRequired: 'Name cannot be empty',
      dslRequired: 'DSL cannot be empty',
      createSuccess: 'Created successfully',
      deleteNodeTitle: 'Delete Node',
      deleteConfirm: 'Are you sure you want to delete the node?',
      deleteSuccess: 'Deleted successfully',
      syncTitle: 'Sync DSL',
      syncConfirm: 'Are you sure you want to sync?',
      syncSuccess: 'Synced successfully',
    },
    editor: {
      cancel: 'Cancel',
      previousStep: 'Previous Step',
      saveAndBack: 'Save and Return',
      save: 'Save',
      selectProjectGroup: 'Select Project Group',
      pleaseSelectProjectGroup: 'Please select a project group',
      dslMode: 'DSL Mode',
      validationProjectGroup: 'Please select a project group',
      dslEmptyError: 'DSL cannot be empty',
      saveSuccess: 'Saved successfully',
      createSuccess: 'Created successfully',
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
