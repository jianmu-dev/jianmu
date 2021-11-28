import { DebouncedFunc } from 'lodash';
import { ObjectDirective } from 'vue';
export type CustomObjectDirective = ObjectDirective & {
  handler: () => void;
  initHeight?: number;
  timer?: any;
  throttleScroll: DebouncedFunc<() => void>;
};
