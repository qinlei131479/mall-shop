<template>
    <el-form-item
        v-if="formState.productType != 1 && formState.productType != 5 && formState.productType != 6"
        :rules="[{ required: true, message: '请选择配送方式!' }]"
        label="配送方式"
        prop="noShipping"
    >
        <el-radio-group v-model="formState.noShipping" :disabled="examine == 1" style="width: 100%; margin-bottom: 10px">
            <el-radio :value="1">无需配送</el-radio>
        </el-radio-group>
        <el-radio-group v-model="formState.noShipping" :disabled="examine == 1 || formState.productType != 1" style="width: 100%">
            <el-radio :value="0">
                <div class="flex">
                    <div class="mr10">运费模板</div>
                    <el-form-item label="" prop="shippingTplId">
                        <div class="flex flex-justify-between">
                            <el-select
                                v-model="formState.shippingTplId"
                                :disabled="examine == 1 || formState.productType != 1 || formState.noShipping == 1"
                                placeholder="请选择"
                                style="width: 100%"
                            >
                                <el-option
                                    v-for="item in formState.shippingTplList"
                                    :key="item.shippingTplId"
                                    :label="item.shippingTplName"
                                    :value="item.shippingTplId"
                                />
                            </el-select>
                            <div v-if="examine != 1" class="con-btn">
                                <DialogForm
                                    :params="{ act: 'add' }"
                                    isDrawer
                                    path="setting/shippingTpl/Info"
                                    title="添加运费模板"
                                    width="900px"
                                    @okCallback="fetchProductConfig"
                                >
                                    <el-button link type="primary">新建</el-button>
                                </DialogForm>
                                <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                                <el-button :loading="configLoading" link type="primary" @click="fetchProductConfig">刷新 </el-button>
                            </div>
                        </div>
                    </el-form-item>
                </div>
            </el-radio>
        </el-radio-group>
    </el-form-item>
    <el-form-item v-if="formState.productType == 1 && !isStore()" :rules="[{ required: true }]" label="快递运费" prop="fixedShippingType">
        <el-radio-group v-model="formState.fixedShippingType" :disabled="examine == 1" style="width: 100%; margin-bottom: 25px">
            <div>
                <el-radio :value="1">
                    <div class="fixed-shipping-type">
                        <div class="flex flex-align-center">
                            <div class="mr10">固定运费</div>
                            <el-form-item
                                :rules="[
                                    {
                                        required: true,
                                        validator: validateShippinFee
                                    }
                                ]"
                                class="inner-item"
                                label=""
                                prop="fixedShippingFee"
                            >
                                <PriceInput
                                    v-model:modelValue="formState.fixedShippingFee"
                                    :disabled="examine == 1 || formState.fixedShippingType == 2"
                                    placeholder="请输入运费"
                                    width="200px"
                                ></PriceInput>
                            </el-form-item>
                        </div>
                    </div>
                </el-radio>
                <div class="extra" style="margin-top: 5px; margin-left: 20px">
                    设置固定运费为0时，前台展示为包邮。
                    <el-popover :width="400" placement="right-end" trigger="click">
                        <template #reference>
                            <a>查看示例</a>
                        </template>
                        <template #default>
                            <img src="@/style/images/fixedShippingFee.png" style="width: 380px" />
                        </template>
                    </el-popover>
                </div>
            </div>
        </el-radio-group>

        <el-radio-group v-model="formState.fixedShippingType" :disabled="examine == 1 || formState.productType != 1" style="width: 100%">
            <el-radio :value="2">
                <div class="flex">
                    <div class="mr10">运费模板</div>
                    <el-form-item label="" prop="shippingTplId">
                        <div class="flex flex-justify-between">
                            <el-select
                                v-model="formState.shippingTplId"
                                :disabled="examine == 1 || formState.productType != 1 || formState.fixedShippingType == 1"
                                placeholder="请选择"
                                style="width: 100%"
                            >
                                <el-option
                                    v-for="item in formState.shippingTplList"
                                    :key="item.shippingTplId"
                                    :label="item.shippingTplName"
                                    :value="item.shippingTplId"
                                />
                            </el-select>
                            <div v-if="examine != 1" class="con-btn">
                                <DialogForm
                                    :params="{ act: 'add' }"
                                    isDrawer
                                    path="setting/shippingTpl/Info"
                                    title="添加运费模板"
                                    width="900px"
                                    @okCallback="fetchProductConfig"
                                >
                                    <el-button link type="primary">新建</el-button>
                                </DialogForm>
                                <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                                <el-button :loading="configLoading" link type="primary" @click="fetchProductConfig">刷新 </el-button>
                            </div>
                        </div>
                    </el-form-item>
                </div>
            </el-radio>
        </el-radio-group>
    </el-form-item>
    <el-form-item v-if="formState.productType == 1 && isStore()" :rules="[{ required: true, validator: deliveryMethod }]" label="配送方式" prop="isLogistics">
        <div>
            <el-checkbox v-model="formState.isLogistics" label="快递发货" :true-label="1" :false-label="0" />
            <div v-if="formState.isLogistics == 1">
                <el-radio-group v-model="formState.fixedShippingType" :disabled="examine == 1 && getShopType() !== 2" style="width: 100%; margin-bottom: 25px">
                    <div>
                        <el-radio :value="1">
                            <div class="fixed-shipping-type">
                                <div class="flex flex-align-center">
                                    <div class="mr10">固定运费</div>
                                    <el-form-item
                                        :rules="[
                                            {
                                                required: true,
                                                validator: validateShippinFee
                                            }
                                        ]"
                                        class="inner-item"
                                        label=""
                                        prop="fixedShippingFee"
                                    >
                                        <PriceInput
                                            v-model:modelValue="formState.fixedShippingFee"
                                            :disabled="(examine == 1 && getShopType() !== 2) || formState.fixedShippingType == 2"
                                            placeholder="请输入运费"
                                            width="200px"
                                        ></PriceInput>
                                    </el-form-item>
                                </div>
                            </div>
                        </el-radio>
                        <div class="extra" style="margin-top: 5px; margin-left: 20px">
                            设置固定运费为0时，前台展示为包邮。
                            <el-popover :width="400" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>查看示例</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/fixedShippingFee.png" style="width: 380px" />
                                </template>
                            </el-popover>
                        </div>
                    </div>
                </el-radio-group>

                <el-radio-group
                    v-model="formState.fixedShippingType"
                    :disabled="(examine == 1 && getShopType() !== 2) || formState.productType != 1"
                    style="width: 100%"
                >
                    <el-radio :value="2">
                        <div class="flex">
                            <div class="mr10">运费模板</div>
                            <el-form-item label="" prop="shippingTplId">
                                <div class="flex flex-justify-between">
                                    <el-select
                                        v-model="formState.shippingTplId"
                                        :disabled="(examine == 1 && getShopType() !== 2) || formState.productType != 1 || formState.fixedShippingType == 1"
                                        placeholder="请选择"
                                        style="width: 100%"
                                    >
                                        <el-option
                                            v-for="item in formState.shippingTplList"
                                            :key="item.shippingTplId"
                                            :label="item.shippingTplName"
                                            :value="item.shippingTplId"
                                        />
                                    </el-select>
                                    <div v-if="examine != 1 || getShopType() === 2" class="con-btn">
                                        <DialogForm
                                            :params="{ act: 'add' }"
                                            isDrawer
                                            path="setting/shippingTpl/Info"
                                            title="添加运费模板"
                                            width="900px"
                                            @okCallback="fetchProductConfig"
                                        >
                                            <el-button link type="primary">新建</el-button>
                                        </DialogForm>
                                        <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                                        <el-button :loading="configLoading" link type="primary" @click="fetchProductConfig">刷新 </el-button>
                                    </div>
                                </div>
                            </el-form-item>
                        </div>
                    </el-radio>
                </el-radio-group>
            </div>
        </div>
        <div style="width: 100%">
            <el-checkbox v-model="formState.isShopPickup" label="到店自提" :true-label="1" :false-label="0" :disabled="pickupTplData.status === 0" />
            <div class="wechat-config" v-if="formState.isShopPickup == 1">
                <div class="table-container">
                    <el-row>
                        <el-col :span="7">
                            <div class="table-tr">系统自动完成备货</div>
                        </el-col>
                        <el-col :span="10"><div class="table-tr">可自提时间</div></el-col>
                        <el-col :span="7"><div class="table-tr">提货有效期</div></el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="7">
                            <div class="table-td">
                                <div class="info">
                                    {{ pickupTplData.stockingStatus ? "已开启" : "已关闭" }}
                                </div>
                                <div class="btn">
                                    <DialogForm
                                        :maskClose="false"
                                        isDrawer
                                        :params="{ act: 'deawer' }"
                                        path="setting/pickupSetting/Index"
                                        title="自提设置"
                                        width="1010px"
                                        @okCallback="_getShopPickupTpl"
                                    >
                                        <el-button type="primary" link> 去设置 </el-button>
                                    </DialogForm>
                                </div>
                            </div>
                        </el-col>
                        <el-col :span="10">
                            <div class="table-td">
                                <div class="flex table-info">
                                    <div class="info" v-if="pickupTplData.pickupTimeStatus == 1">
                                        <template v-if="pickupTplData.pickupTimeJson.timeList && pickupTplData.pickupTimeJson.timeList.length > 0">
                                            <div class="info-conetnt">
                                                <div class="label">可自提时段：</div>
                                                <div class="val">
                                                    <div v-for="item in pickupTplData.pickupTimeJson.timeList">{{ item.startTime }}-{{ item.endTime }}</div>
                                                </div>
                                            </div>
                                        </template>
                                        <p v-if="pickupTplData.stockingStatus == 0">
                                            预约自提：需提前{{ pickupTplData.pickupTimeJson.appointmentTime.timeNum
                                            }}{{ timeUnit(pickupTplData.pickupTimeJson.appointmentTime.timeUnit) }}预约自提
                                        </p>
                                        <p v-else>
                                            已开启自动完成备货
                                        </p>
                                    </div>
                                    <div class="info" v-else>已关闭</div>
                                    <div class="btn">
                                        <DialogForm
                                            :maskClose="false"
                                            isDrawer
                                            :params="{ act: 'deawer' }"
                                            path="setting/pickupSetting/Index"
                                            title="自提设置"
                                            width="1010px"
                                            @okCallback="_getShopPickupTpl"
                                        >
                                            <el-button type="primary" link> 去设置 </el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                            </div>
                        </el-col>
                        <el-col :span="7">
                            <div class="table-td">
                                <div class="info">
                                    {{ pickupTplData.pickupEndStatus ? "已开启" : "已关闭" }}
                                </div>
                                <div class="btn">
                                    <DialogForm
                                        :maskClose="false"
                                        isDrawer
                                        :params="{ act: 'deawer' }"
                                        path="setting/pickupSetting/Index"
                                        title="自提设置"
                                        width="1010px"
                                        @okCallback="_getShopPickupTpl"
                                    >
                                        <el-button type="primary" link> 去设置 </el-button>
                                    </DialogForm>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                </div>
            </div>
        </div>
    </el-form-item>
