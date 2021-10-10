import { ComponentPublicInstance, createApp } from 'vue';
import App from './app.vue';
import router from './router';
import components from './components';
// 设置全局样式
import './global.less';
import store from './store';
import { globalErrorHandler } from './utils/global-error-handler';
import './utils/operation-btn-position.ts';

const app = createApp(App);
// 全局注册公共组件
app.use(components);
// 注册路由器
app.use(router);
// 安装vuex
app.use(store);

// 注册全局异常处理方法
app.config.errorHandler = (err, instance, info) =>
  globalErrorHandler(err as Error, instance, info, router, store);
app.config.globalProperties.$throw = (err: Error, instance: ComponentPublicInstance | null) =>
  globalErrorHandler(err, instance, null, router, store);

app.mount('#app');