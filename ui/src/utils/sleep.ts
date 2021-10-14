/**
 * sleep
 * @param time  单位：毫秒
 */
export default function sleep(time: number) {
  return new Promise(resolve => setTimeout(resolve, time));
}