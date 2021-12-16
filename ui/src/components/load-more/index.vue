<template>
  <div class="jm-load-more" v-if="haveMore || noMore">
    <jm-button
      v-if="haveMore"
      class="have-more"
      @click="loadMore"
      :loading="moreLoading"
      type="text"
      >显示更多
      <div class="icon" v-if="iconVisible"></div>
    </jm-button>
    <div class="no-more" v-if="noMore">没有更多了</div>
  </div>
</template>
<script lang="ts">
import { defineComponent, ref, watch, onMounted, PropType } from 'vue';
import { StateEnum } from './enumeration';
export default defineComponent({
  name: 'jm-load-more',
  props: {
    state: {
      type: String as PropType<StateEnum>,
      required: true,
    },
    loadMore: {
      type: Function,
      required: true,
    },
  },
  setup(props) {
    // 显示更多
    const haveMore = ref<boolean>(false);
    // 没有更多了
    const noMore = ref<boolean>(false);
    // loading
    const moreLoading = ref<boolean>(false);
    // 倒三角
    const iconVisible = ref<boolean>(false);

    const state = () => {
      // 显示更多
      if (props.state === StateEnum.MORE) {
        noMore.value = false;
        haveMore.value = true;
        moreLoading.value = false;
        iconVisible.value = true;
        return;
      }
      // loading
      if (props.state === StateEnum.LOADING) {
        noMore.value = false;
        haveMore.value = true;
        moreLoading.value = true;
        iconVisible.value = false;
        return;
      }
      // 没有更多了
      if (props.state === StateEnum.NO_MORE) {
        noMore.value = true;
        haveMore.value = false;
        return;
      }
      // 什么都不显示
      if (props.state === StateEnum.NONE) {
        noMore.value = false;
        haveMore.value = false;
      }
    };
    onMounted(() => state());
    watch(
      () => props.state,
      () => state(),
    );
    return {
      haveMore,
      noMore,
      moreLoading,
      iconVisible,
    };
  },
});
</script>

<style scoped lang="less">
.jm-load-more {
  display: inline-block;
  font-size: 14px;
  color: #7b8c9c;
  .have-more {
    position: relative;
    color:#7b8c9c;
    ::v-deep(.el-icon-loading) {
      position: absolute;
      right: -17px;
    }
    .icon {
      width: 0;
      height: 0;
      border-left: 6px solid transparent;
      border-right: 6px solid transparent;
      border-top: 7px solid #7b8c9c;
      position: absolute;
      right: -17px;
      top: 16px;
    }
  }
  .have-more:hover {
    color: #096dd9;
  }
  .no-more {
    line-height: 40px;
    opacity: 0.45;
  }
}
</style>
