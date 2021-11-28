/**
 * 裁剪图片圆角
 * @param url
 * @param width
 * @param height
 * @param radius
 * @param callback
 */
export function clipImageBorder(
  url: string,
  width: number,
  height: number,
  radius: number,
  callback: (base64: string) => void,
) {
  const image = new Image();
  image.src = url;
  // 解决：Tainted canvases may not be exported.
  image.setAttribute('crossOrigin', 'Anonymous');

  image.onload = function () {
    const x = 0,
      y = 0;
    const w = width,
      h = height,
      r = radius;

    const canvas = document.createElement('canvas');
    canvas.width = w;
    canvas.height = h;
    const ctx = canvas.getContext('2d') as CanvasRenderingContext2D;

    ctx.beginPath();
    // 左上角
    ctx.arc(x + r, y + r, r, Math.PI, 1.5 * Math.PI);
    // 右上角
    ctx.arc(x + w - r, y + r, r, 1.5 * Math.PI, 2 * Math.PI);
    // 右下角
    ctx.arc(x + w - r, y + h - r, r, 0, 0.5 * Math.PI);
    // 左下角
    ctx.arc(x + r, y + h - r, r, 0.5 * Math.PI, Math.PI);
    // 此方法下面的部分为待剪切区域，上面的部分为剪切区域
    ctx.clip();
    ctx.drawImage(image, x, y, w, h);
    ctx.closePath();

    const base64 = canvas.toDataURL('image/png', 1);

    callback(base64);

    return base64;
  };
}
