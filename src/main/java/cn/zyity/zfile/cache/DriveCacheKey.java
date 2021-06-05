package cn.zyity.zfile.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriveCacheKey {

    private Integer driveId;

    private String key;

}