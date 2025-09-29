package com.tigshop.api.controller.merchant;

import com.tigshop.bean.param.merchant.MerchantApplyApplyParam;
import com.tigshop.bean.vo.merchant.MerchantApplyDetailVO;
import com.tigshop.bean.vo.merchant.MerchantMyApplyVO;
import com.tigshop.service.merchant.MerchantApplyService;
import com.tigshop.service.merchant.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商户控制器
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/merchant/merchant")
@Tag(name = "商户")
@Validated
public class MerchantClientController {
    @Resource
    private MerchantService merchantService;

    @Resource
    private MerchantApplyService merchantApplyService;

    @GetMapping("/applyShopAgreement")
    @Operation(summary = "申请入驻协议")
    public String applyShopAgreement(){
        return merchantService.applyShopAgreement();
    }

    @GetMapping("/myApply")
    @Operation(summary = "我的申请")
    public MerchantMyApplyVO myApply(){
        return merchantApplyService.myApply();
    }

    @GetMapping("/applyDetail")
    @Operation(summary = "申请详情")
    public MerchantApplyDetailVO applyDetail(@RequestParam Integer id){
        return merchantApplyService.applyDetail(id);
    }

    @PostMapping("/apply")
    @Operation(summary = "申请入驻")
    public MerchantApplyDetailVO apply(@RequestBody @Validated MerchantApplyApplyParam param){
        return merchantApplyService.apply(param);
    }

}
