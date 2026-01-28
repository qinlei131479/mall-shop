<template>
    <div
        class="module-ad-con"
        :style="commonStyle.moduleStyle.boxPaddingTopBottom + commonStyle.moduleStyle.boxPadding + commonStyle.moduleStyle.backgroundColor"
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundColor +
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.paddingTopBottom +
                commonStyle.moduleContentStyle.paddingLeftRight
            "
        >
            <div class="store-nav-box">
                <div class="store-header">
                    <div class="header-title" :style="titleStyle">附近门店</div>
                    <div class="header-more">更多<span class="iconfont-h5 icon-sanjiaoright"></span></div>
                </div>
                <div class="stores">
                    <div class="stores-item">
                        <div class="item-left">
                            <img src="https://oss.tigshop.com/img/gallery/202508/1756284426i6dOj6OQOWT7PEgTsu.jpeg" />
                        </div>
                        <div class="item-right">
                            <div class="store-title">门店名称门店名称</div>
                            <template v-if="module.showRating == 1">
                                <div class="store-desc">
                                    <img src="@/style/images/decorate/storeNav/store-tag.png" alt="" srcset="" />
                                    5.00
                                </div>
                            </template>

                            <div class="store-address-info">
                                <div class="address line1">江西省南昌市青山湖区艾溪湖312123312</div>
                                <div class="distance">
                                    <span class="iconfont-h5 icon-daohang"></span>
                                    520m
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, Ref, computed } from "vue";
import { ModuleType, ModulestoreNavType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultSubTitle, defaultModuleStyle, defaultContentStyle, formatCommonStyle } from "@/views/decorate/decorate/src/modules/";

const module = defineModel<ModuleType & ModulestoreNavType>("module") as Ref<ModuleType & ModulestoreNavType>;

const defaultModule = ref({
    subTitle: defaultSubTitle, //标题文字
    showRating: 1, // 是否显示店铺名称 0:不显示, 1:显示
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});

mergeDefaultModule(module.value, defaultModule.value);

const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});

const titleStyle = computed(() => {
    return `
        font-size: ${module.value.subTitle?.titleFontSize}px;
        color: ${module.value.subTitle?.titleColor};
        font-weight: ${module.value.subTitle?.titleFontBold};
    `;
});
</script>
<style lang="less" scoped>
.store-nav-box {
    .store-header {
        display: flex;
        align-items: center;
        padding-bottom: 10px;
        border-bottom: 1px solid #f4f4f4;

        .header-title {
            font-weight: 600;
            font-size: 14px;
            color: #1a1a1a;
        }

        .header-more {
            margin-left: auto;
            display: flex;
            align-items: center;
            font-weight: 500;
            font-size: 12px;
            color: #2a2a2a;
            column-gap: 2px;

            .icon-sanjiaoright {
                font-size: 12px;
            }
        }
    }

    .stores {
        padding-top: 15px;

        .stores-item {
            width: 100%;
            display: flex;
            align-items: center;
            column-gap: 12px;

            .item-left {
                width: 68px;
                height: 68px;
                border-radius: 8px;
                overflow: hidden;

                img {
                    display: block;
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }

            .item-right {
                flex: 1;
                min-height: 68px;
                overflow: hidden;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                padding-top: 2px;

                .store-title {
                    font-weight: 500;
                    font-size: 14px;
                    color: #1a1a1a;
                }

                .store-desc {
                    font-weight: 400;
                    font-size: 14px;
                    color: #1a1a1a;
                    display: flex;
                    align-items: center;
                    column-gap: 2px;

                    img {
                        display: block;
                        width: 58px;
                        height: 14px;
                    }
                }

                .store-address-info {
                    display: flex;
                    align-items: center;
                    column-gap: 20px;

                    .address {
                        font-weight: 500;
                        font-size: 14px;
                        color: #1a1a1a;
                        flex: 1;
                        overflow: hidden;
                    }

                    .distance {
                        background: #f4f4f4;
                        font-size: 12px;
                        color: #666666;
                        display: flex;
                        align-items: center;
                        column-gap: 5px;
                        border-radius: 999px;
                        padding: 4px 6px;

                        .icon-daohang {
                            color: rgba(102, 102, 102, 1);
                            font-size: 12px;
                        }
                    }
                }
            }
        }
    }
}
</style>
