<template>
    <template v-if="appManageStore.showPermissionPop && appManageStore.permissionType">
        <view class="app-permission">
            <view class="content-box" :style="{ marginTop: saveTop + 10 + 'px' }">
                <view class="content">
                    <view class="title"> {{ permissionMessages[appManageStore.permissionType].title }} </view>
                    <view class="desc">{{ permissionMessages[appManageStore.permissionType].content }}</view>
                </view>
            </view>
        </view>
    </template>
</template>

<script setup lang="ts">
import { useSafeAreaInsets } from "@/hooks";
import { useAppManageStore } from "@/store/appManage";

const { saveTop } = useSafeAreaInsets();

const appManageStore = useAppManageStore();

const permissionMessages: { [key: string]: any } = {
    camera: {
        title: "相机权限说明",
        content: "为了让您能够拍摄和上传照片，需要访问您的相机权限。请点击允许开启相机权限。"
    },
    photos: {
        title: "相册权限说明",
        content: "为了让您能够选择和上传照片，需要访问您的相册权限。请点击允许开启相册权限。"
    },
    microphone: {
        title: "麦克风权限说明",
        content: "为了让您能够录制音频，需要访问您的麦克风权限。请点击允许开启麦克风权限。"
    },
    location: {
        title: "位置权限说明",
        content: "为了提供更好的服务，需要访问您的位置信息。请点击允许开启位置权限。"
    }
};
</script>

<style lang="scss" scoped>
.app-permission {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 999;
    width: 100%;

    .content-box {
        padding: 0 30rpx;

        .content {
            background-color: #fff;
            border-radius: 30rpx;
            padding: 20rpx;
            font-size: 28rpx;

            .title {
                font-weight: bold;
                padding-bottom: 15rpx;
            }

            .desc {
                font-size: 24rpx;
            }
        }
    }
}
</style>
