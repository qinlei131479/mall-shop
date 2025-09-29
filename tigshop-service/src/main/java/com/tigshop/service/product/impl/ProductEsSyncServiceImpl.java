// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product.impl;

import com.tigshop.bean.dto.job.SingleSyncRequest;
import com.tigshop.bean.dto.job.BatchSyncRequest;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.service.product.ProductEsSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品ES同步服务实现
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Slf4j
@Service
public class ProductEsSyncServiceImpl implements ProductEsSyncService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void syncProductAsync(Long productId) {
        try {
            SingleSyncRequest request = new SingleSyncRequest();
            request.setProductId(productId);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SINGLE_SYNC_ROUTING_KEY, 
                request
            );
            log.info("发送单个产品ES同步任务: {}", productId);
        } catch (Exception e) {
            log.error("发送单个产品ES同步任务失败: {}", productId, e);
        }
    }

    @Override
    public void batchSyncProductsAsync(List<Long> productIds) {
        try {
            BatchSyncRequest request = new BatchSyncRequest();
            request.setProductIds(productIds);
            request.setBatchSize(100);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.BATCH_SYNC_ROUTING_KEY, 
                request
            );
            log.info("发送批量产品ES同步任务: {}", productIds.size());
        } catch (Exception e) {
            log.error("发送批量产品ES同步任务失败: {}", productIds.size(), e);
        }
    }

    @Override
    public void deleteProductAsync(Long productId) {
        try {
            // 对于删除操作，我们发送一个特殊的同步请求，让消费者处理删除逻辑
            SingleSyncRequest request = new SingleSyncRequest();
            request.setProductId(productId);
            request.setDelete(true);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.SINGLE_SYNC_ROUTING_KEY, 
                request
            );
            log.info("发送产品ES删除任务: {}", productId);
        } catch (Exception e) {
            log.error("发送产品ES删除任务失败: {}", productId, e);
        }
    }
}