</template>
<script lang="ts" setup>
import { ref, watch,nextTick } from "vue";
import { message } from "ant-design-vue";
import { ProductFormState } from "@/types/product/product.d";
import { getProductConfig } from "@/api/product/product";
import PriceInput from "@/views/product/product/src/PriceInput.vue";
import { DialogForm } from "@/components/dialog";
import { getShopPickupTpl } from "@/api/setting/pickupSetting";
import { pickupSettingData } from "@/types/setting/pickupSetting";
import { isStore } from "@/utils/version";
import { getAdminType, getShopType } from "@/utils/storage";
import shop from "@/router/asyncRoutes/admin/shop";

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: 0
    },
    examine: {
        type: Number,
        default: 0
    },
    loading: {
        type: Boolean,
        default: false
    }
});

const formState = defineModel<ProductFormState>("formState", { default: {} });

const validateShippinFee = (rule: any, value: any, callback: any) => {
    if (!value || Number(value) !== 0) {
        if (formState.value.fixedShippingType == 1) {
            if (value !== 0 && !value) {
                callback(new Error("运费不能为空"));
                return;
            }
            if (Number(value) > 999999) {
                callback(new Error("运费不能超过999999"));
                return;
            }
            callback();
            return;
        }
        callback();
    } else {
        callback();
    }
};

