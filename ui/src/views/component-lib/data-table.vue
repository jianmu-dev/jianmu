<template>
  <div class="data-table">
    <div>表格：</div>
    <jm-table
      :data="tableData"
      style="width: 100%">
      <jm-table-column
        type="selection"
        width="55">
      </jm-table-column>
      <jm-table-column type="expand">
        <template #default>
          展开内容
        </template>
      </jm-table-column>
      <jm-table-column
        label="商品 ID"
        prop="id">
      </jm-table-column>
      <jm-table-column
        label="商品名称"
        prop="name">
      </jm-table-column>
      <jm-table-column
        label="描述"
        prop="desc">
      </jm-table-column>
    </jm-table>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    return {
      tableData: [{
        id: '12987122',
        name: '好滋好味鸡蛋仔',
        category: '江浙小吃、小吃零食',
        desc: '荷兰优质淡奶，奶香浓而不腻',
        address: '上海市普陀区真北路',
        shop: '王小虎夫妻店',
        shopId: '10333',
      }, {
        id: '12987123',
        name: '好滋好味鸡蛋仔',
        category: '江浙小吃、小吃零食',
        desc: '荷兰优质淡奶，奶香浓而不腻',
        address: '上海市普陀区真北路',
        shop: '王小虎夫妻店',
        shopId: '10333',
      }, {
        id: '12987125',
        name: '好滋好味鸡蛋仔',
        category: '江浙小吃、小吃零食',
        desc: '荷兰优质淡奶，奶香浓而不腻',
        address: '上海市普陀区真北路',
        shop: '王小虎夫妻店',
        shopId: '10333',
      }, {
        id: '12987126',
        name: '好滋好味鸡蛋仔',
        category: '江浙小吃、小吃零食',
        desc: '荷兰优质淡奶，奶香浓而不腻',
        address: '上海市普陀区真北路',
        shop: '王小虎夫妻店',
        shopId: '10333',
      }],
    };
  },
});
</script>

<style scoped lang="less">
.data-table {
  display: flex;
  align-items: center;
}
</style>