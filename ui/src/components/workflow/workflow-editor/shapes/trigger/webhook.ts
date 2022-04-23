import { ports, shapeSize } from '../gengral-config';
import { NodeTypeEnum } from '../../model/enumeration';

const { width, height } = shapeSize;

export default {
  shape: 'vue-shape',
  width,
  height,
  component: 'custom-vue-shape',
  data: {
    nodeType: NodeTypeEnum.WEBHOOK,
    text: 'webhook',
  },
  ports: { ...ports },
};