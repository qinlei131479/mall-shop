<template>
    <div class="product_info">
        <div class="basic bg" v-if="productType == 'exchange'">
            <div class="item exchange">
                <div class="tit">{{ $t(`${commonStore.integralName}兑换`) }}</div>
                <div class="price1">
                    <FormatPrice v-model="exchangeDetail.discountsPrice"></FormatPrice>
                    <span class="red">+</span>
                    &nbsp;&nbsp;
                    <span class="exchange-num red">{{ exchangeDetail.exchangeIntegral }}</span>
                    <span class="red">{{ $t(commonStore.integralName) }}</span>
                </div>
            </div>
        </div>
        <template v-else>
            <template v-if="isSeckill == 1">
                <div class="active-box flex justify-between">
                    <div class="active-ico flex align-center">
                        <i class="iconfont-pc icon-miaosha"></i>
                        <span>{{ $t("秒杀") }}</span>
                    </div>
                    <div class="active-time">
                        <ClientOnly>
                            <Countdown :endTime="seckillValue.seckillEndTime"></Countdown>
                        </ClientOnly>
                    </div>
                </div>
            </template>
            <template v-if="isDiscount == 1">
                <div class="active-box flex justify-between">
                    <div class="active-ico flex align-center">
                        <i class="iconfont-pc icon-miaosha"></i>
                        <span>{{ $t("限时折扣") }}</span>
                    </div>
                    <div class="active-time">
                        <ClientOnly>
                            <Countdown :endTime="discountValue.discountEndTime"></Countdown>
                        </ClientOnly>
                    </div>
                </div>
            </template>
        </template>
        <div class="basic bg" v-if="productType != 'exchange'">
            <div class="item" v-if="isSeckill != 1 && productPrice !== '' && commonStore.showMarketprice">
                <template v-if="product.marketPrice && Number(product.marketPrice) > 0">
                    <div class="tit">{{ $t("市场价") }}：</div>
                    <div class="market_price">
                        <FormatPrice v-model="product.marketPrice"></FormatPrice>
                    </div>
                </template>
            </div>
            <div class="item">
                <div class="tit">{{ isSeckill == 1 ? $t("秒杀价") : $t("本站价") }}：</div>
                <div class="price1" v-if="productPrice !== ''">
                    <FormatPrice v-model="productPrice"></FormatPrice>
                </div>
                <div class="tit2" v-if="rankDetail.total > 0">{{ $t("评价") }}：</div>
                <div class="star" v-if="rankDetail.total > 0">
                    <span class="icon-star" :class="`icon-star-${rankDetail.averageRank}`"></span>
                </div>
                <div class="evaluate" v-if="rankDetail.total > 0">
                    {{ $t("已有") }} <span class="font-color"> {{ rankDetail.total }}</span> {{ $t("人评价") }}，{{ rankDetail.goodPercent }}% {{ $t("好评") }}
                </div>
            </div>
            <div class="item">
                <ProductCouponList :promotionList="promotionList" :productId="product.productId">
                    <div class="tit">{{ $t("优惠活动") }}：</div></ProductCouponList
                >
            </div>
        </div>
        <div class="basic">
            <div class="item" v-if="commonStore.showSelledCount">
                <div class="tit">{{ $t("销量") }}：</div>
                <div class="num">{{ product.virtualSales }} {{ $t("件") }}</div>
            </div>
            <template v-if="serviceList.length > 0">
                <div class="item align-start">
                    <div class="tit">{{ $t("服务") }}：</div>
                    <div class="tips_i_box">
                        <template v-for="(item, index) in serviceList" :key="index">
                            <!-- <template v-if="item.icoImg"> -->
                            <span>
                                <template v-if="item.icoImg"> <img :src="imageFormat(item.icoImg)" width="15" height="15" />&nbsp; </template>
                                {{ item.productServiceName }}
                            </span>
                            <!-- </template> -->
                        </template>
                    </div>
                </div>
            </template>
        </div>

        <template v-if="isB2B() && skuList.length > 1 && commonStore.bulkPurchase === 1 && product.productType !== 4">
            <ProductB2bTab v-model:tabIndex="tabIndex"></ProductB2bTab>
        </template>

        <div class="actions" :class="{ 'no-border': isB2B() && skuList.length > 1 && commonStore.bulkPurchase === 1 }">
            <template v-if="tabIndex !== 1">
                <template v-if="skuList.length > 0">
                    <ProductSku v-model="attrList" :skuList="skuList" :checkedValue="checkedValue" :productType="productType" @change="onProductSkuChange">
                    </ProductSku>
                </template>
                <template v-if="!(product.productType === 4 && product.isBuy === 1)">
                    <div class="number-box">
                        <div class="title">{{ $t("数量") }}：</div>
                        <div class="con">
                            <InputNumber
                                v-model="productNumber"
                                :max="maxProductNumber > 0 ? maxProductNumber : productStock > 0 ? productStock : 1"
                            ></InputNumber>
                            <div class="flex" style="margin-left: 10px">
                                {{ $t("件") }}
                                <div style="color: #999">
                                    （<ProductStockShow v-model="productStock" :isSeckill="isSeckill" :maxProductNumber="maxProductNumber"> </ProductStockShow
                                    >）
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </template>
            <template v-if="isBatch">
                <ProductB2bSku
                    v-model:sku-item="skuItem"
                    v-model:select-skus="selectSkus"
                    v-model:product-amount="productAmount"
                    :id="product.productId"
                    :b2bSkuList="b2bSkuList"
                ></ProductB2bSku>
            </template>
            <ProductAttr v-model="attrList" @change="handleAttrChange"> </ProductAttr>
            <template v-if="isBatch && productAmount && productAmount.count > 0">
                <ProductAmount :select-skus="selectSkus" :product-amount="productAmount"></ProductAmount>
            </template>

            <template v-if="!(product.productType === 4 && product.isBuy === 1)">
                <div class="add_cart">
                    <template v-if="isBatch">
                        <ProductBuy :extraAttrIds="attrIds" :id="product.productId" :skuItem="skuItem" :disabled="skuItem.length === 0" :isQuick="true">
                            <el-button :disabled="skuItem.length === 0" class="buy-btn buynow" type="primary">{{ $t("立即购买") }}</el-button>
                        </ProductBuy>
                        <ProductBuy :extraAttrIds="attrIds" :id="product.productId" :disabled="skuItem.length === 0" :skuItem="skuItem">
                            <el-button :disabled="skuItem.length === 0" class="buy-btn buynow">{{ $t("批量加入购物车") }}</el-button>
                        </ProductBuy>
                        <template v-if="commonStore.isEnquiry === 1">
                            <el-button class="buy-btn buynow" @click="showEnquiry = !showEnquiry">{{ $t("立即询价") }}</el-button>
                        </template>
                    </template>
                    <template v-else>
                        <template v-if="productType == 'product'">
                            <ProductBuy :extraAttrIds="attrIds" :id="product.productId" :skuId="skuId" :disabled="productStock == 0" :number="productNumber">
                                <el-button :disabled="productStock == 0" class="buy-btn" v-if="isSeckill == 1 && productStock == 0">{{
                                    $t("已抢完")
                                }}</el-button>
                                <el-button :disabled="productStock == 0" class="buy-btn" v-else-if="product.productType === 1">{{
                                    $t("加入购物车")
                                }}</el-button>
                            </ProductBuy>

                            <template v-if="productStock == 0">
                                <el-button class="buy-btn buynow" type="primary" @click="$router.push('/search')">{{ $t("查看其他商品") }}</el-button>
                            </template>
                            <template v-else>
                                <ProductBuy
                                    :extraAttrIds="attrIds"
                                    :id="product.productId"
                                    :skuId="skuId"
                                    :disabled="productStock == 0"
                                    :number="productNumber"
                                    :isQuick="true"
                                >
                                    <el-button :disabled="productStock == 0" class="buy-btn buynow" type="primary">{{ $t("立即购买") }}</el-button>
                                </ProductBuy>
                            </template>
                        </template>
                        <template v-if="productType == 'exchange'">
                            <ProductBuy
                                :extraAttrIds="attrIds"
                                :id="exchangeDetail.id"
                                :skuId="skuId"
                                :disabled="productStock == 0"
                                :number="productNumber"
                                :productType="productType"
                                :isQuick="true"
                            >
                                <el-button class="buy-btn buynow" type="primary" :disabled="productStock == 0">{{ $t("立即兑换") }}</el-button>
                            </ProductBuy>
                        </template>
                    </template>
                </div>
            </template>
        </div>
        <ProductEnquiry v-model="showEnquiry" :productId="product.productId"></ProductEnquiry>
    </div>
