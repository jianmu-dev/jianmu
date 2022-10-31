export async function pushTop(url: string) {
  window.top.location.href = url;
}
