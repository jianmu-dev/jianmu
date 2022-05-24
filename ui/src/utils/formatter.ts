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
