package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.PriceInquiryDTO;
import com.tigshop.bean.model.product.PriceInquiry;
import com.tigshop.bean.vo.product.PriceInquiryVO;
import com.tigshop.service.common.BaseService;

/**
 * 商品询价
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PriceInquiryService extends BaseService<PriceInquiry> {

    /**
     * 分页查询
     * @param dto 查询参数
     * @return 分页结果
     */
    Page<PriceInquiryVO> list(PriceInquiryDTO dto);
}
