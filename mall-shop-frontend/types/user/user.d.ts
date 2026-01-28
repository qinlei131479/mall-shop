export interface UserFormResult {
    item: UserFormState;
    errcode: number;
    message: string;
}

export interface UserFormState {
    dimUsername: string;
    userId: number;
    username: string;
    nickname: string;
    avatar: string;
    points: number;
    balance: number;
    frozenBalance: string;
    birthday: string;
    mobile: string;
    email: string;
    rankId: number;
    wechatImg: string;
    isCompanyAuth: number;
    rankName: string;
    minGrowthPoints: number;
    rankIco: string;
    discount: string;
    rankType: number;
    rankBg: string;
    rankPoint: string;
    freeShipping: number;
    rights: Right[];
    rankLevel: string;
    rankCardType: number;
    rankLogo: string;
    totalBalance: string;
    coupon: number;
    rankExpireTime: string;
    growth: number;
    rankConfig: RankConfig;
    isBindWechat: boolean;
    salesman: Salesman;
    showSign: number;
    hasShop: boolean;
}

export interface RankConfig {
    id: number;
    code: string;
    rankType: number;
    data: Data;
}

export interface Data {
    type: number;
    rankAfterMonth: number;
    useMonth: number;
}

export interface Right {
    name: string;
    showName: string;
    icon: string;
    value: number | string;
    describe: string;
    isChecked: number;
}

export interface Salesman {
    salesmanId: number;
    userId: number;
    level: number;
    groupId: number;
    pid: number;
    addTime: string;
    shopId: number;
    saleAmount: string;
}
