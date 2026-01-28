<template>
    <div class="modal_region_box">
        <el-form
            label-width="auto"
            :inline-message="false"
            ref="formRef"
            :rules="formRules"
            :model="formState"
            autocomplete="off"
            v-if="!loading"
            label-suffix="："
        >
            <el-form-item :label="$t('收货人')" prop="consignee" :rules="[{ required: true, message: $t('请您填写收货人姓名') }]">
                <el-input v-model="formState.consignee" :maxlength="20" />
            </el-form-item>
            <el-form-item :label="$t('所在地区')" prop="regionIds" :rules="[{ required: true, message: $t('请选择收货人所在地区') }]" autoLink>
                <SelectRegion v-model="formState.regionIds" @change="onRegionIdsChange"></SelectRegion>
            </el-form-item>
            <el-form-item
                :label="$t('详细地址')"
                prop="address"
                :rules="[
                    { required: true, message: $t('请您填写详细收货地址') },
                    { min: 3, message: $t('收货人地址过短') }
                ]"
            >
                <el-input v-model="formState.address" :maxlength="100" />
            </el-form-item>
            <el-form-item :label="$t('手机号码')" prop="mobile">
                <el-input v-model="formState.mobile" :maxlength="20" />
            </el-form-item>
            <el-form-item :label="$t('固定电话')" prop="telephone" :rules="[]" :wrapper-col="{ span: 8 }" extra="">
                <el-input v-model="formState.telephone" :maxlength="20" />
            </el-form-item>
            <el-form-item :required="false" :label="$t('邮箱地址')" prop="email" :rules="[]" :wrapper-col="{ span: 8 }">
                <el-input v-model="formState.email" :maxlength="50" />
            </el-form-item>
        </el-form>
        <div class="address_from">
            <div class="from_item">
                <div class="tit"></div>
                <div class="btn_box">
                    <el-button type="primary" @click="onOk">{{ action === "add" ? $t("新增") : $t("修改") }}{{ $t("地址") }} </el-button>
                    <el-button v-if="props.showClose" @click="onClose">{{ $t("取消") }}</el-button>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { SelectRegion } from "@/components/region";
import { ref, reactive, onMounted } from "vue";
import { getAddressData, updateAddressData } from "@/api/user/address";
import type { AddressFormState } from "~/types/user/address.d";
import { message } from "ant-design-vue";
import { useCommonStore } from "~/store/common";
import { isOverseas } from "@/utils/util";

const commonStore = useCommonStore();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "change"]);
const props = defineProps({
    id: Number,
    act: { type: String, default: "add" },
    isDialog: Boolean,
    addressId: { type: Number, default: 0 },
    showClose: { type: Boolean, default: true }
});
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const operation = ref<string>(action.value === "add" ? "create" : "update");
const visible = ref<boolean>(false);
const formState = reactive<AddressFormState>({
    consignee: "",
    regionIds: [],
    address: "",
    mobile: "",
    telephone: "",
    email: ""
});
const { t } = useI18n();
const loading = ref(true);
onMounted(() => {
    if (props.addressId > 0) {
        getAddress();
    } else {
        loading.value = false;
    }
});
const getAddress = async () => {
    try {
        const result = await getAddressData(action.value, { id: props.addressId });
        Object.assign(formState, result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const validateMobile = (rule: any, value: any, callback: any) => {
    const regex = commonStore.isOpenMobileAreaCode ? /^(\+?[1-9]\d{1,14})$/ : /^(?:\+86)?1(?:3[0-9]|4[5-9]|5[0-35-9]|6[5-7]|7[0-8]|8[0-9]|9[189])\d{8}$/;
    if (!value) {
        return callback(new Error(t("手机号不能为空")));
    } else if (!isOverseas() && !regex.test(value)) {
        return callback(new Error(t("格式错误，请输入正确的手机号码")));
    } else {
        callback();
    }
};
const validateMail = (rule: any, value: any, callback: any) => {
    if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
        return callback(new Error(t("格式错误，请输入正确的邮箱地址")));
    } else {
        callback();
    }
};
const formRules = reactive<any>({
    mobile: [{ validator: validateMobile, trigger: "blur", required: true }],
    email: [{ validator: validateMail, trigger: "blur", required: true }]
});

const formRef = ref<any>(null);
const onOk = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateAddressData(operation.value, { id: props.addressId, ...formState });
        setTimeout(() => {
            message.success(t("操作成功"));
        }, 100);
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
const onRegionIdsChange = () => {
    formRef.value.validateField("regionIds");
};
const onClose = () => {
    emit("close");
};
// 表单提交
const onFormSubmit = () => {
    onOk();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.modal_region_box {
    padding: 10px;

    :deep(.el-form-item__error) {
        position: relative;
        top: 0;
        padding-left: 10px;
    }

    :deep(.is-error .select_region) {
        box-shadow: 0 0 0 1px #ff4d4f inset;
    }
}

.address_from {
    .from_item {
        display: flex;
        margin-bottom: 10px;

        .tit {
            display: inline-block;
            font-family: "宋体";
            margin-right: 5px;
            width: 85px;
            text-align: right;
            line-height: 30px;
        }

        .inp {
            span {
                color: #888888;
                margin-left: 5px;
            }

            select {
                margin-right: 10px;
            }
        }

        .btn_box {
            display: flex;
            margin-top: 10px;
            margin-bottom: 10px;

            .info {
                background: #f7f7f7;
                border: 1px solid #ddd;
                color: #666;
            }
        }
    }
}
</style>
