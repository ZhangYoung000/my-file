package cn.zyity.zfile.service.impl;

import cn.zyity.zfile.exception.InitializeDriveException;
import cn.zyity.zfile.exception.NotExistFileException;
import cn.zyity.zfile.model.constant.StorageConfigConstant;
import cn.zyity.zfile.model.constant.SystemConfigConstant;
import cn.zyity.zfile.model.constant.ZFileConstant;
import cn.zyity.zfile.model.dto.FileItemDTO;
import cn.zyity.zfile.model.entity.StorageConfig;
import cn.zyity.zfile.model.entity.SystemConfig;
import cn.zyity.zfile.model.enums.FileTypeEnum;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.repository.SystemConfigRepository;
import cn.zyity.zfile.service.StorageConfigService;
import cn.zyity.zfile.service.SystemConfigService;
import cn.zyity.zfile.service.base.AbstractBaseFileService;
import cn.zyity.zfile.service.base.BaseFileService;
import cn.zyity.zfile.util.FileComparator;
import cn.zyity.zfile.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public  class LocalServiceImpl extends AbstractBaseFileService implements BaseFileService {

    private static final Logger log = LoggerFactory.getLogger(LocalServiceImpl.class);

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    private SystemConfigRepository systemConfigRepository;

    @Resource
    private SystemConfigService systemConfigService;

    private  String  filePath ;

    @Override
    public void init(Integer driveId) {
        this.driveId = driveId;
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByDriveId(driveId);
        filePath = stringStorageConfigMap.get(StorageConfigConstant.FILE_PATH_KEY).getValue();
        if (Objects.isNull(filePath)) {
            log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
            isInitialized = false;
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
//            throw new InitializeDriveException("文件路径: \"" + file.getAbsolutePath() + "\"不存在, 请检查是否填写正确.");
            file.mkdirs();
        } else {
            testConnection();
            isInitialized = true;
        }
    }


    @Override
    public List<FileItemDTO> fileList(String path) throws FileNotFoundException {
        List<FileItemDTO> fileItemList = new ArrayList<>();
        System.out.println("=========path in fileList========");
        System.out.println(path);
        if (filePath == null) {
//            管理员文件列表
            filePath = systemConfigService.getRootPath();
        }
        String fullPath = StringUtils.removeDuplicateSeparator(filePath + path);
        System.out.println("=========fullpath in fileList========");
        System.out.println(fullPath);
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在");
        }
        File[] files = file.listFiles();

        if (files == null) {
            return fileItemList;
        }
        for (File f : files) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setType(f.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
            fileItemDTO.setTime(new Date(f.lastModified()));
            fileItemDTO.setSize(f.length());
            fileItemDTO.setName(f.getName());
            fileItemDTO.setPath(path);
            if (f.isFile()) {
                fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(path, f.getName())));
            }
            fileItemList.add(fileItemDTO);
        }

        return fileItemList;
    }

    @Override
    public List<FileItemDTO> fileList(String path,String orderBy,String orderDirection) throws FileNotFoundException{
        List<FileItemDTO> files = fileList(path);
        files.sort(new FileComparator(orderBy,orderDirection));
        return files;
    }

    public List<FileItemDTO> fileListAll(String path,String orderBy,String orderDirection) throws FileNotFoundException {
        List<FileItemDTO> fileItemDTOS = fileList(path,orderBy,orderDirection);
        return fileItemDTOS;
    }

    @Override
    public String getDownloadUrl(String path) {
        SystemConfig usernameConfig = systemConfigRepository.findByKey(SystemConfigConstant.DOMAIN);
        return StringUtils.removeDuplicateSeparator(usernameConfig.getValue() + "/file/" + driveId + ZFileConstant.PATH_SEPARATOR + path);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.LOCAL;
    }

    @Override
    public FileItemDTO getFileItem(String path) {
        String fullPath = filePath + path;

        File file = new File(fullPath);

        if (!file.exists()) {
            throw new NotExistFileException();
        }

        FileItemDTO fileItemDTO = new FileItemDTO();
        fileItemDTO.setType(file.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
        fileItemDTO.setTime(new Date(file.lastModified()));
        fileItemDTO.setSize(file.length());
        fileItemDTO.setName(file.getName());
        fileItemDTO.setPath(filePath);
        if (file.isFile()) {
            fileItemDTO.setUrl(getDownloadUrl(path));
        }

        return fileItemDTO;
    }

    @Override
    public List<StorageConfig> storageStrategyConfigList() {
        return new ArrayList<StorageConfig>() {{
            add(new StorageConfig("filePath", "文件路径"));
        }};
    }

}