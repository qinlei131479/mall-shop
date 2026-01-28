<template>
    <el-select
        v-model="areaStoreManagerId"
        :loading="loading"
        filterable
        remote
        :remoteMethod="remoteMethod"
        :multiple="props.multiple"
        placeholder="请输入区域名称"
        clearable
        reserve-keyword
        @change="onChange"
    >
        <el-option v-for="item in options" :key="item.areaStoreManagerId" :label="item.areaStoreName" :value="item.areaStoreManagerId" />
    </el-select>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from "vue";
import { areaStoreManagerList } from "@/api/store/areaManage";
import { message } from "ant-design-vue";
import type { RequestList } from "@/types/store/areaManage";

const props = defineProps({
    modelValue: { type: [Number, String] },
    multiple: {
        type: Boolean,
        default: false
    }
    //multiple 是否多选，可直接写在父组件
});
const loading = ref<boolean>(true);
const filterParams = reactive<RequestList>({
    //初使化用于查询的参数
    keyword: ""
});
const emit = defineEmits(["update:modelValue", "onChange"]);
const areaStoreManagerId = defineModel<number | string | number[]>("areaStoreManagerId");
const onChange = (e: any) => {
    emit("update:modelValue", e);
    emit("onChange", e);
};
onMounted(() => {
    loadFilter();
});

const options: any = ref([]);

const loadFilter = async (query?: number | string) => {
    loading.value = true;
    filterParams.keyword = String(query || "");
    try {
        let result: any;

        result = await areaStoreManagerList({ ...filterParams, size: 99 });
        options.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const remoteMethod = (query: string) => {
    loadFilter(query);
};
</script>
<style lang="less" scoped></style>
