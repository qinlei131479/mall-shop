<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="收货地址"></CommonHeader>
    <div class="container flex">
        <div class="menu">
            <MemberNav></MemberNav>
        </div>
        <div class="info-row">
            <div class="title-or-tabs">
                <span>{{ $t("收货地址") }}</span>
            </div>
            <div class="address-info">
                <MemberAddressDialogEditRegion
                    ref="addAddressRef"
                    :maskClosable="true"
                    :closable="true"
                    @okCallback="loadFilter"
                    :title="$t('添加收货地址')"
                    width="600px"
                    path="MemberEditRegion"
                    :params="{ act: 'add', showClose: true }"
                    :showFooter="false"
                >
                    <el-button type="primary">{{ $t("添加新地址") }}</el-button>
                </MemberAddressDialogEditRegion>
                <!-- <DialogForm :params="{ act: 'add' }" :showFooter="false" path="member/address/EditRegion"
                            title="添加新地址" width="800px">
                    <a class="tig-button red-btn">添加新地址</a>
                </DialogForm> -->
                <div v-loading="loading" class="card-list">
                    <template v-if="total > 0">
                        <ul v-for="item in filterState" class="card">
                            <li class="first">
                                <div>{{ $t("收货人姓名") }}：{{ item.consignee || "-" }}</div>
                                <div>
                                    <MemberAddressDialogEditRegion
                                        @okCallback="loadFilter"
                                        :title="$t('编辑收货地址')"
                                        width="800px"
                                        path="MemberEditRegion"
                                        :params="{ act: 'detail', addressId: item.addressId }"
                                        :showFooter="false"
                                    >
                                        <span class="editAddress font-color">{{ $t("修改") }}</span>
                                    </MemberAddressDialogEditRegion>
                                    |
                                    <DeleteRecord :params="{ id: item.addressId }" :requestApi="delAddress" @afterDelete="loadFilter"
                                        ><span class="editAddress font-color">{{ $t("删除") }}</span>
                                    </DeleteRecord>
                                </div>
                            </li>
                            <li class="w100">{{ $t("收货地址") }}： {{ item.regionName }} {{ item.address }}</li>
                            <li class="ju">
                                <div>{{ $t("电子邮箱") }}：{{ item.email || "-" }}</div>
                                <div>{{ $t("电话") }}：{{ item.telephone || "-" }}</div>
                                <div>{{ $t("手机") }}：{{ item.mobile || "-" }}</div>
                            </li>
                        </ul>
                        <div class="el-page">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
                    </template>
                    <template v-else>
                        <div class="no-result">
                            <span v-if="!loading">{{ $t("暂无收货地址") }}</span>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
import { reactive, ref } from "vue";
import type { AddressFilterParams, AddressFilterState } from "~/types/user/address.d";
import { delAddress, getAddressList } from "~/api/user/address";
import DialogForm from "~/components/dialog/src/DialogForm.vue";
definePageMeta({
    middleware: "auth"
});
import DeleteRecord from "~/components/member/DeleteRecord.vue";
import { Pagination } from "~/components/list";

const filterState = ref(<AddressFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive<AddressFilterParams>({
    //初使化用于查询的参数
    page: 1
} as AddressFilterParams);

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAddressList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
@import "/assets/css/member/member";

.address-info {
    background: #fff;
    border: 0;
    padding: 20px 20px;

    .card-list {
        padding: 10px 0;

        .card {
            background: none repeat scroll 0 0 #f3f3f3;
            border: 1px solid #e4e4e4;
            margin-bottom: 15px;
            padding: 15px;
            overflow: hidden;
            height: 100%;
            line-height: 2;
            border-radius: 4px;

            .first {
                background: url("/assets/images/address.png") no-repeat scroll left bottom transparent;
                padding-bottom: 10px;
                display: flex;
                justify-content: space-between;
            }

            .ju {
                display: flex;

                & > div {
                    flex: 1;
                }
            }

            .operation {
                color: #005ea7;
                cursor: pointer;
            }
        }
    }
}
</style>
