<template>
  <el-popper
    v-model:visible="visible"
    trigger="click"
    effect="light"
    popper-class="el-popover"
    append-to-body
  >
    <div class="el-popconfirm">
      <p class="el-popconfirm__main">
        <i
          v-if="!hideIcon"
          :class="icon"
          class="el-popconfirm__icon"
          :style="{color: iconColor}"
        ></i>
        {{ title }}
      </p>
      <div class="el-popconfirm__action">
        <el-button
          size="mini"
          :type="cancelButtonType"
          :icon="cancelButtonIcon"
          @click="cancel"
        >
          {{ cancelButtonText_ }}
        </el-button>
        <el-button
          size="mini"
          :type="confirmButtonType"
          :icon="confirmButtonIcon"
          @click="confirm"
        >
          {{ confirmButtonText_ }}
        </el-button>
      </div>
    </div>
    <template #trigger>
      <slot name="reference"></slot>
    </template>
  </el-popper>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue';
import { ElButton, ElPopper } from 'element-plus';

export default defineComponent({
  name: 'jm-popconfirm',

  components: {
    ElButton,
    ElPopper,
  },

  props: {
    title: {
      type: String,
    },
    confirmButtonText: {
      type: String,
    },
    cancelButtonText: {
      type: String,
    },
    confirmButtonType: {
      type: String,
      default: 'primary',
    },
    cancelButtonType: {
      type: String,
      default: 'default',
    },
    confirmButtonIcon: {
      type: String,
    },
    cancelButtonIcon: {
      type: String,
    },
    icon: {
      type: String,
      default: 'el-icon-question',
    },
    iconColor: {
      type: String,
      default: '#f90',
    },
    hideIcon: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['confirm', 'cancel'],
  setup(props, { emit }) {
    const visible = ref(false);
    const confirm = () => {
      visible.value = false;
      emit('confirm');
    };
    const cancel = () => {
      visible.value = false;
      emit('cancel');
    };
    const confirmButtonText_ = computed(() => {
      return props.confirmButtonText || '确认';
    });
    const cancelButtonText_ = computed(() => {
      return props.cancelButtonText || '取消';
    });
    return {
      visible,
      confirm,
      cancel,
      confirmButtonText_,
      cancelButtonText_,
    };
  },
});
</script>
