// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.salesman.impl;

import com.alibaba.fastjson.JSON;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.param.salesman.SalesmanConfigSaveParam;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.salesman.SalesmanConfigMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.salesman.SalesmanConfigService;
import org.springframework.stereotype.Service;

/**
 * 分销模式设置服务实现类
 *
 * @author kidd
 * @since 2023-07-05
 */
@Service
public class SalesmanConfigServiceImpl extends BaseServiceImpl<SalesmanConfigMapper, SalesmanConfig> implements SalesmanConfigService {

    @Override
    public SalesmanConfigVO detail(String code) {
        SalesmanConfig salesmanConfig = this.lambdaQuery().eq(SalesmanConfig::getCode, code).one();
        if (salesmanConfig == null) {
            return new SalesmanConfigVO();
        }

        SalesmanConfigVO salesmanConfigVO = new SalesmanConfigVO();
        salesmanConfigVO.setId(salesmanConfig.getId());
        salesmanConfigVO.setShopId(salesmanConfig.getShopId());
        if (salesmanConfig.getData() != null) {
            salesmanConfigVO = JsonUtil.fromJson(salesmanConfig.getData(), SalesmanConfigVO.class);
        }

        return salesmanConfigVO;
    }

    @Override
    public void save(String code, SalesmanConfigSaveParam param) {
        if (SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode().equals(code)) {
            this.saveSalesmanConfig(param);
        }

        if (SalesmanConfigTypeEnum.SALESMAN_SETTLEMENT.getCode().equals(code)) {
            this.saveSalesmanSettlement(param);
        }
    }

    private void saveSalesmanSettlement(SalesmanConfigSaveParam param) {
        Integer shopId = HeaderUtils.getShopId();
        String code = SalesmanConfigTypeEnum.SALESMAN_SETTLEMENT.getCode();

        // 1. 新增/更新分销模式
        saveOrUpdateConfig(param, code, shopId);
    }

    private void saveSalesmanConfig(SalesmanConfigSaveParam param) {
        Integer shopId = HeaderUtils.getShopId();
        String code = SalesmanConfigTypeEnum.SALESMAN_CONFIG.getCode();

        // 1. 验证分销模式参数
        param.adjustLevels();

        // 2. 新增/更新分销模式
        saveOrUpdateConfig(param, code, shopId);
    }

    private void saveOrUpdateConfig(SalesmanConfigSaveParam param, String code, Integer shopId) {
        // 1. 查询分销模式
        SalesmanConfig salesmanConfig = this.lambdaQuery()
                .eq(SalesmanConfig::getCode, code)
                .eq(SalesmanConfig::getShopId, shopId)
                .one();

        // 2. 新增/更新分销模式
        if (salesmanConfig != null) {
            // 编辑
            salesmanConfig.setData(JSON.toJSONString(param));
            this.updateById(salesmanConfig);
        } else {
            // 新增
            salesmanConfig = SalesmanConfig.builder()
                    .code(code)
                    .shopId(shopId)
                    .data(JSON.toJSONString(param))
                    .build();
            this.save(salesmanConfig);
        }
    }

}