</template>
<script lang="ts" setup>
import { reactive, ref, onMounted } from "vue";
import { InputNumber, Countdown } from "@/components/product";
import { SelectRegion } from "@/components/region";
import { getProductSkuDetail } from "~/api/product/product";
import { imageFormat } from "@/utils/format";
import type { ProductItem, AttrList, SkuList, ServiceList, SkuPromotion, ExchangeDetail, ProductAmountItem } from "~/types/product/product.d";
import { useCommonStore } from "~/store/common";

const props = defineProps({
    product: {
        type: Object as PropType<ProductItem>,
        default: {}
    },
    attrList: {
        type: Object as PropType<AttrList>,
        default: {}
    },
    exchangeDetail: {
        type: Object as PropType<ExchangeDetail>,
        default: {}
    },
    skuList: {
        type: Array as PropType<SkuList[]>,
        default: []
    },
    b2bSkuList: {
        type: Array as PropType<SkuList[]>,
        default: []
    },
    checkedValue: {
        type: Array as PropType<string[]>,
        default: []
    },
    serviceList: {
        type: Array as PropType<ServiceList[]>,
        default: []
    },
    rankDetail: {
        type: Object,
        default: {}
    },
    productPrice: {
        type: String,
        default: ""
    },
    productNumber: {
        type: Number,
        default: 1
    },
    productStock: {
        type: Number,
        default: 0
    },
    skuId: {
        type: Number,
        default: 0
    },
    id: {
        type: Number,
        default: 0
    },
    productType: {
        type: String,
        default: "product"
    }
});
const attrList = ref<object>(props.attrList);
const exchangeDetail = ref<ExchangeDetail>(props.exchangeDetail);
const productPrice = ref<any>(props.productPrice);
const productStock = ref<number>(props.productStock);
const isSeckill = ref<number>(0);
const maxProductNumber = ref(0);
const isDiscount = ref(0);
const productNumber = ref<number>(props.productNumber);
const skuId = ref<number>(props.skuId);
const regionIds = ref<any[]>([]);
const promotionList = ref<SkuPromotion[]>([]);
const seckillValue = ref({
    seckillEndTime: "",
    seckillSales: 0,
    seckillStock: 0
});
const discountValue = ref({
    discountEndTime: "",
    discountPrice: 0
});
const productAmount = ref<ProductAmountItem>({
    count: 0,
    total: "0"
});
const selectSkus = ref<any[]>([]);
const skuItem = ref<any[]>([]);
const showEnquiry = ref(false);
const commonStore = useCommonStore();

