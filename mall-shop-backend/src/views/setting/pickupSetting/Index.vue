<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="auto" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="item flex flex-align-center">
                        <div class="label mr10">到店自提</div>
                        <div class="value">
                            <el-switch v-model="formState.status" :active-value="1" :inactive-value="0" />
                            <div></div>
                        </div>
                    </div>
                    <div class="extra">启用后，买家下单可选择到店自提，可选择自提点进行提货。</div>
                </div>
                <div class="content_wrapper">
                    <div class="title">基础设置</div>
                    <el-form-item label="完成备货" prop="stockingStatus">
                        <div>
                            <el-checkbox v-model="formState.stockingStatus" label="开启系统自动完成备货" :true-label="1" :false-label="0" />
                            <div class="extra">针对预售自提订单，实际完成自动备货的时间为预售开始发货时间和自动备货时间中较晚的时间。</div>
                        </div>
                    </el-form-item>
                    <el-form-item label="自提时间" prop="pickupTimeStatus">
                        <div>
                            <el-radio-group v-model="formState.pickupTimeStatus" class="itemWidth">
                                <el-radio :value="0">不开启</el-radio>
                                <el-radio :value="1">需要买家选择自提时间</el-radio>
                            </el-radio-group>
                            <div class="extra" v-if="formState.pickupTimeStatus == 1">买家下单选择到店自提，必须要选择自提时间，卖家需要按约定时间备货</div>
                            <div class="extra" v-else>不开启，不展示提货时间</div>
                            <div class="wechat-config" v-if="formState.pickupTimeStatus === 1">
                                <el-form-item label="自提时间段">
                                    <div style="width: 90%">
                                        <el-radio-group v-model="formState.pickupTimeJson.type" class="itemWidth">
                                            <el-radio :value="1">每天</el-radio>
                                        </el-radio-group>
                                        <div class="time-list-row">
                                            <div v-for="(item, index) in formState.pickupTimeJson.timeList" class="flex flex-align-center mb10">
                                                <el-time-picker
                                                    format="HH:mm"
                                                    value-format="HH:mm"
                                                    style="width: 110px"
                                                    v-model="item.startTime"
                                                    placeholder="开始时间"
                                                />
                                                <div style="margin: 0 10px">至</div>
                                                <el-time-picker
                                                    format="HH:mm"
                                                    value-format="HH:mm"
                                                    style="width: 110px"
                                                    v-model="item.endTime"
                                                    placeholder="结束时间"
                                                />
                                                <el-button
                                                    v-if="formState.pickupTimeJson.timeList.length > 1"
                                                    class="ml10"
                                                    type="primary"
                                                    link
                                                    @click="deleteDayTime(index)"
                                                    >删除</el-button
                                                >
                                            </div>
                                            <div class="flex flex-align-center">
                                                <el-button type="primary" link @click="addDayTime()" :disabled="formState.pickupTimeJson.timeList.length >= 15"
                                                    >添加时间段</el-button
                                                >
                                                <div class="tips">最多添加15个时间段</div>
                                            </div>
                                        </div>
                                    </div>
                                </el-form-item>
                                <el-form-item
                                    label="预约自提"
                                    :rules="[{ required: formState.stockingStatus === 0 ? true : false, message: '请输入预约自提时间段' }]"
                                    prop="pickupTimeJson"
                                >
                                    <div class="flex flex-align-center" v-if="formState.stockingStatus === 0">
                                        <el-radio-group v-model="formState.pickupTimeJson.type" class="mr10">
                                            <el-radio :value="1">需提前</el-radio>
                                        </el-radio-group>
                                        <div v-if="formState.pickupTimeJson.appointmentTime && formState.pickupTimeJson.appointmentTime.timeUnit">
                                            <TigInput v-model="formState.pickupTimeJson.appointmentTime.timeNum" width="160px">
                                                <template #append>
                                                    <el-select v-model="formState.pickupTimeJson.appointmentTime.timeUnit" style="width: 80px">
                                                        <el-option :label="item.label" :value="item.value" v-for="item in timeTypes" />
                                                    </el-select>
                                                </template>
                                            </TigInput>
                                        </div>
                                        <div class="ml10">预约自提</div>
                                    </div>
                                    <div v-else class="time-type-row">已开启自动完成备货，最早可预约自提时间与备货完成时间相同</div>
                                </el-form-item>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item label="提货有效期" prop="pickupEndStatus">
                        <div>
                            <el-checkbox v-model="formState.pickupEndStatus" label="开启提货有效期" :true-label="1" :false-label="0" />
                            <div class="wechat-config" v-if="formState.pickupEndStatus === 1">
                                <el-form-item label="停止提货时间" :rules="[{ required: true, validator: validatePickupEnd }]" prop="pickupEndJson">
                                    <div class="flex flex-align-center">
                                        <el-radio-group v-model="formState.pickupEndJson.type" class="mr10">
                                            <el-radio :value="1">备货完成</el-radio>
                                        </el-radio-group>
                                        <TigInput v-model="formState.pickupEndJson.day" width="130px" type="integer">
                                            <template #append> 天 </template>
                                        </TigInput>
                                        <TigInput v-model="formState.pickupEndJson.hour" width="130px" class="ml10" type="integer">
                                            <template #append> 小时 </template>
                                        </TigInput>
                                        <div class="ml10">后，停止提货</div>
                                        <div class="tips">填写0天或0小时，则仅限当天可提货</div>
                                    </div>
                                </el-form-item>
                            </div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div v-if="act !== 'deawer'" class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">提 交</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import { pickupSettingData } from "@/types/setting/pickupSetting";
