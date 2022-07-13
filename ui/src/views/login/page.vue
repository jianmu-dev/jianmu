<template>
  <div class="page" v-loading="isAuthorize">
    <div v-show="!isAuthorize">
      <div class="logo">
        <router-link :to="{ name: 'index' }">
          <div class="icon"></div>
        </router-link>
        <!--      <div class="separator"></div>-->
        <!--      <div class="desc">-->
        <!--        <div class="title">自动化集成平台</div>-->
        <!--        <div class="subtitle">Automation Integration Platform</div>-->
        <!--      </div>-->
      </div>
      <login :code="code" :error_description="error_description" :gitRepo="gitRepo" :gitRepoOwner="gitRepoOwner"/>
      <bottom-nav/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import BottomNav from '@/views/nav/bottom.vue';
import Login from '@/views/common/login.vue';
import { useRoute } from 'vue-router';
import { AUTHORIZE_INDEX } from '@/router/path-def';

export default defineComponent({
  components: { BottomNav, Login },
  props: {
    gitRepo: String,
    gitRepoOwner: String,
    code: String,
    error_description: String,
  },
  setup() {
    const route = useRoute();
    const isAuthorize = ref<boolean>(route.path === AUTHORIZE_INDEX);
    return {
      isAuthorize,
    };
  },
});
</script>

<style scoped lang="less">
.page {
  width: 100vw;
  height: 100vh;
  padding-top: 15vh;

  .logo {
    margin: 0 auto 15px auto;
    width: 350px;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      width: 150px;
      height: 34px;
      background-image: url('@/assets/svgs/logo/main.svg');
      background-repeat: no-repeat;
      background-size: contain;
      background-position: center center;
    }

    //.separator {
    //  width: 1px;
    //  height: 28px;
    //  background-color: #B9CFE6;
    //  border-radius: 1px;
    //  overflow: hidden;
    //}
    //
    //.desc {
    //  .title {
    //    font-size: 24px;
    //    font-weight: bold;
    //    color: #082340;
    //    letter-spacing: 1px;
    //  }
    //
    //  .subtitle {
    //    white-space: nowrap;
    //    font-size: 12px;
    //    color: #082340;
    //  }
    //}
  }

  ::v-deep(.login) {
    border-radius: 4px;
    padding: 30px;
    width: 350px;
    min-height: 344px;
  }
}
</style>
