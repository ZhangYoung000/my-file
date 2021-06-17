package cn.zyity.zfile.aspect;

import cn.zyity.zfile.cache.ZFileCache;
import cn.zyity.zfile.model.dto.FileItemDTO;
import cn.zyity.zfile.model.entity.DriveConfig;
import cn.zyity.zfile.service.DriveConfigService;
import cn.zyity.zfile.service.base.AbstractBaseFileService;
import cn.zyity.zfile.util.FileComparator;
import cn.zyity.zfile.util.StringUtils;
import net.bytebuddy.implementation.bytecode.Throw;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 缓存切面类, 用于访问文件夹时, 缓存文件列表内容.
 */
@Aspect
@Component
public class FileListCacheAspect {

    @Resource
    private ZFileCache zFileCache;

    @Resource
    private DriveConfigService driveConfigService;


    /**
     * 缓存切面, 如果此驱动器开启了缓存, 则从缓存中取数据, 没有开启, 则直接调用方法.
     */
    @Around(value = "execution(public * cn.zyity.zfile.service.base.AbstractBaseFileService.fileList(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        List<FileItemDTO> result;
        // 获取请求路径
        Object[] args = point.getArgs();
        String path = String.valueOf(args[0]);
        // 获取当前驱动器
        AbstractBaseFileService fileService = ((AbstractBaseFileService) point.getTarget());
        Integer driveId = fileService.driveId;
        // 判断驱动器是否开启了缓存
        DriveConfig driveConfig = driveConfigService.findById(driveId);
        boolean enableCache = driveConfig.getEnableCache();
        if (enableCache) {
            List<FileItemDTO> cacheFileList = zFileCache.get(driveId, path);
            if (cacheFileList == null) {
//                缓存未命中
                result = Collections.unmodifiableList((List<FileItemDTO>) point.proceed());
                zFileCache.put(driveId, path, result);
                return result;
            } else {
//                缓存命中
                result = cacheFileList;
                if (args.length == 3) {
//                    需要排序
                    String orderBy = (String)args[1];
                    String orderDirection = (String)args[2];
                    result.sort(new FileComparator(orderBy, orderDirection));
                    return result;
                }
            }
        } else {
            result = (List<FileItemDTO>) point.proceed();
        }
        return result;
    }

}