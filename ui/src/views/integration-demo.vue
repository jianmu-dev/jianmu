<template>
  <div class="integration-demo">
    <div class="top">
      <div class="desc">
        <p>从仓库url中获取Owner和Repo，格式：https://testforgeplus.trustie.net/${owner}/${repo}</p>
        <p>Git平台：<a href="https://testforgeplus.trustie.net/" target="_blank">https://testforgeplus.trustie.net/</a>
        </p>
        <p>新建仓库：<a href="https://testforgeplus.trustie.net/projects/deposit/new" target="_blank">https://testforgeplus.trustie.net/projects/deposit/new</a>
        </p>
      </div>
    </div>
    <div class="form">
      <div>
        <jm-form ref="formRef" :inline="true" :model="form" :rules="rules" @submit.prevent>
          <jm-form-item label="owner:" prop="owner">
            <jm-input v-model="form.owner" @focus="iframeVisible=false" size="small" clearable
                      placeholder="如，jianmu-dev"/>
          </jm-form-item>
          <jm-form-item label="repo:" prop="repo">
            <jm-input v-model="form.repo" @focus="iframeVisible=false" size="small" clearable
                      placeholder="如，jianmu-ci-server"/>
          </jm-form-item>
          <jm-form-item>
            <jm-button type="primary" size="small" @click="confirm">确定</jm-button>
          </jm-form-item>
        </jm-form>
      </div>
    </div>
    <iframe v-if="iframeVisible" :src="iframeSrc"/>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, nextTick, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

export default defineComponent({
  props: {
    owner: {
      type: String,
      default: '',
    },
    repo: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    const router = useRouter();
    const route = useRoute();
    const formRef = ref<HTMLFormElement>();
    const form = ref({
      owner: props.owner,
      repo: props.repo,
    });
    const iframeSrc = computed<string>(() =>
      `/login?gitRepoOwner=${encodeURIComponent(form.value.owner)}&gitRepo=${encodeURIComponent(form.value.repo)}`);
    const iframeVisible = ref<boolean>(!!(props.owner && props.repo));

    return {
      formRef,
      form,
      rules: {
        owner: [{ required: true, trigger: 'blur' }],
        repo: [{ required: true, trigger: 'blur' }],
      },
      iframeVisible,
      iframeSrc,
      confirm: async () => {
        formRef.value!.validate(async (valid: boolean) => {
          if (!valid) {
            return false;
          }
          // 同步地址栏参数
          await router.push(`${route.path}?owner=${encodeURIComponent(form.value.owner)}&repo=${encodeURIComponent(form.value.repo)}`);

          iframeVisible.value = false;
          await nextTick();
          iframeVisible.value = true;
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.integration-demo {
  width: 1200px;
  height: 100vh;
  margin: 0 auto;

  .top {
    height: 100px;

    .desc {
      font-size: 14px;
      height: 75px;
      line-height: 25px;
    }
  }

  .form {
    height: 70px;
    box-sizing: border-box;
    border-bottom: 1px solid #E6EBF2;
  }

  iframe {
    width: 100%;
    height: calc(100vh - 170px);
    border: 0;
  }
}
</style>
