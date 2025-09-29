package com.tigshop.service.o2o;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.o2o.UserPickupInfo;
import com.tigshop.bean.param.o2o.UserPickupParam;
import com.tigshop.bean.query.o2o.UserPickupQuery;
import com.tigshop.bean.vo.o2o.UserPickupInfoVO;
import com.tigshop.service.common.BaseService;

/**
 * 用户自提信息服务接口
 * @author Tigshop项目组
 */
public interface UserPickupInfoService extends BaseService<UserPickupInfo> {
    /**
     * 用户自提信息列表
     * @param query 查询参数
     * @return 用户自提信息列表
     */
    Page<UserPickupInfoVO> getUserPickups(UserPickupQuery query);

    /**
     * 创建用户自提信息
     * @param param 创建参数
     */
    void create(UserPickupParam param);

    /**
     * 修改用户自提信息
     * @param param 修改参数
     */
    void update(UserPickupParam param);

    /**
     * 用户自提信息详情
     * @param userPickupId 用户自提信息ID
     */
    UserPickupInfoVO detail(Integer userPickupId);
}
