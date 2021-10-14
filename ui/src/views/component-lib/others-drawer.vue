<template>
  <div class="others-drawer">
    抽屉：
    <jm-radio-group v-model="direction">
      <jm-radio label="ltr">从左往右开</jm-radio>
      <jm-radio label="rtl">从右往左开</jm-radio>
      <jm-radio label="ttb">从上往下开</jm-radio>
      <jm-radio label="btt">从下往上开</jm-radio>
    </jm-radio-group>

    <jm-button @click="drawer = true" type="primary" size="small" style="margin-left: 16px;">
      点击打开Drawer
    </jm-button>

    <jm-drawer
      title="抽屉标题"
      size="40%"
      v-model="drawer"
      :direction="direction"
      :before-close="handleClose" destroy-on-close>
      <span>抽屉内容</span>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref } from 'vue';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;

    return {
      drawer: ref<boolean>(false),
      direction: ref<string>('rtl'),

      handleClose(done: any) {
        proxy.$root.$confirm('确认关闭？')
          .then(() => {
            done();
          })
          .catch(() => {
          });
      },
    };
  },
});
</script>

<style scoped lang="less">
.others-drawer {

}
</style>