if (import.meta.env.MODE === 'cdn') {
  console.debug('Baidu tongji installed');
  // @ts-ignore
  // eslint-disable-next-line no-use-before-define
  const _hmt = _hmt || [];
  (function () {
    const hm = document.createElement('script');
    hm.src = 'https://hm.baidu.com/hm.js?87e15dc54a0ffd343a6bda56232c2fcf';
    const s = document.getElementsByTagName('script')[0];
    // @ts-ignore
    s.parentNode.insertBefore(hm, s);
  })();
}
