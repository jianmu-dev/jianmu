if (import.meta.env.VITE_BAIDU_TONGJI_ID) {
  console.debug('Baidu tongji installed');
  (function () {
    const hm = document.createElement('script');
    hm.src = `https://hm.baidu.com/hm.js?${import.meta.env.VITE_BAIDU_TONGJI_ID}`;
    const s = document.getElementsByTagName('script')[0];
    // @ts-ignore
    s.parentNode.insertBefore(hm, s);
  })();
}
