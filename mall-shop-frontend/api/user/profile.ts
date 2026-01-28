import type { ProfileFormResult } from "~/types/user/profile.d";
// 获取商品分类列表

//获取分类详情
export const getProfile = () => {
    return asyncRequest<ProfileFormResult>({
        url: "user/user/detail",
        method: "get",
        noSkipLogin: true
    });
};

// 更新个人信息
export const updateProfile = (data: object) => {
    return asyncRequest({
        url: "user/user/updateInformation",
        method: "post",
        data
    });
};
