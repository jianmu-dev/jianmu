<template>
  <div class="basic-icon">
    <div class="group">
      <div class="title">按钮icons:</div>
      <div v-for="cls in buttonIconClasses" :key="cls">
        <i :class="cls"></i>
        {{ cls }}
      </div>
    </div>
    <div class="group">
      <div class="title">标签页icons:</div>
      <div v-for="cls in tabIconClasses" :key="cls">
        <i :class="cls"></i>
        {{ cls }}
      </div>
    </div>
    <div class="group">
      <div class="title">文字链接icons:</div>
      <div v-for="cls in linkIconClasses" :key="cls">
        <i :class="cls"></i>
        {{ cls }}
      </div>
    </div>
    <div class="group">
      <div class="title">菜单icons:</div>
      <div v-for="cls in menuIconClasses" :key="cls">
        <i :class="cls"></i>
        {{ cls }}
      </div>
    </div>
    <div class="group">
      <div class="title">输入框icons:</div>
      <div v-for="cls in inputIconClasses" :key="cls">
        <i :class="cls"></i>
        {{ cls }}
      </div>
    </div>
    <div class="group">
      <div class="title">流程icons:</div>
      <div v-for="cls in workflowIconClasses" :key="cls">
        <i :class="cls"></i>
        {{ cls }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    const buttonIconClasses: string[] = [];
    Object.keys(import.meta.globEager('../../components/theme/button/svgs/*.svg')).forEach(key =>
      buttonIconClasses.push(`jm-icon-button-${key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'))}`));

    const tabIconClasses: string[] = [];
    Object.keys(import.meta.globEager('../../components/theme/tabs/svgs/*.svg')).forEach(key =>
      tabIconClasses.push(`jm-icon-tab-${key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'))}`));

    const linkIconClasses: string[] = [];
    Object.keys(import.meta.globEager('../../components/theme/link/svgs/*.svg')).forEach(key =>
      linkIconClasses.push(`jm-icon-link-${key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'))}`));

    const menuIconClasses: string[] = [];
    Object.keys(import.meta.globEager('../../components/theme/menu/svgs/*.svg')).forEach(key =>
      menuIconClasses.push(`jm-icon-menu-${key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'))}`));

    const inputIconClasses: string[] = [];
    Object.keys(import.meta.globEager('../../components/theme/input/svgs/*.svg')).forEach(key =>
      inputIconClasses.push(`jm-icon-input-${key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'))}`));

    const workflowIconClasses: string[] = [];
    Object.keys(import.meta.globEager('../../components/theme/workflow/svgs/*.svg')).forEach(key =>
      workflowIconClasses.push(`jm-icon-workflow-${key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'))}`));

    return {
      buttonIconClasses,
      tabIconClasses,
      linkIconClasses,
      menuIconClasses,
      inputIconClasses,
      workflowIconClasses,
    };
  },
});
</script>

<style scoped lang="less">
.basic-icon {
  .group {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 20px;

    .title {
      width: 100%;
      border-bottom: 1px dashed #dedede;
    }

    :nth-child(n):not(.title) {
      width: 33%;
    }
  }
}
</style>
