<template>
  <div class="component-lib">
    <div class="group">
      <div>基础：</div>
      <basic-button/>
      <basic-link/>
      <basic-icon/>
      <basic-scrollbar/>
    </div>
    <div class="group">
      <div>数据：</div>
      <data-badge/>
      <data-date-picker/>
      <data-log-viewer/>
      <data-pagination/>
      <data-state/>
      <data-table/>
      <data-tree/>
    </div>
    <div class="group">
      <div>表单：</div>
      <form-form/>
      <form-input/>
      <form-checkbox/>
      <form-radio/>
      <form-select/>
      <form-tags-input/>
      <form-cascader/>
    </div>
    <div class="group">
      <div>导航：</div>
      <navigation-breadcrumb/>
      <navigation-dropdown/>
      <navigation-tabs/>
      <navigation-steps/>
    </div>
    <div class="group">
      <div>通知：</div>
      <notice-loading/>
      <notice-message/>
      <notice-message-box/>
      <notice-popconfirm/>
    </div>
    <div class="group">
      <div>其他：</div>
      <others-dialog/>
      <others-draggable/>
      <others-drawer/>
      <others-tooltip/>
    </div>
    <div class="group">
      <div>流程：</div>
      <workflow-dsl-editor/>
      <workflow-workflow-viewer/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BasicButton from './basic-button.vue';
import BasicIcon from './basic-icon.vue';
import BasicLink from './basic-link.vue';
import BasicScrollbar from './basic-scrollbar.vue';
import DataBadge from './data-badge.vue';
import DataDatePicker from './data-date-picker.vue';
import DataLogViewer from '@/views/component-lib/data-log-viewer.vue';
import DataPagination from './data-pagination.vue';
import DataState from './data-state.vue';
import DataTable from './data-table.vue';
import DataTree from './data-tree.vue';
import FormCheckbox from './form-checkbox.vue';
import FormForm from './form-form.vue';
import FormInput from './form-input.vue';
import FormRadio from './form-radio.vue';
import FormSelect from './form-select.vue';
import FormTagsInput from './form-tags-input.vue';
import FormCascader from './form-cascader.vue';
import NavigationBreadcrumb from './navigation-breadcrumb.vue';
import NavigationDropdown from './navigation-dropdown.vue';
import NavigationTabs from './navigation-tabs.vue';
import NavigationSteps from './navigation-steps.vue';
import NoticeLoading from './notice-loading.vue';
import NoticeMessage from './notice-message.vue';
import NoticeMessageBox from './notice-message-box.vue';
import NoticePopconfirm from './notice-popconfirm.vue';
import OthersDialog from './others-dialog.vue';
import OthersDraggable from './others-draggable.vue';
import OthersDrawer from './others-drawer.vue';
import OthersTooltip from './others-tooltip.vue';
import WorkflowDslEditor from './workflow-dsl-editor.vue';
import WorkflowWorkflowViewer from './workflow-workflow-viewer.vue';

export default defineComponent({
  components: {
    BasicButton, BasicIcon, BasicLink, BasicScrollbar,
    DataBadge, DataDatePicker, DataLogViewer, DataPagination, DataState, DataTable, DataTree,
    FormCheckbox, FormForm, FormInput, FormRadio, FormSelect, FormTagsInput, FormCascader,
    NavigationBreadcrumb, NavigationDropdown, NavigationTabs, NavigationSteps,
    NoticeLoading, NoticeMessage, NoticeMessageBox, NoticePopconfirm,
    OthersDialog, OthersDraggable, OthersDrawer, OthersTooltip,
    WorkflowDslEditor, WorkflowWorkflowViewer,
  },
});
</script>

<style scoped lang="less">
.component-lib {
  .group {
    margin: 15px 0;
    border-bottom: 1px solid #ccc;

    > div {
      &:first-child {
        font-weight: bold;
        background-color: #FFFBE6;
        padding: 10px 20px;
        border: 1px solid #FFD7AD;
      }

      margin: 15px 0;
    }
  }
}
</style>
