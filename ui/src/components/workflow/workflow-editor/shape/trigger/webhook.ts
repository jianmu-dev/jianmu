import { PORTS, SHAPE_SIZE } from '../gengral-config';
import { NodeTypeEnum } from '../../model/enumeration';

const { width, height } = SHAPE_SIZE;

export default {
  shape: 'vue-shape',
  width,
  height,
  component: 'custom-vue-shape',
  data: {
    nodeType: NodeTypeEnum.WEBHOOK,
    text: 'webhook',
  },
  ports: { ...PORTS },
};