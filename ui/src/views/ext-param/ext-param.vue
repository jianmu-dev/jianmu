<template>
  <div class="ext-param" v-loading="pageLoading">
    <!--  tab  -->
    <div class="tab-container">
      <jm-scrollbar>
        <div class="classification-tabs">
            <span :class="['tab-item',currentTab===index?'is-active':'']" v-for="(item,index) in data" :key="index"
                  @click="currentTab=index">{{ item.label }}・{{ item.counter }}
            </span>
        </div>
      </jm-scrollbar>
    </div>
    <!--  内容  -->
    <div class="ext-content">
      <div class="add-param" @click="add" v-if="!pageLoading">
        <i class="jm-icon-button-add"/>
        新增参数
      </div>
      <!--  外部参数卡片    -->
      <ext-param-card
        v-for="ext in data[currentTab]?.projects"
        :key="ext.ref"
        :reference="ext.ref"
        :name="ext.name"
        :type="ext.type"
        :value="ext.value"
        :label="ext.label"
        :id="ext.id"
        @delete="del"
        @editor="editor"
      />
    </div>

    <!--  新增/编辑 外部参数  -->
    <jm-dialog v-model="addExtParamVisible" :title="!editorExtParams.flag?'新增参数':'编辑参数'" destroy-on-close
               v-if="addExtParamVisible" :custom-class="[entry?'':'center']">
      <jm-form label-position="top" :model="addParam" ref="addParamRef">
        <jm-form-item label="唯一标识" prop="ref" :rules="rules.ref">
          <jm-input show-word-limit :maxlength="30" v-model="addParam.ref" :disabled="editorExtParams.flag"
                    placeholder="以英文字母或下划线开头，支持下划线、数字、英文字母"/>
        </jm-form-item>
        <jm-form-item label="名称">
          <jm-input show-word-limit :maxlength="45" v-model="addParam.name" placeholder="请输入参数名称"/>
        </jm-form-item>
        <jm-form-item label="类型" prop="type" :rules="rules.type">
          <jm-radio-group v-model="addParam.type" @change="changeType">
            <jm-radio :label="ParamTypeEnum.STRING">字符串</jm-radio>
            <jm-radio :label="ParamTypeEnum.NUMBER">数字</jm-radio>
            <jm-radio :label="ParamTypeEnum.BOOL">布尔</jm-radio>
          </jm-radio-group>
        </jm-form-item>
        <jm-form-item v-if="valueVisible" label="值" prop="value" :rules="rules.value">
          <jm-input v-if="addParam.type === ParamTypeEnum.STRING" v-model="addParam.value" type="textarea"
                    :maxlength="512"
                    placeholder="请输入参数值"/>
          <jm-input v-else-if="addParam.type === ParamTypeEnum.NUMBER" v-model="addParam.value" type="number"
                    placeholder="请输入参数值"
                    :maxlength="512"/>
          <jm-radio-group v-else-if="addParam.type === ParamTypeEnum.BOOL" v-model="addParam.value">
            <jm-radio :label="'true'">true</jm-radio>
            <jm-radio :label="'false'">false</jm-radio>
          </jm-radio-group>
        </jm-form-item>
        <jm-form-item label="标签" prop="label" :rules="rules.label">
          <jm-select
            ref="labelSelectRef"
            @keyup.enter="enterSelect"
            @visible-change="checkSize"
            v-model="addParam.label"
            filterable
            allow-create
            clearable
            placeholder="请选择或创建参数标签"
          >
            <jm-option
              v-for="item in labelOption"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </jm-select>
        </jm-form-item>
      </jm-form>
      <template #footer>
      <span class="dialog-footer">
        <jm-button @click="addExtParamVisible = false">取消</jm-button>
        <jm-button v-if="!editorExtParams.flag" type="primary" @click="sure(addParamRef)">确定</jm-button>
        <jm-button v-else type="primary" @click="save(addParamRef)">保存</jm-button>
      </span>
      </template>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, nextTick, ref } from 'vue';
import { addExtParam, deleteExtParam, editorExtParam, getExtParamLabelList, getExtParamList } from '@/api/ext-param';
import { IExternalParameterLabelVo, IExternalParameterVo } from '@/api/dto/ext-param';
import ExtParamCard from './ext-param-card.vue';
import { ParamTypeEnum } from '@/api/dto/enumeration';
import { useStore } from 'vuex';

interface addParamType {
  ref: string;
  name?: string;
  type: ParamTypeEnum;
  value: string;
  label: string;
}

