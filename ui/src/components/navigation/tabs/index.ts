import {
  Component,
  ComponentInternalInstance,
  Fragment,
  getCurrentInstance,
  nextTick, onMounted, onUpdated,
  provide,
  ref,
  VNode,
  watch,
} from 'vue';
import { ElTabs } from 'element-plus';
import { IETabsProps, Pane, RootTabs, UpdatePaneStateCallback } from 'element-plus/es/el-tabs/src/tabs.vue';
import TabNav from 'element-plus/es/el-tabs/src/tab-nav.vue';

const TAB_PANE_NAME = 'jm-tab-pane';

export default {
  ...ElTabs,
  name: 'jm-tabs',
  setup(props: IETabsProps, ctx: any) {
    // @ts-ignore
    const nav$ = ref<typeof TabNav>(null);
    const currentName = ref(props.modelValue || props.activeName || '0');
    const panes = ref([]);
    const instance = getCurrentInstance();
    const paneStatesMap = {};

    provide<RootTabs>('rootTabs', {
      props,
      currentName,
    });

    provide<UpdatePaneStateCallback>('updatePaneState', (pane: Pane) => {
      // @ts-ignore
      paneStatesMap[pane.uid] = pane;
    });

    watch(() => props.activeName, modelValue => {
      // eslint-disable-next-line no-use-before-define
      setCurrentName(modelValue);
    });

    watch(() => props.modelValue, modelValue => {
      // eslint-disable-next-line no-use-before-define
      setCurrentName(modelValue);
    });

    watch(currentName, () => {
      if (nav$.value) {
        nextTick(() => {
          nav$.value.$nextTick(() => {
            nav$.value.scrollToActiveTab();
          });
        });
      }
      // eslint-disable-next-line no-use-before-define
      setPaneInstances(true);
    });

    const getPaneInstanceFromSlot = (vnode: VNode, paneInstanceList: ComponentInternalInstance[] = []) => {

      Array.from((vnode.children || []) as ArrayLike<VNode>).forEach(node => {
        let type = node.type;
        type = (type as Component).name || type;
        if (type === TAB_PANE_NAME && node.component) {
          paneInstanceList.push(node.component);
        } else if (type === Fragment || type === 'template') {
          getPaneInstanceFromSlot(node, paneInstanceList);
        }
      });
      return paneInstanceList;
    };

    const setPaneInstances = (isForceUpdate = false) => {
      if (ctx.slots.default) {
        // @ts-ignore
        const children = instance.subTree.children;

        const content = Array.from(children as ArrayLike<VNode>).find(({ props }) => {
          // @ts-ignore
          return props.class === 'el-tabs__content';
        });

        if (!content) return;

        const paneInstanceList: Pane[] = getPaneInstanceFromSlot(content).map(paneComponent => {
          // @ts-ignore
          return paneStatesMap[paneComponent.uid];
        });
        // @ts-ignore
        const panesChanged = !(paneInstanceList.length === panes.value.length && paneInstanceList.every((pane, index) => pane.uid === panes.value[index].uid));

        if (isForceUpdate || panesChanged) {
          // @ts-ignore
          panes.value = paneInstanceList;
        }
      } else if (panes.value.length !== 0) {
        panes.value = [];
      }
    };

    const changeCurrentName = (value: any) => {
      currentName.value = value;
      ctx.emit('input', value);
      ctx.emit('update:modelValue', value);
    };

    const setCurrentName = (value: any) => {
      if (currentName.value !== value && props.beforeLeave) {
        const before = props.beforeLeave(value, currentName.value);
        if (before && (before as Promise<void>).then) {
          (before as Promise<void>).then(() => {
            changeCurrentName(value);
            nav$.value && nav$.value.removeFocus();
          }, () => {
            // ignore promise rejection in `before-leave` hook
          });
        } else if (before !== false) {
          changeCurrentName(value);
        }
      } else {
        changeCurrentName(value);
      }
    };

    const handleTabClick = (tab: any, tabName: any, event: any) => {
      if (tab.props.disabled) return;
      setCurrentName(tabName);
      ctx.emit('tab-click', tab, event);
    };

    const handleTabRemove = (pane: any, ev: any) => {
      if (pane.props.disabled) return;
      ev.stopPropagation();
      ctx.emit('edit', pane.props.name, 'remove');
      ctx.emit('tab-remove', pane.props.name);
    };

    const handleTabAdd = () => {
      ctx.emit('edit', null, 'add');
      ctx.emit('tab-add');
    };

    onUpdated(() => {
      setPaneInstances();
    });

    onMounted(() => {
      setPaneInstances();
    });

    return {
      nav$,
      handleTabClick,
      handleTabRemove,
      handleTabAdd,
      currentName,
      panes,
    };

  },
};
