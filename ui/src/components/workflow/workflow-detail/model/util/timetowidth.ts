/**
 * 开始时间结束时间 计算px宽度 返回类名
 */
export const startAndEndToWidthClass = (start:string, end: string|undefined):'one'|'two'|'three'|'four' => {
  if (!end || !start) {
    return 'one';
  }
  const sencends = (new Date(end).getTime() - new Date(start).getTime())/1000;
  if (sencends < 60) {
    // 59s
    return 'one';
  } else if (sencends > 60 && sencends < 21600){
    // 59m 59s
    return 'two';
  } else if (sencends > 21600 && sencends < 5184000) {
    // 59h 59m 59s
    return 'three';
  } else {
    // 77d 59h 59m 59s
    return 'four';
  }
};