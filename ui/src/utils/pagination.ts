/**
 * 计算总页数
 * @param totalElements 总数据数
 * @param pageSize      每页个数
 */
export function calculateTotalPages(totalElements: number, pageSize: number): number {
  return Math.floor(totalElements / pageSize) + (totalElements % pageSize === 0 ? 0 : 1);
}