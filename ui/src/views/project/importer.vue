<template>
  <div class="project-importer">
    <div class="steps">
      <div>
        <jm-steps :active="step" finish-status="success">
          <jm-step title="克隆Git仓库"/>
          <jm-step title="选择DSL文件"/>
        </jm-steps>
      </div>
    </div>
    <div class="step">
      <jm-scrollbar>
        <step-one v-show="step === 0"/>
        <step-two v-if="step === 1" :data="stepTwoData"/>
      </jm-scrollbar>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, provide, ref } from 'vue';
import StepOne from './import-step-one.vue';
import StepTwo from './import-step-two.vue';
import { IGitVo } from '@/api/dto/project';
import { IGitCloneForm } from '@/model/modules/project';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/session';
import LoginVerify from '@/views/login/dialog.vue';

export default defineComponent({
  components: { StepOne, StepTwo },
  setup() {
    const { appContext } = getCurrentInstance() as any;
    const store = useStore();
    const sessionState = { ...store.state[namespace] };
    if (!sessionState.session) {
      store.dispatch(`${namespace}/openAuthDialog`, {
        appContext, LoginVerify,
      });
    }
    const step = ref<number>(0);
    const stepTwoData = ref<{
      git: IGitVo;
      gitCloneForm: IGitCloneForm;
    }>();

    const nextStep = (git: IGitVo, gitCloneForm: IGitCloneForm) => {
      stepTwoData.value = { git, gitCloneForm };

      step.value = 1;
    };

    provide('nextStep', nextStep);
    provide('previousStep', () => (step.value = 0));

    return {
      step,
      stepTwoData,
    };
  },
});
</script>

<style scoped lang="less">
.project-importer {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;

  .steps {
    border-radius: 4px;
    background-color: #ffffff;
    border: 1px solid #e6ebf2;

    > div {
      width: 30%;
      margin: 0 auto;
      padding: 30px 100px 30px 0;
    }
  }

  .step {
    margin-top: 16px;
    border-radius: 4px;
    background-color: #ffffff;
    height: calc(100vh - 250px);
  }
}
</style>
