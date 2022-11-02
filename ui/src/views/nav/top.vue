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
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue';
import { useStore } from 'vuex';
import { version as v } from '@/../package.json';
import { IRootState } from '@/model';
import { IVersionVo } from '@/api/dto/common';

export default defineComponent({
  setup() {
    const store = useStore();
    const rootState = store.state as IRootState;
    const currentVersion = `v${v}`;
    const newVersion = computed<IVersionVo | undefined>(() => {
      if (rootState.versions.length === 0 || rootState.versions[0].versionNo === currentVersion) {
        return undefined;
      }

      return rootState.versions[0];
    });

    return {
      currentVersion,
      newVersion,
      view: () => {
        if (!newVersion.value) {
          return;
        }

        window.open(newVersion.value.releaseUrl, '_blank');
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
}
</style>
