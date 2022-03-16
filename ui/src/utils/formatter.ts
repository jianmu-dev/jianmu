import dateFormat from 'dateformat';
import { DEFAULT_DATETIME_FORMAT } from '@/utils/constants';

/**
 * datetime格式器
 * @param value   格式：yyyy-MM-ddTHH:mm:ssZ
 */
export function datetimeFormatter(value?: string) {
  if (!value) {
    return '无';
  }

  return dateFormat(Date.parse(value), DEFAULT_DATETIME_FORMAT);
}

/**
 * 执行时间格式器
 * @param startTime     格式：yyyy-MM-ddTHH:mm:ssZ
 * @param endTime       格式：yyyy-MM-ddTHH:mm:ssZ
 * @param executing     是否执行中
 */
export function executionTimeFormatter(startTime: string, endTime: string | undefined, executing: boolean) {
  if (!startTime) {
    return '无';
  }

  const startTimeMillis = Date.parse(startTime);
  let endTimeMillis;
  if (executing) {
    endTimeMillis = new Date().getTime();
  } else {
    if (!endTime) {
      return '无';
    }
    endTimeMillis = Date.parse(endTime);
  }

  const millisecond = endTimeMillis - startTimeMillis;

  const days = Math.floor(millisecond / (1000 * 60 * 60 * 24));
  const hours = Math.floor((millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((millisecond % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.round((millisecond % (1000 * 60)) / 1000);

  let result = '';
  if (days > 0) {
    result += `${days} 天 `;
  }
  if (hours > 0) {
    result += `${hours} h`;
  }
  if (minutes > 0) {
    result += `${minutes} m `;
  }
  if (seconds >= 0) {
    result += `${seconds} s `;
  }

  return result || '无';
}