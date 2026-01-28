<template>
    <view style="height: inherit; width: inherit" @click="handleChoose">
        <slot />
    </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { getSecret, baseUrl } from "@/utils/request";
import { useI18n } from "vue-i18n";
// #ifdef APP-PLUS
import { requestPhotosPermission } from "@/utils/appPermission";
// #endif

const { t } = useI18n();

const props = defineProps({
    count: {
        type: Number,
        default: 1
    },
    modelValue: {
        type: String,
        default: ""
    },
    requestUrl: {
        type: String,
        default: "user/user/uploadImg"
    }
});
const files = ref<string[]>([]);
const emit = defineEmits(["update:modelValue", "change"]);

// 处理上传点击事件，在APP端检查权限
const handleBeforeChoose = async () => {
    // #ifdef APP-PLUS
    try {
        // 请求相册权限
        const hasPhotosPermission = await requestPhotosPermission();
        if (!hasPhotosPermission) {
            uni.showToast({
                title: t("需要相册权限才能选择图片"),
                icon: "none"
            });
            return false;
        }
    } catch (error) {
        console.error("权限请求失败:", error);
        uni.showToast({
            title: t("权限请求失败，请重试"),
            icon: "none"
        });
        return false;
    }
    // #endif
    return true;
};

const handleChoose = async () => {
    const canProceed = await handleBeforeChoose();
    if (!canProceed) {
        return;
    }

    uni.chooseImage({
        count: props.count,
        success: async (res: any) => {
            uni.showLoading({
                title: t("上传中...")
            });
            res.tempFilePaths.map((file: string) => {
                uni.uploadFile({
                    url: baseUrl + import.meta.env.VITE_API_PREFIX + props.requestUrl,
                    filePath: file,
                    name: "file",
                    header: {
                        Authorization: uni.getStorageSync("token"),
                        Secret: getSecret()
                    },
                    success: (uploadFileRes) => {
                        if (props.count > 1) {
                            files.value.push(JSON.parse(uploadFileRes.data).data);
                            emit("update:modelValue", files.value);
                            uni.hideLoading();
                        } else {
                            uni.showToast({
                                title: t("图片上传成功")
                            });
                            emit("change");
                            uni.hideLoading();
                        }
                    },
                    fail: (error) => {
                        uni.hideLoading();
                        uni.showToast({
                            title: t("图片上传失败"),
                            icon: "none"
                        });
                    }
                });
            });
        },
        fail: (error) => {
            uni.showToast({
                title: t("图片选择失败"),
                icon: "none"
            });
        }
    });
};
</script>

<style lang="scss" scoped></style>
