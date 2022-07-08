// 动态构建redirectUri
export function getRedirectUri(gitRepo?: string, gitRepoOwner?: string) {
  if(gitRepo&&gitRepoOwner){
    return `${location.protocol}//${location.host}/login?gitRepo=${encodeURIComponent(gitRepo)}&gitRepoOwner=${encodeURIComponent(gitRepoOwner)}`;
  }
  return `${location.protocol}//${location.host}/login`;
}
