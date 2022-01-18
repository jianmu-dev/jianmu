<template>
  <div class="platform" ref="platFormRef">
    <div class="header"></div>
    <jm-container class="container">
      <jm-header height="64px">
        <top-nav/>
      </jm-header>
      <jm-container>
        <jm-header v-if="pathNavsDisplay" class="path-nav">
          <jm-breadcrumb>
            <jm-breadcrumb-item
              v-for="{ name, path } in pathNavs"
              :key="path"
              :to="path"
            >{{ name }}
            </jm-breadcrumb-item
            >
          </jm-breadcrumb>
        </jm-header>
        <jm-main :class="mainClass">
          <jm-scrollbar ref="mainScrollbarRef">
            <div class="main-content" v-if="loadMain" id="platform-main">
              <router-view v-slot="{ Component }">
                <keep-alive :include="bufferList">
                  <component :is="Component"></component>
                </keep-alive>
              </router-view>
            </div>
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
import { PLATFORM_INDEX } from '@/router/path-def';
import { ElScrollbar } from 'element-plus';
import { mapMutations, useStore } from 'vuex';
import { IRootState } from '@/model';

interface IPathNav {
  name: string;
  path: string;
}

function buildPathNav(
  pathNavs: Ref<IPathNav[]>,
  route: RouteLocationNormalizedLoaded | RouteLocationNormalized,
) {
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
    const store = useStore();
    const rootState = store.state as IRootState;
    const route = useRoute();
    const platFormRef = ref<HTMLElement>();
    const bufferList = reactive<string[]>([]);
    const pathNavs = ref<IPathNav[]>([]);
    const loadMain = ref<boolean>(true);
    const pathNavsDisplay = computed<boolean>(() => route.path !== PLATFORM_INDEX);
    const mainClass = ref<string>(pathNavsDisplay.value ? 'main' : 'main2');
    const mainScrollbarRef = ref<InstanceType<typeof ElScrollbar>>();
    buildPathNav(pathNavs, useRoute());
    // 直接访问要被缓冲的路由地址时，添加缓冲
    if (route.meta.keepAlive && !bufferList.includes(route.name as string)) {
      bufferList.push(route.name as string);
    }
    onBeforeRouteUpdate(to => {
      if (to.meta.keepAlive && !bufferList.includes(to.name as string)) {
        bufferList.push(to.name as string);
      }
      if (to.name === 'index') {
        bufferList.length = 0;
      }
      mainClass.value = to.path !== PLATFORM_INDEX ? 'main' : 'main2';

      buildPathNav(pathNavs, to);
    });
    const reloadMain = () => {
      loadMain.value = false;
      proxy.$nextTick(() => (loadMain.value = true));
    };
    const setScrollbarOffset = async () => {
      const scrollableEl = mainScrollbarRef.value?.scrollbar.firstElementChild;

      if (!scrollableEl) {
        setTimeout(() => setScrollbarOffset(), 300);
        return;
      }

      const { left, top } = rootState.scrollbarOffset[route.fullPath] || { left: 0, top: 0 };

      if (scrollableEl.scrollHeight <= top) {
        setTimeout(() => setScrollbarOffset(), 300);
        return;
      }

      setTimeout(() => {
        console.debug('before', scrollableEl.scrollTop, route.fullPath, top, scrollableEl.scrollHeight);
        scrollableEl.scrollLeft = left;
        scrollableEl.scrollTop = top;
        console.debug('after', scrollableEl.scrollTop, route.fullPath, top, scrollableEl.scrollHeight);
      }, 400);
    };

    provide('reloadMain', reloadMain);
    provide('scrollableEl', () => {
      return mainScrollbarRef.value?.scrollbar.firstElementChild;
    });
    provide('setScrollbarOffset', setScrollbarOffset);
    provide('updateScrollbarOffset', () => {
      const scrollableEl = mainScrollbarRef.value?.scrollbar.firstElementChild;
      if (!scrollableEl) {
        return;
      }
      proxy.mutateScrollbarOffset({
        fullPath: route.fullPath,
        left: scrollableEl.scrollLeft,
        top: scrollableEl.scrollTop,
      });
    });

    return {
      platFormRef,
      mainScrollbarRef,
      bufferList,
      pathNavs,
      pathNavsDisplay,
      mainClass,
      loadMain,
      reloadMain,
      ...mapMutations({
        mutateScrollbarOffset: 'mutateScrollbarOffset',
      }),
    };
  },
});
</script>

<style scoped lang="less">
@main-min-width: 1200px;
@main-max-width: 1600px;

.platform {
  margin: 0 auto;
  height: 100vh;

  .header {
    position: fixed;
    left: 0;
    top: 0;
    width: 100vw;
    height: 64px;
    background-color: #ffffff;
    box-shadow: 0 0 8px 0 #cfd7e5;
  }

  .container {
    .el-header {
      width: 100%;
      max-width: @main-max-width;
      margin: 0 auto;
    }

    .path-nav {
      display: flex;
      align-items: center;
    }

    .main {
      height: calc(100vh - 125px);
    }

    .main2 {
      height: calc(100vh - 65px);
    }

    .main,
    .main2 {
      padding: 0;

      > .el-scrollbar {
        .main-content {
          width: 100%;
          min-width: @main-min-width;
          max-width: @main-max-width;
          margin: 0 auto;
          padding: 0 20px;
          box-sizing: border-box;
        }
      }
    }
  }
}
</style>

