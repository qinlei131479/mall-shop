<template>
    <div style="background: #fff" v-if="!loading">
        <CommonPageHeader
            ref="pageHeaderRef"
            :immediateLog="false"
            :showService="true"
            :shopId="shopInfo && shopInfo.shopId ? shopInfo.shopId : 0"
            :phone="shopInfo && shopInfo.kefuPhone ? shopInfo.kefuPhone : ''"
            :productId="productId ?? 0"
        >
        </CommonPageHeader>
        <el-dialog v-model="visible" :title="$t('分享给小伙伴')" :footer="null" top="15vh" width="800px">
            <div>
                <ProductShareModal :picList="picList" :product="product" :productPrice="productPrice"> </ProductShareModal>
            </div>
        </el-dialog>
        <div class="container">
            <div>
                <CommonCategoryNavigation v-model:id="product.categoryId" :productName="product.productName"> </CommonCategoryNavigation>
                <div class="product_details flex justify-between">
                    <div class="extra_details">
                        <div class="thumb">
                            <ProductThumb :videoList="videoList" :picList="picList"></ProductThumb>
                        </div>
                        <div class="share_wish flex" v-if="productType == 'product' || productType == 'exchange'">
                            <span class="product-number">{{ $t("商品编号") }}：{{ product.productSn }}</span>
                            <!--                            <a class="shareGold flex hand-pointer" @click="showModal"><i></i>分享</a>-->
                            <span
                                class="iconfont-pc collect ico-color"
                                :class="{ 'icon-shoucang': !isCollect, 'icon-yishoucang': isCollect }"
                                @click="addCollect(isCollect)"
                            >
                                <i>{{ $t("收藏") }}</i>
                            </span>
                        </div>
                    </div>
                    <div class="details">
                        <div class="product_name">
                            <div class="name">
                                {{ product.productName }}
                            </div>
                            <p v-if="product.productBrief" class="desc">
                                {{ product.productBrief }}
                            </p>
                        </div>
                        <ProductInfo
                            :product="product"
                            :serviceList="serviceList"
                            :productType="productType"
                            :id="productId"
                            :productPrice="productPrice"
                            :attrList="attrList"
                            :skuList="skuList"
                            :b2bSkuList="b2bSkuList"
                            :productStock="productStock"
                            :productNumber="productNumber"
                            :rankDetail="rankDetail"
                            :descArr="descArr"
                            :checkedValue="checkedValue"
                            :exchangeDetail="exchangeDetail"
                        ></ProductInfo>
                    </div>
                </div>
                <div class="detail_main flex">
                    <template v-if="productType == 'product' || productType == 'exchange'">
                        <div class="lyecs_side">
                            <template v-if="shopInfo && isMerchant()">
                                <div class="shop-box">
                                    <div class="tit">
                                        <NuxtLink :to="urlFormat({ path: 'shop', id: shopInfo.shopId })">{{ shopInfo.shopTitle }}</NuxtLink>
                                    </div>
                                    <div class="btn-box flex justify-between">
                                        <div class="btn">
                                            <span class="iconfont-pc icon-guanli"></span>
                                            <NuxtLink :to="urlFormat({ path: 'shop', id: shopInfo.shopId })">{{ $t("进店逛逛") }}</NuxtLink>
                                        </div>
                                        <div class="btn" @click="addShopCollect(shopInfo.shopId, shopInfo.collectShop)">
                                            <span
                                                class="iconfont-pc collect ico-color"
                                                :class="{ 'icon-shoucang': !shopInfo.collectShop, 'icon-yishoucang': shopInfo.collectShop }"
                                            >
                                            </span>
                                            <span>{{ $t("收藏店铺") }}</span>
                                        </div>
                                    </div>
                                </div>
                            </template>

                            <template v-if="!loading">
                                <ProductCompetitorsList :productId="productId"></ProductCompetitorsList>
                            </template>
                        </div>
                    </template>

                    <div class="lyecs_main">
                        <div class="goods_tab_wrap sticky-element">
                            <div class="tabs">
                                <ul class="flex">
                                    <li @click="tabChange(1)">
                                        <a href="javascript:;" tab-data="info" class="tab_a" :class="tabIndex == 1 ? 'current' : ''"
                                            ><span>{{ $t("产品详情") }}</span></a
                                        >
                                    </li>

                                    <li @click="tabChange(2)">
                                        <a href="javascript:;" tab-data="com" class="tab_a" :class="tabIndex == 2 ? 'current' : ''"
                                            ><span
                                                >{{ $t("用户评价") }}<em>({{ rankDetail.total }})</em></span
                                            ></a
                                        >
                                    </li>
                                    <li @click="tabChange(3)">
                                        <a href="javascript:;" tab-data="ask" class="tab_a" :class="tabIndex == 3 ? 'current' : ''"
                                            ><span
                                                >{{ $t("商品咨询") }}<em>({{ consultationTotal }})</em></span
                                            ></a
                                        >
                                    </li>
                                    <li @click="tabChange(4)">
                                        <a href="javascript:;" tab-data="ser" class="tab_a" :class="tabIndex == 4 ? 'current' : ''"
                                            ><span>{{ $t("售后服务") }}</span></a
                                        >
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="goods_desc paidcontent" v-if="tabIndex == 1" id="xiangqing">
                            <template v-if="isPro() && product.productType === 4">
                                <div class="goods_description">
                                    <template v-if="product.isBuy && product.paidContent">
                                        <template v-for="item in product.paidContent" :key="item">
                                            <img class="el-img" v-if="item.type == 'pic'" fit="cover" :src="imageFormat(item.pic)" />
                                            <ClientOnly>
                                                <div class="desc-text-item" v-if="item.type == 'text'" v-html="formatRichText(item.html)"></div>
                                            </ClientOnly>
                                        </template>
                                    </template>
                                    <template v-else>
                                        <img fit="cover" class="img" src="~/assets/images/product/paidcontent-bg.png" />
                                    </template>
                                </div>
                            </template>
                            <template v-if="attrList.normal && attrList.normal.length > 0">
                                <ul class="shuxin flex">
                                    <template v-for="(item, index) in attrList.normal" :key="index">
                                        <li>
                                            {{ item.attrName }}：
                                            <template v-for="(attr, arrtIndex) in item.attrList" :key="arrtIndex"> {{ attr.attrValue }} &nbsp; </template>
                                        </li>
                                    </template>
                                </ul>
                            </template>

                            <div class="goods_description">
                                <template v-for="item in descArr">
                                    <img class="el-img" v-if="item.type == 'pic'" fit="cover" :src="imageFormat(item.pic)" />
                                    <ClientOnly>
                                        <div class="desc-text-item" v-if="item.type == 'text'" v-html="formatRichText(item.html)"></div>
                                    </ClientOnly>
                                </template>
                            </div>
                        </div>

                        <div class="goods_desc shouhou" v-show="tabIndex == 4" id="shouhou">
                            <div class="tab_title">
                                <h2><span class="icon"></span>{{ $t("售后服务") }}</h2>
                            </div>
                            <ClientOnly>
                                <ProductAfterSaleService></ProductAfterSaleService>
                            </ClientOnly>
                        </div>

                        <div class="goods_desc" v-if="tabIndex == 2" id="pingjia">
                            <div class="tab_title">
                                <h2><span class="icon"></span>{{ $t("用户评价") }}</h2>
                            </div>
                            <ProductComment v-model="product.productId"></ProductComment>
                        </div>
                        <div class="goods_desc goods_ask" v-if="tabIndex == 3" id="zixun">
                            <div class="tab_title">
                                <h2><span class="icon"></span>{{ $t("商品咨询") }}</h2>
                            </div>
                            <ProductConsultation :product="product" :productStock="productStock" :productNumber="productNumber"> </ProductConsultation>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <CommonPageFooter></CommonPageFooter>
        <ClientOnly>
            <ElevatorProduct></ElevatorProduct>
        </ClientOnly>
    </div>
