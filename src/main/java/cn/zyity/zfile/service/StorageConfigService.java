package cn.zyity.zfile.service;

import cn.zyity.zfile.model.entity.StorageConfig;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.repository.StorageConfigRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Service
public class StorageConfigService {

    @Resource
    private StorageConfigRepository storageConfigRepository;


    public List<StorageConfig> selectStorageConfigByType(StorageTypeEnum storageTypeEnum) {
        return storageConfigRepository.findByTypeOrderById(storageTypeEnum);
    }


    public List<StorageConfig> selectStorageConfigByDriveId(Integer driveId) {
        return storageConfigRepository.findByDriveIdOrderById(driveId);
    }


    public StorageConfig findByDriveIdAndKey(Integer driveId, String key) {
        return storageConfigRepository.findByDriveIdAndKey(driveId, key);
    }


    public Map<String, StorageConfig> selectStorageConfigMapByKey(StorageTypeEnum storageTypeEnum) {
        Map<String, StorageConfig> map = new HashMap<>(24);
        for (StorageConfig storageConfig : selectStorageConfigByType(storageTypeEnum)) {
            map.put(storageConfig.getKey(), storageConfig);
        }
        return map;
    }


    public Map<String, StorageConfig> selectStorageConfigMapByDriveId(Integer driveId) {
        Map<String, StorageConfig> map = new HashMap<>(24);
        for (StorageConfig storageConfig : selectStorageConfigByDriveId(driveId)) {
            map.put(storageConfig.getKey(), storageConfig);
        }
        return map;
    }


    public void updateStorageConfig(List<StorageConfig> storageConfigList) {
        storageConfigRepository.saveAll(storageConfigList);
    }

}