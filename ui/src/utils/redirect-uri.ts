// 动态构建redirectUri
export function getRedirectUri(ref?: string, owner?: string) {
  if (ref && owner) {
    return `${location.protocol}//${location.host}/login?ref=${encodeURIComponent(ref)}&owner=${encodeURIComponent(owner)}`;
  }
  return `${location.protocol}//${location.host}/login`;
}
