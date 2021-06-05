package cn.zyity.zfile.controller.admin;

import cn.zyity.zfile.context.StorageTypeContext;
import cn.zyity.zfile.model.entity.StorageConfig;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.model.support.ResultBean;
import cn.zyity.zfile.service.base.AbstractBaseFileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统元数据 Controller
 */
@RestController
@RequestMapping("/admin")
public class MateDataController {

    /**
     * 返回支持的存储引擎.
     */
    @GetMapping("/support-strategy")
    public ResultBean supportStrategy() {
        StorageTypeEnum[] values = StorageTypeEnum.values();
        return ResultBean.successData(values);
    }


    /**
     * 获取指定存储策略的表单域
     *
     * @param   storageType
     *          存储策略
     *
     * @return  所有表单域
     */
    @GetMapping("/strategy-form")
    public ResultBean getFormByStorageType(StorageTypeEnum storageType) {
        AbstractBaseFileService storageTypeService = StorageTypeContext.getStorageTypeService(storageType);
        List<StorageConfig> storageConfigList = storageTypeService.storageStrategyConfigList();
        return ResultBean.success(storageConfigList);
    }

}