package com.tigshop.service.vendor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.vendor.VendorWithdrawAuditDTO;
import com.tigshop.bean.dto.vendor.VendorWithdrawCreateDTO;
import com.tigshop.bean.dto.vendor.VendorWithdrawListDTO;
import com.tigshop.bean.dto.vendor.VendorWithdrawPayVoucherDTO;
import com.tigshop.bean.model.vendor.VendorWithdraw;
import com.tigshop.bean.vo.common.StatusListVO;
import com.tigshop.bean.vo.vendor.VendorWithdrawVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
* @author Admin
* @description 针对表【vendor_withdraw(供应商提现记录)】的数据库操作Service
* @createDate 2025-07-16 17:27:35
*/
public interface VendorWithdrawService extends BaseService<VendorWithdraw> {

    /**
     * 创建供应商提现记录
     * @param createDTO 创建参数
     * @return 提现记录ID
     */
    boolean create(VendorWithdrawCreateDTO createDTO);

    StatusListVO<List<String>> getStatusList();

    Page<VendorWithdrawVO> list(VendorWithdrawListDTO listDTO);

    VendorWithdrawVO detail(Integer id);

    void audit(VendorWithdrawAuditDTO dto);

    void uploadPayVoucher(VendorWithdrawPayVoucherDTO dto);
}
