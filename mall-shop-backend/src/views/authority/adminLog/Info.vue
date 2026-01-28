<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" label-width="auto">
                    <el-form-item label="操作人：">
                        {{ data.adminUsername }}
                    </el-form-item>
                    <el-form-item label="操作IP：">
                        {{ data.adminIp }}
                    </el-form-item>
                    <el-form-item label="操作时间：">
                        {{ data.addTime }}
                    </el-form-item>
                    <el-form-item label="操作版本：">
                        {{ data.platform }}
                    </el-form-item>
                    <el-form-item label="请求接口：">
                        {{ data.url }}
                    </el-form-item>
                    <el-form-item label="请求方式：">
                        {{ data.httpMethod }}
                    </el-form-item>
                    <el-form-item label="请求参数：">
                        {{ data.requestParams }}
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
const props = defineProps({
    data: {
        type: Object,
        default: {}
    }
});
</script>
<style scoped lang="less"></style>
