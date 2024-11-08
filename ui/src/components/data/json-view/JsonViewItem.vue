<template>
  <div class="json-view-item">
    <!-- Handle Objects and Arrays-->
    <div v-if="data.type === 'object' || data.type === 'array'">
      <button @click.stop="open = !open" class="data-key" :aria-expanded="open ? 'true' : 'false'">
        <span :class="{ 'chevron-arrow': true, opened: open }"></span>
        {{ data.key }}:
        <span class="properties">{{ lengthString }}</span>
      </button>
      <json-view-item
        @update:selected="bubbleSelected"
        v-for="child in data.children"
        :key="getKey(child)"
        :data="child"
        v-show="open"
        :maxDepth="maxDepth"
        :canSelect="canSelect"
      />
    </div>
    <!-- Handle Leaf Values -->
    <div
      :class="{ 'value-key': true, 'can-select': canSelect }"
      @click="emitSelect(data)"
      @keyup.enter="emitSelect(data)"
      @keyup.space="emitSelect(data)"
      :role="canSelect ? 'button' : undefined"
      :tabindex="canSelect ? '0' : undefined"
      v-if="data.type === 'value'"
    >
      <span class="value-key">{{ data.key }}:</span>
      <span :style="getValueStyle(data.value)">{{ dataValue }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import type { PropType } from 'vue';
import { JsonDataType } from './types';
import type { JsonData, SelectedJsonData } from './types';

export default /* #__PURE__*/ defineComponent({
  name: 'json-view-item',
  data() {
    return {
      open: this.data.depth < this.maxDepth,
    };
  },
  props: {
    data: {
      required: true,
      type: Object as PropType<JsonData>,
    },
    maxDepth: {
      type: Number,
      required: false,
      default: 1,
    },
    canSelect: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  emits: ['update:selected'],
  methods: {
    emitSelect(data: JsonData): void {
      this.$emit('update:selected', {
        key: data.key,
        value: data.type === JsonDataType.VALUE ? data.value : undefined,
        path: data.path,
      } as SelectedJsonData);
    },
    bubbleSelected(data: SelectedJsonData): void {
      this.$emit('update:selected', data);
    },
    getKey(value: any): string {
      if (!isNaN(value.key)) {
        return value.key + ':';
      } else {
        return '"' + value.key + '":';
      }
    },
    getValueStyle(value: any): any {
      switch (typeof value) {
        case 'string':
          return { color: 'var(--vjc-string-color)' };
        case 'number':
          return { color: 'var(--vjc-number-color)' };
        case 'boolean':
          return { color: 'var(--vjc-boolean-color)' };
        case 'object':
          return { color: 'var(--vjc-null-color)' };
        case 'undefined':
          return { color: 'var(--vjc-null-color)' };
        default:
          return { color: 'var(--vjc-valueKey-color)' };
      }
    },
  },
  computed: {
    lengthString(): string {
      switch (this.data.type) {
        case JsonDataType.ARRAY:
          return this.data.length === 1 ? this.data.length + ' element' : this.data.length + ' elements';
        case JsonDataType.OBJECT:
          return this.data.length === 1 ? this.data.length + ' property' : this.data.length + ' properties';
        default:
          return '';
      }
    },
    dataValue(): string {
      if (this.data.type === JsonDataType.VALUE) {
        if (typeof this.data.value === 'undefined') {
          return 'undefined';
        }
        return JSON.stringify(this.data.value);
      }
      return '';
    },
  },
});
</script>

<style lang="less" scoped>
.json-view-item:not(.root-item) {
  margin-left: 15px;
}

.value-key {
  color: var(--vjc-valueKey-color);
  font-weight: 600;
  margin-left: 10px;
  border-radius: 2px;
  white-space: nowrap;
  padding: 5px 5px 5px 10px;

  &.can-select {
    cursor: pointer;

    &:hover {
      background-color: rgba(0, 0, 0, 0.08);
    }

    &:focus {
      outline: 2px solid var(--vjc-hover-color);
    }
  }
}

.data-key {
  // Button overrides
  font-size: 100%;
  font-family: inherit;
  border: 0;
  background-color: transparent;
  width: 100%;

  // Normal styles
  color: var(--vjc-key-color);
  display: flex;
  align-items: center;
  border-radius: 2px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  padding: 5px;

  &:hover {
    background-color: var(--vjc-hover-color);
  }

  &:focus {
    outline: 2px solid var(--vjc-hover-color);
  }

  &::-moz-focus-inner {
    border: 0;
  }

  .properties {
    font-weight: 300;
    opacity: 0.9;
    margin-left: 4px;
    user-select: none;
  }
}

.chevron-arrow {
  flex-shrink: 0;
  border-right: 4px solid var(--vjc-arrow-color);
  border-bottom: 4px solid var(--vjc-arrow-color);
  width: var(--vjc-arrow-size);
  height: var(--vjc-arrow-size);
  margin-right: 20px;
  margin-left: 5px;
  transform: rotate(-45deg);

  &.opened {
    margin-top: -3px;
    transform: rotate(45deg);
  }
}
</style>
