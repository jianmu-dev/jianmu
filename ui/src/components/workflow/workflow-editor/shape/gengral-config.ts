export const PRIMARY_COLOR = '#096DD9';

export const NODE = {
  iconSize: {
    width: 72,
    height: 72,
  },
  textMaxHeight: 40,
};

export const EDGE = {
  stroke: {
    _default: '#667085',
    connecting: '#A7B0BB',
    hover: '#B9CFE6',
  },
};

export const PORT = {
  fill: {
    _default: '#FFFFFF',
    connectingSource: '#A7B0BB',
    connectingTarget: PRIMARY_COLOR,
  },
};

export const PORTS = {
  groups: {
    top: {
      position: 'top',
      attrs: {
        circle: {
          r: 5,
          magnet: true,
          stroke: '#A7B0BB',
          strokeWidth: 1,
          fill: PORT.fill._default,
          style: {
            visibility: 'hidden',
          },
        },
      },
    },
    right: {
      position: 'right',
      attrs: {
        circle: {
          r: 5,
          magnet: true,
          stroke: '#A7B0BB',
          strokeWidth: 1,
          fill: PORT.fill._default,
          style: {
            visibility: 'hidden',
          },
        },
      },
    },
    bottom: {
      position: 'bottom',
      attrs: {
        circle: {
          r: 5,
          magnet: true,
          stroke: '#A7B0BB',
          strokeWidth: 1,
          fill: PORT.fill._default,
          style: {
            visibility: 'hidden',
          },
        },
      },
    },
    left: {
      position: 'left',
      attrs: {
        circle: {
          r: 5,
          magnet: true,
          stroke: '#A7B0BB',
          strokeWidth: 1,
          fill: PORT.fill._default,
          style: {
            visibility: 'hidden',
          },
        },
      },
    },
  },
  items: [
    {
      group: 'top',
    },
    {
      group: 'right',
    },
    {
      group: 'bottom',
    },
    {
      group: 'left',
    },
  ],
};