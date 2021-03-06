package cn.zyity.zfile.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.model.enums.StorageTypeEnumSerializerConvert;
import lombok.Data;
import lombok.ToString;

/**
 * 系统设置传输类
 *
 */
@ToString
@Data
public class SystemConfigDTO {

    @JsonIgnore
    private Integer id;

    private String siteName;

    private String username;

    @JsonSerialize(using = StorageTypeEnumSerializerConvert.class)
    private StorageTypeEnum storageStrategy;

    @JsonIgnore
    private String password;

    private String domain;

    private String customJs;

    private String customCss;

    private String tableSize;

    private Boolean showOperator;

    private Boolean showDocument;

    private String announcement;

    private Boolean showAnnouncement;

    private String layout;

    private String root;
}