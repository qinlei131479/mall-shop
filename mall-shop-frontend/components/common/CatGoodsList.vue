<template>
    <div class="goods-list__box" ref="productBox">
        <div class="goods-list-c">
            <div class="wrap">
                <div class="item" v-for="(item, index) in productList" :key="index">
                    <div class="g-item-warp">
                        <div class="pic">
                            <NuxtLink :to="urlFormat({ path: 'product', id: item.productId, sn: item.productSn })" target="_blank">
                                <el-image class="ease" lazy :src="imageFormat(item.picUrl)" />
                            </NuxtLink>
                        </div>
                        <div class="info">
                            <FormatPrice class="" v-model="item.price"></FormatPrice>
                            <div class="name">
                                <NuxtLink :to="urlFormat({ path: 'product', id: item.productId, sn: item.productSn })" target="_blank">{{
                                    item.productName
                                }}</NuxtLink>
                            </div>
                            <template v-if="isMerchant()">
                                <div class="shop-info">
                                    <NuxtLink class="shop-name" v-if="item.shop" :to="urlFormat({ path: 'shop', id: item.shopId })" target="_blank">{{
                                        $t(item.shop.shopTitle)
                                    }}</NuxtLink>
                                    <div class="shop-name" v-else>
                                        {{ commonStore.defaultShopName ? $t(commonStore.defaultShopName) : $t("官方自营") }}
                                    </div>
                                </div>
                            </template>
                            <!-- <div class="brief">
                                <div class="desc" v-if="item.productBrief">{{ item.productBrief }}</div>
                            </div> -->

                            <div class="promotion-box">
                                <template
                                    v-if="
                                        promotionList[item.productId] &&
                                        promotionList[item.productId].activityInfo &&
                                        promotionList[item.productId].activityInfo.length > 0
                                    "
                                >
                                    <div class="promotion-box-item" v-for="subItem in promotionList[item.productId].activityInfo" :key="subItem.productId">
                                        <div class="activity" v-if="subItem.type != 2">
                                            {{ $t(getActivityText(subItem.type)) }}
                                        </div>
                                        <div class="promotion" v-if="subItem.type == 2">
                                            <div class="ticket-type">{{ $t("券") }} <span class="ticket-line"></span></div>
                                            <div class="ticket-content">{{ subItem.data.promotionDesc }}</div>
                                        </div>
                                    </div>
                                </template>
                            </div>

                            <div class="item_cart_num">
                                <div class="shopping_num">
                                    <input v-model.number="item.number" type="text" @input="validateInput($event, item)" />
                                    <span>
                                        <a class="add" href="javascript:;" @click="increment(item)"></a>
                                        <a
                                            class="decrease"
                                            :class="item.number && item.number <= 1 ? 'disabled' : ''"
                                            href="javascript:;"
                                            @click="decrement(item)"
                                        ></a>
                                    </span>
                                </div>
                                <div class="shopping_act">
                                    <NuxtLink
                                        :to="urlFormat({ path: 'product', id: item.productId, sn: item.productSn })"
                                        v-if="item.productSku && item.productSku.length > 0"
                                    >
                                        <el-button size="small" type="primary">{{ $t(getBuyText(item.productType)) }}</el-button>
                                    </NuxtLink>
                                    <ProductBuy
                                        :isQuick="item.productType !== 1"
                                        :id="item.productId"
                                        :disabled="item.productStock == 0 || item.seckillStock == 0"
                                        :number="item.number"
                                        v-else
                                    >
                                        <el-button :disabled="item.productStock == 0 || item.seckillStock == 0" size="small" type="primary">{{
                                            $t(getBuyText(item.productType))
                                        }}</el-button>
                                    </ProductBuy>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="search-empty" v-if="productList.length === 0 && !loadingProduct">
                    <div class="bd">
                        <h4>{{ $t("好狠的筛选条件啊，把相关的商品都埋没了！") }}</h4>
                        <p>{{ $t("建议您") }}</p>
                        <p>{{ $t("1、适当减少筛选条件") }}</p>
                        <p>{{ $t("2、调整价格区间") }}</p>
                        <p>{{ $t("3、尝试其他关键字") }}</p>
                        <el-button class="graybtn mt10" @click="goBack">{{ $t("返回上一步") }}</el-button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="pagination">
        <Pagination v-if="searchStore.total > 0" v-model:page="filter.page" :size="size" :total="searchStore.total" @callback="pageChange" />
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { useCommonStore } from "@/store/common";
import { getSearchProductList } from "@/api/search/search";
import { getPromotion } from "@/api/product/product";
import { urlFormat, imageFormat } from "@/utils/format";
import { useSearchStore } from "@/store/search";
import { Pagination } from "@/components/list";
import { useRouter } from "vue-router";
import type { SearchFilter } from "@/types/search/search.d";
import type { ProductItem, PromotionList, PromotionFilterParams } from "@/types/product/product.d";
import { isMerchant } from "@/utils/util";
import getBuyText from "@/utils/getBuyText";