const productRegionIds = useCookie<any[]>("productRegionIds");
if (productRegionIds.value) {
    regionIds.value = productRegionIds.value;
}

const onProductSkuChange = (item: any) => {
    if (item !== null) {
        skuId.value = item.skuId;
        if (props.productType == "product") {
            productStock.value = item.skuStock;
        }
    }

    if (props.productType == "product") {
        loadPrice();
    }
};

const attrIds = ref("");
const handleAttrChange = (value: string) => {
    attrIds.value = value;
    loadPrice();
};

const loadPrice = async () => {
    try {
        const result = await getProductSkuDetail(props.id, {
            skuId: skuId.value ? skuId.value : null,
            extraAttrIds: attrIds.value
        });
        productPrice.value = result.price;
        productStock.value = result.stock;

        // 是否存在营销活动
        if (result.promotion.length > 0) {
            promotionManage(result.promotion);
        }

        if (isSeckill.value && maxProductNumber.value > 0) {
            productNumber.value = productNumber.value > maxProductNumber.value ? maxProductNumber.value : productNumber.value;
        } else if (productStock.value === 0) {
            productNumber.value = 1;
        } else {
            productNumber.value = productNumber.value > productStock.value ? productStock.value : productNumber.value;
        }
    } catch (error) {
    } finally {
    }
};

if (props.skuList.length === 0) {
    loadPrice();
}

// 营销活动管理
const promotionManage = (promotion: SkuPromotion[]) => {
    // 排除秒杀 & 限时折扣
    promotionList.value = promotion.filter((item: any) => item.type !== 1 && item.type !== 6);
    console.log("promotionManage", promotionList.value);
    const seckillData = promotion.find((item: any) => item.type === 1);
    const discountData = promotion.find((item: any) => item.type === 6);

    // 判断限时折扣存在
    if (discountData) {
        isDiscount.value = 1;
        discountValue.value.discountEndTime = discountData.data.item.endTime!;
        discountValue.value.discountPrice = discountData.data.item.discountPrice!;
    } else {
        isDiscount.value = 0;
        discountValue.value = {
            discountEndTime: "",
            discountPrice: 0
        };
    }

    // 判断秒杀存在
    if (seckillData) {
        isSeckill.value = 1;
        seckillValue.value.seckillEndTime = seckillData.data.seckillEndTime!;
        seckillValue.value.seckillSales = seckillData.data.item.seckillSales!;
        seckillValue.value.seckillStock = seckillData.data.item.seckillStock!;
        maxProductNumber.value = seckillData.data.seckillLimitNum!;
        productStock.value = seckillData.data.item.seckillStock;
    } else {
        isSeckill.value = 0;
        seckillValue.value = {
            seckillEndTime: "",
            seckillSales: 0,
            seckillStock: 0
        };
    }
};

