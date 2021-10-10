export interface IAutoHeight {
  elementId: string,
  offsetTop: number,
}

const registeredHeights: IAutoHeight[] = [];

/**
 * 适配高度
 * @param autoHeight
 */
export function adaptHeight(autoHeight: IAutoHeight) {
  if (!registeredHeights.includes(autoHeight)) {
    registeredHeights.push(autoHeight);
  }

  const element = document.getElementById(autoHeight.elementId);
  if (!element) {
    return;
  }

  element.style.height = (document.documentElement.offsetHeight - autoHeight.offsetTop) + 'px';
}

(function () {
  const curOnresize = window.onresize;

  // 全局注册
  window.onresize = function (evt: UIEvent) {
    if (curOnresize) {
      // @ts-ignore
      curOnresize(evt);
    }

    registeredHeights.forEach(item => adaptHeight(item));
  };
})();