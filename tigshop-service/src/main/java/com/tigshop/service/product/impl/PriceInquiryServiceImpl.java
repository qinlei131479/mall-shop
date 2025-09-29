package com.tigshop.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.PriceInquiryDTO;
import com.tigshop.bean.model.product.PriceInquiry;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.product.PriceInquiryVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.PriceInquiryMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.PriceInquiryService;
import com.tigshop.service.product.ProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Tigshop
 */
@Service
public class PriceInquiryServiceImpl extends BaseServiceImpl<PriceInquiryMapper, PriceInquiry>
        implements PriceInquiryService {

    @Resource
    private ProductService productService;

    @Override
    public Page<PriceInquiryVO> list(PriceInquiryDTO listDTO) {
        // 分页
        Page<PriceInquiry> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<PriceInquiry> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        queryWrapper.like(StrUtil.isNotBlank(listDTO.getMobile()), PriceInquiry::getMobile, listDTO.getMobile())
                .eq(listDTO.getStatus() != null, PriceInquiry::getStatus, listDTO.getStatus())
                .eq(listDTO.getProductId() != null, PriceInquiry::getProductId, listDTO.getProductId());

        Page<PriceInquiry> pageInfo = this.page(page, queryWrapper);

        List<PriceInquiry> records = pageInfo.getRecords();

        if (CollUtil.isEmpty(records)){
            return PageUtil.convertPage(pageInfo, List.of());
        }

        List<Integer> productId = records.stream().map(PriceInquiry::getProductId).toList();

        Map<Integer, Product> productMap = productService.lambdaQuery()
                .in(Product::getProductId, productId)
                .list()
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<PriceInquiryVO> priceInquiryVOList = records.stream().map(record -> {
            PriceInquiryVO priceInquiryVO = BeanUtil.copyProperties(record, PriceInquiryVO.class);
            priceInquiryVO.setCreateTime(TigUtils.handelTime(record.getCreateTime()));
            Product product = productMap.get(record.getProductId());
            if (product != null){
                PriceInquiryVO.ProductVO productVO = new PriceInquiryVO.ProductVO();
                productVO.setPicThumb(product.getPicThumb());
                productVO.setProductName(product.getProductName());
                productVO.setProductId(product.getProductId());
                productVO.setProductSn(product.getProductSn());
                priceInquiryVO.setProduct(productVO);
            }
            return priceInquiryVO;
        }).toList();

        return PageUtil.convertPage(pageInfo, priceInquiryVOList);
    }
}

