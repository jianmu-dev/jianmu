export default class Timer {
  private readonly startTime?: string;
  private readonly endTime?: string;
  private readonly tipAppendToBody: boolean;
  private readonly abbr: boolean;
  // eslint-disable-next-line no-undef
  private timer?: NodeJS.Timeout;

  constructor(startTime: string | undefined, endTime: string | undefined, tipAppendToBody: boolean, abbr: boolean) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.tipAppendToBody = tipAppendToBody;
    this.abbr = abbr;
  }

  /**
   *
   * @param startTimeMillis 开始时间戳
   * @param endTimeMillis 结束时间戳
   */
  private calculateTime(startTimeMillis: number, endTimeMillis: number) {
    const millisecond = endTimeMillis - startTimeMillis;
    if (millisecond < 0) {
      return '无';
    }
    const days = Math.floor(millisecond / (1000 * 60 * 60 * 24));
    const hours = Math.floor((millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((millisecond % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = (millisecond % (1000 * 60)) / 1000;

    let result = '';

    if (days > 0) {
      result += `${days}d `;
    }

    if (hours > 0 && hours < 24) {
      result += `${hours}h `;
    } else if (hours === 24) {
      result += '0h ';
    }

    if (minutes > 0 && minutes < 60) {
      result += `${minutes}m `;
    } else if (minutes === 60) {
      result += '0m ';
    }
    if (seconds === 0) {
      result += '0s';
    } else if (seconds > 0 && seconds < 1) {
      result += millisecond > 60 * 1000 ? '0s' : '不足1s';
    } else if (seconds >= 1 && seconds < 60) {
      result += `${Math.floor(seconds)}s`;
    } else if (seconds === 60) {
      result += '0s';
    }
    if (this.abbr) {
      const arr = result.split(' ');
      if (arr.length > 2) {
        return `${arr[0]} ${arr[1]}`;
      }
    }
    return result || '无';
  }

  /**
   * 向外提供的获取时间方法
   * @param callBack
   */
  getTime(callBack: (time: string) => void): void {
    if (!this.startTime && !this.endTime) {
      callBack('无');
      return;
    }
    let startTimeMillis;
    let endTimeMillis;
    // 倒计时（时间递减）
    if (!this.startTime) {
      const handler = () => {
        startTimeMillis = new Date().getTime();
        endTimeMillis = Date.parse(this.endTime!);
        const result = this.calculateTime(startTimeMillis, endTimeMillis);
        callBack(result);
        return handler;
      };
      // 设置定时任务之前，需先调用一次，不然渲染有问题
      this.timer = setInterval(handler(), 1000);
      return;
    } else {
      startTimeMillis = Date.parse(this.startTime);
    }
    // 计时（时间自增）
    if (!this.endTime) {
      const handler = () => {
        startTimeMillis = Date.parse(this.startTime!);
        endTimeMillis = new Date().getTime();
        const result = this.calculateTime(startTimeMillis, endTimeMillis);
        callBack(result);
        return handler;
      };
      this.timer = setInterval(handler(), 1000);
      return;
    } else {
      endTimeMillis = Date.parse(this.endTime);
    }
    const result = this.calculateTime(startTimeMillis, endTimeMillis);
    callBack(result);
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
