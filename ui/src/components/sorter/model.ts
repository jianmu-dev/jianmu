interface IDraggingItem {
  // 当前拖拽的元素
  el?: HTMLElement;
  // 当前拖拽元素在页面中的索引
  index?: number;
  // 控制元素是在前方还是后方插入，0表示前，1表示后
  insertAfter: boolean;
  // 拖拽drop时的元素
  dropEl?: HTMLElement;
}

interface ISortResult {
  oldIndex: number;
  newIndex: number;
  oldElement: object;
  newElement: object;
  originArr: Array<object>;
}

function addInsertion(el: HTMLElement, insertAfter: boolean) {
  el.style.position = 'relative';
  const div = document.createElement('div');
  div.className = 'drag-target-insertion';
  div.style.top = -el.clientTop + 'px';
  div.style.height = el.offsetHeight + 'px';
  el.appendChild(div);
  div.style.left = (insertAfter ? el.clientWidth : -div.offsetWidth) + 'px';
}

function removeInsertion(el: HTMLElement) {
  if (el.lastElementChild?.className !== 'drag-target-insertion') {
    return;
  }
  el.style.position = '';
  el.removeChild(el.lastElementChild!);
}

export default class Sorter {
  private readonly completeCallback: () => void;
  private readonly elements: HTMLElement[] = [];
  private readonly draggingItem: IDraggingItem = { insertAfter: false };

  constructor(completeCallback: () => void) {
    this.elements = Array.from(document.querySelector('.jm-sorter')!.children) as HTMLElement[];
    this.completeCallback = completeCallback;
    // 让元素实现拖拽的前期准备
    this.elements.forEach((el, index) => {
      const item = el as HTMLElement;
      // 让元素可拖拽
      item.setAttribute('draggable', 'true');
      item.setAttribute('data-index', String(index));
      // 注册拖拽相关事件
      item.ondragstart = (e: DragEvent) => {
        if (navigator.userAgent.indexOf('Chrome') > -1) {
          // 解决chrome浏览器下拖拽元素默认样式失效问题
          const { offsetX, offsetY } = e;
          e.dataTransfer!.setDragImage(item, 2 * offsetX, 2 * offsetY);
          // 更改chrome浏览器拖拽时默认的鼠标样式
          e.dataTransfer!.effectAllowed = 'copyMove';
        }
        this.draggingItem.index = index;
        this.draggingItem.el = item;
      };
      item.ondragover = (e: DragEvent) => this.dragOverItem(e, item);
      // 被拖拽的节点，被释放后进行元素替换
      item.ondrop = () => this.dropItem(item);
      // 样式交互
      item.ondragend = () => removeInsertion(item);
      item.ondragleave = () => removeInsertion(item);
    });
  }

  complete(arr: Array<object>): ISortResult {
    const originArr = [...arr];
    // 获取拖拽元素当前索引
    let { index } = this.draggingItem;
    const { insertAfter } = this.draggingItem;
    index = index as number;
    // 获取触发drop事件元素当前索引
    const swapIndex = this.elements.findIndex(({ dataset: { index } }) => index === this.draggingItem.dropEl!.dataset.index);
    const draggedData: object = arr[index];
    // 插入元素
    arr.splice(swapIndex + (insertAfter ? 1 : 0), 0, draggedData);
    // 拖拽元素向后走
    const draggedBackWard = swapIndex > index;
    // 删除原有拖拽元素
    arr.splice(draggedBackWard ? index : index + 1, 1);
    // 元素位置变化后的索引
    let newIndex;
    let oldElement;
    if (insertAfter) {
      newIndex = draggedBackWard ? swapIndex : swapIndex + 1;
      oldElement = draggedBackWard ? arr[newIndex - 1] : arr[newIndex + 1 >= arr.length ? newIndex : newIndex + 1];
    } else {
      newIndex = draggedBackWard ? swapIndex - 1 : swapIndex;
      oldElement = draggedBackWard ? arr[newIndex - 1 < 0 ? 0 : newIndex - 1] : arr[newIndex + 1];
    }
    return {
      newIndex,
      oldIndex: index,
      newElement: { ...arr[newIndex] },
      oldElement: { ...oldElement },
      originArr: [...originArr],
    };
  }

  private dragOverItem(e: DragEvent, item: HTMLElement) {
    e.preventDefault();
    if (item === this.draggingItem.el) {
      return;
    }
    // offsetX 鼠标据当前元素左边的距离
    const { offsetX, target } = e;
    const { offsetWidth } = target as HTMLElement;
    this.draggingItem.insertAfter = offsetX > (offsetWidth / 2);
    // 获取拖拽元素的前后的元素
    const index = this.elements.findIndex(i => i.dataset.index === this.draggingItem.el?.dataset.index);
    const preElement = this.elements[index - 1];
    const nextElement = this.elements[index + 1];
    // 如果往拖拽元素的前一个元素后面插入或者后一个元素前插入，禁止拖拽。（添加未交换成功的动画）
    if ((item === preElement && this.draggingItem.insertAfter) || (item === nextElement && !this.draggingItem.insertAfter)) {
      e.dataTransfer!.dropEffect = 'none';
    }
    removeInsertion(item);
    addInsertion(item, this.draggingItem.insertAfter);
  }

  private dropItem(item: HTMLElement) {
    if (this.draggingItem.el === item) {
      return;
    }
    this.draggingItem.dropEl = item;
    this.completeCallback();
    removeInsertion(item);
  }
}
