<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <el-form ref="formRef" :model="formState" label-width="auto" style="display: flex; gap: 12px; flex-direction: column">
                    <div class="content_wrapper">
                        <el-form-item label="商品分配后状态" prop="storePostAllocationStatus">
                            <div>
                                <el-radio-group v-model="formState.storePostAllocationStatus">
                                    <el-radio :value="1">上架</el-radio>
                                    <el-radio :value="0">下架</el-radio>
                                </el-radio-group>
                                <div class="extra">在将商品分配到组织时，可预设分配成功后商品在组织的上下架状态。</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="门店独立商品" prop="storeIndependentGoods">
                            <div>
                                <el-checkbox v-model="formState.storeIndependentGoods" label="允许门店自建商品" :true-label="1" :false-label="0" />
                                <div class="extra">门店不可自行创建商品，若已存在自建商品，则将失效。</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="门店商品审核" prop="shopProductNeedCheck" v-if="formState.storeIndependentGoods == 1">
                            <div>
                                <el-checkbox v-model="formState.shopProductNeedCheck" label="商品默认审核通过" :true-label="0" :false-label="1" />
                                <div class="extra">门店发布商品的时候是否需要审核才能显示商品</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="分配商品名称" prop="storeAssignProductName">
                            <div>
                                <el-checkbox v-model="formState.storeAssignProductName" label="门店可修改商品名称" :true-label="1" :false-label="0" />
                                <div class="extra">
                                    勾选后，门店可修改总部/区域分配商品的名称，总部/区域编辑名称后则不同步至门店。若不勾选，总部/区域修改名称后将自动同步至门店。
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item label="分配商品价格" prop="storeAssignProductPrice">
                            <div>
                                <el-radio-group v-model="formState.storeAssignProductPrice">
                                    <el-radio :value="0">使用总部/区域统一售价</el-radio>
                                    <el-radio :value="1">使用门店独立售价</el-radio>
                                </el-radio-group>
                                <div class="extra">用统一售价时，门店商品销售价将取总部/区域设置的统一售价，门店不可修改。</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="库存模式" prop="storeUseTotalProductStock">
                            <el-checkbox
                                v-model="formState.storeUseSoloProductStock"
                                :disabled="formState.storeUseSoloProductStock === 1 && formState.storeUseTotalProductStock === 0 ? true : false"
                                label="使用门店独立库存"
                                :true-label="1"
                                :false-label="0"
                            />
                            <el-checkbox
                                v-model="formState.storeUseTotalProductStock"
                                :disabled="formState.storeUseTotalProductStock === 1 && formState.storeUseSoloProductStock === 0 ? true : false"
                                label="使用平台库存"
                                :true-label="1"
                                :false-label="0"
                            />
                        </el-form-item>
                        <el-form-item label="客户下单配送方式" prop="storeCustomSubmitShippingType">
                            <div>
                                <el-radio-group v-model="formState.storeCustomSubmitShippingType">
                                    <el-radio :value="0">自提优先</el-radio>
                                    <el-radio :value="1">配送优先</el-radio>
                                </el-radio-group>
                                <div class="extra">
                                    商品存在多种配送方式时，客户下单，默认配送方式为您勾选的方式，客户可在订单提交页面手动修改配送方式。<el-popover
                                        :width="400"
                                        placement="right-end"
                                        trigger="click"
                                    >
                                        <template #reference>
                                            <a>查看示例</a>
                                        </template>
                                        <template #default>
                                            <img src="@/style/images/store/storeCustomSubmitShippingType.png" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item label="显示地区其他门店" prop="storeShowOtherCityStore">
                            <div>
                                <el-radio-group v-model="formState.storeShowOtherCityStore" class="itemWidth">
                                    <el-radio :value="1">显示</el-radio>
                                    <el-radio :value="0">不显示</el-radio>
                                </el-radio-group>
                            </div>
                        </el-form-item>
                    </div>
                </el-form>
                <div style="height: 20px"></div>
                <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                    <div class="selected-action">
                        <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">提 交 </el-button>
                    </div>
                </div>
            </a-spin>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { storeSettings, saveStore } from "@/api/store/setting";
import type { Response } from "@/types/store/setting";
import { useConfigStore } from "@/store/config";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useRoute } from "vue-router";
import { useThemeStore } from "@/store/theme";
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<Response>>({
    storePostAllocationStatus: undefined,
    storeIndependentGoods: undefined,
    storeAssignProductName: undefined,
    storeAssignProductPrice: undefined,
    shopProductNeedCheck: undefined,
    storeUseTotalProductStock: undefined,
    storeCustomSubmitShippingType: undefined,
    storeShowOtherCityStore: undefined,
    storeUseSoloProductStock: undefined
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});

const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await storeSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await saveStore(formState.value);
        message.success("修改成功");
        configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.content {
    padding: 0 20px;
}

.content_wrapper {
    min-height: calc(100vh - 260px);
}
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}

.itemWidth {
    width: 100%;
}

.error {
    color: red;
}
</style>
