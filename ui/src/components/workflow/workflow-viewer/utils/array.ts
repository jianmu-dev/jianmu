interface Array<T> {
  split(size: number): Array<T[]>;
}

Array.prototype.split = function <T>(size: number): Array<T[]> {
  const arr = new Array<T[]>();

  for (let i = 0; i < Math.ceil(this.length / size); i++) {
    arr[i] = [];
  }

  let j = 0;
  for (let i = 0; i < this.length; i++) {
    if (i % size !== 0 || i === 0) {
      arr[j].push(this[i]);
      continue;
    }

    arr[++j].push(this[i]);
  }

  return arr;
};