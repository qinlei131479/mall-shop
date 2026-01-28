import { request, asyncRequest } from "@/utils/request";
import type { quickLoginSettingResult, wechatEventResult, wechatLoginUrlResult } from "~/types/user/login";

export const userSignin = (data: any, headers = {}) => {
    return asyncRequest<any>({
        url: "user/login/signin",
        method: "post",
        data,
        headers
    });
};

export const csrfCreate = () => {
    return asyncRequest({
        url: "common/csrf/create",
        method: "get"
    });
};

export const sendMobileCode = (data: any) => {
    return asyncRequest({
        url: "user/login/sendMobileCode",
        method: "post",
        data
    });
};
//邮箱找回
export const sendEmailCode = (data: any) => {
    return asyncRequest({
        url: "user/login/sendEmailCode",
        method: "post",
        data
    });
};
export const checkEmail = (data: any) => {
    return request({
        url: "user/login/checkEmail",
        method: "post",
        data
    });
};

export const getQuickLoginSetting = () => {
    return asyncRequest<quickLoginSettingResult>({
        url: "user/login/getQuickLoginSetting",
        method: "get"
    });
};

export const getWechatLoginUrl = () => {
    return asyncRequest<wechatLoginUrlResult>({
        url: "user/login/getWxLoginUrl",
        method: "get"
    });
};

export const checkWechatEvent = (data: any) => {
    return asyncRequest<wechatEventResult>({
        url: "user/login/wechatEvent",
        method: "post",
        data
    });
};

export const checkMobile = (data: any) => {
    return asyncRequest({
        url: "user/login/checkMobile",
        method: "post",
        data
    });
};

export const forgetPassword = (data: any) => {
    return asyncRequest({
        url: "user/login/forgetPassword",
        method: "post",
        data
    });
};

export const getOauth = (path: string, redirectUri: string) => {
    return asyncRequest<string>({
        url: `user/oauth/render/${path}?redirectUri=${redirectUri}`,
        method: "get"
    });
};

export const getOauthCallback = (path: string, redirectUri: string, data: object) => {
    return asyncRequest<any>({
        url: `user/oauth/callback/${path}?redirectUri=${redirectUri}`,
        method: "post",
        data
    });
};

export const userLogout = () => {
    return asyncRequest({
        url: "user/user/logout",
        method: "post"
    });
};
