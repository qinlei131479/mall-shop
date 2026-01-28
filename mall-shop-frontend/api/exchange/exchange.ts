import { asyncRequest } from "~/utils/request";
import type { ExchangeFilterResult } from "~/types/exchange/exchange";
export const getExchangeList = (params: {}) => {
    return asyncRequest<ExchangeFilterResult>({
        url: "product/exchange/list",
        method: "get",
        params
    });
};
