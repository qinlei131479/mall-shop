<template>
    <div class="form_item">
        <div class="top_row flex justify-between">
            <div class="tit">{{ $t("确认收货人信息") }}</div>
            <div class="add_btn font-color">
                <MemberAddressDialogEditRegion
                    ref="addressRef"
                    @okCallback="onChangeAddress"
                    :title="$t('添加收货地址')"
                    width="600px"
                    path="MemberEditRegion"
                    :params="{ act: 'add', }"
                    :showFooter="false"
                >
                    <i>+</i> <span>{{ $t("使用新地址") }}</span>
                </MemberAddressDialogEditRegion>
            </div>
        </div>
        <div class="address_info" :class="{ more: showMoreAddress }" ref="addressScrollRef">
            <div
                v-if="addressList.length > 0"
                class="address_item flex"
                v-for="(item, index) in addressList"
                :key="index"
                :class="{ active: activeAddressId == item.addressId }"
            >
                <div style="width: auto" class="btn" @click="onChangeAddress(item)">
                    <span>{{ item.consignee }}</span>
                    <i class="icon-gou"></i>
                </div>
                <div class="item flex justify-between">
                    <div class="info">
                        <span>{{ item.regionName }}</span>
                        <span>&nbsp;{{ item.address }}</span>
                        <span v-if="item.mobile">（{{ item.mobile }}）</span>
                    </div>
                    <div class="set flex">
                        <MemberAddressDialogEditRegion
                            @okCallback="onChangeAddress"
                            :title="$t('编辑收货地址')"
                            width="600px"
                            path="MemberEditRegion"
                            :params="{ act: 'detail', addressId: item.addressId }"
                            :showFooter="false"
                        >
                            <span class="set-text">{{ $t("编辑地址") }}</span>
                        </MemberAddressDialogEditRegion>
                        <DeleteRecord :params="{ id: item.addressId }" :requestApi="delAddress" v-if="index !== 0" @afterDelete="$emit('addrssRefresh')">
                            <span class="set-text editAddress font-color">{{ $t("删除") }}</span>
                        </DeleteRecord>
                    </div>
                </div>
            </div>
        </div>
        <template v-if="addressList.length > 1">
            <div class="address-more" :class="{ more: showMoreAddress }" @click="changeShowMoreAddress">
                {{ showMoreAddress ? $t("收起地址") : $t("更多地址") }} <span class="iconfont-pc icon-zhankai"></span>
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
import { delAddress } from "~/api/user/address";
import type { AddressFilterState } from "~/types/user/address.d";
import DeleteRecord from "~/components/member/DeleteRecord.vue";

const props = defineProps({
    activeAddressId: {
        type: Number
    },
    addressList: {
        type: Array as PropType<AddressFilterState[]>,
        default: () => []
    }
});

const showMoreAddress = defineModel({ type: Boolean, default: false });

const emit = defineEmits(["okCallback", "addrssRefresh"]);

const addressRef = useTemplateRef("addressRef");

const addressScrollRef = ref<null | Element>(null);

const changeShowMoreAddress = () => {
    showMoreAddress.value = !showMoreAddress.value;
};

watch(
    () => showMoreAddress.value,
    (value) => {
        addressScrollRef.value?.scrollTo(0, 0);
    }
);

const show = () => {
    addressRef.value?.show();
};

const onChangeAddress = (item: AddressFilterState) => {
    emit("okCallback", item);
};

defineExpose({
    show
});
</script>

<style lang="less" scoped></style>
