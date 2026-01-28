<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="店铺街"></CommonHeader>
    <div class="container">
        <template v-if="!loading && total > 0">
            <div class="shop-list-box">
                <div class="shop-list">
                    <template v-for="item in filterState" :key="item.id">
                        <div class="shop-item" @click="handleShop(item)">
                            <div class="shop-info">
                                <div class="shop-title">
                                    <div class="shop-title-box">
                                        {{ item.shopTitle }}
                                    </div>
                                </div>
                                <div class="shop-desc">
                                    <div class="shop-desc-item">
                                        <span>{{$t('上架商品')}}</span>
                                        <span class="num">{{ item.listingCount }}</span>
                                    </div>
                                    <div class="shop-desc-item">
                                        <span>{{$t('店铺收藏数')}}</span>
                                        <span class="num">{{ item.collectCount }}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="shop-product">
                                <div class="product-list">
                                    <template v-for="subItem in item.listingProduct" :key="subItem.productId">
                                        <div class="product-item">
                                            <div class="product-img">
                                                <NuxtLink
                                                    @click.stop=""
                                                    :to="urlFormat({ path: 'product', id: subItem.productId, sn: subItem.productSn })"
                                                    target="_blank"
                                                >
                                                    <el-image :src="imageFormat(subItem.picThumb)" lazy />
                                                </NuxtLink>
                                            </div>
                                            <div class="product-price">
                                                <FormatPrice v-model="subItem.productPrice" class="pro_price"></FormatPrice>
                                            </div>
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <div class="el-page" v-if="filterState.length > 0 && !loading">
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
            </div>
        </template>
        <template v-if="!loading && total === 0">
            <el-empty :description="$t('暂无店铺数据')" />
        </template>
    </div>
    <CommonPageFooter :type="2" bg="#fff"></CommonPageFooter>
</template>
<script lang="ts" setup>
import { reactive, ref } from "vue";
import { Pagination } from "~/components/list";
import { imageFormat, urlFormat } from "@/utils/format";
import { getShopList } from "~/api/shop/shop";
import type { shopListItem } from "~/types/shop/shop.d";

const router = useRouter();

const filterState = ref<shopListItem[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive({
    //初使化用于查询的参数
    page: 1,
    size: 10
});
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
loadFilter();

const handleShop = (data: shopListItem) => {
    router.push({ path: urlFormat({ path: "shop", id: data.shopId }) });
};
</script>

<style scoped lang="less">
.shop-list-box {
    position: relative;
    width: 100%;
    padding-bottom: 10px;
    padding-top: 30px;
    .shop-list {
        width: 100%;
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        grid-gap: 15px;
        .shop-item {
            padding: 12px 12px;
            box-sizing: border-box;
            border-radius: 6px;
            cursor: pointer;
            overflow: hidden;
            background: #fff;
            transition: all 0.5s;
            &:hover {
                box-shadow: 0 0 10px rgba(100, 100, 100, 0.3);
            }

            .shop-info {
                .shop-title-box {
                    font-weight: bold;
                    max-width: 392px;
                    font-size: 20px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    color: #333;
                }

                .shop-desc {
                    display: flex;
                    margin-top: 10px;
                    color: #999;
                    .shop-desc-item {
                        padding-right: 5px;
                    }

                    .num {
                        padding: 0 3px;
                    }
                }
            }

            .shop-product {
                margin-top: 2dvh;

                .product-list {
                    display: flex;
                    .product-item {
                        cursor: pointer;
                        margin-left: 16px;

                        &:first-child {
                            margin-left: 0;
                        }

                        .product-img {
                            width: 94px;
                            height: 94px;
                            border-radius: 2px;
                            border: 1px solid #e5e5e5;
                            &:hover {
                                opacity: 0.8;
                            }
                        }

                        .product-price {
                            width: 100%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding-top: 10px;
                            .pro_price {
                                height: 25px;
                                align-items: center;
                                justify-content: center;
                                display: flex;
                                line-height: 25px;
                                color: var(--general);
                                font-weight: bold;

                                :deep(.num) {
                                    font-weight: bold;
                                    font-size: 16px;
                                    font-family: arial, sans-serif;
                                }

                                :deep(.util) {
                                    margin-bottom: -5px;
                                }

                                :deep(.decimals) {
                                    font-size: 12px;
                                    font-weight: bold;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

.el-page {
    display: flex;
    justify-content: flex-end;
    padding: 20px 0;
}
</style>
