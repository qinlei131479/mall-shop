package com.tigshop.service.print.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.feign.feieyun.AddPrinterResult;
import com.tigshop.bean.feign.feieyun.FeieyunApiResult;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.print.PrintConfig;
import com.tigshop.bean.param.print.PrintAddParam;
import com.tigshop.bean.param.print.PrintUpdateParam;
import com.tigshop.bean.query.print.PrintPageQuery;
import com.tigshop.bean.vo.print.PrintDetailVO;
import com.tigshop.bean.vo.print.PrintPageVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.FeieyunApiUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.FeieyunApiClient;
import com.tigshop.mapper.print.PrintConfigMapper;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.print.PrintConfigService;
import com.tigshop.service.print.PrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 * @description 针对表【print(打印机表)】的数据库操作Service实现
 * @createDate 2025-07-22 09:22:01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrintServiceImpl extends BaseServiceImpl<PrintMapper, Print> implements PrintService {

    private final FeieyunApiClient feieyunApiClient;
    private final PrintConfigMapper printConfigMapper;
    private final PrintConfigService printConfigService;

    @Override
    public Page<PrintPageVO> list(PrintPageQuery pageQuery) {
        // 分页
        Page<Print> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Print> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Print::getShopId, getShopId());

        // 排序字段
        buildSortOrder(page, pageQuery.getSortField(), pageQuery.getSortOrder());

        // 搜索条件
        if (StringUtils.isNotEmpty(pageQuery.getPrintName())) {
            queryWrapper.like(Print::getPrintName, pageQuery.getPrintName());
        }
        if (StringUtils.isNotEmpty(pageQuery.getPrintSn())) {
            queryWrapper.like(Print::getPrintSn, pageQuery.getPrintSn());
        }
        if (pageQuery.getStatus() != null) {
            queryWrapper.eq(Print::getStatus, pageQuery.getStatus());
        }
        if (pageQuery.getPlatform() != null) {
            queryWrapper.eq(Print::getPlatform, pageQuery.getPlatform());
        }

        // 执行查询
        Page<Print> printPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<PrintPageVO> result = new Page<>(printPage.getCurrent(), printPage.getSize(), printPage.getTotal());
        result.setRecords(printPage.getRecords().stream()
                .map(PrintPageVO::new)
                .toList());

        return result;
    }

    @Override
    public PrintDetailVO detail(Integer id) {
        Print print = this.lambdaQuery()
                .eq(Print::getPrintId, id)
                .eq(Print::getShopId, getShopId())
                .one();

        if (print == null) {
            throw new GlobalException("打印机不存在");
        }

        return new PrintDetailVO(print);
    }

    @Override
    @Transactional
    public boolean create(PrintAddParam printAddParam) {
        try {
            // 生成时间戳
            String stime = FeieyunApiUtils.getCurrentTimestamp();
            // 生成签名
            String sig = FeieyunApiUtils.generateSign(printAddParam.getThirdAccount(), printAddParam.getThirdKey(), stime);
            // 构建打印机信息
            String printerContent = printAddParam.getPrintSn() + "#" + printAddParam.getPrintKey() + "#" + printAddParam.getPrintName();

            // 调用飞鹅云API添加打印机
            String resultStr = feieyunApiClient.addPrinter(
                    printAddParam.getThirdAccount(),
                    stime,
                    sig,
                    "Open_printerAddlist",
                    printerContent
            );

            AddPrinterResult result = JSONUtil.parseObj(resultStr).toBean(AddPrinterResult.class);

            // 检查API调用结果
            if (result != null && result.isSuccess() && (!result.getData().getOk().isEmpty() || result.getData().getNo().getFirst().contains("已被添加过"))) {
                // API调用成功，保存打印机信息到数据库
                Print print = new Print();
                print.setPrintName(printAddParam.getPrintName());
                print.setPrintSn(printAddParam.getPrintSn());
                print.setPrintKey(printAddParam.getPrintKey());
                print.setThirdAccount(printAddParam.getThirdAccount());
                print.setThirdKey(printAddParam.getThirdKey());
                print.setPrintNumber(1);
                print.setPlatform(PrintPlatformEnum.FEIEYUN.getCode());
                print.setShopId(getShopId());
                print.setStatus(PrintStatusEnum.DISABLED.getCode());
                print.setAutoPrint(printAddParam.getAutoPrint());
                print.setAddTime(StringUtils.getCurrentTime());
                print.setUpdateTime(StringUtils.getCurrentTime());
                boolean save = save(print);

                // 初始化默认的打印配置
                if (save) {
                    boolean initConfigResult = printConfigService.initConfig(print.getPrintId());
                    if (!initConfigResult) {
                        log.warn("打印机配置初始化失败，打印机ID：{}", print.getPrintId());
                    }
                }

                return save;
            } else {
                // API调用失败
                log.error("添加打印机失败，API返回结果：{}", resultStr);
                if (result == null) {
                    throw new GlobalException("添加打印机失败");
                }
                if (ObjectUtil.notEqual(result.getMsg(), "ok")) {
                    throw new GlobalException(result.getMsg());
                }
                if (ObjectUtil.isNotEmpty(result.getData().getNo())) {
                    throw new GlobalException(result.getData().getNo().getFirst());
                }
                throw new GlobalException("添加打印机失败");
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException("添加打印机失败");
        }
    }

    @Override
    @Transactional
    public boolean update(PrintUpdateParam printUpdateParam) {
        try {
            // 查询原打印机信息
            Print oldPrint = this.lambdaQuery()
                    .eq(Print::getPrintId, printUpdateParam.getPrintId())
                    .eq(Print::getShopId, getShopId())
                    .one();

            if (oldPrint == null) {
                throw new GlobalException("打印机不存在");
            }

            // 判断是否修改了第三方账号信息
            boolean thirdAccountChanged = !oldPrint.getThirdAccount().equals(printUpdateParam.getThirdAccount())
                    || !oldPrint.getThirdKey().equals(printUpdateParam.getThirdKey());

            // 判断是否修改了设备相关信息
            boolean deviceInfoChanged = !oldPrint.getPrintSn().equals(printUpdateParam.getPrintSn())
                    || !oldPrint.getPrintKey().equals(printUpdateParam.getPrintKey());

            // 如果修改了第三方账号或者设备相关信息，则需要先删除原打印机，再添加新打印机
            if (thirdAccountChanged || deviceInfoChanged) {
                // 先删除原打印机
                String stime = FeieyunApiUtils.getCurrentTimestamp();
                String sig = FeieyunApiUtils.generateSign(oldPrint.getThirdAccount(), oldPrint.getThirdKey(), stime);
                String deleteResultStr = feieyunApiClient.deletePrinter(
                        oldPrint.getThirdAccount(),
                        stime,
                        sig,
                        "Open_printerDelList",
                        oldPrint.getPrintSn()
                );

                FeieyunApiResult deleteResult = JSONUtil.parseObj(deleteResultStr).toBean(FeieyunApiResult.class);

                if (deleteResult == null || !deleteResult.isSuccess()) {
                    // 继续执行，因为可能是打印机在飞鹅云平台已经不存在
                    log.error("删除原打印机失败，API返回结果：{}", deleteResultStr);
                }

                // 添加新打印机
                stime = FeieyunApiUtils.getCurrentTimestamp();
                sig = FeieyunApiUtils.generateSign(printUpdateParam.getThirdAccount(), printUpdateParam.getThirdKey(), stime);
                String printerContent = printUpdateParam.getPrintSn() + "#" + printUpdateParam.getPrintKey() + "#" + printUpdateParam.getPrintName();
                String resultStr = feieyunApiClient.addPrinter(
                        printUpdateParam.getThirdAccount(),
                        stime,
                        sig,
                        "Open_printerAddlist",
                        printerContent
                );

                AddPrinterResult addResult = JSONUtil.parseObj(resultStr).toBean(AddPrinterResult.class);

                if (addResult == null || !addResult.isSuccess() || addResult.getData().getOk().isEmpty()) {
                    log.error("添加新打印机失败，API返回结果：{}", resultStr);
                    throw new GlobalException("更新打印机失败：" + (addResult != null ? addResult.getMsg() : "调用API失败"));
                }
            }
            // 更新数据库中的打印机信息
            Print updatePrint = new Print();
            updatePrint.setPrintId(printUpdateParam.getPrintId());
            updatePrint.setPrintName(printUpdateParam.getPrintName());
            updatePrint.setPrintSn(printUpdateParam.getPrintSn());
            updatePrint.setPrintKey(printUpdateParam.getPrintKey());
            updatePrint.setThirdAccount(printUpdateParam.getThirdAccount());
            updatePrint.setThirdKey(printUpdateParam.getThirdKey());
            updatePrint.setPrintNumber(1);
            updatePrint.setUpdateTime(StringUtils.getCurrentTime());
            updatePrint.setStatus(printUpdateParam.getStatus());
            updatePrint.setAutoPrint(printUpdateParam.getAutoPrint());

            if (PrintStatusEnum.ENABLED == PrintStatusEnum.getByCode(printUpdateParam.getStatus()) &&
                    !oldPrint.getStatus().equals(printUpdateParam.getStatus())) {
                lambdaUpdate()
                        .set(Print::getStatus, PrintStatusEnum.DISABLED.getCode())
                        .eq(Print::getShopId, getShopId())
                        .update();
            }
            return this.updateById(updatePrint);
        } catch (GlobalException globalException) {
            // 将异常抛出
            throw globalException;
        } catch (Exception e) {
            log.error("更新打印机异常", e);
            throw new GlobalException("更新打印机失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        // 如果修改的是状态字段，并且修改为启用，则应将关联的其他打印机都修改为关闭
        if ("status".equals(updateField.getField()) && PrintStatusEnum.ENABLED.getCode() == Integer.parseInt(updateField.getVal())) {
            lambdaUpdate()
                    .set(Print::getStatus, PrintStatusEnum.DISABLED.getCode())
                    .eq(Print::getShopId, getShopId())
                    .update();
        }
        return super.updateField(updateField, allowFields);
    }

    @Override
    @Transactional
    public boolean del(Integer id) {
        // 查询打印机信息
        Print print = this.lambdaQuery()
                .eq(Print::getPrintId, id)
                .eq(Print::getShopId, getShopId())
                .one();

        if (print == null) {
            throw new GlobalException("打印机不存在");
        }
        // 从数据库删除打印机
        this.removeById(id);
        printConfigMapper.delete(new LambdaQueryWrapper<PrintConfig>().eq(PrintConfig::getPrintId, id));
        return true;
    }

    @Override
    public boolean hasEnabled() {
        return this.lambdaQuery()
                .eq(Print::getShopId, getShopId())
                .eq(Print::getStatus, PrintStatusEnum.ENABLED.getCode())
                .exists();
    }
}