const commonStore = useCommonStore();

const props = defineProps({
    queryParams: {
        type: Object,
        default: {}
    },
    attrs: {
        type: Array,
        default: () => []
    }
});
const searchStore = useSearchStore();
const router = useRouter();
const size = ref(25);
const emit = defineEmits(["change", "pageChange"]);
const filter = defineModel<SearchFilter>("filter") as Ref<SearchFilter>;
const queryParams = defineModel("queryParams");
const loadingProduct = defineModel("loadingProduct");
const productList = ref<ProductItem[]>([]);
const promotionList = ref<{ [key: string]: PromotionList }>({} as { [key: string]: PromotionList });

const getSearchProduct = async () => {
    try {
        loadingProduct.value = true;
        productList.value = [];
        const result = await getSearchProductList({ ...props.queryParams, size: size.value, attrs: props.attrs });
        productList.value = result.records;
        searchStore.total = result.total ?? 0;
        productList.value.forEach((item) => {
            item.number = 1;
        });
        const productIds = result.records.map((item: any) => ({ productId: item.productId }));
        getPromotionList({ products: productIds, from: "list" });
    } catch (error) {
    } finally {
        loadingProduct.value = false;
    }
};

const getPromotionList = async (data: PromotionFilterParams) => {
    try {
        const result = await getPromotion({
            products: data.products,
            shopId: data.shopId,
            from: data.from
        });
        promotionList.value = result;
    } catch (error) {
        console.error(error);
    }
};
//1秒杀2优惠券3满减4折扣5满赠6限时折扣
type ActivityInfo = {
    [key: number]: string;
};
const activitType: ActivityInfo = {
    1: "秒杀价",
    2: "券",
    3: "满减",
    4: "折扣",
    5: "满赠",
    6: "限时折扣"
};
const getActivityText = (type: number) => {
    return activitType[type];
};

watch(
    () => queryParams.value,
    () => {
        getSearchProduct();
    },
    { deep: true, immediate: true }
);
watch(
    () => props.attrs,
    () => {
        getSearchProduct();
    },
    { deep: true, immediate: true }
);
const productBox: Ref<HTMLElement | null> = ref(null);
const pageChange = () => {
    emit("change", filter.value.page);
    emit("pageChange");
};

const validateInput = (event: any, item: ProductItem) => {
    // 使用正则表达式只允许数字输入
    const numericValue = event?.target.value.replace(/[^0-9]/g, ""); // 替换非数字字符
    // 如果输入的数字小于1，则设置为1
    item.number = numericValue ? Math.max(1, Number(numericValue)) : undefined;
};

