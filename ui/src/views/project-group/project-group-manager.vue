<template>
  <jm-scrollbar>
    <div class="group-manager">
      <div class="right-top-btn">
        <router-link :to="{ name: 'index' }">
          <jm-button type="primary" class="jm-icon-button-cancel" size="small"
            >关闭</jm-button
          >
        </router-link>
      </div>
      <div class="menu-bar">
        <button class="add" @click="add">
          <div class="label">新建分组</div>
        </button>
      </div>
      <div class="title">
        <div>
          <span>项目分组</span>
          <span class="desc">（共有 {{ projectGroupList?.length }} 个组）</span>
        </div>
        <jm-tooltip content="关闭排序" placement="top" v-if="isActive">
          <div
            :class="['move', isActive ? 'active' : '']"
            @click="() => (isActive = !isActive)"
          ></div>
        </jm-tooltip>
        <jm-tooltip content="排序" placement="top" v-else>
          <div class="move" @click="() => (isActive = !isActive)"></div>
        </jm-tooltip>
      </div>
      <div class="content" v-loading="loading">
        <jm-empty v-if="projectGroupList?.length === 0" />
        <jm-draggable
          class="list"
          v-model="projectGroupList"
          @change="sortList"
          @start="start"
          @end="() => (currentSelected = false)"
          v-else-if="isActive"
        >
          <transition-group type="transition" name="flip-list">
            <div
              v-for="(i, index) in projectGroupList"
              :class="['item', moveClassList[index], `${i}`]"
              :key="i.id"
              :_id="i.id"
              @mouseover="over(i.id)"
              @mouseleave="leave"
            >
              <div class="wrapper">
                <div class="top">
                  <!-- <router-link to="/">
                    <div class="name">{{ i.name }}</div>
                  </router-link> -->
                  <div class="name">{{ i.name }}</div>
                </div>
                <div class="description">
                  {{ i.description || '无' }}
                </div>
                <div class="update-time">
                  <span>最后修改时间：</span
                  ><span>{{ datetimeFormatter(i.lastModifiedTime) }}</span>
                </div>
                <div class="total">
                  共<span class="count"> {{ i.projectCount }} </span>条项目
                </div>
                <div class="cover">
                  <div class="drag-icon"></div>
                </div>
              </div>
            </div>
          </transition-group>
        </jm-draggable>
        <div class="item" v-for="i in projectGroupList" :key="i.id" v-else>
          <div class="wrapper">
            <div class="top">
              <!-- <router-link to="/">
                <div class="name">{{ i.name }}</div>
              </router-link> -->
              <div class="name">{{ i.name }}</div>
              <div class="operation">
                <div
                  class="edit op-item"
                  @click="
                    toEdit(
                      i.id,
                      i.name,
                      i.isDefaultGroup,
                      i.isShow,
                      i.description
                    )
                  "
                ></div>
                <div
                  class="delete op-item"
                  @click="toDelete(i.name, i.id)"
                ></div>
              </div>
            </div>
            <div class="description">
              {{ i.description || '无' }}
            </div>
            <div class="update-time">
              <span>最后修改时间：</span
              ><span>{{ datetimeFormatter(i.lastModifiedTime) }}</span>
            </div>
            <div class="switch">
              <jm-switch
                v-model="i.isShow"
                @change="showChange($event, i.id)"
                active-color="#096DD9"
              ></jm-switch>
              <span>首页展示</span>
            </div>
            <div class="total">
              共<span class="count"> {{ i.projectCount }} </span>条项目
            </div>
          </div>
        </div>
      </div>
      <group-creator
        v-if="creationActivated"
        @closed="creationActivated = false"
        @completed="addCompleted"
      />
      <group-editor
        :default-group="defaultProjectGroup"
        :name="groupName || ''"
        :description="groupDescription"
        :is-show="showInHomePage"
        :id="projectGroupId || ''"
        v-if="editionActivated"
        @closed="editionActivated = false"
        @completed="editCompleted"
      />
    </div>
  </jm-scrollbar>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  getCurrentInstance,
  onMounted,
  computed,
  nextTick,
} from 'vue';
import GroupCreator from './project-group-creator.vue';
import GroupEditor from './project-group-editor.vue';
import { queryProjectGroup } from '@/api/view-no-auth';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { datetimeFormatter } from '@/utils/formatter';
import {
  deleteProjectGroup,
  updateProjectGroupShow,
  updateProjectGroupSort,
} from '@/api/project-group';
import { Mutable } from '@/utils/lib';
export default defineComponent({
  components: {
    GroupCreator,
    GroupEditor,
  },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const isShow = ref<boolean>(false);
    const loading = ref<boolean>();
    const isActive = ref<boolean>(false);
    const creationActivated = ref<boolean>(false);
    const editionActivated = ref<boolean>(false);
    const projectGroupList = ref<Mutable<IProjectGroupVo[]>>([]);
    const groupName = ref<string>();
    const groupDescription = ref<string>();
    const projectGroupId = ref<string>();
    const defaultProjectGroup = ref<boolean>(false);
    const currentItem = ref<string>('-1');
    const currentSelected = ref<boolean>(false);
    const showInHomePage = ref<boolean>(false);
    const showChange = async (e: boolean, id: string) => {
      try {
        await updateProjectGroupShow(id);
        e
          ? proxy.$success('项目分组首页显示')
          : proxy.$success('项目分组首页隐藏');
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };
    const moveClassList = computed<string[]>(() =>
      projectGroupList.value.map(({ id }) => {
        return id === currentItem.value ? 'move' : '';
      })
    );
    onMounted(async () => {
      projectGroupList.value = await queryProjectGroup();
    });
    const addCompleted = async () => {
      projectGroupList.value = await queryProjectGroup();
    };
    const editCompleted = async () => {
      projectGroupList.value = await queryProjectGroup();
    };
    const add = () => {
      creationActivated.value = true;
    };
    const toEdit = (
      id: string,
      name: string,
      isDefault: boolean,
      isShow: boolean,
      description?: string
    ) => {
      defaultProjectGroup.value = isDefault;
      groupName.value = name;
      groupDescription.value = description;
      projectGroupId.value = id;
      editionActivated.value = true;
      showInHomePage.value = isShow;
    };
    const toDelete = async (name: string, projectGroupId: string) => {
      let msg = '<div>确定要删除分组吗?</div>';
      msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${name}</div>`;

      proxy
        .$confirm(msg, '删除分组', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true,
        })
        .then(async () => {
          try {
            await deleteProjectGroup(projectGroupId);
            proxy.$success('项目分组删除成功');
            projectGroupList.value = await queryProjectGroup();
          } catch (err) {
            proxy.$throw(err, proxy);
          }
        })
        .catch(() => {});
    };
    const sortList = async (e: any) => {
      const {
        moved: { newIndex: targetSort, oldIndex: originSort, element },
      } = e;
      // 向移动
      if (targetSort < originSort) {
        try {
          await updateProjectGroupSort({
            targetSort: projectGroupList.value![targetSort + 1].sort,
            originSort: element.sort,
          });
          projectGroupList.value = await queryProjectGroup();
          proxy.$success('项目分组排序成功');
        } catch (err) {
          proxy.$throw(err, proxy);
        }
      } else {
        try {
          await updateProjectGroupSort({
            targetSort: projectGroupList.value![targetSort - 1].sort,
            originSort: element.sort,
          });
          projectGroupList.value = await queryProjectGroup();
          proxy.$success('项目分组排序成功');
        } catch (err) {
          proxy.$throw(err, proxy);
        }
      }
      nextTick(() => {
        currentItem.value = e.moved.element.id;
        currentSelected.value = false;
      });
    };

    return {
      leave() {
        currentItem.value = '';
      },
      over(id: string) {
        if (currentSelected.value) {
          return;
        }
        currentItem.value = id;
      },
      showChange,
      start(e: any) {
        currentSelected.value = true;
        currentItem.value = e.item.getAttribute('_id');
      },
      isShow,
      currentSelected,
      showInHomePage,
      moveClassList,
      defaultProjectGroup,
      groupName,
      groupDescription,
      projectGroupId,
      addCompleted,
      editCompleted,
      datetimeFormatter,
      sortList,
      projectGroupList,
      loading,
      isActive,
      creationActivated,
      editionActivated,
      add,
      toEdit,
      toDelete,
    };
  },
});
</script>

<style scoped lang="less">
.group-manager {
  padding: 15px;
  background-color: #ffffff;
  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before {
      font-weight: bold;
    }
  }
  .menu-bar {
    button {
      position: relative;

      .label {
        position: absolute;
        left: 0;
        bottom: 40px;
        width: 100%;
        text-align: center;
        font-size: 18px;
        color: #b5bdc6;
      }

      &.add {
        margin: 0.5%;
        width: 19%;
        min-width: 260px;
        height: 170px;
        background-color: #ffffff;
        border: 1px dashed #b5bdc6;
        background-image: url('@/assets/svgs/btn/add.svg');
        background-position: center 45px;
        background-repeat: no-repeat;
        cursor: pointer;
      }
    }
  }
  .title {
    font-size: 20px;
    font-weight: bold;
    color: #082340;
    position: relative;
    padding-left: 20px;
    padding-right: 20px;
    margin: 30px -15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .move {
      cursor: pointer;
      width: 24px;
      height: 24px;
      background-image: url('@/assets/svgs/sort/move.svg');
      background-size: contain;
      &.active {
        background-image: url('@/assets/svgs/sort/move-active.svg');
      }
    }

    .desc {
      font-weight: normal;
      margin-left: 12px;
      font-size: 14px;
      color: #082340;
      opacity: 0.46;
    }
  }
  .content {
    display: flex;
    flex-wrap: wrap;
    .list {
      display: flex;
      flex-wrap: wrap;
      width: 100%;
      .item {
        cursor: move;
        &.move {
          .wrapper {
            position: relative;
            border-color: #096dd9;
            .cover {
              position: absolute;
              top: 0;
              left: 0;
              width: 100%;
              height: 170px;
              background-color: rgba(140, 140, 140, 0.3);
              .drag-icon {
                position: absolute;
                right: 0;
                bottom: 0;
                width: 30px;
                height: 30px;
                background-image: url('@/assets/svgs/sort/drag.svg');
                background-size: contain;
              }
            }
          }
        }
      }
    }
    .item {
      position: relative;
      margin: 0.5%;
      width: 19%;
      min-width: 260px;
      height: 170px;
      background-color: #ffffff;
      box-shadow: 0 0 8px 0 #9eb1c5;
      .wrapper {
        padding: 15px;
        border: 1px solid transparent;
        height: 138px;
        color: #6b7b8d;
        font-size: 13px;
        position: relative;

        &:hover {
          border-color: #096dd9;
          .top {
            .operation {
              display: flex;
            }
          }
        }

        a {
          text-decoration: none;
          &:hover {
            color: #000000;
            text-decoration: underline;
          }
        }
        .top {
          display: flex;
          justify-content: space-between;
          align-items: center;
          .name {
            max-width: 150px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            color: #082340;
            font-size: 20px;
            font-weight: 500;
          }
          .operation {
            display: none;
            .op-item {
              width: 22px;
              height: 22px;
              background-size: contain;
              cursor: pointer;
              &.edit {
                background-image: url('@/assets/svgs/btn/edit.svg');
              }
              &.delete {
                margin-left: 15px;
                background-image: url('@/assets/svgs/btn/del.svg');
              }
            }
          }
        }
        .description {
          margin-top: 5px;
          line-height: 20px;
          max-height: 60px;
          text-overflow: ellipsis;
          overflow: hidden;
        }
        .update-time {
          position: absolute;
          bottom: 38px;
          // margin-top: 10px;
          // margin-top: 20px;
        }
        .switch {
          position: absolute;
          bottom: 10px;
          left: 15px;
          display: flex;
          align-items: center;
          span {
            margin-left: 5px;
            opacity: 0.5;
          }
        }
        .total {
          position: absolute;
          bottom: 12px;
          right: 25px;
          font-weight: 400;
          text-align: end;
          margin-top: 10px;
          .count {
            color: #096dd9;
          }
        }
      }
    }
  }
}
</style>