</template>
<script lang="ts" setup>
import { ref, nextTick, computed } from "vue";
import { getProductDetail, getCollectProduct, getShopDetail, getShopCollect, getExchangeDetail, getBatchProductAvailability } from "~/api/product/product";
import type { ProductItem, AttrList, PicList, RankDetail, SkuList, DescArr, ServiceList, ExchangeDetail, VideoList } from "~/types/product/product.d";
import { updateCollectProduct } from "~/api/user/collectProduct";
import { delCollectProduct } from "~/api/user/collectProduct";
import ElevatorProduct from "~/components/product/ElevatorProduct.vue";
import { useCommonStore } from "@/store/common";
import { useUserStore } from "@/store/user";
import { formatRichText } from "@/utils/util";
import { message } from "ant-design-vue";

const { t } = useI18n();
const commonStore = useCommonStore();
const title = ref(t("商品详情"));
const titleStr = computed(() => {
    let str = title.value;
    if (commonStore.poweredByStatus != 1) {
        str += "-powered by tigshop";
    }
    return str;
});
const description = ref("");
const keywords = ref("");
const pageHeaderRef = ref();

useHead({
    title: titleStr,
    meta: [
        { name: "description", content: description },
        { name: "keywords", content: keywords }
    ]
});

const props = defineProps({
    productType: {
        type: String,
        default: "product"
    }
});
const route = useRoute();
const productId = ref<number>(0);
const consultationTotal = ref<number>(0);
const product = ref<ProductItem>({
    productId: 0,
    productStock: 0,
    productName: "",
    description: "",
    keywords: ""
});
const productStock = ref<number>(0);
const productNumber = ref(1);
const attrList = ref<AttrList>({
    normal: []
});
const skuList = ref<SkuList[]>([]);
const b2bSkuList = ref<SkuList[]>([]);
const picList = ref<PicList[]>([]);
const serviceList = ref<ServiceList[]>([]);
const rankDetail = ref<RankDetail>({});
const productPrice = ref<string>("");
const loading = ref(true);
const descArr = ref<DescArr[]>([]);
const checkedValue = ref<string[]>([]);
const exchangeDetail = ref<ExchangeDetail>({});
const videoList = ref<VideoList[]>([]);

