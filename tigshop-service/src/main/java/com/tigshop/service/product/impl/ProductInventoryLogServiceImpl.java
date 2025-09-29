package com.tigshop.service.product.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.product.ProductInventoryLog;
import com.tigshop.bean.query.product.ProductInventoryLogPageQuery;
import com.tigshop.bean.vo.product.ProductInventoryLogVO;
import com.tigshop.mapper.product.ProductInventoryLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductInventoryLogService;
import org.springframework.stereotype.Service;

/**
 * 库存日志服务实现类
 *
 * @author kidd
 * @create 2025/7/3
 */
@Service
public class ProductInventoryLogServiceImpl extends BaseServiceImpl<ProductInventoryLogMapper, ProductInventoryLog> implements ProductInventoryLogService {

    @Override
    public Page<ProductInventoryLogVO> list(ProductInventoryLogPageQuery pageQuery) {
        pageQuery.init();

        Page<ProductInventoryLogVO> page = buildSortOrder(pageQuery);

        return this.baseMapper.productInventoryLog(page, pageQuery);
    }
}
