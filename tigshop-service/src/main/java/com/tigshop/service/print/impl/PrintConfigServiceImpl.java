package com.tigshop.service.print.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigshop.bean.constant.print.PrintTemplateConstant;
import com.tigshop.bean.dto.print.PrintOrderDTO;
import com.tigshop.bean.enums.print.PrintConfigTypeEnum;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.print.PrintConfig;
import com.tigshop.bean.param.print.PrintConfigUpdateParam;
import com.tigshop.bean.param.print.PrintOrdersParam;
import com.tigshop.bean.vo.print.PrintConfigVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.print.PrintConfigMapper;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.print.PrintConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Admin
 * @description 针对表【print_config(打印机配置表)】的数据库操作Service实现
 * @createDate 2025-07-22 09:22:06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrintConfigServiceImpl extends BaseServiceImpl<PrintConfigMapper, PrintConfig> implements PrintConfigService {

    private final RabbitTemplate rabbitTemplate;
    private final PrintMapper printMapper;

    @Override
    public PrintConfigVO getConfigsByPrintId(Integer printId) {
        List<PrintConfig> configs = this.lambdaQuery()
                .eq(PrintConfig::getPrintId, printId)
                .list();
        List<PrintConfigVO> list = configs.stream()
                .map(PrintConfigVO::new)
                .toList();
        return list.stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public boolean updateConfig(PrintConfigUpdateParam param) {
        PrintConfig config = this.getById(param.getId());
        if (config == null) {
            throw new GlobalException("打印配置不存在");
        }

        try {
            // 将PrintTemplate对象转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String templateJson = objectMapper.writeValueAsString(param.getTemplate());
            config.setTemplate(templateJson);
            config.setUpdateTime(StringUtils.getCurrentTime());
            return this.updateById(config);
        } catch (Exception e) {
            log.error("更新打印配置异常", e);
            throw new GlobalException("更新打印配置失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean initConfig(Integer printId) {
        try {
            // 检查是否已存在配置
            Long count = this.lambdaQuery()
                    .eq(PrintConfig::getPrintId, printId)
                    .count();

            if (count > 0) {
                log.info("打印机ID:{}已存在配置，无需初始化", printId);
                return true;
            }

            // 创建默认购物小票配置
            PrintConfig receiptConfig = new PrintConfig();
            receiptConfig.setPrintId(printId);
            receiptConfig.setType(PrintConfigTypeEnum.RECEIPT.getCode());
            receiptConfig.setTemplate(PrintTemplateConstant.DEFAULT_RECEIPT_TEMPLATE);
            receiptConfig.setAddTime(StringUtils.getCurrentTime());
            receiptConfig.setUpdateTime(StringUtils.getCurrentTime());
            return this.save(receiptConfig);
        } catch (Exception e) {
            log.error("初始化打印机配置异常", e);
            return false;
        }
    }

    @Override
    public Boolean print(PrintOrdersParam param) {
        // 更具当前店铺获取打印机信息
        Print print = printMapper.selectOne(new LambdaQueryWrapper<Print>()
                .eq(Print::getShopId, getShopId())
                .eq(Print::getStatus, PrintStatusEnum.ENABLED.getCode())
        );
        if (print == null) {
            throw new GlobalException("当前店铺没有启用的打印机");
        }

        for (Integer id : param.getIds()) {
            PrintOrderDTO printOrderDTO = PrintOrderDTO.builder()
                    .print(print)
                    .orderId(id)
                    .build();
            // 发送到RabbitMQ队列
            rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE,
                    RabbitMQConfig.PRINT_ORDER_ROUTING_KEY, printOrderDTO);
        }
        return true;
    }
}




