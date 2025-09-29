package com.tigshop.service.o2o;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.o2o.ProductOrg;
import com.tigshop.bean.param.o2o.ProductOrgParam;
import com.tigshop.bean.vo.o2o.ProductOrgVO;

/**
 * @author kidd
 */
public interface ProductOrgService extends IService<ProductOrg> {

    /**
     * 分配组织
     */
    void distribution(ProductOrgParam param);

    /**
     * 分配组织详情
     */
    ProductOrgVO info(Long productId);
}
