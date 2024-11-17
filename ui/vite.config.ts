import { ConfigEnv, UserConfigExport } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import { name, version } from './package.json';
const target = 'http://localhost:8081';
const changeOrigin = true;
// https://vitejs.dev/config/
export default ({ command, mode }: ConfigEnv): UserConfigExport => {
  return {
    plugins: [vue()],
    // base public path
    base: command === 'build' && mode === 'cdn' ? `https://jianmu-ci.assets.dghub.cn/${name}/v${version}/` : '/',
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
        // 解决：
        // Component provided template option but runtime compilation is not supported in this build of Vue
        // Configure your bundler to alias "vue" to "vue/dist/vue.esm-bundler.js".
        vue: 'vue/dist/vue.esm-bundler.js',
      },
    },
    server: {
      // 配置服务端代理
      proxy: {
        // 会话
        '/auth': { target, changeOrigin },
        // worker
        '/frontend/workers': { target, changeOrigin },
        // 密钥管理
        '/secrets': { target, changeOrigin },
        // 流程定义
        '/projects': { target, changeOrigin },
        '/git': { target, changeOrigin },
        '/webhook': { target, changeOrigin },
        // 流程执行中心
        '/workflow_instances': { target, changeOrigin },
        '/logs': { target, changeOrigin },
        // 查询
        '/view': { target, changeOrigin },
        // 节点库
        '/library': { target, changeOrigin },
        // 触发器
        '/trigger': { target, changeOrigin },
        // '/xxx': {
        //   target: 'http://xxx.xxx.xxx.xxx',
        //   // 发送请求头中，host会设置成target
        //   changeOrigin: true,
        // },
      },
    },
  };
};
