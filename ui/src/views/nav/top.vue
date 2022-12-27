<template>
  <div class="top-nav">
    <div class="left">
      <router-link to="/">
        <div class="logo" />
      </router-link>
      <jm-popconfirm
        v-if="newVersion"
        :title="`最新版本为${newVersion.versionNo}`"
        icon="jm-icon-info"
        confirmButtonText="查看"
        cancelButtonText="忽略"
        confirmButtonIcon="jm-icon-button-visible"
        cancelButtonIcon="jm-icon-button-terminate"
        @confirm="view()"
      >
        <template #reference>
          <div class="version">
            <div class="new"></div>
            <span class="txt">{{ currentVersion }}</span>
          </div>
        </template>
      </jm-popconfirm>
      <div v-else class="version">
        <span class="txt">{{ currentVersion }}</span>
      </div>
    </div>
    <div class="right" v-if="authMode !== 'false'">
      <router-link v-if="!session" :to="{ name: 'login' }">
        <div class="no-login"></div>
      </router-link>
      <jm-dropdown v-else trigger="click">
        <span class="el-dropdown-link">
          <jm-tooltip :content="session.username" placement="left" v-if="loginType">
            <img
              :src="session.avatarUrl"
              class="avatar"
              @error="loadedError"
              v-if="session.avatarUrl !== 'https://gitee.com/assets/no_portrait.png' && loaded"
            />
            <span class="username" v-else>{{ session.username?.charAt(0).toUpperCase() }}</span>
          </jm-tooltip>
          <jm-tooltip :content="session.username" placement="left" v-else>
            <span class="username">{{ session.username?.charAt(0).toUpperCase() }}</span>
          </jm-tooltip>
          <i class="el-icon-arrow-down el-icon--right"></i>
        </span>
        <template #dropdown>
          <jm-dropdown-menu>
            <jm-dropdown-item @click="logout">退出</jm-dropdown-item>
          </jm-dropdown-menu>
        </template>
      </jm-dropdown>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, ref } from 'vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { namespace } from '@/store/modules/session';
import { IState } from '@/model/modules/session';
import { LOGIN_INDEX } from '@/router/path-def';
import { ISessionVo } from '@/api/dto/session';
import { version as v } from '@/../package.json';
import { IRootState } from '@/model';
import { IVersionVo } from '@/api/dto/common';

const { mapMutations } = createNamespacedHelpers(namespace);

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const store = useStore();
    const rootState = store.state as IRootState;
    const authMode = computed<string>(() => rootState.authMode);
    // 头像图片正常显示
    const loaded = ref<boolean>(true);
    const loginType = computed<string>(() => rootState.thirdPartyType);
    const defaultSession = ref<ISessionVo>();
    const state = store.state[namespace] as IState;
    const session = computed<ISessionVo | undefined>(() => {
      return defaultSession.value ? defaultSession.value : state.session;
    });
    const currentVersion = `v${v}`;
    const newVersion = computed<IVersionVo | undefined>(() => {
      if (rootState.versions.length === 0 || rootState.versions[0].versionNo === currentVersion) {
        return undefined;
      }

      return rootState.versions[0];
    });

    return {
      authMode,
      loaded,
      loginType,
      currentVersion,
      newVersion,
      session,
      ...mapMutations({
        deleteSession: 'mutateDeletion',
        mutateSession: 'oauthMutate',
      }),
      view: () => {
        if (!newVersion.value) {
          return;
        }

        window.open(newVersion.value.releaseUrl, '_blank');
      },
      logout: () => {
        try {
          // 清理token
          proxy.deleteSession();
          // 退出登录时，将临时登录模式内容清空
          localStorage.removeItem('temp-login-mode');
          proxy.$success('退出成功');

          router.push(LOGIN_INDEX);
        } catch (err) {
          proxy.$throw(err, proxy);
        }
      },
      // 头像加载失败
      loadedError() {
        loaded.value = false;
      },
    };
  },
});
</script>

<style scoped lang="less">
.top-nav {
  margin-right: 0.5%;
  position: relative;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left {
    display: flex;
    align-items: center;
    position: relative;

    .logo {
      width: 150px;
      height: 34px;
      background-image: url('@/assets/svgs/logo/main.svg');
      background-repeat: no-repeat;
      background-size: contain;
      background-position: center center;
      cursor: pointer;
    }

    .version {
      position: absolute;
      left: 150px;
      bottom: 2px;
      letter-spacing: normal;
      white-space: nowrap;
      padding: 0 10px 1px;
      height: 16px;
      background-color: #f2f2f2;
      border-radius: 10px;
      display: flex;
      align-items: center;

      .new {
        position: absolute;
        right: -3px;
        top: -3px;
        width: 8px;
        height: 8px;
        background-color: #ff0d0d;
        border-radius: 4px;

        & + .txt {
          cursor: pointer;
        }
      }

      .txt {
        font-size: 12px;
        color: #082340;
      }
    }
  }

  .right {
    .no-login {
      width: 36px;
      height: 36px;
      background-image: url('@/assets/svgs/nav/top/default-avatar.svg');
      background-position: center center;
      background-repeat: no-repeat;
    }

    .el-dropdown-link {
      display: inline-flex;
      align-items: center;
      margin-left: 10px;
      color: #333333;
      cursor: pointer;

      .avatar {
        width: 36px;
        height: 36px;
        overflow: hidden;
        border-radius: 50%;
      }

      .username {
        display: inline-block;
        width: 36px;
        height: 36px;
        line-height: 36px;
        text-align: center;
        overflow: hidden;
        background-color: #7b8c9c;
        border-radius: 18px;
        font-size: 26px;
        font-weight: bold;
        color: #ffffff;
      }

      .el-icon-arrow-down::before {
        color: #082340;
      }
    }
  }
}
</style>
