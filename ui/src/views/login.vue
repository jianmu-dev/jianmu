<template>
  <div class="login">
    <div class="logo">
      <router-link :to="{name: 'index'}">
        <div class="icon"></div>
      </router-link>
      <div class="separator"></div>
      <div>
        <div class="title">自动化集成平台</div>
        <div class="subtitle">Automation Integration Platform</div>
      </div>
    </div>
    <div class="main">
      <div class="desc">欢迎登录</div>
      <jm-form :model="loginForm" :rules="loginRule" ref="loginFormRef">
        <div class="item">
          <jm-form-item prop="username">
            <jm-input v-model="loginForm.username" prefix-icon="jm-icon-input-user" clearable placeholder="请输入用户名"
                      @keyup.enter="login"/>
          </jm-form-item>
        </div>
        <div class="item">
          <jm-form-item prop="password">
            <jm-input v-model="loginForm.password" prefix-icon="jm-icon-input-lock" type="password" clearable
                      show-password placeholder="请输入密码" @keyup.enter="login"/>
          </jm-form-item>
        </div>
        <div class="item">
          <jm-checkbox v-model="loginForm.remember" @keyup.enter="login">
            <span class="label">记住用户名</span>
          </jm-checkbox>
        </div>
        <div class="btn">
          <jm-button type="primary" @click="login" :loading="loading">登录</jm-button>
        </div>
      </jm-form>
    </div>
    <bottom-nav/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref } from 'vue';
import BottomNav from '@/views/nav/bottom.vue';
import { createNamespacedHelpers, mapActions, useStore } from 'vuex';
import { ILoginForm, IState } from '@/model/modules/session';
import { namespace } from '@/store/modules/session';
import { useRouter } from 'vue-router';
import { PLATFORM_INDEX } from '@/router/path-def';

const { mapActions: mapSessionActions } = createNamespacedHelpers(namespace);

export default defineComponent({
  components: { BottomNav },
  props: {
    redirectUrl: String,
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const { username, remember } = useStore().state[namespace] as IState;

    const loginFormRef = ref<any>(null);
    const loginForm = ref<ILoginForm>({
      username: remember ? username : '',
      password: '',
      remember,
    });
    const loading = ref<boolean>(false);

    return {
      loginFormRef,
      loginForm,
      loginRule: ref<object>({
        username: [
          { required: true, message: '用户名不能为空', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' },
        ],
      }),
      loading,
      login: () => {
        // 开启loading
        loading.value = true;

        loginFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            // 关闭loading
            loading.value = false;

            return false;
          }

          proxy.createSession({ ...loginForm.value })
            .then(() => {
              // 初始化vuex根状态
              proxy.initialize()
                .then(() => router.push(props.redirectUrl || PLATFORM_INDEX))
                .catch((err: Error) => {
                  // 关闭loading
                  loading.value = false;

                  proxy.$throw(err, proxy);
                });
            })
            .catch((err: Error) => {
              // 关闭loading
              loading.value = false;

              proxy.$throw(err, proxy);
            });
        });
      },
      ...mapActions(['initialize']),
      ...mapSessionActions({
        createSession: 'create',
      }),
    };
  },
});
</script>

<style scoped lang="less">
.login {
  width: 100vw;
  height: 100vh;
  padding-top: 15vh;

  .logo {
    margin: 0 auto 30px auto;
    width: 350px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .icon {
      width: 130px;
      height: 35px;
      background-image: url('@/assets/svgs/logo/secondary.svg');
      background-repeat: no-repeat;
      background-size: contain;
      background-position: center center;
    }

    .separator {
      width: 1px;
      height: 28px;
      background-color: #B9CFE6;
      border-radius: 1px;
      overflow: hidden;
    }

    .title {
      font-size: 24px;
      font-weight: bold;
      color: #082340;
      letter-spacing: 3.5px;
    }

    .subtitle {
      font-size: 12px;
      color: #082340;
      letter-spacing: 0.3px;
    }
  }

  .main {
    margin: 0 auto 0;
    padding: 30px 30px 40px 30px;
    width: 290px;
    background-color: #FFFFFF;
    border-radius: 4px;

    .desc {
      margin-bottom: 20px;
      font-size: 14px;
      font-weight: bold;
      color: #082340;
    }

    .item {
      margin-bottom: 15px;

      .label {
        font-size: 13px;
        font-weight: 400;
        color: #6B7B8D;
      }
    }

    .btn {
      padding-top: 15px;

      .el-button {
        width: 100%;
        letter-spacing: 5px;
      }
    }
  }
}
</style>