import { getShopPickupTpl, saveShopPickupTpl } from "@/api/setting/pickupSetting";
import { useConfigStore } from "@/store/config";
import { useRoute } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    act: {
        type: String,
        default: ""
    }
});
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
const timeTypes = [
    {
        value: "d",
        label: "天"
    },
    {
        value: "h",
        label: "小时"
    },
    {
        value: "m",
        label: "分钟"
    }
];
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<pickupSettingData>({
    pickupTimeJson: {
        type: 1,
        timeList: [
            {
                startTime: "",
                endTime: ""
            }
        ],
        appointmentTime: {
            timeNum: undefined,
            timeUnit: ""
        }
    },
    pickupEndJson: {
        type: 1,
        day: undefined,
        hour: undefined
    }
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getShopPickupTpl();
        if (result.pickupTimeJson.type == null) {
            result.pickupTimeJson.type = 1;
        }
        if (result.pickupTimeJson.timeList == null) {
            result.pickupTimeJson.timeList = [
                {
                    startTime: "",
                    endTime: ""
                }
            ];
        }
        if (result.pickupTimeJson.appointmentTime == null || result.pickupTimeJson.appointmentTime == 'null' || !result.pickupTimeJson.appointmentTime == null) {
            result.pickupTimeJson.appointmentTime = {
                timeUnit: "d",
                timeNum: 1
            };
        } else {
            result.pickupTimeJson.appointmentTime = result.pickupTimeJson.appointmentTime;
        }
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const deleteDayTime = (index: number) => {
    formState.value.pickupTimeJson.timeList.splice(index, 1);
};

const addDayTime = () => {
    formState.value.pickupTimeJson.timeList.push({
        startTime: "",
        endTime: ""
    });
};

const validatePickupEnd = (rule: any, value: any, callback: any) => {
    if (value.type !== 1) {
        callback(new Error("请选择停止提货时间"));
    } else if (value.day === null || value.hour === null || value.hour === '' || value.day === '') {
        console.log(value.day, value.hour);
        callback(new Error("请输入提货有效期"));
    } else {
        callback();
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    confirmLoading.value = true;
    try {
        const result = await saveShopPickupTpl(formState.value);
        message.success("修改成功");
        configStore.updateConfig();
        if (props.act === "deawer") {
            emit("submitCallback", result);
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

watch(formState.value, (newVal) => {
    if (newVal.closeAuth == 0) {
        formState.value.isIdentity = 0;
    }
});

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-top: 20px;
    padding-bottom: 20px;
    font-size: 14px;
}

.extra {
    font-size: 12px;
    color: #9598a6;
    line-height: 20px;
    padding-top: 3px;
}

.label {
    font-weight: bold;
    font-size: 14px;
}

.wechat-config {
    background: #f5f7fa;
    min-width: 600px;
    padding: 20px;
    margin-top: 10px;
    .extra {
        margin: 0px 0 0px 0;
    }
    .time-list-row {
        width: 100%;
        background-color: #fff;
        padding: 20px;
        margin-bottom: 20px;
    }
    .time-type-row {
        background-color: #fff;
        padding: 10px;
        width: 90%;
    }
    .tips {
        color: #9598a6;
        margin-left: 10px;
    }
    :deep(.el-select) {
        min-width: 80px !important;
    }
}
@media (max-width: 768px) {
    .wechat-config {
        width: 90% !important;
    }
}
</style>
