package cn.zyity.zfile.model.entity;

import cn.zyity.zfile.model.enums.StorageTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Proxy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 */
@Proxy(lazy = false)

@Entity(name = "STORAGE_CONFIG")
@Data
public class StorageConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private StorageTypeEnum type;

    @Column(name = "k")
    private String key;

    private String title;

    @Lob
    private String value;

    private Integer driveId;

    public StorageConfig(String key, String title) {
        this.key = key;
        this.title = title;
    }

}