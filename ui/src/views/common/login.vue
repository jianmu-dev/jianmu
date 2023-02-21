<template>
  <div class="login">
    <div class="number-login" v-if="!loginType">
      <div class="desc">
        <span>欢迎登录</span>
        <div class="tip">
          <slot name="tip"></slot>
        </div>
      </div>
      <jm-form :model="loginForm" :rules="loginRule" ref="loginFormRef">
        <div class="item">
          <jm-form-item prop="username">
            <jm-input
              v-model="loginForm.username"
              prefix-icon="jm-icon-input-user"
              clearable
              placeholder="请输入用户名"
              @keyup.enter="login"
            />
          </jm-form-item>
        </div>
        <div class="item">
          <jm-form-item prop="password">
            <jm-input
              v-model="loginForm.password"
              prefix-icon="jm-icon-input-lock"
              type="password"
              clearable
              show-password
              placeholder="请输入密码"
              @keyup.enter="login"
            />
          </jm-form-item>
        </div>
        <div class="item">
          <jm-checkbox v-model="loginForm.remember" @keyup.enter="login">
            <span class="label">记住用户名</span>
          </jm-checkbox>
        </div>
        <div class="btn">
          <jm-button type="primary" @click="login" :loading="loading">登录 </jm-button>
        </div>
      </jm-form>
    </div>
    <div v-else-if="authError" class="error-login">
      <div class="logo">
        <div class="img"></div>
      </div>
      <div class="tip">登录遇到问题，请尝试重新登录</div>
      <div class="operations">
        <jm-button class="btn cancel" @click="$emit('cancel')">取消</jm-button>
        <jm-button type="primary" class="btn" @click="fetchThirdAuthUrl">重新登录</jm-button>
      </div>
    </div>
    <div :class="[`${loginType.toLowerCase()}-login`, loading ? 'loading' : '']" @click="fetchThirdAuthUrl" v-else>
      <div class="logo">
        <div class="img" v-if="!loading"></div>
        <div class="loading" v-else></div>
      </div>
      <span class="tip">{{ loading ? `${Type} 账号登录中…` : `使用 ${Type} 账号登录` }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeUnmount, onMounted, ref } from 'vue';
import { createNamespacedHelpers, mapActions, useStore } from 'vuex';
import { ILoginForm, IState } from '@/model/modules/session';
import { namespace } from '@/store/modules/session';
import { useRoute, useRouter } from 'vue-router';
import { AUTHORIZE_INDEX, PLATFORM_INDEX } from '@/router/path-def';
import { fetchAuthUrl } from '@/api/session';
import { getRedirectUri } from '@/utils/redirect-uri';

const { mapActions: mapSessionActions, mapMutations } = createNamespacedHelpers(namespace);