// 加减
const increment = (item: ProductItem) => {
    if (item.number) item.number++;
};
const decrement = (item: ProductItem) => {
    if (item.number && item.number > 1) {
        item.number--;
    }
};
const goBack = () => {
    router.replace({
        path: router.currentRoute.value.path,
        query: {}
    });
};
</script>
<style lang="less" scoped>
.promotion-box {
    display: flex;
    align-items: center;
    height: 20px;
    padding-top: 5px;

    .activity {
        max-width: 100px;
        background-color: var(--general);
        color: var(--main-text);
        padding: 0 8px;
        border-radius: 2px;
        padding-bottom: 2px;
        margin-right: 10px;
    }

    .promotion {
        display: flex;
        background-color: var(--tag-bg);
        color: var(--tag-text);
        border-radius: 2px;
        padding-bottom: 2px;

        .ticket-type {
            padding: 0 6px;
            max-width: 50px;
            position: relative;

            .ticket-line {
                position: absolute;
                display: inline-block;
                height: 100%;
                opacity: 0.9;
                width: 2px;
                top: 0;
                right: 0px;
                transform: scaleY(0.7);
                background: linear-gradient(
                    to bottom,
                    var(--ump-border, #c9c9ff) 20%,
                    var(--ump-border, #c9c9ff) 20%,
                    var(--ump-border, #c9c9ff) 20%,
                    rgba(255, 255, 255, 0) 20%,
                    rgba(255, 255, 255, 0) 35%,
                    rgba(136, 76, 255, 1) 35%,
                    var(--ump-border, #c9c9ff) 35%,
                    var(--ump-border, #c9c9ff) 35%,
                    var(--ump-border, #c9c9ff) 60%,
                    rgba(255, 255, 255, 0) 60%,
                    rgba(255, 255, 255, 0) 55%,
                    rgba(255, 255, 255, 0) 75%,
                    var(--ump-border, #c9c9ff) 75%,
                    var(--ump-border, #c9c9ff) 75%,
                    var(--ump-border, #c9c9ff) 100%
                );
            }
        }
        .ticket-content {
            padding: 0 4px;
            max-width: 90px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }
}

.shop-info {
    line-height: 18px;
    height: 18px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;

    .shop-name {
        color: #999;
    }
}

.goods-list-c {
    width: 100%;
    overflow: hidden;
    .wrap {
        display: flex;
        flex-wrap: wrap;
        min-height: 400px;
        .item {
            border: 1px solid #fff;
            padding: 18px 9px 20px;
            transition: border-color 0.1s ease 0s;
            margin: 0 5px 14px 0;
            position: relative;
            width: 214px;
            background: #fff;
            .pic {
                width: 214px;
                height: 214px;
                margin-bottom: 10px;

                a {
                    height: 100%;
                    width: 100%;
                }

                .ease {
                    height: 100%;
                    width: 100%;
                }
            }
            .info {
                :deep(.price) {
                    height: 27px;
                    line-height: 27px;
                    color: var(--price);
                    .num {
                        font-size: 18px;
                        font-weight: normal;
                    }
                    .util {
                        font-size: 12px;
                        margin-bottom: -2px;
                    }
                }
                .name {
                    height: 40px;
                    line-height: 20px;
                    word-break: break-all;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    -webkit-box-orient: vertical;
                    overflow: hidden;
                    a {
                        color: #666;
                    }
                }
                .brief {
                    height: 20px;
                    line-height: 20px;
                    overflow: hidden;
                    .desc {
                        color: #ff4040;
                    }
                }
            }
        }
        .item:nth-child(5n) {
            margin-right: 0;
        }
        .item:hover {
            border-color: #e9e9e9;
            box-shadow: 0 0 2px 2px #f8f8f8;
        }
    }
}

.item_cart_num {
    display: flex;
    align-items: center;
    padding-top: 10px;
    .shopping_num {
        display: flex;
        align-items: center;
        margin-right: 8px;
        input {
            background: url("/assets/images/category/search.png") no-repeat scroll 0px -310px;
            background-position: 0 -310px;
            border: 0 none;
            color: #555;
            padding: 0;
            height: 28px;
            line-height: 28px;
            text-align: center;
            width: 30px;
        }
        span {
            height: 28px;
            margin-left: 2px;
            width: 16px;
        }
        a {
            background: url("/assets/images/category/search.png") no-repeat scroll;
            cursor: pointer;
            display: block;
            height: 14px;
            line-height: 14px;
            overflow: hidden;
            text-indent: -99em;
            width: 16px;
        }
        .add {
            background-position: -17px -281px;
        }
        .decrease {
            background-position: -17px -295px;
        }
        .decrease.disabled {
            background-position: 0 -295px;
        }
    }
    .shopping_act {
        height: 28px;
        overflow: hidden;
        display: flex;
        align-items: center;
    }
}
.search-empty {
    margin-bottom: 10px;
    position: relative;
    display: flex;
    justify-content: center;
    width: 100%;
}

.search-empty .bd {
    background: url("/assets/images/common/empty.gif") no-repeat scroll 56px 45px;
    padding: 45px 50px 45px 200px;
}

.search-empty h4 {
    font-size: 14px;
    padding-bottom: 10px;
}

.search-empty p {
    color: #666666;
    margin-bottom: 5px;
}
.pagination {
    display: flex;
    justify-content: end;
    margin-top: 10px;
    margin-bottom: 20px;
}
</style>