const getProduct = async () => {
    try {
        let result: any = {};
        if (props.productType == "exchange") {
            result = await getExchangeDetail(route.params.id);
            exchangeDetail.value = result.exchangeInfo;
            productStock.value = result.exchangeInfo.productStock;
        } else {
            result = await getProductDetail(route.params.id, route.query.skuId);
            productStock.value = result.productStock;
        }
        title.value = result.productName;
        keywords.value = result.keywords;
        description.value = result.description;
        productId.value = result.item.productId;
        product.value = result.item;
        attrList.value = result.attrList;
        picList.value = result.picList;
        rankDetail.value = result.rankDetail ?? {};
        skuList.value = result.skuList;
        checkedValue.value = result.checkedValue;
        descArr.value = result.descArr;
        serviceList.value = result.serviceList;
        consultationTotal.value = result.consultationTotal;
        videoList.value = result.videoList;
        loading.value = false;

        if (isB2B() && result.skuList.length > 1 && commonStore.bulkPurchase === 1) {
            getBatchProductAvailabilityList();
        }
        if (result.item.shopId) {
            await getShop(result.item.shopId);
        }
        if (props.productType == "product") {
            const userStore = useUserStore();

            if(!userStore.isAuthenticated) {
                return;
            }

            getCollect();
        }

        if (import.meta.client) {
            nextTick(() => {
                pageHeaderRef.value?.handleLog();
            });
        }
    } catch (error: any) {
        console.error(error);
        // message.error(error.message);
        navigateTo("/404");
    } finally {
    }
};
getProduct();

// b2b
const getBatchProductAvailabilityList = async () => {
    try {
        const skuIds = skuList.value.map((item: SkuList) => item.skuId).join(",");
        const result = await getBatchProductAvailability({ skuIds });
        b2bSkuList.value = skuList.value.map((sku: SkuList) => {
            const newSku = {
                ...sku,
                num: 0,
                title: sku.skuValue.replace(/\|/g, ",")
            };

            const skuId = sku.skuId.toString();
            if (result[skuId]) {
                Object.assign(newSku, {
                    price: result[skuId].price,
                    stock: result[skuId].stock
                });
            }

            return newSku;
        });
    } catch (error) {
        console.error(error);
    }
};

