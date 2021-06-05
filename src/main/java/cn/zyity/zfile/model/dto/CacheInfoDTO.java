package cn.zyity.zfile.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 */
@Data
@AllArgsConstructor
public class CacheInfoDTO {

    private Integer cacheCount;

    private Integer hitCount;

    private Integer missCount;

    private Set<String> cacheKeys;

}
