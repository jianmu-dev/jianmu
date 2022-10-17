export default class TimeViewer {
  private readonly value: string;
  // eslint-disable-next-line no-undef
  private timer?: NodeJS.Timeout;

  constructor(value: string) {
    this.value = value;
  }

  /**
   * 根据结束时间得到距当前过了多久
   * @param endTime
   */
  private timeToEnd(endTime: number = new Date().getTime()): string {
    // 过了多少秒 (当前时间戳 减去 结束时间戳)
    const nowTime = new Date().getTime();
    const seconds = Math.floor((nowTime - endTime) / 1000);
    if (seconds <= 60) {
      // 一分钟内
      return '刚刚';
    } else if (seconds > 60 && seconds < 3600) {
      // 几分钟前 一个小时内(60 * 60) 60个60秒 = 3600
      const minute = Math.floor(seconds / 60);
      return minute + '分钟前';
    } else if (seconds >= 3600 && seconds < 86400) {
      // 几小时前 一天之内 (24 * 3600) 24个3600秒 = 86400
      const hour = Math.floor(seconds / 3600);
      return hour + '小时前';
    } else if (seconds >= 86400 && seconds < 2592000) {
      // 一个月之内 (30 * 86400) 30个86400秒 = 2592000
      const day = Math.floor(seconds / 86400);
      return day + '天前';
    } else if (seconds >= 2592000 && seconds <= 31104000) {
      // 一年之内 (12 * 2592000) 12个2592000秒 = 31104000
      const month = Math.floor(seconds / 2592000);
      return month + '个月前';
    } else {
      // 一年之上
      const year = Math.floor(seconds / 31104000);
      return year + '年前';
    }
  }

  /**
   * 提供给外部获取动态计时后的展示文本
   * @param callback
   */
  getTime(callback: (timeText: string) => void): void {
    this.clearTimer();
    const setTimer = (startTime: number, interval = 1000, cb: (n: number) => string) => {
      this.timer = setInterval(() => {
        callback(cb(startTime));
      }, interval);
    };
    const seconds = Math.floor((new Date().getTime() - new Date(this.value).getTime()) / 1000);
    // 没超过一天设置定时器
    if (seconds <= 86400) {
      // 超过一分钟 定时器一分钟执行一次; 超过一小时 定时器一小时执行一次
      const interval = seconds >= 3600 ? 3600000 : seconds >= 60 ? 60000 : 1000;
      callback(this.timeToEnd(new Date(this.value).getTime()));
      setTimer(new Date(this.value).getTime(), interval, this.timeToEnd);
    } else {
      callback(this.timeToEnd(new Date(this.value).getTime()));
    }
  }

  /**
   * 清除定时器
   */
  clearTimer(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }
}
