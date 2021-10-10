<template>
  <div class="form-form">
    <div>表单：</div>
    <jm-form :model="ruleForm" :rules="rules" ref="form" label-width="100px" class="demo-ruleForm">
      <jm-form-item label="活动名称" prop="name">
        <jm-input v-model="ruleForm.name" clearable></jm-input>
      </jm-form-item>
      <jm-form-item label="活动区域" prop="region">
        <jm-select v-model="ruleForm.region" placeholder="请选择活动区域">
          <jm-option label="区域一" value="shanghai"></jm-option>
          <jm-option label="区域二" value="beijing"></jm-option>
        </jm-select>
      </jm-form-item>
      <jm-form-item label="活动性质" prop="type">
        <jm-checkbox-group v-model="ruleForm.type">
          <jm-checkbox label="美食/餐厅线上活动" name="type"></jm-checkbox>
          <jm-checkbox label="地推活动" name="type"></jm-checkbox>
          <jm-checkbox label="线下主题活动" name="type"></jm-checkbox>
          <jm-checkbox label="单纯品牌曝光" name="type"></jm-checkbox>
        </jm-checkbox-group>
      </jm-form-item>
      <jm-form-item label="特殊资源" prop="resource">
        <jm-radio-group v-model="ruleForm.resource">
          <jm-radio label="线上品牌商赞助"></jm-radio>
          <jm-radio label="线下场地免费"></jm-radio>
        </jm-radio-group>
      </jm-form-item>
      <jm-form-item>
        <jm-button type="primary" @click="submitForm">立即创建</jm-button>
      </jm-form-item>
    </jm-form>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, ref, Ref } from 'vue';

export default defineComponent({
  setup() {
    const form: Ref = ref(null);

    const
      state = reactive({
        ruleForm: {
          name: '',
          region: '',
          type: [],
          resource: '',
        },
        rules: {
          name: [
            { required: true, message: '请输入活动名称', trigger: 'blur' },
            { min: 3, max: 5, message: '长度在 3 到 5 个字符', trigger: 'blur' },
          ],
          region: [
            { required: true, message: '请选择活动区域', trigger: 'change' },
          ],
          type: [
            { type: 'array', required: true, message: '请至少选择一个活动性质', trigger: 'change' },
          ],
          resource: [
            { required: true, message: '请选择活动资源', trigger: 'change' },
          ],
        },
      });

    function submitForm() {
      form.value.validate((valid: boolean) => {
        console.log(valid);
        if (valid) {
          alert('submit!');
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    }

    return { form, ...toRefs(state), submitForm };
  },
});
</script>

<style scoped lang="less">
.form-form {
  display: flex;
  align-items: center;

  > :last-child {
    width: 80%;
  }
}
</style>