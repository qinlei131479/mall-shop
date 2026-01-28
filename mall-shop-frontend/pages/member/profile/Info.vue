<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="个人资料"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="title-or-tabs">
                <span>{{ $t("用户信息") }}</span>
            </div>
            <div class="profile-info">
                <div class="title">
                    <h4>{{ $t("请在这里填写准确的个人信息") }}</h4>
                </div>
                <el-form v-if="!loading" ref="formRef" label-width="auto" :model="formState" class="form-body" label-suffix="：">
                    <el-form-item :rules="[{ required: true, message: $t('请选择生日') }]" :label="$t('生日')" prop="birthday">
                        <el-date-picker v-model="formState.birthday" clearable :placeholder="$t('请选择生日')" type="date" value-format="YYYY-MM-DD" />
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: $t('请输入昵称') }]" :label="$t('昵称')" prop="nickname">
                        <el-input v-model="formState.nickname" clearable :placeholder="$t('请输入昵称')" />
                    </el-form-item>
                    <el-form-item :label="$t('手机')">
                        <span style="font-size: 12px">
                            <template v-if="formState.mobile">
                                {{ getMaskedMobile(formState.mobile) }}
                                <NuxtLink class="a-btn" to="/member/security/info">[{{ $t("修改") }}]</NuxtLink>
                            </template>
                            <template v-else>
                                {{ $t("未绑定") }}
                                <NuxtLink class="a-btn" to="/member/security/info">[{{ $t("去绑定") }}]</NuxtLink>
                            </template>
                        </span>
                    </el-form-item>
                    <el-form-item :label="$t('邮箱')">
                        <span style="font-size: 12px">
                            <template v-if="formState.email">
                                {{ getMaskedEmail(formState.email) }}
                                <NuxtLink class="a-btn" to="/member/security/info">[{{ $t("修改") }}]</NuxtLink>
                            </template>
                            <template v-else>
                                {{ $t("未绑定") }}
                                <NuxtLink class="a-btn" to="/member/security/info">[{{ $t("去绑定") }}]</NuxtLink>
                            </template>
                        </span>
                    </el-form-item>
                    <el-form-item label="">
                        <el-button type="primary" @click="onSubmit">{{ $t("确认修改") }}</el-button>
                    </el-form-item>
                </el-form>
                <div v-else>
                    <div v-loading="loading" class="no-result"></div>
                </div>
            </div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import type { ProfileFormState } from "~/types/user/profile";
import { getProfile, updateProfile } from "~/api/user/profile";
definePageMeta({
    middleware: "auth"
});
const { t } = useI18n();
const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref<ProfileFormState>({});
const fetchProfile = async () => {
    try {
        const result = await getProfile();
        Object.assign(formState.value, result);
        if (result.birthday == "0000-00-00") {
            formState.value.birthday = "";
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const getMaskedEmail = (email: string | undefined) => {
    if (typeof email === "string") {
        // 使用正则表达式来处理邮箱
        return email.replace(/(.{2})(.*)(@.*)/, "$1*****$3");
    }
    return t("未绑定");
};
const getMaskedMobile = (mobile: string | undefined) => {
    if (typeof mobile === "string") {
        // 检查是否包含国际区号 86
        const internationalPrefix = "86";
        if (mobile.startsWith(internationalPrefix)) {
            // 保留国际区号和手机号的后四位
            return mobile.replace(new RegExp(`^(${internationalPrefix})(\\d{14})\\d*(\\d{4})$`), "$1$2*****$3");
        } else {
            // 保留手机号的前三位和后四位
            return mobile.replace(/(\d{3})\d*(\d{4})$/, "$1*****$2");
        }
    }
    return t("未绑定");
};
onMounted(() => {
    // 获取详情数据
    fetchProfile();
});
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        loading.value = true;
        const result = await updateProfile({ ...formState.value });
        message.success(t('修改成功'));
        await fetchProfile();
    } catch (error: any) {
    } finally {
        loading.value = false;
    }
};
</script>
<style></style>
<style lang="less" scoped>
@import "/assets/css/member/member";

.profile-info {
    background: #fff;
    border: 0;
    padding: 20px 20px;

    .title {
        display: flex;
        justify-content: space-between;
        align-items: center;

        h4 {
            color: var(--general);
            font-size: 14px;
            font-weight: bold;
        }

        .req {
            color: #999999;

            & > b {
                color: #ff4040;
            }
        }
    }

    .form-body {
        width: 300px;
        margin-top: 20px;

        .a-btn {
            color: var(--general);
            margin-left: 6px;
        }
    }
}
</style>
