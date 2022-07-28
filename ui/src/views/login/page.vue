<template>
  <div class="page" v-loading="isAuthorize">
    <div v-show="!isAuthorize && isShow">
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
      <login :code="code" :error_description="error_description" :reference="reference" :owner="owner"/>
      <bottom-nav/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onMounted, ref } from 'vue';
import BottomNav from '@/views/nav/bottom.vue';
import Login from '@/views/common/login.vue';
import { useRoute, useRouter } from 'vue-router';
import { AUTHORIZE_INDEX } from '@/router/path-def';
import { namespace } from '@/store/modules/session';
import { createNamespacedHelpers, useStore } from 'vuex';
import { IState } from '@/model/modules/session';
import { IGitRepoTokenRefreshingDto } from '@/api/dto/session';

const { mapActions: mapSessionActions } = createNamespacedHelpers(namespace);
export default defineComponent({
  components: { BottomNav, Login },
  props: {
    reference: String,
    owner: String,
    code: String,
    error_description: String,
  },
  setup(props) {
    const route = useRoute();
    const router = useRouter();
    const store = useStore();
    const entry = store.state.entry;
    const isShow = ref<boolean>(false);
    const { proxy } = getCurrentInstance() as any;
    const { session, associationData } = store.state[namespace] as IState;
    onMounted(async () => {
      // 如果页面嵌入在iframe里面，localstorage中session存在，直接进入首页，condition防止参数被串改
      // session可能被清空
      if (!session) {
        isShow.value = true;
        return;
      }
      if (entry && associationData) {
        if ((associationData.ref !== props.reference || associationData.owner !== props.owner)) {
          try {
            // 调用refreshApi
            await proxy.oauthRefresh({
              ref: props.reference,
              owner: props.owner,
            } as IGitRepoTokenRefreshingDto);
          } catch (err) {
            isShow.value = true;
            throw err;
          }
        }
        await router.push({ name: 'index' });
        return;
      }
      isShow.value = true;
    });
    const isAuthorize = ref<boolean>(route.path === AUTHORIZE_INDEX);
    return {
      isShow,
      isAuthorize,
      ...mapSessionActions({
        oauthRefresh: 'oauthRefresh',
      }),
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
    box-shadow: 0 6px 16px 4px #E6EEF6;
  }
}
</style>
