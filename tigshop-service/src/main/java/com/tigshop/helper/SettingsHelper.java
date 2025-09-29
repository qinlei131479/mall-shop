package com.tigshop.helper;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.mapper.setting.ConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置项 Helper
 *
 * @author kidd
 * @since 2025/6/10 15:42
 */
@Slf4j
@Component
public class SettingsHelper {

    private final ConfigMapper configMapper;

    /**
     * 缓存数据库配置值
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    public SettingsHelper(ConfigMapper configMapper) {
        this.configMapper = configMapper;
        this.initialize();
    }

    /**
     * 初始化缓存所有数据库配置值
     */
    private void initialize() {
        // TODO 不用项目缓存，后面换 redis 缓存
        // List<ConfigPO> configs = configMapper.selectList(Wrappers.emptyWrapper());
        // for (ConfigPO config : configs) {
        //     configCache.put(config.getBizCode(), config.getBizVal());
        // }

        // TODO 补充数据库中没有的默认项, 暂时注释，当配置项的默认值都设定好可以打开
        // for (SettingsEnum setting : SettingsEnum.values()) {
        //     if (!configCache.containsKey(setting.getBizCode())) {
        //         set(setting, setting.getDefaultValue());
        //     }
        // }
    }

    /**
     * 获取配置（自动解析类型 + 默认值回退）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(SettingsEnum setting) {
        String rawValue = getRaw(setting);
        return (T) parse(setting.getValueType(), rawValue);
    }

    /**
     * 获取原始字符串（带默认值）
     */
    public String getRaw(SettingsEnum setting) {
        // TODO 不用项目缓存，后面换 redis 缓存
        // return configCache.getOrDefault(setting.getBizCode(), setting.getDefaultValue());
        ConfigPO config = configMapper.selectOne(
                Wrappers.lambdaQuery(ConfigPO.class)
                        .eq(ConfigPO::getBizCode, setting.getBizCode()));
        return config != null ? config.getBizVal() : setting.getDefaultValue() != null ? setting.getDefaultValue() : "0";
    }

    /**
     * 设置配置值（更新数据库 + 刷新缓存）
     */
    public void set(SettingsEnum setting, Object value) {
        String bigVal = String.valueOf(value);

        // 更新数据库
        ConfigPO config = new ConfigPO();
        config.setBizCode(setting.getBizCode());
        config.setBizVal(bigVal);
        this.set(config);
    }

    public void set(ConfigPO config) {
        String bizVal = String.valueOf(config.getBizVal());

        // 更新数据库
        configMapper.delete(Wrappers.lambdaQuery(ConfigPO.class).eq(ConfigPO::getBizCode, config.getBizCode()));
        configMapper.insert(config);

        // 更新缓存
        // configCache.put(config.getBizCode(), bizVal);
    }

    /**
     * 类型解析方法
     */
    private Object parse(SettingsEnum.ValueTypeEnum type, String valueStr) {
        try {
            return switch (type) {
                case STRING -> valueStr;
                case INTEGER -> Integer.parseInt(valueStr);
                case LONG -> Long.parseLong(valueStr);
                case BOOLEAN -> Boolean.parseBoolean(valueStr);
                case DECIMAL -> new BigDecimal(valueStr);
                case JSON -> JSON.parse(valueStr);
                case JSON_ARRAY -> JSON.parseArray(valueStr);
                case JSON_OBJECT -> JSON.parseObject(valueStr);
            };
        } catch (Exception e) {
            log.error("配置项类型转换失败：{} -> {}", valueStr, type, e);
            return null;
            // throw new IllegalArgumentException("配置项类型转换失败：" + valueStr + " -> " + type, e);
        }
    }
}
