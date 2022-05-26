<template>
  <div class="login">
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
        <jm-button type="primary" @click="login" :loading="loading"
        >登录
        </jm-button
        >
      </div>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref } from 'vue';
import { createNamespacedHelpers, mapActions, useStore } from 'vuex';
import { ILoginForm, IState } from '@/model/modules/session';
import { namespace } from '@/store/modules/session';
import { useRoute, useRouter } from 'vue-router';
import { PLATFORM_INDEX } from '@/router/path-def';

const { mapActions: mapSessionActions } = createNamespacedHelpers(namespace);

export default defineComponent({
  emits: ['logged'],
  props: {
    redirectUrl: String,
  },
  setup(props: any, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const route = useRoute();
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
            emit('logged');
          }
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
  box-sizing: border-box;
  margin: 0 auto;
  background-color: #ffffff;

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
}
</style>