export default defineComponent({
  emits: ['logined', 'cancel'],
  props: {
    code: String,
    error_description: String,
    gitRepo: String,
    gitRepoOwner: String,
    type: {
      type: String,
      default: 'index',
    },
  },
  setup(props: any, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    // 系统初始化后，自动决定登录方式（密码/第三方平台登录）
    const loginType = ref<string>(store.state.thirdPartyType);
    const Type = computed<string>(() => {
      switch (loginType.value) {
        case 'GITEE':
          return 'Gitee';
        case 'GITLINK':
          return 'GitLink';
        case 'GITLAB':
          return 'GitLab';
        case 'GITEA':
          return 'Gitea';
        default:
          return '';
      }
    });
    const { username, remember } = store.state[namespace] as IState;
    const loading = ref<boolean>(false);
    const loginFormRef = ref<any>(null);
    const loginForm = ref<ILoginForm>({
      username: remember ? username : '',
      password: '',
      remember,
    });
    // 是否oauth验证出错
    const authError = ref<boolean>(false);
    // 获取三方登录授权地址
    const fetchThirdAuthUrl = async () => {
      authError.value = false;
      localStorage.setItem('temp-login-mode', props.type);
      // 弹框登录方式获取授权地址时显示登录中
      localStorage.getItem('temp-login-mode') !== 'index' && (loading.value = true);
      try {
        const { authorizationUrl } = await fetchAuthUrl({
          thirdPartyType: loginType.value,
          redirectUri: getRedirectUri(props.gitRepo, props.gitRepoOwner),
        });
        // 登录模式（login页面登录/弹窗登录）
        window.open(authorizationUrl, localStorage.getItem('temp-login-mode') !== 'index' ? '_blank' : '_self');
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    const refreshState = async (e: any) => {
      if (e.key === 'session') {
        const newSession = JSON.parse(e.newValue)['_default'].session;
        // 如果session的token不为空,证明是刷新token导致的storage变化，不用提示登录成功[https://gitee.com/jianmu-dev/jianmu/issues/I6FI2D]
        if (store.state[namespace].session?.token) {
          loading.value = false;
          authError.value = false;
          setTimeout(() => {
            emit('logined');
          }, 500);
          return;
        }
        // 登录成功
        loading.value = false;
        authError.value = false;
        proxy.$success('登录成功');
        setTimeout(() => {
          emit('logined');
        }, 500);
        proxy.mutateSession(newSession);
      }
      if (e.key === 'temp-login-error-message') {
        // 只有是弹窗登录授权失败才展示重新登录
        localStorage.getItem('temp-login-mode') !== 'index' && (authError.value = true);
        // 登录失败
        loading.value = false;
      }
    };
    onMounted(async () => {
      window.addEventListener('storage', refreshState);
      // 判断是否为弹窗方式登录
      const dialogLogin = localStorage.getItem('temp-login-mode') !== 'index';
      if (props.error_description) {
        proxy.$error(props.error_description);
        // dialogLogin ? proxy.$error(props.error_description + '，页面即将关闭') : proxy.$error(props.error_description);
        localStorage.setItem('temp-login-error-message', props.error_description);
        dialogLogin &&
          setTimeout(() => {
            window.close();
          }, 2000);
        return;
      }
      // 三方登录 有code码证明进行了授权验证，避免登录接口重复调用
      if (props.code) {
        loading.value = true;
        try {
          await proxy.createOAuthSession({
            code: props.code,
            thirdPartyType: loginType.value,
            redirectUri: getRedirectUri(props.gitRepo, props.gitRepoOwner),
            gitRepo: props.gitRepo,
            gitRepoOwner: props.gitRepoOwner,
          });
          dialogLogin ? window.close() : await router.push(PLATFORM_INDEX);
        } catch (err) {
          proxy.$throw(err, proxy);
          localStorage.setItem('temp-login-error-message', err.message);
          dialogLogin &&
            setTimeout(() => {
              window.close();
            }, 2000);
        } finally {
          loading.value = false;
          emit('logined');
        }
        return;
      }

      if (route.path === AUTHORIZE_INDEX) {
        await fetchThirdAuthUrl();
      }
    });
    onBeforeUnmount(() => {
      window.onstorage = null;
    });
    return {
      authError,
      loginType,
      Type,
      fetchThirdAuthUrl,
      loading,
      loginFormRef,
      loginForm,
      loginRule: ref<Record<string, any>>({
        username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
      }),
      login: () => {
        // 开启loading
        loading.value = true;

        loginFormRef.value.validate(async (valid: boolean) => {
          if (!valid) {
            // 关闭loading
            loading.value = false;

            return false;
          }
          try {
            await proxy.createSession({ ...loginForm.value });
            await proxy.initialize();
            if (route.name === 'login') {
              await router.push(PLATFORM_INDEX);
            }
          } catch (err) {
            proxy.$throw(err, proxy);
          } finally {
            // 关闭loading
            loading.value = false;
            emit('logined');
          }
        });
      },
      ...mapActions(['initialize']),
      ...mapSessionActions({
        createSession: 'create',
        createOAuthSession: 'oauthLogin',
      }),
      ...mapMutations({
        mutateSession: 'oauthMutate',
      }),
    };
  },
});
</script>

<style scoped lang="less">
.login {
  box-sizing: border-box;
  margin: 0 auto;
  background-color: #ffffff;
  position: relative;

  .desc {
    margin-bottom: 20px;
    font-size: 14px;
    font-weight: bold;
    color: #082340;

    .tip {
      font-weight: 400;
      color: #082340;
      font-size: 14px;
      margin-top: 10px;
    }
  }

  .item {
    margin-bottom: 15px;

    .label {
      font-size: 13px;
      font-weight: 400;
      color: #6b7b8d;
    }
  }

  .btn {
    padding-top: 15px;

    .el-button {
      width: 100%;
      letter-spacing: 5px;
    }

    .el-button--primary {
      box-shadow: none;
    }
  }

  .gitee-login,
  .gitlink-login,
  .gitlab-login,
  .gitea-login,
  .error-login {
    cursor: pointer;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;

    &.loading {
      pointer-events: none;
    }

    &:hover {
      .tip {
        color: #096dd9;
      }
    }

    .logo {
      .img {
        width: 56px;
        height: 56px;
        background: url('@/assets/svgs/logo/gitee.svg') no-repeat 100%;
      }

      .loading {
        width: 30px;
        height: 30px;
        background: url('@/assets/svgs/logo/loading.svg') no-repeat;
        animation: rotating 2s linear infinite;
      }
    }

    .tip {
      font-size: 14px;
      font-weight: 400;
      color: #012c53;
      margin-top: 20px;
    }
  }

  .gitlink-login {
    .logo {
      .img {
        width: 56px;
        height: 35px;
        background-image: url('@/assets/svgs/logo/gitlink.svg');
        background-size: cover;
        background-repeat: no-repeat;
      }
    }
  }

  .gitlab-login {
    .logo {
      .img {
        width: 56px;
        height: 56px;
        background-image: url('@/assets/svgs/logo/gitlab.svg');
        background-size: cover;
        background-repeat: no-repeat;
      }
    }
  }

  .gitea-login {
    .logo {
      .img {
        width: 56px;
        height: 56px;
        background-image: url('@/assets/svgs/logo/gitea.svg');
        background-size: cover;
        background-repeat: no-repeat;
      }
    }

    .tip {
      margin-top: 10px;
    }
  }

  .error-login {
    cursor: default;
    width: 100%;

    &:hover {
      .tip {
        color: #012c53;
      }
    }

    .operations {
      display: flex;
      margin-top: 30px;

      .btn {
        cursor: pointer;
        padding: 8px 24px;
        text-align: center;
        border-radius: 2px;
        font-size: 14px;
        font-weight: 400;
        border: none;
        margin: 0;

        &.cancel {
          &:hover {
            background-color: #eeeeee;
          }

          color: #082340;
          background-color: #f5f5f5;
          margin-right: 40px;
        }
      }
    }

    .logo {
      .img {
        width: 56px;
        height: 56px;
        background-image: url('@/assets/svgs/logo/error.svg');
        background-size: cover;
        background-repeat: no-repeat;
      }
    }
  }
}
</style>
