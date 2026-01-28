import { request, asyncRequest } from "@/utils/request";
import type { RegionList } from "~/types/user/address.d";

// 获取购物车
export const getRegionByIds = (ids: string) => {
    return asyncRequest<RegionList[]>({
        url: "sys/region/getRegion",
        method: "get",
        params: { regionIds: ids }
    });
};
