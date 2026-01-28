import { isMerchant } from "@/utils/version";
import { isStore } from "@/utils/version";
export const getAdminType = (): string | null => {
    return localStorage.getItem("adminType");
};

export const getShopType = (): number => {
    let shopType = 0
    if(getAdminType() === 'shop' && (isMerchant() || isStore())){
        shopType = Number(localStorage.getItem("shopType")) || 0;
    }
    return shopType;
};

export const loginOut = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("adminType");
    localStorage.removeItem("config");
    localStorage.removeItem("lastOpenTime");
    localStorage.removeItem("licensedData");
    localStorage.removeItem("menus");
    localStorage.removeItem("region");
    localStorage.removeItem("routers");
    localStorage.removeItem("shopId");
    localStorage.removeItem("theme");
    localStorage.removeItem("user");
    localStorage.removeItem("decorate");
    localStorage.removeItem("version");
    localStorage.removeItem("shopInfo");
    localStorage.removeItem("vendorId");
    localStorage.removeItem("shopId");
    localStorage.removeItem("vendorInfo");
    localStorage.removeItem("shopType");
};