// b2b
const tabIndex = ref(0);
const isBatch = computed(() => {
    return isB2B() && tabIndex.value === 1 && props.product.productType !== 4;
});
</script>
<style lang="less" scoped>
.product_info {
    .bg {
        background: url("/assets/images/product/price-bg.png") repeat-x scroll 0 bottom #f3f3f3;
        padding-bottom: 12px;
        padding-top: 10px;
    }

    .active-box {
        background: url("/assets/images/product/pro-time-bg.png") no-repeat;
        background-size: 100% 100%;
        padding: 13px 0;
        border-radius: 6px 6px 0 0;
        font-size: 16px;
        .active-ico {
            color: #fff;
            margin-left: 10px;
            font-size: 16px;
            i {
                margin-right: 5px;
                font-size: 18px;
            }
        }
        .active-time {
            margin-right: 10px;
            color: #fff;
        }
    }

    .basic {
        .item {
            display: flex;
            align-items: center;
            padding: 5px 10px;

            .tit {
                color: #999;
                min-width: 60px;
            }

            .tit2 {
                color: #999;
            }

            .market_price {
                color: #888;
                text-decoration: line-through;
            }

            .price1 {
                :deep(.price) {
                    color: var(--general);
                    font-size: 26px;
                    margin-right: 20px;

                    .util {
                        font-size: 12px;
                        line-height: 26px;
                    }
                    .num {
                        line-height: 18px;
                    }
                }
            }

            .evaluate {
                margin-left: 10px;
                color: #999;
            }

            .num {
                color: #999;
            }

            .tips_i_box {
                display: flex;
                flex-wrap: wrap;
                width: 95%;

                span {
                    display: flex;
                    align-items: center;
                    margin-right: 15px;
                    color: #999;
                    margin-bottom: 10px;
                }
            }
        }
        .exchange {
            display: flex;
            align-items: center;
            .tit {
                padding: 2px 5px;
                background: var(--main-bg);
                font-size: 14px;
                color: #fff;
                cursor: pointer;
                text-align: center;
                margin-right: 10px;
            }
            .price1 {
                margin-top: 5px;
                :deep(.price) {
                    color: var(--general);
                    font-size: 26px;
                    margin-right: 15px;

                    .util {
                        font-size: 12px;
                        line-height: 26px;
                    }
                    .num {
                        line-height: 18px;
                    }
                }
                .exchange-num {
                    font-size: 18px;
                    margin-right: 5px;
                }
            }
        }
    }

    .actions {
        border-top: 1px dotted #dddddd;
        overflow: hidden;
        padding: 15px 1px 0 10px;
        &.no-border {
            border-top: none;
        }

        .number-box {
            display: flex;
            margin-bottom: 25px;

            .title {
                color: #999;
                height: 33px;
                line-height: 32px;
                padding-right: 10px;
            }

            .con {
                display: flex;
                align-items: center;
            }
        }

        .add_cart {
            display: flex;
            margin-left: 45px;
            padding-bottom: 25px;

            .buy-btn {
                height: 40px;
                padding: 0 20px;
                border-radius: 2px;
                &:hover {
                    opacity: 0.9;
                }
                margin-right: 10px;
            }

            .disabled .btn {
                color: #ccc;
                background-color: #f7f7f7;
                border: 1px solid #ebebeb;
                cursor: not-allowed;

                &:hover {
                    opacity: 0.9;
                }
            }
            .buynow {
                height: 40px;
                padding: 0 20px;
            }
        }

        .link-code {
            display: flex;
            margin-left: 60px;
            margin-bottom: 30px;

            .link-code-title {
                display: flex;
                align-items: center;

                .link-code-phone {
                    width: 20px;
                    height: 20px;
                    background: url("/assets/images/product/phone_color.png") no-repeat center 0;
                    background-size: 100% 100%;
                }

                .link-code-font {
                    font-size: 12px;
                    color: var(--general);
                }
            }

            .link-code-main {
                display: none;
                cursor: pointer;
                width: 188px;
                height: 176px;
                margin-left: 140px;
                z-index: 9;
                float: left;
                position: absolute;
                background: url("/assets/images/product/code-border.png") no-repeat center 0;
                background-size: cover;

                .link-code-qrimg {
                    margin-top: 13px;
                    margin-left: 25px;
                    width: 150px;
                }
            }
        }

        .link-code:hover .link-code-main {
            display: block;
        }
    }
}
</style>
