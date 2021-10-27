<template>
  <div class="platform">
    <div class="header"></div>
    <jm-container class="container">
      <jm-header height="64px">
        <top-nav/>
      </jm-header>
      <jm-container>
        <jm-header v-if="pathNavsDisplay" class="path-nav">
          <jm-breadcrumb>
            <jm-breadcrumb-item v-for="{name, path} in pathNavs" :key="path" :to="path">{{ name }}</jm-breadcrumb-item>
          </jm-breadcrumb>
        </jm-header>
        <jm-main class="main" id="platform-main">
          <jm-scrollbar >
            <template v-if="loadMain">
              <router-view v-slot="{ Component }">
                <keep-alive :include="bufferList">
                  <component :is="Component"></component>
                </keep-alive>
              </router-view>
            </template>
          </jm-scrollbar>
        </jm-main>
      </jm-container>
    </jm-container>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, provide, reactive, Ref, ref } from 'vue';
import { onBeforeRouteUpdate, RouteLocationNormalized, RouteLocationNormalizedLoaded, useRoute } from 'vue-router';
import TopNav from '@/views/nav/top.vue';
import { adaptHeight, IAutoHeight } from '@/utils/auto-height';
import { PLATFORM_INDEX } from '@/router/path-def';
const autoHeight: IAutoHeight = {
  elementId: 'platform-main',
  offsetTop: 0,
};

interface IPathNav {
  name: string;
  path: string;
}

function buildPathNav(pathNavs: Ref<IPathNav[]>, route: RouteLocationNormalizedLoaded | RouteLocationNormalized) {
  pathNavs.value.length = 0;

  route.matched.forEach(item => {
    if (!item.meta.title) {
      console.warn('尚未定义title，请检查', item);

      return;
    }

    pathNavs.value.push({
      name: item.meta.title as string,
      path: item.path,
    });
  });
}

export default defineComponent({
  components: { TopNav },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const route = useRoute();
    const bufferList = reactive<string[]>([]);
    const pathNavs = ref<IPathNav[]>([]);
    const loadMain = ref<boolean>(true);
    const pathNavsDisplay = computed<boolean>(() => route.path !== PLATFORM_INDEX);

    autoHeight.offsetTop = pathNavsDisplay.value ? 125 : 65;

    proxy.$nextTick(() => adaptHeight(autoHeight));
    buildPathNav(pathNavs, useRoute());

    // 直接访问要被缓冲的路由地址时，添加缓冲
    if(route.meta.keepAlive && !bufferList.includes(route.name as string)){
      bufferList.push(route.name as string);
    }
    onBeforeRouteUpdate(to => {
      if(to.meta.keepAlive && !bufferList.includes(to.name as string)){
        bufferList.push(to.name as string);
      }
      if(to.name === 'index' ){
        bufferList.length = 0;
      }
      autoHeight.offsetTop = to.path !== PLATFORM_INDEX ? 125 : 65;

      proxy.$nextTick(() => adaptHeight(autoHeight));
      buildPathNav(pathNavs, to);
    });
    const reloadMain = () => {
      loadMain.value = false;
      proxy.$nextTick(() => (loadMain.value = true));
    };

    provide('reloadMain', reloadMain);

    return {
      bufferList,
      pathNavs,
      pathNavsDisplay,
      loadMain,
      reloadMain,
    };
  },
});
</script>

<style scoped lang="less">
.platform {
  margin: 0 auto;
  max-width: 1600px;
  height: 100vh;

  .header {
    position: fixed;
    left: 0;
    top: 0;
    width: 100vw;
    height: 64px;
    background-color: #FFFFFF;
    box-shadow: 0 0 8px 0 #CFD7E5;
  }

  .container {
    .path-nav {
      display: flex;
      align-items: center;
    }

    .main {
      padding: 0;

      > div {
        padding: 0 20px;
      }
    }
  }
}
</style>
