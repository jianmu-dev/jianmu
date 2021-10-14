import { ElTableColumn } from 'element-plus';

ElTableColumn.props = {
  ...ElTableColumn.props,
  // 篡改默认值
  showOverflowTooltip: {
    type: Boolean,
    default: true,
  },
};

export default {
  ...ElTableColumn,
  name: 'jm-table-column',
};
