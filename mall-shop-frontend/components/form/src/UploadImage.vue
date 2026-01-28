<template>
    <el-upload ref="upload" v-model:file-list="fileList" :action="requestPrefix + '/user/user/uploadImg'"
        :headers="uploadHeaders" :before-upload="beforeAvatarUpload" :on-exceed="handleExceed"
        :on-preview="handlePictureCardPreview" :on-remove="handleRemove" :on-success="handleSuccess" :limit="limit"
        :multiple="limit > 1 ? true : false" list-type="picture-card"
        :class="{ 'hide-upload-button': limit == 1 && fileList.length > 0 }">
        <!-- 上传按钮 -->
        <template #trigger v-if="fileList.length === 0">
            <el-icon>
                <Plus />
            </el-icon>
        </template>
    </el-upload>
    <el-dialog v-model="dialogVisible">
        <div class="image">
            <el-image :src="dialogImageUrl" alt="" fit="cover" />
        </div>
    </el-dialog>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { Plus } from "@element-plus/icons-vue";
import { requestPrefix, requestHeader } from "~/utils/request";
import type { UploadProps, UploadUserFile } from "element-plus";
import { imageFormat } from "~/utils/format";
const uploadHeaders = requestHeader();
const { t } = useI18n();
const upload = ref<any>();
const props = defineProps({
    limit: {
        type: Number,
        default: 1
    },
    fileTypes: {
        type: Array,
        default: []
    }
});
const beforeAvatarUpload: UploadProps["beforeUpload"] = (rawFile) => {
    // 检查是否已经达到上传限制
    if (fileList.value.length >= props.limit) {
        ElMessage.error(t("已经达到上传数量限制"));
        return false; // 拒绝这次上传
    }
    const fileName = rawFile.type.replace(/.*\//, "");
    if (!props.fileTypes.includes(fileName)) {
        ElMessage.error(`${t("无法上传")} ${fileName} ${t("文件!")}` + `${t("只能上传")} ${props.fileTypes.join(", ")} ${t("文件!")}`);
        return false;
    }
    if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error(t("只能上传2M以内的图片"));
        return false;
    }
    return true;
};
const uploadList: any = defineModel("uploadList", { default: [] });
const fileList = ref<UploadUserFile[]>([]);
const processingData = () => {
    if(uploadList.value == null){
        uploadList.value = [];
        return
    }
    if (uploadList.value.length > 0) {
        uploadList.value.forEach((item: any) => {
            item.url = imageFormat(item.picUrl);
        });
        fileList.value = uploadList.value;
    }
};
processingData();

const dialogImageUrl = ref("");
const dialogVisible = ref(false);

const handleRemove: UploadProps["onRemove"] = (uploadFile: any, uploadFiles) => {
    if (uploadFile && uploadList.value) {
        uploadList.value = uploadList.value.filter((item: any) => item.picThumb !== uploadFile.picThumb);
    } else {
        // 可选：设置默认的空数组
        uploadList.value = [];
    }
};
const handleExceed: UploadProps["onExceed"] = (files: File[] = [], uploadFiles: UploadUserFile[]) => {
    if (props.limit === 1 && fileList.value.length > 0) {
        fileList.value.pop();
        uploadList.value.pop();
        upload.value.clearFiles(); //删除所有上传的文件
        upload.value.handleStart(files[0]); //handleStart()指的是手动选择文件，Element Plus 的el-upload有说明
        upload.value.submit(); //手动上传
    }
};

const handlePictureCardPreview: UploadProps["onPreview"] = (uploadFile) => {
    dialogImageUrl.value = uploadFile.url!;
    dialogVisible.value = true;
};
const handleSuccess: UploadProps["onSuccess"] = (response, uploadFile) => {
    try {
        uploadList.value.push({
            picThumb: response.data.picThumb,
            picUrl: response.data.picUrl,
            picId: response.data.picId,
            picName: response.data.picName
        });
    } catch (e) { }
};
</script>
<style lang="less" scoped>
.hide-upload-button {
    :deep(.el-upload--picture-card) {
        display: none !important;
    }
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 80px;
    /* 设置缩略图的宽度 */
    height: 80px;
    /* 设置缩略图的高度 */
}

:deep(.el-upload--picture-card) {
    width: 80px;
    /* 设置缩略图的宽度 */
    height: 80px;
    /* 设置缩略图的高度 */
}

:deep(.el-upload-list__item-status-label) {
    display: none !important;
}

.image {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