const deliveryMethod = (rule: any, value: any, callback: any) => {
    if (formState.value.isShopPickup == 0 && formState.value.isLogistics == 0) {
        callback(new Error("请选择快递配送方式"));
        return;
    }
    callback();
};

const configLoading = ref(false);

const fetchProductConfig = async (isMsg: boolean = true) => {
    configLoading.value = true;
    try {
        const result = await getProductConfig({ shopId: props.shopId });
        formState.value.shippingTplList = result.shippingTplList;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        setTimeout(() => {
            configLoading.value = false;
        }, 200);
    }
};

const pickupTplData = ref<pickupSettingData>({
    pickupTimeJson: {
        type: 1,
        timeList: [
            {
                startTime: "",
                endTime: ""
            }
        ],
        timeType: 1
    },
    pickupEndJson: {
        type: 1,
        day: undefined,
        hour: undefined
    }
});
const _getShopPickupTpl = async () => {
    try {
        const result = await getShopPickupTpl({shopId: props.shopId});
        pickupTplData.value = result;
        nextTick(() => {
            if (pickupTplData.value.status === 0) {
                formState.value.isShopPickup = 0;
            }
        });
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

const timeUnit = (data: string) => {
    let unit = "";
    switch (data) {
        case "m":
            unit = "分钟";
            break;
        case "h":
            unit = "小时";
            break;
        case "d":
            unit = "天";
            break;
    }
    return unit;
};

watch(
    () => formState.value,
    (newVal) => {
        if (newVal.productType == 1 && isStore()) {
            _getShopPickupTpl();
        }
    },
    { immediate: true }
);
</script>
<style lang="less" scoped>
.con-btn {
    display: flex;
    align-content: center;
    padding-left: 10px;

    span {
        color: #999;
    }

    a {
        display: block;
        word-break: keep-all;
        padding: 0 5px;
    }
}
.wechat-config {
    background-color: #f6f7fa;
    padding: 20px;
    margin-top: 10px;
    .table-container {
        border: 1px solid #ddd;
        border-right: none;
        .table-tr {
            padding: 15px;
            border-right: 1px solid #ddd;
        }
        .table-td {
            background-color: #fff;
            padding: 15px;
            border-right: 1px solid #ddd;
            border-top: 1px solid #ddd;
            display: flex;
            align-items: center;
            height: 100%; /* 添加这一行 */
            box-sizing: border-box; /* 确保padding和border不会增加高度 */
            .table-info {
                align-items: flex-end;
                .info-conetnt {
                    display: flex;
                    align-items: flex-start;
                    flex-wrap: wrap;
                    line-height: 20px;
                    .val {
                        display: flex;
                        gap: 0 10px;
                        flex-wrap: wrap;
                    }
                }
            }
            .btn {
                margin-left: 10px;
            }
        }
    }
}
</style>
