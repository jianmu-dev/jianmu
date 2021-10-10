import { ElMessageBox } from 'element-plus';

// 从场景上说，MessageBox 的作用是美化系统自带的 alert、confirm 和 prompt，
// 因此适合展示较为简单的内容。如果需要弹出较为复杂的内容，请使用 Dialog。

export default {
  ...ElMessageBox,
  name: 'jm-message-box',
};