const isCollect = ref(false);
const getCollect = async () => {
    try {
        const result = await getCollectProduct({ id: productId });
        isCollect.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
const shopInfo = ref();
const getShop = async (id: number) => {
    try {
        const result = await getShopDetail(id);
        shopInfo.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

const tabIndex = ref(1);
const visible = ref<boolean>(false);

const tabChange = async (data: any) => {
    tabIndex.value = data;
    await nextTick();
    const tabBarElement: any = document.querySelector(".lyecs_main");
    tabBarElement.scrollIntoView({ behavior: "smooth", block: "start" });
};
const addCollect = async (isCollect: boolean) => {
    try {
        if (!isCollect) {
            await updateCollectProduct({
                productId: productId.value
            });
        } else {
            await delCollectProduct({
                id: productId.value
            });
        }

        await getCollect();
        message.success(isCollect ? t("取消收藏成功") : t("收藏成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

const addShopCollect = async (shopId: number, collectShop: boolean) => {
    try {
        await getShopCollect(shopId);
        await getShop(shopId);
        message.success(collectShop ? t("取消收藏成功") : t("收藏成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

watch(
    () => product.value,
    (newVal) => {
        title.value = newVal.productName;
    },
    { deep: true }
);
</script>

<style lang="less" scoped>
.paidcontent {
    .img {
        width: 100%;
    }
}

.product_details {
    margin-top: 15px;

    .extra_details {
        width: 360px;

        .gallery {
            width: 358px;
            height: 358px;
            margin: 0 auto 0;
            margin-bottom: 10px;
            border: 1px solid #eee;

            img {
                width: 358px;
                height: 358px;
                font-size: 5px;
            }
        }

        .share_wish {
            width: 100%;
            margin-top: 20px;
            padding-bottom: 30px;
            display: flex;
            align-items: center;
            column-gap: 10px;

            .product-number {
                color: #999;
                width: fit-content;
                max-width: calc(100% - 60px);
                float: left;
                font-size: 12px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            a {
                font-size: 12px;
                color: #5e5e5e;
                margin-left: 22px;
                overflow: hidden;
                text-decoration: none;

                i {
                    background: url("/assets/images/common/sprite-product.png") no-repeat scroll 0 0;
                }
            }

            .shareGold i {
                background-position: 0 0;
                height: 13px;
                margin-bottom: 2px;
                margin-right: 3px;
                margin-top: 2px;
                width: 13px;
            }

            .showWish i {
                background-position: -14px 0;
                height: 11px;
                margin: 3px 4px 3px 0;
                width: 12px;
            }

            .collect {
                cursor: pointer;
                width: 77px;
            }

            .collect i {
                background-size: 100%;
                margin-bottom: 2px;
                margin-right: 4px;
                width: 16px;
                height: 16px;
                font-size: 12px;
            }
        }
    }

    .details {
        width: 807px;

        .product_name {
            .name {
                font-size: 18px;
                color: #666;
                font-weight: bold;
                padding-bottom: 12px;
            }

            .desc {
                padding: 6px 0 12px;
                color: #888888;
                font-size: 14px;
                font-weight: normal;
            }
        }
    }
}

.detail_main {
    margin-bottom: 20px;

    .lyecs_side {
        width: 216px;

        .shop-box {
            width: 100%;
            background-color: #fff;
            border: 1px solid #eeeeee;
            margin-bottom: 10px;

            .tit {
                background: #f6f6f6;
                padding: 8px 12px;
                line-height: 25px;
                height: 25px;
                font-size: 16px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .btn-box {
                padding: 10px;

                .btn {
                    width: 93px;
                    height: 35px;
                    background: #f6f6f6;
                    text-align: center;
                    border: 1px solid #e8e8e8;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;

                    .iconfont-pc {
                        font-size: 18px;
                        color: var(--general);
                        margin-right: 5px;
                    }
                }
            }
        }
    }

    .lyecs_main {
        width: 964px;
        margin-left: 10px;
        position: relative;

        .goods_tab_wrap {
            background-color: #f6f6f6;
            border: 1px solid #eee;
            border-bottom: none;

            .tabs {
                .tab_a {
                    display: inline-block;
                    padding: 0 20px;
                    font-size: 14px;
                    text-align: center;
                    text-decoration: none;
                    line-height: 36px;
                    height: 36px;
                }

                .current {
                    height: 34px;
                    background-color: #fff;
                    border-bottom: 1px solid #fff;
                    border-top: 3px solid var(--general);
                    line-height: 32px;
                    margin-top: -1px;
                    color: var(--general);
                }
            }
        }

        .goods_desc {
            padding: 10px;
            border: 1px solid #eee;
            margin-bottom: 10px;

            .shuxin {
                background: none repeat scroll 0 0 #f9f9f9;
                border: 1px solid #e8e8e8;
                padding: 10px;
                height: 100%;
                margin-bottom: 10px;
                flex-wrap: wrap;

                li {
                    line-height: 26px;
                    overflow: hidden;
                    width: 300px;
                    font-weight: normal;
                    padding: 0 10px;
                    box-sizing: border-box;
                }
            }

            .goods_description {
                .el-img {
                    width: 100%;
                    display: block;
                }
            }

            .tab_title {
                background: #f6f6f6;
                height: 30px;
                padding-left: 10px;
                margin-bottom: 10px;
                padding-top: 7px;

                h2 {
                    display: inline-block;
                    font-family: "微软雅黑", "SimHei";
                    font-size: 16px;
                    font-weight: normal;
                }

                .icon {
                    background: url("/assets/images/product/product_title_bg.png") no-repeat scroll -80px 0;
                    display: inline-block;
                    height: 16px;
                    line-height: 16px;
                    margin-right: 5px;
                    margin-top: 4px;
                    vertical-align: top;
                    width: 16px;
                }
            }
        }

        .shouhou {
            .tab_title {
                .icon {
                    background-position: -112px 0;
                }
            }
        }

        .shouhou {
            .tab_title {
                .icon {
                    background-position: -112px 0;
                }
            }
        }

        .goods_ask {
            .tab_title {
                .icon {
                    background-position: -128px 0;
                }
            }
        }
    }
}

.ico-color {
    color: var(--general);
}
</style>
