import { ConfigEnv, UserConfigExport } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import { name, version } from './package.json';

const target = 'http://172.16.101.231';
const changeOrigin = true;
// https://vitejs.dev/config/
export default ({ command, mode }: ConfigEnv): UserConfigExport => {
  let base = '/';
  if (command === 'build') {
    switch (mode) {
      case 'cdn':
        base = `https://jianmu-saas.assets.dghub.cn/${name}/v${version}/`;
        break;
    }
  }

  return {
    plugins: [vue()],
    // base public path
    base,
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
        '/jianmu_saas': { target, changeOrigin },
      },
    },
  };
};
