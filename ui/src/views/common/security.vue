<template>
  <div class="security">
    <jm-dialog v-model="dialogVisible"
               width="707px">
      <div class="tip">
        <div class="title">
          <i class="icon"></i>
          提示
        </div>
        <div class="waring" v-if="!isSafari">
          <div class="txt">
            <p>您当前使用的浏览器兼容性较差，推荐使用最新版谷歌Chrome浏览器，运行更稳定</p>
          </div>
        </div>
        <div class="waring" v-else>
          <div class="txt">
            <p>您当前使用的Safari浏览器默认阻止跨站跟踪</p>
            <p class="bold">解决方案一（推荐）</p>
            <p>推荐使用最新版谷歌Chrome浏览器，运行更稳定</p>
            <p class="bold">解决方案二</p>
          </div>
          <div class="bottom">
            <div class="item">
              <span>1、点击偏好设置</span>
              <img src="~@/assets/images/security/safari/setting.png" alt="">
            </div>
            <div class="item">
              <span>2、点击隐私，取消勾选阻止跨站跟踪</span>
              <img src="~@/assets/images/security/safari/privacy.png" alt="">
            </div>
          </div>
        </div>
      </div>
    </jm-dialog>
  </div>
</template>

<script lang='ts'>
import { defineComponent, ref } from 'vue';
// 判断是否为safari浏览器
const { userAgent: ua } = navigator;
const isSafari = !ua.includes('Chrome') && ua.includes('Safari');

export default defineComponent({
  setup() {
    const dialogVisible = ref<boolean>(true);
    return {
      dialogVisible,
      isSafari,
    };
  },
});
</script>

<style scoped lang='less'>
.security {
  ::v-deep(.el-dialog) {
    .el-dialog__header {
      height: 5px;
      padding: 0 30px;
      border-bottom: none;
    }

    .el-dialog__body {
      padding-bottom: 30px;

      .tip {
        .title {
          display: flex;
          align-items: center;
          font-weight: 400;
          color: #333333;
          font-size: 18px;
          margin-bottom: 10px;

          .icon {
            width: 24px;
            height: 24px;
            margin-right: 10px;
            background-image: url("@/components/theme/message-box/svgs/warning.svg");
            background-repeat: no-repeat;
            background-size: 100%;
          }
        }

        .waring {
          .txt {
            color: #666666;
            padding-left: 34px;
            font-size: 14px;

            p {
              margin-bottom: 20px;
              font-weight: 400;
            }

            p.bold {
              font-weight: 600;
            }

            p:nth-child(1) {
              font-size: 16px;
            }
          }

          .bottom {
            padding-left: 34px;
            display: flex;
            justify-content: space-between;

            .item {
              display: flex;
              flex-direction: column;
              align-items: flex-start;

              span {
                margin-bottom: 15px;
              }

              &:nth-child(1) {
                img {
                  width: 254px;
                  height: 221px;
                }
              }

              &:nth-child(2) {
                margin-right: 30px;

                img {
                  width: 301px;
                  height: 110px;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
