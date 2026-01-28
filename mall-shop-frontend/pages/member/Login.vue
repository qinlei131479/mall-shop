<template>
    <CommonHeader title="登录"></CommonHeader>
    <CommonLoginHeader type="login"></CommonLoginHeader>
    <div
        :style="{
            backgroundImage: `url(${imageFormat(backgroundPicture)})`
        }"
        class="login-warp"
    >
        <MemberLoginBaseLogin></MemberLoginBaseLogin>
    </div>
    <CommonPageFooter :type="2"></CommonPageFooter>
</template>
<script lang="ts" setup>
import { useUserStore } from "~/store/user";
import { imageFormat } from "~/utils/format";
import { getMobileNav } from "~/api/home/home";

if (useUserStore().isAuthenticated) {
    navigateTo("/", { replace: true });
}
const backgroundPicture = ref("");

const getBackgroundPicture = async () => {
    try {
        const result = await getMobileNav({ decorateSn: "pcUserLogin" });
        backgroundPicture.value = result.data.backgroundPic.picUrl;
    } catch (error) {}
};
getBackgroundPicture();
</script>
<style lang="less" scoped>
.login-warp {
    padding: 40px 0;
    align-items: center;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 40px 0;
    background-position: center;
    background-repeat: no-repeat;

    .login-center {
        width: 100%;
        max-width: 1230px;
        padding: 0 20px;
        box-sizing: border-box;

        .login_border {
            position: relative;

            .login-main {
                background: #ffffff none repeat scroll 0 0;
                min-height: 280px;
                padding-bottom: 20px;
                position: relative;
                min-width: 330px;

                .login-top {
                    border-bottom: 1px solid #e7e7e7;
                    height: 46px;
                    padding: 0 20px;

                    .login-tab {
                        .login_mode {
                            min-width: 58px;
                            height: 46px;
                            line-height: 46px;
                            text-align: center;
                            margin: 0 10px;
                            font-size: 12px;
                            color: #333;
                        }

                        .login-on {
                            border-bottom: 1px solid var(--general) !important;
                            color: var(--general);
                            font-size: 14px;
                        }
                    }

                    .login-toreg-btn {
                        color: #333;

                        a {
                            margin-right: 5px;
                        }

                        i {
                            color: #999;
                        }
                    }
                }

                .login-body {
                    overflow: visible;
                    padding: 15px 30px 0;

                    .login-mode-normal {
                        .login-input {
                            width: 100%;
                            margin-top: 20px;
                            border-radius: 3px;

                            :deep(.el-select__wrapper) {
                                box-shadow: none;
                            }

                            img {
                                width: 20px;
                                height: 20px;
                                margin-right: 8px;
                            }

                            .el-input {
                                height: 36px;
                                border-radius: 2px;
                            }
                        }
                    }

                    .sendMobileMsg {
                        margin-top: 20px;

                        .input-box {
                            width: 60%;
                            border: 1px solid #d6d6d6;
                            border-radius: 3px;
                            padding: 5px 8px;
                            height: 24px;
                            line-height: 24px;

                            img {
                                width: 22px;
                                height: 22px;
                                margin-right: 8px;
                            }

                            input {
                                width: 80%;
                                height: 24px;
                            }
                        }

                        .btn {
                            background: #f0f0f0 none repeat scroll 0 0;
                            border: 1px solid #d6d6d6;
                            border-radius: 3px;
                            cursor: pointer;
                            height: 34px;
                            margin-left: 4px;
                            width: 90px;
                            display: inline-block;
                            text-align: center;
                            line-height: 34px;
                            vertical-align: middle;
                        }
                    }

                    .error-notice {
                        color: rgba(250, 44, 25, 1);
                        padding-top: 10px;
                    }

                    .login-forgot {
                        margin-top: 10px;

                        .forgot-txt {
                            &:hover {
                                color: var(--general) !important;
                            }
                        }
                    }

                    .login_last {
                        margin-top: 20px;

                        .login_btn {
                            width: 100%;
                            background: var(--general) none repeat scroll 0 0;
                            border: 0 none;
                            border-radius: 3px;
                            color: var(--main-text);
                            cursor: pointer;
                            font-size: 16px;
                            height: 40px;
                        }

                        .login_btn.is-disabled {
                            opacity: 0.6;
                            cursor: not-allowed;
                        }
                    }

                    .login_bottom {
                        margin-top: 20px;

                        .other-login {
                            margin-top: 10px;

                            a {
                                margin-right: 20px;
                            }
                        }
                    }

                    .send-valicode {
                        margin-left: 10px;
                        height: 36px;
                    }

                    :deep(.mobile-code-text .code-btn) {
                        color: var(--general);
                        font-size: 12px;
                    }
                }

                .qiehuan1 {
                    background: url("/assets/images/user/login/login-code.png") no-repeat scroll right top;
                    bottom: 0px;
                    cursor: pointer;
                    height: 60px;
                    position: absolute;
                    right: 0px;
                    width: 60px;
                }

                .qiehuan {
                    background: url("/assets/images/user/login/login-code.png") no-repeat scroll left bottom;
                    bottom: 0px;
                    cursor: pointer;
                    height: 60px;
                    position: absolute;
                    right: 0px;
                    width: 60px;
                }
            }

            .wechat-main {
                display: flex;
                justify-content: center;
                align-items: center;
                flex-direction: column;
                gap: 18px;
                height: 325px;

                .tow-title {
                    font-size: 16px;
                    line-height: 20px;
                    color: #858585;
                    height: 20px;
                    font-weight: bold;
                }

                .two-image {
                    width: 140px;
                    height: 140px;
                    border: 1px solid #d5d5d5;
                    border-radius: 2px;
                }
            }
        }
    }
}
</style>
