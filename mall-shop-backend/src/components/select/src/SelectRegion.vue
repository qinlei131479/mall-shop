<template>
    <el-cascader
        placement="right"
        style="width: 100%"
        :props="cascaderProps"
        placeholder="请选择地区"
        :options="options"
        v-model="selectedIds"
        clearable
        @change="onChange"
        @clear="onClear"
    />
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRegionStore } from "@/store/region";
import { message } from "ant-design-vue";
const region = useRegionStore();

// 传值
const props = defineProps({
    // modelValue: { type: Array as () => number[], default: [] },
    checkStrictly: { type: Boolean, default: false }
});
const selectedIds = defineModel<number | string | number[]>("selectedIds");
const cascaderProps = {
    checkStrictly: props.checkStrictly,
    label: "regionName",
    value: "regionId",
    children: "children"
};

// 选项卡
const options = ref([]);

const loaded = ref(false);

// 用于存储选中的地址名称和ID
const selectedRegions = ref<any[]>([]);

// 给父组件传值
const emit = defineEmits(["update:modelValue", 'change', 'clear']);

// 加载分类
onMounted(() => {
    if (region.allRegion == null) {
        loadRegion();
    } else {
        options.value = region.allRegion;
    }
});

const loadRegion = async () => {
    if (loaded.value) {
        return;
    }
    loaded.value = true;
    try {
        await (region as any).getRegionList();
        options.value = (region as any).allRegion;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loaded.value = false;
    }
};

const onChange = (ids: number[]) => {
    selectedIds.value = ids;
    selectedRegions.value = findSelectedRegions(ids, options.value);
    emit("change", selectedRegions.value);
    emit("update:modelValue", ids);
};

const onClear = () => {
    selectedIds.value = [];
    selectedRegions.value = [];
    emit("clear");
};

// 递归查找选中的地区
const findSelectedRegions = (ids: number[], options: any[]): any[] => {
    const result: any[] = [];
    if (ids && ids.length > 0) {
        for (const id of ids) {
            const region = findRegionById(id, options);
            if (region) {
                result.push(region);
            }
        }
    }
    return result;
};

const findRegionById = (id: number, options: any[]): any => {
    for (const option of options) {
        if (option.regionId === id) {
            return option;
        }
        if (option.children && option.children.length > 0) {
            const found = findRegionById(id, option.children);
            if (found) {
                return found;
            }
        }
    }
    return null;
};
</script>

<style lang="less" scoped>
.cascader {
    width: 100%;
}
</style>
