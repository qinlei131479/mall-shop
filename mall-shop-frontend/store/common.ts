import { getNav } from "@/api/common";
import { getBaseConfig, getThemeSettings } from "@/api/common";
import type { DecoratePageConfig } from "@/types/common/common.d.ts";
import { useThemeStore } from "@/store/theme";

export const useCommonStore = defineStore("common", {
    state: () => {
        return {
            shopName: "", //商城名称
            shopTitle: "", //商城标题
            shopTitleSuffix: "", //商城标题后缀
            shopKeywords: "",
            shopDesc: "",
            shopLogo: "", //商城logo
            storageUrl: "", // 存储地址
            dollarSign: "", //货币符号
            icoImg: "", //图标
            mainNav: [],
            topBarNav: [],
            bottomNav: [],
            sidebarNav: [],
            topRegion: [],
            navLoaded: false,
            allCate: null,
            decoratePageConfig: {
                headerStyle: 0
            } as DecoratePageConfig,
            defaultHeaderStyle: 1,
            personApplyEnabled: 0, //是否允许个人入驻
            provinceId: 0,
            canInvoice: 0,
            invoiceAdded: 0,
            autoRedirect: 1,
            h5Domain: "",
            shopIcpNo: "",
            shopIcpNoUrl: "",
            shop110No: "",
            shop110Link: "",
            shopCompanyTxt: "",
            companyAddress: "",
            kefuPhone: "",
            kefuTime: "",
            kefuAddress: "",
            isEnterprise: "",
            deCopyright: 0,
            poweredByStatus: 0,
            poweredBy: 0,
            defaultShopName: "",
            isOpenMobileAreaCode: 0,
            showMarketprice: 1,
            showSelledCount: 1,
            companyDataType: 1,
            companyDataTips: "",
            adminDomain: "", // 后台地址路径
            isIdentity: 0,
            isEnquiry: 0,
            closeOrder: 0,
            shopRegClosed: 0,
            useCoupon: 0,
            usePoints: 0,
            useSurplus: 0,
            openWechatPcLogin: 0,
            wechatRegisterBindPhone: 0,
            openWechatOauth: 0,
            googleLoginOn: 0,
            facebookLoginOn: 0,
            defaultTechSupport: "",
            poweredByLogo: "",
            openEmailRegister: 0,
            integralName: "",
            closeAuth: 0,
            bulkPurchase: 0,
            enableAttributeFilter: 0
        };
    },
    getters: {
        pageTitle: (state) => {
            return state.shopTitleSuffix ? " - " + state.shopTitleSuffix : " ";
        }
    },
    actions: {
        async loadNav() {
            if (this.navLoaded === false) {
                this.navLoaded = true;
                try {
                    const result: any = await getNav();
                    this.mainNav = result.mainNav ?? [];
                    this.topBarNav = result.topBarNav ?? [];
                    this.bottomNav = result.bottomNav ?? [];
                    this.sidebarNav = result.sidebarNav ?? [];
                } catch (error) {}
            }
        },
        async loadBaseConfig(params = {}) {
            try {
                const result = await getBaseConfig(params);
                this.companyDataTips = result.companyDataTips;
                this.companyDataType = result.companyDataType;
                this.personApplyEnabled = result.personApplyEnabled;
                this.shopName = result.shopName;
                this.canInvoice = result.canInvoice;
                this.invoiceAdded = result.invoiceAdded;
                this.shopTitle = result.shopTitle;
                this.shopTitleSuffix = result.shopTitleSuffix ?? "";
                this.shopLogo = result.shopLogo;
                this.shopKeywords = result.shopKeywords;
                this.shopDesc = result.shopDesc;
                this.storageUrl = result.storageUrl;
                this.dollarSign = result.dollarSign;
                this.icoImg = result.icoImg;
                this.decoratePageConfig = result.decoratePageConfig || ({ headerStyle: 1 } as DecoratePageConfig);
                this.defaultHeaderStyle = result.defaultHeaderStyle || 1;
                this.autoRedirect = result.autoRedirect ?? 0;
                this.h5Domain = result.h5Domain;
                this.shopIcpNo = result.shopIcpNo;
                this.shopIcpNoUrl = result.shopIcpNoUrl;
                this.shop110No = result.shop110No;
                this.shop110Link = result.shop110Link;
                this.shopCompanyTxt = result.shopCompanyTxt;
                this.companyAddress = result.companyAddress;
                this.kefuPhone = result.kefuPhone;
                this.kefuTime = result.kefuTime;
                this.kefuAddress = result.kefuAddress;
                this.deCopyright = result.deCopyright;
                this.poweredByStatus = result.poweredByStatus;
                this.poweredBy = result.poweredBy;
                this.defaultShopName = result.defaultShopName;
                this.isOpenMobileAreaCode = result.isOpenMobileAreaCode;
                this.showMarketprice = result.showMarketprice;
                this.showSelledCount = result.showSelledCount;
                this.adminDomain = result.adminDomain;
                this.isIdentity = result.isIdentity;
                this.isEnquiry = result.isEnquiry;
                this.closeOrder = result.closeOrder;
                this.shopRegClosed = result.shopRegClosed;
                this.useCoupon = result.useCoupon;
                this.usePoints = result.usePoints;
                this.useSurplus = result.useSurplus;
                this.openWechatPcLogin = result.openWechatPcLogin;
                this.wechatRegisterBindPhone = result.wechatRegisterBindPhone;
                this.openWechatOauth = result.openWechatOauth;
                this.googleLoginOn = result.googleLoginOn;
                this.facebookLoginOn = result.facebookLoginOn;
                this.defaultTechSupport = result.defaultTechSupport;
                this.poweredByLogo = result.poweredByLogo;
                this.openEmailRegister = result.openEmailRegister;
                this.integralName = result.integralName;

                this.closeAuth = result.closeAuth;
                this.bulkPurchase = result.bulkPurchase;
                this.enableAttributeFilter = result.enableAttributeFilter;

            } catch (error) {
                console.error(error);
            }
        },

        async loadThemeSettings() {
            const themeStore = useThemeStore();
            const themeColor = useCookie<any>("themeColor");
            try {
                const result = await getThemeSettings();
                let theme = JSON.parse(result?.themeStyle);
                if (themeColor.value) {
                    // 演示站才有的功能
                    themeStore.themeId = themeColor.value.themeId;
                    themeStore.themeColor = themeColor.value;
                } else {
                    let { themeId, check, ...themeColor } = theme;
                    // 正式部署都是通过接口返回主题色
                    themeStore.themeId = themeId;
                    themeStore.themeColor = themeColor; // 主题色~~~~~
                }
            } catch (error) {
                console.error(error);
            }
        }
    }
});
