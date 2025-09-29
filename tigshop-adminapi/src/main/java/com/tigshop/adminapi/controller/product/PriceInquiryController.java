package com.tigshop.adminapi.controller.product;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.product.PriceInquiryDTO;
import com.tigshop.bean.dto.product.PriceInquiryReplyDTO;
import com.tigshop.bean.model.product.PriceInquiry;
import com.tigshop.bean.vo.product.PriceInquiryVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.service.product.PriceInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 *
 * 商品询价
 * @author wzh
 */
@RestController
@RequestMapping("/adminapi/product/priceInquiry")
@Tag(name = "商品询价", description = "商品询价")
@AllArgsConstructor
public class PriceInquiryController {

    private final PriceInquiryService priceInquiryService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<PriceInquiryVO> list(PriceInquiryDTO dto) {
        Integer shopId = HeaderUtils.getShopId();
        dto.setShopId(shopId);
        return priceInquiryService.list(dto);
    }


    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public PriceInquiryVO detail(@RequestParam Integer id) {
        PriceInquiry priceInquiry = priceInquiryService.getById(id);
        PriceInquiryVO priceInquiryVO = BeanUtil.copyProperties(priceInquiry, PriceInquiryVO.class);
        priceInquiryVO.setCreateTime(TigUtils.handelTime(priceInquiry.getCreateTime()));
        return priceInquiryVO;
    }

    @Operation(summary = "回复")
    @PreAuthorize("@pms.hasPermission('priceInquiryModifyManage')")
    @PostMapping("/reply")
    public void reply(@RequestBody @Valid PriceInquiryReplyDTO dto) {
        priceInquiryService.lambdaUpdate()
                .eq(PriceInquiry::getId, dto.getId())
                .set(PriceInquiry::getStatus, 1)
                .set(PriceInquiry::getRemark, dto.getRemark())
                .update();

    }

    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('priceInquiryModifyManage')")
    @PostMapping("/del")
    public void del(@RequestBody OperateDTO dto) {
        priceInquiryService.removeById(dto.getId());

    }
}
