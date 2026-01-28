<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="提现"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="title-or-tabs">
                <div class="tag-and-input">
                    <div class="tig-tabs">
                        <MemberTopMenu :menuList="menuList"></MemberTopMenu>
                    </div>
                </div>
            </div>
            <div class="account-raplytocard-content">
                <div class="form-container">
                    <el-form ref="formRef" label-width="auto" :model="formState" class="form-body" label-suffix="：">
                        <el-form-item :label="$t('可用余额')">
                            <FormatPrice v-model="userInfo.balance" :fontStyle="{ color: 'var(--price)', fontSize: '14px' }" :showText="false"></FormatPrice>
                        </el-form-item>
                        <el-form-item :label="$t('提现方式')">
                            <div style="display: flex; gap: 10px">
                                <div :class="{ selected: formState.accountData.accountType === 1 }" class="card" @click="onClick(1)">
                                    <div>{{ $t("银行卡") }}</div>
                                </div>
                                <div :class="{ selected: formState.accountData.accountType === 2 }" class="card" @click="onClick(2)">
                                    <div>{{ $t("支付宝") }}</div>
                                </div>
                                <div :class="{ selected: formState.accountData.accountType === 3 }" class="card" @click="onClick(3)">
                                    <div>{{ $t("微信") }}</div>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item
                            :rules="[{ required: true, message: $t('请选择') + accountPlaceholder + $t('账号') }]"
                            :label="$t('提现至')"
                            prop="accountData.accountNo"
                        >
                            <el-select
                                v-model="formState.accountData.accountNo"
                                :placeholder="$t('请选择') + accountPlaceholder + $t('账号')"
                                @change="selectNo"
                            >
                                <el-option
                                    v-for="item in formState.accountData.accountType === 1
                                        ? yhkList
                                        : formState.accountData.accountType === 2
                                          ? zfbList
                                          : wxList"
                                    :key="item.accountId"
                                    :label="item.accountNo"
                                    :value="item.accountNo"
                                />
                            </el-select>
                        </el-form-item>
                        <InfoCard :page="0" :type="formState.accountData.accountType" act="add" @loadFilter="saveOver">
                            <div class="tips">
                                {{ isOverseas() ? $t("当前{0}账号已不用", [accountPlaceholder]) : `当前${accountPlaceholder}账号已不用` }}？<span
                                    class="font-color"
                                    style="cursor: pointer"
                                    >{{ $t("点击这里，去添加新的") }}</span
                                >{{ accountPlaceholder }}{{ $t("账号") }}。
                            </div>
                        </InfoCard>
                        <el-form-item :label="$t('提现姓名')" prop="accountData.accountName">
                            <el-input v-model="formState.accountData.accountName" :placeholder="$t('请选择') + accountPlaceholder + $t('账号')" disabled />
                        </el-form-item>
                        <el-form-item v-if="formState.accountData.accountType === 1" :label="$t('银行卡详情')" prop="accountData.bankName">
                            <el-input v-model="formState.accountData.bankName" :rows="4" disabled :placeholder="$t('银行卡详情')" type="textarea" />
                        </el-form-item>
                        <el-form-item :rules="amountRules" :label="$t('提现金额')" prop="amount">
                            <el-input v-model="formState.amount" :placeholder="$t('提现金额')" />
                        </el-form-item>
                        <el-form-item label="">
                            <el-button type="primary" @click="onSubmit">{{ $t("提交") }}</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { getMemberCenter } from "~/api/user/user";
import type { UserFormState } from "~/types/user/user";
import type { AccountFormState, AccountInfo } from "~/types/user/account";
import { getAccountNoList, updateWithdrawApply } from "~/api/user/account";
import InfoCard from "~/pages/member/account/src/InfoCard.vue";
import { isOverseas } from "@/utils/util";
definePageMeta({
    middleware: "auth"
});
const userInfo = ref<UserFormState>({} as UserFormState);
const getUserInfo = async () => {
    try {
        const result = await getMemberCenter();
        Object.assign(userInfo.value, result);
    } catch (error: any) {
        message.error(error.message);
    }
};
const formRef = shallowRef();
const onClick = async (number: number) => {
    formState.value.accountData.accountType = number;
    await formRef.value.resetFields();
};

onMounted(() => {
    getUserInfo();
    loadFilter(1);
    loadFilter(2);
    loadFilter(3);
});
const saveOver = () => {
    loadFilter(Number(formState.value.accountData.accountType));
};

