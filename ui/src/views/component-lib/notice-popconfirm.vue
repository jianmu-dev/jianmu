<template>
  <div class="notice-popconfirm-box">
    气泡确认框：
    <jm-popconfirm title="default">
      <template #reference>
        <jm-button type="text">点击打开 default</jm-button>
      </template>
    </jm-popconfirm>
    <jm-popconfirm icon="jm-icon-success" title="success">
      <template #reference>
        <jm-button type="text">点击打开 success</jm-button>
      </template>
    </jm-popconfirm>
    <jm-popconfirm icon="jm-icon-warning" title="warning">
      <template #reference>
        <jm-button type="text">点击打开 warning</jm-button>
      </template>
    </jm-popconfirm>
    <jm-popconfirm icon="jm-icon-info" title="info">
      <template #reference>
        <jm-button type="text">点击打开 info</jm-button>
      </template>
    </jm-popconfirm>
    <jm-popconfirm icon="jm-icon-error" title="error">
      <template #reference>
        <jm-button type="text">点击打开 error</jm-button>
      </template>
    </jm-popconfirm>
  </div>
</template>