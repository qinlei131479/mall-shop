<template>
    <el-pagination
        class="pagination"
        @size-change="sizeChange"
        @current-change="pageChange"
        :current-page="page"
        :page-size="size"
        background
        layout="slot ,prev, pager, next"
        :total="total"
    >
        <template #default> {{ $t("第") }}{{ (page - 1) * size + 1 }}-{{ page * size }} {{ $t("条") }} / {{ $t("总共") }} {{ total }} {{ $t("条") }} </template>
    </el-pagination>
</template>
<script setup lang="ts">
import "element-plus/es/components/pagination/style/css";
import { ElPagination } from "element-plus";
import { ref } from "vue";
const props = defineProps({
    page: {
        type: Number,
        default: 1
    },
    size: {
        type: Number,
        default: 15
    },
    total: {
        type: Number,
        default: 10
    }
});
const emit = defineEmits(["update:page", "update:size", "callback"]);
// 修改页面显示数量
const sizeChange = (value: number) => {
    emit("update:size", value);
    emit("callback");
};
// 翻页
const pageChange = (value: number) => {
    emit("update:page", value);
    emit("callback");
};
</script>
<style lang="less" scoped>
.pagination :deep(.el-select .el-input) {
    width: 100px;
}
</style>
