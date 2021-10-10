<template>
  <div class="jm-tags-input">
    <smart-tagz
      ref="smartTagzRef"
      :theme="{
        primary: '#096DD9',
        background: '#FFFFFF',
        tagTextColor: '#FFFFFF',
      }"
      :inputPlaceholder="placeholder"
      :allowPaste="{delimiter: ';'}"
      :defaultTags="modelValue || []"
      :maxTags="100"
      @keydown.enter.prevent
    />
  </div>
</template>

<script type="text/ecmascript-6">
import { defineComponent, onMounted, ref, watchEffect } from 'vue';
import { SmartTagz } from 'smart-tagz';
import 'smart-tagz/dist/smart-tagz.css';

export default defineComponent({
  name: 'jm-tags-input',
  components: { SmartTagz },
  props: {
    placeholder: String,
    modelValue: Array,
  },
  emits: ['update:modelValue', 'change'],
  setup(props, { emit }) {
    const smartTagzRef = ref();

    onMounted(() => {
      watchEffect(() => {
        const newVal = smartTagzRef.value.tagsData.map(tagData => tagData.value);

        emit('update:modelValue', newVal);
        emit('change', newVal);
      });
    });

    return {
      smartTagzRef,
    };
  },
});
</script>

<style scoped lang="less">
@primary-color: #096DD9;
@secondary-color: #0091FF;

.jm-tags-input {
  ::v-deep(.tags-main) {
    padding: 0;
    border: 1px solid #D0E0ED;
    border-radius: 2px;

    &:hover {
      border-color: @secondary-color;
    }

    .tag-container {
      padding: 2px 5px;
      line-height: normal;
      user-select: auto;

      .icon-wrapper {
        width: 1rem;
      }
    }

    .input-wrapper {
      margin-top: 0;

      input[type="text"] {
        margin: 0.4rem 0;
        width: 100%;
        border-bottom-width: 0;
        font-size: 14px;
        color: #333333;

        &:focus {
          border-bottom-width: thin;
          border-bottom-color: @primary-color;
        }

        &::-webkit-input-placeholder { /* Edge */
          color: #C0C4CC;
        }

        &:-ms-input-placeholder { /* Internet Explorer 10-11 */
          color: #C0C4CC;
        }

        &::-moz-placeholder {
          color: #C0C4CC;
        }

        &::placeholder {
          color: #C0C4CC;
        }
      }
    }
  }
}
</style>