<template>
  <div class="content">
    <json-view-item
      :class="[{ 'root-item': true, dark: colorScheme === 'dark' }]"
      :data="parsed"
      :maxDepth="maxDepth"
      @update:selected="itemSelected"
      :canSelect="hasSelectedListener"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import type { PropType } from 'vue';
import JsonViewItem from './JsonViewItem.vue';
import { ColorMode, JsonDataType } from './types';
import type { JsonData, SelectedJsonData } from './types';

export default /* #__PURE__*/ defineComponent({
  name: 'jm-json-view',
  props: {
    data: {
      required: true,
    },
    rootKey: {
      type: String,
      required: false,
      default: 'root',
    },
    maxDepth: {
      type: Number,
      required: false,
      default: 1,
    },
    colorScheme: {
      type: String as PropType<ColorMode>,
      required: false,
      default: ColorMode.LIGHT,
    },
  },
  components: { JsonViewItem },
  emits: ['update:selected'],
  methods: {
    convertKey(key: string): string {
      if (Number.isInteger(key)) {
        return `[${key.toString()}]`;
      }

      if (/[^A-Za-z0-9_$]/.test(key.toString())) {
        return `["${key}"]`;
      }

      return `${key}`;
    },
    build(key: string, val: any, depth: number, path: string, includeKey: boolean): JsonData {
      const children = [];
      if (this.isObject(val)) {
        // Build Object
        for (const [childKey, childValue] of Object.entries(val)) {
          children.push(this.build(childKey, childValue, depth + 1, includeKey ? `${path}${key}.` : `${path}`, true));
        }
        return {
          key: key,
          type: JsonDataType.OBJECT,
          depth: depth,
          path: path,
          length: children.length,
          children: children,
        };
      } else if (this.isArray(val)) {
        // Build Array
        for (let i = 0; i < val.length; i++) {
          children.push(
            this.build(i.toString(), val[i], depth + 1, includeKey ? `${path}${key}[${i}].` : `${path}`, false),
          );
        }
        return {
          key: key,
          type: JsonDataType.ARRAY,
          depth: depth,
          path: path,
          length: children.length,
          children: children,
        };
      } else {
        let convertedKey = this.convertKey(key);
        // Build Value
        return {
          key: key,
          type: JsonDataType.VALUE,
          path: includeKey ? path + convertedKey : path.slice(0, -1),
          depth: depth,
          value: val,
        };
      }
    },
    isObject: (val: any) => typeof val === 'object' && val !== null && !Array.isArray(val),
    isArray: (val: any) => Array.isArray(val),
    itemSelected(data: SelectedJsonData): void {
      this.$emit('update:selected', data);
    },
  },
  computed: {
    parsed(): JsonData {
      if (typeof this.data === 'object') {
        return this.build(this.rootKey, { ...this.data }, 0, '', true);
      }
      return {
        key: this.rootKey,
        type: JsonDataType.VALUE,
        path: '',
        depth: 0,
        value: this.data,
      };
    },
    hasSelectedListener(): boolean {
      return !!this.$attrs.onSelected;
    },
  },
});
</script>

<style lang="less" scoped>
.content {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.root-item {
  --vjc-key-color: #0977e6;
  --vjc-valueKey-color: #073642;
  --vjc-string-color: #268bd2;
  --vjc-number-color: #2aa198;
  --vjc-boolean-color: #cb4b16;
  --vjc-null-color: #6c71c4;
  --vjc-arrow-size: 6px;
  --vjc-arrow-color: #444;
  --vjc-hover-color: rgba(0, 0, 0, 0.2);

  margin-left: 0;
  width: 100%;
  height: auto;
}

.root-item.dark {
  --vjc-key-color: #80d8ff;
  --vjc-valueKey-color: #fdf6e3;
  --vjc-hover-color: rgba(255, 255, 255, 0.2);
  --vjc-arrow-color: #fdf6e3;
}
</style>
