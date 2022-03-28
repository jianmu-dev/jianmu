/**
 * 获取存储
 * @param key 保存在localstorage的key
 */
export function getStorage<T>(key: string): T | undefined {
  let storage;

  const storageStr = localStorage.getItem(key);
  if (!storageStr) {
    return;
  }
  try {
    storage = JSON.parse(storageStr);
  } catch (err) {
    console.warn(err.message);
  }
  return storage;
}

/**
 * @param key localstorage的键
 * @param value localstorage的值
 */
export function setStorage(key: string, value: object): void {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (err) {
    console.warn(err.message);
  }
}
