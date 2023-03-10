/**
 * 唯一标识重复错误
 */
export class RefDuplicateError extends Error {
  readonly ref: string;

  constructor(ref: string, message: string) {
    super(message);
    this.ref = ref;
  }
}
