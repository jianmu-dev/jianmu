<template>
  <div class="form-input">
    <div>输入框：</div>
    <div>
      <jm-input v-model="text" clearable placeholder="请输入内容"></jm-input>
      <jm-input v-model="icon" clearable placeholder="请输入内容"></jm-input>
      <jm-input disabled placeholder="请输入内容"></jm-input>
      <jm-input type="password" clearable show-password
                v-model="password" placeholder="请输入密码" suffix-icon="jm-icon-button-search">
      </jm-input>
      <jm-input type="textarea" v-model="textarea" placeholder="请输入内容"></jm-input>
      <jm-input type="textarea" disabled placeholder="请输入内容"></jm-input>
      <jm-input-number v-model="num" :min="1" :max="10"></jm-input-number>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  setup() {
    return {
      text: ref(''),
      icon: ref(''),
      password: ref(''),
      textarea: ref(''),
      num: ref(2),
    };
  },
});
</script>

<style scoped lang="less">
.form-input {
  display: flex;
  align-items: center;

  > :nth-child(n) {
    margin-bottom: 20px;
  }

  > :last-child {
    width: 80%;
  }
}
</style>