const yhkList = reactive<AccountInfo[]>([]);
const zfbList = reactive<AccountInfo[]>([]);
const wxList = reactive<AccountInfo[]>([]);

const loadFilter = async (type: number) => {
    try {
        let temp: any = {
            accountType: type
        };
        const result = await getAccountNoList({ ...temp });
        if (type == 1) {
            yhkList.length = 0;
            yhkList.push(...result.records);
        } else if (type === 2) {
            zfbList.length = 0;
            zfbList.push(...result.records);
        } else {
            wxList.length = 0;
            wxList.push(...result.records);
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

const formState = ref<AccountFormState>({
    accountData: {
        accountType: 1
    }
} as AccountFormState);

const selectNo = (value: any) => {
    const list = formState.value.accountData.accountType === 1 ? yhkList : formState.value.accountData.accountType === 2 ? zfbList : wxList;
    let selectedItem = list.find((item) => item.accountNo === value);
    formState.value.accountData.accountNo = selectedItem?.accountNo as string;
    formState.value.accountData.accountName = selectedItem?.accountName as string;
    formState.value.accountData.bankName = selectedItem?.bankName as string;
};

const accountPlaceholder = computed(() => {
    switch (formState.value.accountData.accountType) {
        case 1:
            return t("银行卡");
        case 2:
            return t("支付宝");
        case 3:
            return t("微信");
        default:
            return "";
    }
});
const { t } = useI18n();
const amountRules = computed(() => {
    return [
        { required: true, message: t("请输入提现金额"), trigger: "blur" }, // 必填验证
        {
            validator: (rule: any, value: any, callback: any) => {
                // 正则表达式验证金额是否为0-12300的两位小数
                const regExp = /^(0|\d{0,9})(\.\d{1,2})?$/;
                if (!regExp.test(value)) {
                    callback(new Error(t("提现金额必须大于0，且最多保留两位小数")));
                } else if (value > Number(userInfo.value.balance)) {
                    callback(new Error(t("提现金额不能高于可用余额")));
                } else {
                    callback();
                }
            },
            trigger: "blur" // 触发方式，可以是 blur、change等
        }
    ];
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        const result = await updateWithdrawApply({ ...formState.value });
        await formRef.value.resetFields();
        message.success("提交成功");
    } catch (error: any) {
    } finally {
    }
};

const router = useRouter();
const splitStr = (str: string) => {
    return str.split("?")[0];
};
const menuList = reactive<any>([
    { value: "账户明细", url: "/member/account/detail", size: 0 },
    { value: "申请记录", url: "/member/account/log", size: 0 },
    { value: "充值", url: "/member/account/deposit", size: 0 },
    { value: "提现", url: "/member/account/raplytocard", size: 0 },
    { value: "卡管理", url: "/member/account/cardManagement", size: 0 }
]);
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";

.account-raplytocard-content {
    background: #fff;
    border: 0;
    padding: 20px;
    box-sizing: border-box;
    font-size: 14px;
    display: flex;
    justify-content: center;
    align-items: center;

    .tips {
        font-size: 12px;
        color: #a5a5a5;
        margin-left: 100px;
        margin-bottom: 20px;
    }

    .card {
        width: 100px;
        position: relative; /* 为了绝对定位内部的三角形 */
        cursor: pointer; /* 可选，鼠标悬停时显示为指针 */
        color: #666666;
        text-align: center;

        border: 1px solid #ddd;
        display: flex;
        flex-direction: column;
        box-sizing: border-box;
    }

    .selected {
        border-color: var(--general); /* 选中状态下边框变红 */
    }

    .selected::after {
        content: "";
        position: absolute;
        bottom: 0; /* 三角形在右下角，但留有一点间距 */
        right: 0;
        width: 0;
        height: 0;
        border-left: 16px solid transparent;
        border-top: 16px solid transparent;
        border-bottom: 16px solid var(--general); /* 三角形大小和颜色 */
        text-align: center;
        transform: rotate(-0deg); /* 调整勾的位置和角度 */
    }

    .selected::before {
        content: "✔";
        color: var(--main-text); /* 勾的颜色 */
        font-size: 8px; /* 勾的大小 */
        position: absolute;
        bottom: 5px; /* 调整勾在三角形内的垂直位置 */
        right: 0; /* 调整勾在三角形内的水平位置 */
        transform: translateX(0%) translateY(50%); /* 微调勾的位置 */
        z-index: 100;
    }

    .form-body {
        width: 440px;
    }
}
</style>
