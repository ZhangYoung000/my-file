package cn.zyity.zfile.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.model.enums.StorageTypeEnumJsonDeSerializerConvert;
import lombok.Data;

/**
 */
@Data
public class DriveConfigDTO {

    private Integer id;

    private String name;

    @JsonDeserialize(using = StorageTypeEnumJsonDeSerializerConvert.class)
    private StorageTypeEnum type;

    private Boolean enable;

    private boolean enableCache;

    private boolean autoRefreshCache;

    private boolean searchEnable;

    private boolean searchIgnoreCase;

    private boolean searchContainEncryptedFile;

    private Integer orderNum;

    private StorageStrategyConfig storageStrategyConfig;

    private boolean defaultSwitchToImgMode;

}