<template>
    <div class="input-box">
        <el-input-number v-model="number" :step="step" :max="max" :min="min" @change="onChange" />
    </div>
</template>
<script lang="ts" setup>
const props = defineProps({
    modelValue: { type: Number, default: 1 },
    max: Number,
    min: { type: Number, default: 1 },
    step: { type: Number, default: 1 }
});
const number = defineModel<number>("modelValue");
const emit = defineEmits(["change"]);
const onChange = (value: any) => {
    emit("change");
    if (!Number.isInteger(value) || value < 1) {
        number.value = props.min;
    }
};
</script>
<style lang="less" scoped>
.input-box {
    :deep(.el-input-number__decrease) {
        border-radius: 0;
    }

    :deep(.el-input) {
        border-radius: 0;
    }

    :deep(.el-input-number .el-input__wrapper) {
        border-radius: 0;
    }

    :deep(.el-input-number .el-input__wrapper) {
        box-shadow: 0 0 0 1px #e7e7e7 inset !important;
    }

    :deep(.el-input-number__decrease:hover),
    :deep(.el-input-number__increase:hover) {
        color: #333;
    }
}
</style>
