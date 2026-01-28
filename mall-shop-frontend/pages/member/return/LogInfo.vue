<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="申请详情"></CommonHeader>
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
            <template v-if="formState.returnGoodsTip">
                <div class="title-or-tabs">
                    <div>{{ formState.returnGoodsTip }}</div>
                </div>
            </template>

            <div v-if="!loading" class="log-return-content">
                <div class="button-col">
                    <el-button v-if="formState.canCancel" @click="revocation(Number(formState.aftersaleId))">{{ $t("撤销") }}</el-button>
                </div>
                <div class="tig-steps">
                    <template v-for="(item, index) in formState.stepStatus.steps">
                        <div
                            :style="{
                                color: item.active ? 'var(--general)' : '#c4c4c4',
                                border: item.active ? '4px solid var(--general)' : '4px solid #eee'
                            }"
                            class="steps-card"
                        >
                            <div style="font-weight: 600">{{ index + 1 }}</div>
                            <div :style="{ color: item.active ? 'var(--general)' : '#333' }" class="card-text">
                                <div>{{ item.title }}</div>
                                <div>{{ item.description }}</div>
                            </div>
                        </div>
                        <div
                            v-if="index != Number((formState?.stepStatus?.steps?.length ? formState?.stepStatus?.steps?.length : 0) - 1)"
                            :style="{ backgroundColor: formState!.stepStatus!.steps![index + 1]!.active ? 'var(--general)' : '#eee' }"
                            class="steps-line"
                        ></div>
                    </template>
                </div>
                <div v-if="formState.status != 6" class="file-card">
                    <el-form ref="formRef" :label-width="100" :model="formState" class="form-body" label-suffix="：" v-if="formState.status != 7">
                        <el-form-item :label="$t('问题补充')" prop="logInfo">
                            <el-input v-model="formState.logInfo" :row="4" clearable :placeholder="$t('请输入问题补充')" type="textarea" />
                        </el-form-item>
                        <template v-if="formState.status === 4">
                            <el-form-item :label="$t('回寄地址')" prop="returnAddress">
                                <el-input v-model="formState.returnAddress" readonly clearable type="textarea" />
                            </el-form-item>
                            <el-form-item :rules="[{ required: true, message: $t('快递公司不能为空!') }]" :label="$t('快递公司')" prop="logisticsName">
                                <el-input v-model="formState.logisticsName" clearable :placeholder="$t('请输入快递公司')" />
                            </el-form-item>
                            <el-form-item :rules="[{ required: true, message: $t('快递单号不能为空!') }]" :label="$t('快递单号')" prop="trackingNo">
                                <el-input v-model="formState.trackingNo" clearable :placeholder="$t('请输入快递单号')" type="textarea" />
                            </el-form-item>
                        </template>
                        <el-form-item :label="$t('上传文件')" prop="returnPic">
                            <UploadImage v-model:uploadList="formState.returnPic" :fileTypes="['jpeg', 'png']" :limit="5"></UploadImage>
                        </el-form-item>
                        <el-form-item label="">
                            <el-button type="primary" @click="onSubmit">{{ $t("提交") }}</el-button>
                        </el-form-item>
                    </el-form>
                </div>
                <div class="info-card">
                    <table>
                        <thead class="table-header">
                            <tr>
                                <th class="header-cell">{{ $t("商品名称") }}</th>
                                <th class="header-cell" style="width: 60px">{{ $t("商品价格") }}</th>
                                <th class="header-cell" style="width: 60px">{{ $t("购买数量") }}</th>
                                <th class="header-cell" style="width: 60px">{{ $t("小计") }}</th>
                                <th class="header-cell" style="width: 60px">{{ $t("退/换数量") }}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in orderList">
                                <td>
                                    <NuxtLink
                                        :title="item.productName"
                                        :to="urlFormat({ path: 'product', id: item.itemId, sn: item.productSn })"
                                        class="nuxt"
                                        target="_blank"
                                    >
                                        <el-image
                                            :src="imageFormat(item.picThumb)"
                                            class="pro_pic"
                                            loading="lazy"
                                            style="transition: opacity 0.2s linear; flex-shrink: 0"
                                        />
                                        <div>
                                            <div class="text-ellipsis">
                                                <ul>
                                                    <li>{{ $t("货号") }}：{{ item.productSn }}</li>
                                                    <li>{{ item.productName }}</li>
                                                    <li>
                                                        <template v-for="(sku, index) in item.skuData">
                                                            <span class="name">{{ sku.name }}：</span><span class="value">{{ sku.value }}</span
                                                            ><span v-if="index < item.skuData?.length - 1">；</span>
                                                        </template>
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                    </NuxtLink>
                                </td>
                                <td>
                                    <FormatPrice v-model="item.price" :fontStyle="{ color: 'var(--price)' }" :showText="false"></FormatPrice>
                                </td>
                                <td>{{ item.quantity }}</td>
                                <td>
                                    <FormatPrice v-model="item.subtotal" :fontStyle="{ color: 'var(--price)' }" :showText="false"></FormatPrice>
                                </td>
                                <td>
                                    {{ item.number }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="info-card">
                    <table>
                        <tbody>
                            <tr>
                                <td>{{ $t("买家问题描述") }}:</td>
                                <td>{{ formState.description }}</td>
                            </tr>
                            <tr>
                                <td>{{ $t("服务类型") }}:</td>
                                <td>{{ formState.aftersalesTypeName }}</td>
                            </tr>
                            <tr>
                                <td>{{ $t("退款金额") }}:</td>
                                <td><FormatPrice v-model="formState.refundAmount" :showText="false"></FormatPrice></td>
                            </tr>
                            <tr>
                                <td>{{ $t("售后类型") }}:</td>
                                <td>{{ formState.aftersaleReason }}</td>
                            </tr>
                            <tr v-if="formState.status === 4">
                                <td>{{ $t("退货地址") }}:</td>
                                <td>{{ formState.returnAddress }}</td>
                            </tr>
                            <tr>
                                <td>{{ $t("申请图片") }}:</td>
                                <td>
                                    <div class="application-picture">
                                        <template v-for="item in formState.pics">
                                            <el-image
                                                :src="imageFormat(item.picThumb)"
                                                fit="cover"
                                                style="width: 80px; height: 80px"
                                                :preview-src-list="[imageFormat(item.picUrl)]"
                                                :initial-index="0"
                                            />
                                        </template>
                                    </div>
                                </td>
                            </tr>
                            <template v-if="formState.refund">
                                <tr v-if="formState.refund.isOnline > 0">
                                    <td>{{ $t("线上退款金额") }}:</td>
                                    <td>
                                        <FormatPrice v-model="formState.refund.onlineBalance" :showText="false"></FormatPrice>
                                    </td>
                                </tr>
                                <tr v-if="formState.refund.isOffline > 0">
                                    <td>{{ $t("线下退款金额") }}:</td>
                                    <td>
                                        <FormatPrice v-model="formState.refund.offlineBalance" :showText="false"></FormatPrice>
                                    </td>
                                </tr>
                                <tr v-if="formState.refund.isReceive > 0">
                                    <td>{{ $t("余额退款金额") }}:</td>
                                    <td>
                                        <FormatPrice v-model="formState.refund.refundBalance" :showText="false"></FormatPrice>
                                    </td>
                                </tr>
                            </template>
                        </tbody>
                    </table>
                </div>
                <div class="info-card">
                    <table>
                        <thead class="table-header">
                            <tr>
                                <th class="header-cell" style="width: 120px">{{ $t("发言人") }}</th>
                                <th class="header-cell" style="width: 160px">{{ $t("时间") }}</th>
                                <th class="header-cell">{{ $t("信息") }}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in formState.aftersalesLog">
                                <td>{{ item.userName ? item.userName : item.adminName }}</td>
                                <td>{{ item.addTime }}</td>
                                <td>
                                    <div class="bt-image">
                                        <div>{{ item.logInfo }}</div>
                                        <template v-for="img in item.returnPic">
                                            <el-image
                                                :src="imageFormat(img.picUrl)"
                                                fit="cover"
                                                class="bt-img"
                                                loading="lazy"
                                                style="transition: opacity 0.2s linear"
                                                :preview-src-list="[imageFormat(img.picUrl)]"
                                                :initial-index="0"
                                            />
                                        </template>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div v-else v-loading="loading" class="log-return-content"></div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
    <el-dialog v-model="dialogVisible">
        <div class="image">
            <el-image :src="imageFormat(dialogImageUrl)" alt="" fit="cover" />
        </div>
    </el-dialog>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { message, Modal } from "ant-design-vue";
import type { AfterSalesRecordFormState } from "~/types/user/afterSalesRecord";
import { cancelAfterSalesRecord, getAfterSalesRecord, updateAfterSalesRecord } from "~/api/user/afterSalesRecord";
import UploadImage from "~/components/form/src/UploadImage.vue";
import { urlFormat } from "~/utils/format";
import type { AfterSalesFormState } from "~/types/user/afterSales";
definePageMeta({
    middleware: "auth"
});
const { t } = useI18n();
const dialogImageUrl = ref("");
const dialogVisible = ref(false);
const openImage = (url: string) => {
    dialogImageUrl.value = url;
    dialogVisible.value = true;
};
const orderList = ref(<AfterSalesFormState[]>[]);
const router = useRouter();
const formState = ref<AfterSalesRecordFormState>({
    stepStatus: {},
    picsList: [],
    returnPic: [],
    pics: []
});
const loading = ref<boolean>(true);
onMounted(() => {
    fetchAfterSalesRecord();
});
const fetchAfterSalesRecord = async () => {
    try {
        loading.value = true;
        const result = await getAfterSalesRecord({ id: router.currentRoute.value.query.id });
        Object.assign(formState.value, result);
        orderList.value = result.aftersalesItems ? result.aftersalesItems : [];
        orderList.value?.forEach((data: any) => {
            data.subtotal = data.quantity * data.price;
        });
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const formRef = shallowRef();
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        // formState.value.returnPic.forEach((item: any) => {
        //     formState.value.returnPic.push(item.picUrl);
        // });
        const result = await updateAfterSalesRecord({ id: formState.value.aftersaleId, ...formState.value });
        message.success(t("提交成功"));
        await formRef.value.resetFields();
        await fetchAfterSalesRecord();
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
const revocation = async (id: number) => {
    Modal.confirm({
        title: t("确认删要撤销申请吗"),
        cancelText: t("取消"),
        okText: t("确定"),
        maskClosable: true,
        centered: true,
        onOk: async () => {
            try {
                const result = await cancelAfterSalesRecord({ aftersaleId: id });
                message.success(t("撤销成功"));
                navigateTo("/member/return/log");
            } catch (error: any) {
                message.error(error.message);
            } finally {
            }
        }
    });
};

const splitStr = (str: string) => {
    return str.split("?")[0];
};
const menuList = reactive<any>([
    { value: "退/换货申请", url: "/member/return/list", size: 0 },
    { value: "申请记录", url: "/member/return/logInfo", size: 0 }
]);
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";

.log-return-content {
    .button-col {
        display: flex;
        justify-content: flex-end;
    }

    background: #fff;
    border: 0;
    padding: 20px;
    box-sizing: border-box;
    min-height: 340px;

    .file-card {
        margin: 30px;
        padding: 20px;
        background-color: #fafafa;

        .form-body {
            width: 600px;
            margin-top: 20px;

            .a-btn {
                color: #005ea7;
                margin-left: 6px;
            }
        }
    }

    .info-card {
        margin: 0 30px 30px 30px;
        box-sizing: border-box;
        border-collapse: collapse; /* 边框合并为单一边框 */

        .table-header {
            background-color: #f5f5f5;
            border: 1px solid #ddd;
        }

        .header-cell {
            border: 1px solid #ddd;
            border-right: none;
            padding: 8px 10px;
            box-sizing: border-box;
            text-align: left;
        }

        /* 设置单元格样式 */

        td {
            border: 1px solid #e8e8e8; /* 单元格边框颜色 */
            padding: 10px; /* 单元格内边距 */
        }

        /* 设置左侧列宽度 */

        td:first-child {
            width: 120px; /* 左侧列宽度 */
        }
    }

    .application-picture {
        display: flex;
        gap: 8px;
        cursor: pointer;
    }
}

.info-card {
    margin: 0 30px 30px 30px;
    box-sizing: border-box;
    border-collapse: collapse; /* 边框合并为单一边框 */

    .table-header {
        background-color: #f5f5f5;
    }

    .header-cell {
        border: 1px solid #ddd;
        padding: 8px 10px;
        box-sizing: border-box;
        text-align: left;
    }

    /* 设置单元格样式 */

    td {
        border: 1px solid #e8e8e8; /* 单元格边框颜色 */
        padding: 10px; /* 单元格内边距 */

        .nuxt {
            display: flex;
            gap: 10px;

            .pro_pic {
                width: 40px;
                height: 40px;
            }
        }
    }
}

.image {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}

.bt-image {
    display: flex;
    flex-wrap: wrap;
    flex-direction: row;
    align-items: center;
    width: 100%;
    gap: 8px;

    .bt-img {
        width: 80px;
        height: 80px;
        cursor: pointer;
    }
}
</style>
