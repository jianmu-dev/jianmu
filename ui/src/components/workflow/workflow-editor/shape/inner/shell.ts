import { ports, shapeSize } from '../gengral-config';
import { NodeTypeEnum } from '../../model/enumeration';

const { width, height } = shapeSize;

export default {
  shape: 'vue-shape',
  width,
  height,
  component: 'custom-vue-shape',
  data: {
    nodeType: NodeTypeEnum.SHELL,
    text: 'shell',
  },
  // tools: [
  //   'button-remove',
  //   {
  //     name: 'boundary',
  //     args: {
  //       padding: 5,
  //     },
  //   },
  // ],
  ports: { ...ports },
};