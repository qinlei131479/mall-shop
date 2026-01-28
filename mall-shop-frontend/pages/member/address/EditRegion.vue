<template>
    <div>
        <div class="modal_region_box">
            <a-form :model="formState" name="basic" :label-col="{ span: 3 }" :wrapper-col="{ span: 16 }" autocomplete="off" v-if="!loading" ref="formRef">
                <a-form-item label="收货人：" name="consignee" :rules="[{ required: true, message: '请您填写收货人姓名' }]">
                    <a-input v-model:value="formState.consignee" style="width: 300px" :maxlength="20" />
                </a-form-item>
                <a-form-item label="所在地区：" name="regionIds" :rules="[{ required: true, message: '请选择收货人所在地区' }]" autoLink>
                    <SelectRegion v-model="formState.regionIds" @change="onRegionIdsChange"></SelectRegion>
                </a-form-item>
                <a-form-item
                    label="详细地址："
                    name="address"
                    :rules="[
                        { required: true, message: '请您填写详细收货地址' },
                        { min: 3, message: '收货人地址过短' }
                    ]"
                >
                    <a-input v-model:value="formState.address" style="width: 300px" :maxlength="100" />
                </a-form-item>
                <a-form-item label="手机号码：" name="mobile" :rules="[{ required: true, message: '请您填写收货人手机号码' }]" extra="">
                    <a-input v-model:value="formState.mobile" style="width: 300px" :maxlength="20" />
                </a-form-item>
                <a-form-item label="固定电话：" name="telephone" :rules="[]" :wrapper-col="{ span: 8 }" extra="">
                    <a-input v-model:value="formState.telephone" style="width: 300px" :maxlength="20" />
                </a-form-item>
                <a-form-item label="邮箱地址：" name="email" :rules="[]" :wrapper-col="{ span: 8 }">
                    <a-input v-model:value="formState.email" style="width: 300px" :maxlength="50" />
                </a-form-item>
            </a-form>
            <div class="address_from">
                <div class="from_item">
                    <div class="tit"></div>
                    <div class="btn_box">
                        <div class="btn" @click="onOk">确认修改地址</div>
                        <div v-if="props.showClose" class="btn info" @click="onClose">取消</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { SelectRegion } from "@/components/region";
import { ref, reactive, onMounted } from "vue";
import { getAddressData, updateAddressData } from "@/api/user/address";
import { message } from "ant-design-vue";

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "change"]);
const props = defineProps({
    id: Number,
    act: String,
    isDialog: Boolean,
    addressId: { type: Number, default: 0 },
    showClose: { type: Boolean, default: true }
});
const visible = ref<boolean>(false);
const formState = reactive({
    consignee: "",
    regionIds: [],
    address: "",
    mobile: "",
    telephone: "",
    email: ""
});
const loading = ref(true);
onMounted(() => {
    if (props.addressId > 0) {
        getAddress();
    } else {
        loading.value = false;
    }
});
const onAfterClose = () => {};
const getAddress = async () => {
    try {
        const result = await getAddressData(props.addressId);
        Object.assign(formState, result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const updateAddress = async () => {};
const handleOk = async (e: MouseEvent) => {};
const formRef = ref();
const onOk = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateAddressData(formState);
        emit("submitCallback", result.addressId);
    } catch (error:any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
const onRegionIdsChange = () => {
    formRef.value.validateFields(["regionIds"]);
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
:deep(.ant-form-item-control) {
    flex-direction: inherit;
    width: 100%;
}

:deep(.ant-form-item-control-input) {
    margin-right: 10px;
}

:deep(.ant-form-item) {
    margin-bottom: 14px;
}

:deep(.ant-form-margin-offset) {
    display: none;
}

:deep(.ant-form-item-explain) {
    line-height: 30px;
}

:deep(.ant-form-item-has-error .select_region) {
    border-color: #ff4d4f;
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

            .btn {
                background: #ff6c6c;
                border: 1px solid #ff5e5e;
                color: #fff;
                cursor: pointer;
                border-radius: 2px;
                display: inline-block;
                height: 16px;
                line-height: 16px;
                padding: 10px 25px;
                text-align: center;
                text-decoration: none;
                font-size: 14px;
                margin-right: 20px;
            }

            .info {
                background: #f7f7f7;
                border: 1px solid #ddd;
                color: #666;
            }
        }
    }
}
</style>
