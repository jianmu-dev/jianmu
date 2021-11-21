<template>
  <div class="top-nav">
    <div class="left">
      <router-link to="/">
        <div class="logo"/>
      </router-link>
      <div class="version">v{{ version }}</div>
    </div>
    <div class="right">
      <router-link v-if="!session" :to="{name: 'login'}">
        <div class="no-login"></div>
      </router-link>
      <jm-dropdown v-else>
        <span class="el-dropdown-link">
          <jm-tooltip :content="session.username" placement="left">
            <span class="username">{{ session.username.charAt(0).toUpperCase() }}</span>
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
import { computed, defineComponent, getCurrentInstance } from 'vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { namespace } from '@/store/modules/session';
import { IState } from '@/model/modules/session';
import { LOGIN_INDEX } from '@/router/path-def';
import { ISessionVo } from '@/api/dto/session';
import { version } from '@/../package.json';

const { mapMutations } = createNamespacedHelpers(namespace);

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const state = useStore().state[namespace] as IState;

    return {
      version,
      session: computed<ISessionVo | undefined>(() => state.session),
      ...mapMutations({
        deleteSession: 'mutateDeletion',
      }),
      logout: () => {
        try {
          // 清理token
          proxy.deleteSession();
          proxy.$success('退出成功');

          router.push(LOGIN_INDEX);
        } catch (err) {
          proxy.$throw(err, proxy);
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.top-nav {
  margin: 0 0.5%;
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
      width: 160px;
      height: 50px;
      background-image: url('@/assets/svgs/logo/main.svg');
      background-repeat: no-repeat;
      background-size: contain;
      background-position: center center;
      cursor: pointer;
    }

    .version {
      position: absolute;
      left: 170px;
      bottom: 10px;
      font-size: 12px;
      font-weight: bold;
      color: #082340;
      opacity: 0.5;
      letter-spacing: normal;
      white-space: nowrap;
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
      cursor: default;

      .username {
        display: inline-block;
        width: 36px;
        height: 36px;
        line-height: 36px;
        text-align: center;
        overflow: hidden;
        background-color: #7B8C9C;
        border-radius: 18px;
        font-size: 26px;
        font-weight: bold;
        color: #FFFFFF;
      }

      .el-icon-arrow-down::before {
        color: #082340;
      }
    }
  }
}
</style>
