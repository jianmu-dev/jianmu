<template>
  <div class="param-value">
    <div class="value" :style="{maxWidth:maxWidth}">
      <span v-if="type===ParamTypeEnum.SECRET">**********</span>
      <a :href="value" target="_blank" download v-else-if="isLink">
        <jm-text-viewer @loaded="({contentMaxWidth})=>getMaxWidth(contentMaxWidth)" :value="value"
                        :tip-append-to-body="tipAppendToBody" :tip-placement="tipPlacement"/>
      </a>
      <jm-text-viewer @loaded="({contentMaxWidth})=>getMaxWidth(contentMaxWidth)" :value="value"
                      :tip-append-to-body="tipAppendToBody" :tip-placement="tipPlacement" v-else/>
    </div>
    <jm-text-copy :value="value"
                  v-if="type!==ParamTypeEnum.SECRET" class="copy-btn"/>
  </div>
</template>

<script lang='ts'>
import { computed, defineComponent, PropType, ref } from 'vue';
import { ParamTypeEnum } from '@/api/dto/enumeration';

export default defineComponent({
  name: 'jm-param-value',
  props: {
    value: {
      type: String,
      required: true,
    },
    type: {
      type: String as PropType<ParamTypeEnum>,
    },
    // tooltip 显示方向
    tipPlacement: {
      type: String,
      default: 'bottom-end',
    },
    // 控制tooltip是否被放置到body元素上
    tipAppendToBody: {
      type: Boolean,
      default: true,
    },
  },
  setup(props) {
    const maxWidth = ref<string>('');
    const getMaxWidth = (width: number) => {
      maxWidth.value += width + 'px';
    };
    const regExp = /((http|https):\/\/([\w\-]+\.)+[\w\-]+(\/[\w\u4e00-\u9fa5\-\.\/?\@\%\!\&=\+\~\:\#\;\,]*)?)/ig;
    const isLink = computed<boolean>(() => {
      if (props.value.startsWith('http://') || props.value.startsWith('https://')) {
        return !!props.value.match(regExp);
      } else {
        return false;
      }
    });
    return {
      maxWidth,
      getMaxWidth,
      isLink,
      ParamTypeEnum,
    };
  },
});
</script>

<style scoped lang='less'>
.param-value {

  .value {
    display: inline-block;
    width: calc(100% - 26px);
    height: 1.5em;

    a {
      display: inline-block;
      width: 100%;
      color: #096DD9;
    }
  }

  &:hover .copy-btn {
    visibility: visible;
  }

  .copy-btn {
    font-size: 1.25em;
    width: 16px;
    margin-left: 10px;
    visibility: hidden;
  }
}
</style>
