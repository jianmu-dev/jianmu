<template>
  <div class="top-nav">
    <div class="left">
      <router-link to="/">
        <div class="logo"/>
      </router-link>
      <jm-popconfirm title="最新版本为v2.1.0"
                     icon="jm-icon-info"
                     confirmButtonText="查看"
                     cancelButtonText="忽略"
                     confirmButtonIcon="jm-icon-button-visible"
                     cancelButtonIcon="jm-icon-button-terminate">
        <template #reference>
          <div class="version">
            <div class="new"></div>
            <span class="txt">v{{ version }}</span>
          </div>
        </template>
      </jm-popconfirm>
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
      bottom: 8px;
      letter-spacing: normal;
      white-space: nowrap;
      padding: 0 10px 1px;
      height: 16px;
      background-color: #F2F2F2;
      border-radius: 10px;
      display: flex;
      align-items: center;

      .txt {
        font-size: 12px;
        font-weight: bold;
        color: #082340;
        opacity: 0.5;
      }

      .new {
        position: absolute;
        right: -3px;
        top: -3px;
        width: 8px;
        height: 8px;
        background-color: #FF0D0D;
        border-radius: 4px;
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
