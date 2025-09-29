// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.job;

import com.tigshop.bean.dto.job.BatchSyncRequest;
import com.tigshop.bean.dto.job.SingleSyncRequest;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.service.product.ProductEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 产品ES队列任务消费者
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEsJobConsumer {

    private final ProductEsService productEsService;

    /**
     * 批量同步任务
     */
    @RabbitListener(queues = RabbitMQConfig.BATCH_SYNC)
    public void handleBatchSync(BatchSyncRequest request) {
        try {
            log.info("开始执行批量同步任务: {}", request);
            
            // 检查是否需要初始化索引
            if (Boolean.TRUE.equals(request.getInit())) {
                productEsService.initIndex();
            }
            
            // 执行同步
            if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
                // 指定产品ID的批量同步
                productEsService.batchSyncProducts(request.getProductIds(), request.getBatchSize());
            } else {
                // 全量流式同步
                productEsService.syncAllProductsStream(request.getBatchSize());
            }
            
            log.info("批量同步任务执行完成: {}", request);
        } catch (Exception e) {
            log.error("批量同步任务执行失败: {}", request, e);
        }
    }

    /**
     * 单个产品同步任务
     */
    @RabbitListener(queues = RabbitMQConfig.SINGLE_SYNC)
    public void handleSingleSync(SingleSyncRequest request) {
        try {
            log.info("开始执行单个产品同步任务: {}", request);
            
            if (Boolean.TRUE.equals(request.getDelete())) {
                // 删除操作
                productEsService.deleteProduct(request.getProductId());
                log.info("产品删除任务执行完成: {}", request.getProductId());
            } else {
                // 同步操作
                productEsService.syncProduct(request.getProductId());
                log.info("单个产品同步任务执行完成: {}", request);
            }
        } catch (Exception e) {
            log.error("单个产品同步任务执行失败: {}", request, e);
        }
    }
}
