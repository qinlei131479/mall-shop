package com.tigshop.adminapi.controller.product;

import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.tigshop.bean.dto.product.ProductBatchDealDTO;
import com.tigshop.bean.dto.product.ProductBatchEditDTO;
import com.tigshop.bean.vo.product.ProductBatchModifyVO;
import com.tigshop.bean.vo.product.ProductImportExcelVO;
import com.tigshop.service.product.ProductBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 产品批量操作 控制器
 *
 * @author kidd
 * @since 2025/3/26 16:10
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/product/productBatch")
@Tag(name = "商品")
public class ProductBatchController {

    private final ProductBatchService productBatchService;

    @ResponseExcel
    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载商品导入模版", description = "下载商品导入模版")
    public List<ProductImportExcelVO> downloadTemplate() {
        return productBatchService.downloadTemplate();
    }

    @ResponseExcel
    @GetMapping("/productBatchDeal")
    @Operation(summary = "商品导出Excel", description = "商品导出Excel")
    public List<ProductImportExcelVO> productBatchDeal(ProductBatchDealDTO dto) {
        return productBatchService.productBatchDeal(dto);
    }

    @PostMapping("/productBatchModify")
    @Operation(summary = "商品导入", description = "商品导入")
    public ProductBatchModifyVO productBatchModify(
            @RequestParam("isAutoCat") Integer isAutoCat,
            @RequestParam("isAutoBrand") Integer isAutoBrand,
            @RequestParam("isChangePic") Integer isChangePic,
            @RequestParam("isUpload") Integer isUpload,
            @RequestParam("file") MultipartFile file) {
        return productBatchService.productBatchModify(isAutoCat, isAutoBrand, isChangePic, isUpload, file);
    }

    @PostMapping("/productBatchEdit")
    @Operation(summary = "商品批量修改", description = "商品批量修改")
    public void productBatchEdit(@RequestBody ProductBatchEditDTO dto) {
        productBatchService.productBatchEdit(dto);
    }
}
