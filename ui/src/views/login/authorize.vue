<template>
  <div class="authorize" v-loading="loading"></div>
</template>

<script lang='ts'>
import { defineComponent, getCurrentInstance, onMounted, ref } from 'vue';
import { createNamespacedHelpers } from 'vuex';
import { useRouter } from 'vue-router';
import { namespace } from '@/store/modules/session';

const { mapActions } = createNamespacedHelpers(namespace);
export default defineComponent({
  props: {
    code: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const loading = ref<boolean>(false);
    onMounted(async () => {
      loading.value = true;
      try {
        await proxy.oauthSilentLogin(props.code);
        await router.push({ name: 'index' });
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    });
    return {
      loading,
      ...mapActions({
        oauthSilentLogin: 'oauthSilentLogin',
      }),
    };
  },
});
</script>

<style scoped lang='less'>
.authorize {
  width: 100vw;
  height: 100vh;
}
</style>
