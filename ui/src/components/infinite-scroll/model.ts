import { ObjectDirective } from 'vue';
export type CustomObjectDirective = ObjectDirective & {
  handler: () => void;
  initHeight?: number;
  timer?: any;
};
