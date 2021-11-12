<template>
  <jm-dialog
    title="Webhook地址"
    v-model="dialogVisible"
    width="650px"
    @close="close"
  >
    <div class="webhook" v-loading="loading">
      <div class="info">可以通过调用以下地址来触发流程执行</div>
      <div class="link">
        <jm-tooltip v-if="link" :content="link" placement="top">
          <div class="jm-icon-input-hook ellipsis">{{ link }}</div>
        </jm-tooltip>
        <router-link v-else class="jm-icon-input-hook" :to="{name: 'login'}">登录后可查看</router-link>
      </div>
      <div class="operation">
        <jm-button type="primary" size="small" @click="copy" :disabled="!webhook">复制链接</jm-button>
      </div>
    </div>
  </jm-dialog>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeMount, ref, SetupContext } from 'vue';
import useClipboard from 'vue-clipboard3';
import { HttpError } from '@/utils/rest/error';
import { fetchTriggerWebhook } from '@/api/view-no-auth';

export default defineComponent({
  props: {
    projectId: {
      type: String,
      required: true,
    },
  },
  // 覆盖dialog的close事件
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const { toClipboard } = useClipboard();
    const dialogVisible = ref<boolean>(true);
    const loading = ref<boolean>(false);
    const webhook = ref<string>();
    const link = computed<string | undefined>(
      () => webhook.value && `${window.location.protocol}//${window.location.host}${webhook.value}`);
    const close = () => emit('close');

    const loadLink = async () => {
      if (webhook.value) {
        return;
      }

      try {
        loading.value = true;

        const { webhook: webhookUri } = await fetchTriggerWebhook(props.projectId);
        webhook.value = webhookUri;
      } catch (err) {
        if (!(err instanceof HttpError)) {
          close();
          proxy.$throw(err, proxy);
          return;
        }

        const { response: { status } } = err as HttpError;

        if (status !== 401) {
          close();
          proxy.$throw(err, proxy);
          return;
        }
      } finally {
        loading.value = false;
      }
    };

    onBeforeMount(() => loadLink());

    return {
      dialogVisible,
      loading,
      webhook,
      link,
      close,
      copy: async () => {
        if (!link.value) {
          return;
        }

        try {
          await toClipboard(link.value);
          proxy.$success('复制成功');
        } catch (err) {
          proxy.$error('复制失败，请手动复制');
          console.error(err);
        }
      },
    };
  },
  emits: ['close'],
});
</script>

<style scoped lang="less">
.webhook {
  position: relative;
  font-size: 14px;
  color: #082340;
  margin-bottom: 15px;
  height: 130px;

  .ellipsis {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  > div {
    margin-top: 15px;
  }

  .info {
  }

  .link {
    padding: 5px 0;
    background-color: #F6F8FB;
    border-radius: 2px;

    > :first-child:before {
      margin: 0 10px;
      font-size: 16px;
      color: #6B7B8D;
    }

    a {
      color: inherit;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .operation {
    position: absolute;
    right: 0;
    bottom: 5px;
  }
}
</style>