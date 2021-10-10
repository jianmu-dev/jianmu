let initialRightValue: number;

/**
 * 适配位置
 * @param platformMain
 * @param rightTopBtn
 */
function adaptPosition(platformMain: HTMLElement, rightTopBtn: HTMLElement) {
  if (!initialRightValue) {
    let temp: string;

    // @ts-ignore
    if (rightTopBtn.currentStyle) {
      // @ts-ignore
      temp = rightTopBtn.currentStyle['right'];
    } else {
      // @ts-ignore
      temp = getComputedStyle(rightTopBtn, null)['right'];
    }
    initialRightValue = +temp.substring(0, temp.indexOf('px'));
  }

  rightTopBtn.style.right = (platformMain.offsetLeft + initialRightValue) + 'px';
  rightTopBtn.style.display = 'block';
}

// 检测按钮是否存在，若存在，且未设置right时，初始化
setInterval(() => {
  const rightTopBtns = document.getElementsByClassName('right-top-btn');

  if (rightTopBtns.length === 0) {
    return;
  }

  const platformMain = document.getElementById('platform-main');

  if (!platformMain) {
    return;
  }

  for (let i = 0; i < rightTopBtns.length; i++) {
    const rightTopBtn = rightTopBtns[i] as HTMLElement;

    if (rightTopBtn.style.right) {
      continue;
    }

    adaptPosition(platformMain as HTMLElement, rightTopBtns[i] as HTMLElement);
  }

}, 100);

(function () {
  const curOnresize = window.onresize;

  // 全局注册
  window.onresize = function (evt: UIEvent) {
    if (curOnresize) {
      // @ts-ignore
      curOnresize(evt);
    }

    const rightTopBtns = document.getElementsByClassName('right-top-btn');

    if (rightTopBtns.length === 0) {
      return;
    }

    const platformMain = document.getElementById('platform-main');

    if (!platformMain) {
      return;
    }

    for (let i = 0; i < rightTopBtns.length; i++) {
      adaptPosition(platformMain as HTMLElement, rightTopBtns[i] as HTMLElement);
    }
  };
})();