export default defineComponent({
  components: { ExtParamCard },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const store = useStore();
    const entry = store.state.entry;
    const pageLoading = ref<boolean>(false);
    const labelSelectRef = ref<HTMLElement>();
    const extParams = ref<IExternalParameterVo[]>();
    const addExtParamVisible = ref<boolean>(false);
    const addParamRef = ref<HTMLFormElement>();
    // 获取label列表
    const extLabelList = ref<IExternalParameterLabelVo[]>();
    const currentTab = ref<number>(0);
    const valueVisible = ref<boolean>(true);
    // 存储修改-区分新增/修改，使用同一个弹窗
    const editorExtParams = ref<{ flag: boolean, id: string }>({
      flag: false,
      id: '',
    });
    // label默认值
    const labelOption = ref<{ label: string, value: string }[]>();
    const addParam = ref<addParamType>({ ref: '', name: '', type: ParamTypeEnum.STRING, value: '', label: '' });

    const init = async () => {
      try {
        pageLoading.value = true;
        extParams.value = await getExtParamList();
        extLabelList.value = await getExtParamLabelList();
        extLabelList.value?.unshift({ id: '', value: '全部', createdTime: '', lastModifiedTime: '' });
        const list = [];
        //  判断是否已经有默认
        extLabelList.value?.forEach(item => {
          list.push(item.value);
        });
        if (list.indexOf('默认') === -1) {
          extLabelList.value?.splice(1, 0, { id: '', value: '默认', createdTime: '', lastModifiedTime: '' });
        }
      } finally {
        pageLoading.value = false;
      }

    };
    init();

    const getLabelList = () => {
      // 初始化
      labelOption.value = [];
      extLabelList.value?.slice(1).forEach(item => {
        labelOption.value?.push({ label: item.value, value: item.value });
      });
    };
    // 构建tab参数
    const data = computed<{ label: string, projects: IExternalParameterVo[], counter: number }>(() => {
      let info: any = [];

      // label去重
      let labelList = [];
      let newArr = [];
      extLabelList.value?.forEach((ext, index) => {
        if (labelList.includes(ext.value)) {
          return;
        }
        labelList.push(ext.value);
        newArr.push(JSON.parse(JSON.stringify(ext)));
      });
      extLabelList.value = newArr;

      // 计数/构建数据结构
      extLabelList.value?.forEach(labels => {
        info.push({
          label: labels.value,
          projects: extParams.value?.filter(({ label }) => labels.value === '全部' ? label !== labels.value : label === labels.value),
          counter: extParams.value?.filter(({ label }) => labels.value === '全部' ? label !== labels.value : label === labels.value).length,
        });
      });

      // 固定"默认"位置
      for (let i = 0; i < info.length; i++) {
        if (info[i].label === '默认') {
          let single = info[i];
          info.splice(i, 1);
          info.splice(1, 0, single);
        }
      }
      return info;
    });
    return {
      entry,
      pageLoading,
      addExtParamVisible,
      extParams,
      addParam,
      ParamTypeEnum,
      addParamRef,
      editorExtParams,
      currentTab,
      labelSelectRef,
      data,
      labelOption,
      valueVisible,
      rules: {
        ref: [
          { required: true, message: '请输入唯一标识', trigger: 'blur' },
          { pattern: /^[a-zA-Z_]([a-zA-Z0-9_]+)?$/, message: '以英文字母或下划线开头，支持下划线、数字、英文字母', trigger: 'blur' },
        ],
        type: { required: true, message: '请选择类型', trigger: 'change' },
        value: [
          { required: true, message: '请输入参数值', trigger: 'blur' },
        ],
        label: [
          { required: true, message: '请选择或创建参数标签', trigger: 'change' },
        ],
      },
      // 新增-保存
      sure: () => {
        addParamRef.value?.validate(async (valid: boolean) => {
          if (!valid) {
            return;
          }
          if (!addParam.value.name) {
            addParam.value.name = addParam.value.ref;
          }
          try {
            await addExtParam({
              ref: addParam.value.ref,
              name: addParam.value.name,
              type: addParam.value.type,
              value: addParam.value.value,
              label: addParam.value.label,
            });
            addExtParamVisible.value = false;
            proxy.$success('新增参数成功');
            await init();
          } catch (err) {
            proxy.$throw(err, proxy);
          }
        });
      },
      // 新增
      add: () => {
        getLabelList();
        addExtParamVisible.value = true;
        const list = [];
        data.value.forEach(item => {
          list.push(item.label);
        });
        addParam.value = {
          ref: '', name: '', type: ParamTypeEnum.STRING, value: '',
          label: list.length > 0 && currentTab.value !== 0 ? list[currentTab.value] : '',
        };
        editorExtParams.value.flag = false;
      },
      // 编辑
      editor: (id: string) => {
        getLabelList();
        const { ref, name, type, value, label } = extParams.value?.find(item => item.id === id);
        addParam.value = { ref, name, type, value, label };
        addExtParamVisible.value = true;
        editorExtParams.value = { id: id, flag: true };
      },
      // 编辑-保存
      save: () => {
        addParamRef.value?.validate(async (valid: boolean) => {
          if (!valid) {
            return;
          }
          if (!addParam.value.name) {
            addParam.value.name = addParam.value.ref;
          }
          try {
            const { name, type, value, label } = addParam.value;
            await editorExtParam({ id: editorExtParams.value.id, name: name, type: type, value: value, label: label });
            proxy.$success('修改成功');
            addExtParamVisible.value = false;
            await init();
          } catch (err) {
            proxy.$throw(err, proxy);
          }
        });
      },
      // 删除
      del: (id: string, name: string) => {
        let msg = '<div>确定要删除参数吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 14px; line-height: normal;">名称：${name}</div>`;
        msg += '<div style="margin-top: 5px; font-size: 14px; line-height: normal;">删除后已引用该参数的项目将会报错</div>';

        proxy
          .$confirm(msg, '删除参数', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(async () => {
            try {
              await deleteExtParam(id);
              proxy.$success('删除参数成功');
              await init();
            } catch (err) {
              proxy.$throw(err, proxy);
            }
          })
          .catch();
      },
      changeType: async (e: ParamTypeEnum) => {
        addParam.value.value = '';
        if (e === ParamTypeEnum.BOOL) {
          addParam.value.value = 'true';
        }
        valueVisible.value = false;
        await nextTick();
        valueVisible.value = true;
      },
      // enter选中label
      enterSelect: (e: any) => {
        addParam.value.label = e.target.value;
        labelSelectRef.value?.blur();
        if (addParam.value.label.length <= 15) {
          return;
        }
        addParam.value.label = addParam.value.label.substr(0, 15);
      },
      // 选择后检查
      checkSize: () => {
        if (addParam.value.label.length <= 15) {
          return;
        }
        addParam.value.label = addParam.value.label.substr(0, 15);
      },
    };
  },
});
</script>
<style scoped lang="less">
.ext-param {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  min-height: calc(100vh - 185px);
  // tab
  .tab-container {
    align-items: center;
    font-size: 14px;

    .classification-tabs {
      width: 100%;
      padding-left: 0.385%;
      box-sizing: border-box;
      margin-bottom: 20px;
      height: 30px;
      line-height: 22px;
      color: #082340;
      display: flex;
      flex-wrap: nowrap;

      .tab-item {
        cursor: pointer;
        box-sizing: border-box;
        padding: 5px 15px;
        display: flex;
        white-space: nowrap;

        // 取消双击选中
        -moz-user-select: none;
        -webkit-user-select: none;
        -ms-user-select: none;
        user-select: none;

        &.is-active {
          font-weight: 500;
          color: #096DD9;
          background-color: #EBF4FF;
          border-radius: 15px;
        }
      }
    }
  }

  // 外部参数内容
  .ext-content {
    display: flex;
    flex-wrap: wrap;

    // 新增参数
    .add-param {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
      width: 19.2%;
      height: 180px;
      margin: .8% .385% 0 0.385%;
      border-radius: 4px;
      box-sizing: border-box;
      border: 1px solid #E7ECF1;
      cursor: pointer;
      font-size: 14px;
      font-weight: 400;
      color: #096DD9;

      .jm-icon-button-add {
        font-size: 36px;

        &::before {
          font-weight: bold;
        }
      }
    }
  }

  ::v-deep(.el-dialog__footer) {
    background: #fff;
    padding: 0 25px 0 0;
  }

  ::v-deep(.el-button) {
    width: 76px;
    height: 36px;
    box-shadow: none;
    border: none;
  }

  ::v-deep(.el-button--default) {
    background: #F5F5F5;
    border-radius: 2px;

    &:hover {

      background: #EFF7FF;
      border-radius: 2px;
    }
  }

  ::v-deep(.el-button--primary) {
    background: #096DD9;
    border-radius: 2px;

    &:hover {
      background: #3293FD;
      border-radius: 2px;
    }
  }

  ::v-deep(.save) {
    background: #096DD9;
    box-shadow: none;
    border-radius: 2px;
  }

  ::v-deep(.el-form) {
    .el-form-item {
      margin-bottom: 20px;
    }

    .el-form-item__label,
    .el-form-item__content {
      line-height: 20px;
    }

    .el-select {
      width: 100%;
    }

    .el-input__inner {
      height: 36px;
    }

    .el-textarea__inner {
      height: 74px;
      resize: none;
    }
  }
}
</style>
