<template>
  <div class="bottom-nav">
    <span class="copyright">&copy;2020-{{ currentYear }} 版权所有</span>
    <span class="company">九州云信息科技有限公司</span>
    <a href="http://beian.miit.gov.cn/" target="_blank" class="icp">浙ICP备12032350号-5</a>
    <span class="email">邮箱：<a href=mailto:support@jianmu.dev>support@jianmu.dev</a></span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    return {
      currentYear: new Date().getFullYear(),
    };
  },
});
</script>

<style scoped lang="less">
.bottom-nav {
  padding-top: 10px;
  width: 100%;
  height: 50px;
  line-height: 50px;
  font-size: 12px;
  font-weight: 400;
  color: #595959;
  text-align: center;

  .copyright {
    margin-right: 5px;
  }

  .icp, .email {
    margin-left: 10px;
  }

  a {
    color: inherit;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>