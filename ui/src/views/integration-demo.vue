<template>
  <div class="integration-demo">
    <div class="top">
      <div class="desc">
        <p>从仓库url中获取owner和repo，格式：https://testforgeplus.trustie.net/${owner}/${ref}</p>
        <p>获取userId：<a href="https://testforgeplus.trustie.net/api/users/me.json" target="_blank">https://testforgeplus.trustie.net/api/users/me.json</a>
        </p>
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
            <jm-input v-model="form.owner" size="small" clearable placeholder="仓库唯一标识"
                      @focus="iframeVisible=false" @keyup.enter="confirm"/>
          </jm-form-item>
          <jm-form-item label="ref:" prop="ref">
            <jm-input v-model="form.ref" size="small" clearable placeholder="用户登录名或组织账号"
                      @focus="iframeVisible=false" @keyup.enter="confirm"/>
          </jm-form-item>
          <jm-form-item label="userId:" prop="userId">
            <jm-input v-model="form.userId" size="small" clearable placeholder="用户唯一标识"
                      @focus="iframeVisible=false" @keyup.enter="confirm"/>
          </jm-form-item>
          <jm-form-item>
            <jm-button type="primary" size="small" @click="confirm">确定</jm-button>
          </jm-form-item>
        </jm-form>
      </div>
    </div>
    <iframe id="ifm" v-if="iframeVisible" :src="iframeSrc" :style="{height:iframeHeight}"/>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, nextTick, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { restProxy } from '@/api';
import { AUTHORIZE_INDEX } from '@/router/path-def';

function getCode(owner: string, ref: string, userId: string): Promise<string> {
  return restProxy({
    url: '/auth/oauth2/login/silent/git_repo/code',
    method: 'post',
    payload: {
      owner,
      ref,
      userId,
      timestamp: new Date().getTime(),
    },
  });
}

export default defineComponent({
  props: {
    owner: {
      type: String,
      default: '',
    },
    reference: {
      type: String,
      default: '',
    },
    userId: {
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
      ref: props.reference,
      userId: props.userId,
    });
    const code = ref<string>();
    const iframeSrc = computed<string>(() =>
      !code.value ? '' : `http://127.0.0.1:3000${AUTHORIZE_INDEX}?code=${encodeURIComponent(code.value)}`);
    const iframeVisible = ref<boolean>(false);
    const iframeHeight = ref<string>('');
    window.addEventListener('message', e => {
      iframeHeight.value = JSON.parse(e.data).height + 'px';
    });
    onMounted(async () => {
      const { owner, reference: ref, userId } = props;
      if (!owner || !ref || !userId) {
        return;
      }

      // 获取code
      code.value = await getCode(owner, ref, userId);
      iframeVisible.value = true;
    });

    return {
      iframeHeight,
      formRef,
      form,
      rules: {
        owner: [{ required: true, trigger: 'blur' }],
        ref: [{ required: true, trigger: 'blur' }],
        userId: [{ required: true, trigger: 'blur' }],
      },
      iframeVisible,
      iframeSrc,
      confirm: async (e: any) => {
        e.target.blur();

        formRef.value!.validate(async (valid: boolean) => {
          if (!valid) {
            return false;
          }

          const { owner, ref, userId } = form.value;

          // 同步地址栏参数
          await router.push(`${route.path}?owner=${encodeURIComponent(owner)}&ref=${encodeURIComponent(ref)}&userId=${encodeURIComponent(userId)}`);

          // 获取code
          // code.value = await getCode(owner, ref, userId);

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
  overflow: auto;
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
    border: 0;

    html {
      height: 100%;
    }
  }
}
</style>
