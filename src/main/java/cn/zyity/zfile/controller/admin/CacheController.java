package cn.zyity.zfile.controller.admin;

import cn.zyity.zfile.model.dto.CacheInfoDTO;
import cn.zyity.zfile.model.support.ResultBean;
import cn.zyity.zfile.service.DriveConfigService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 缓存 Controller
 *
 */
@RestController
@RequestMapping("/admin/cache")
@Secured("ROLE_ADMIN")
public class CacheController {

    @Resource
    private DriveConfigService driveConfigService;

    @PostMapping("/{driveId}/enable")
    public ResultBean enableCache(@PathVariable("driveId") Integer driveId) {
        driveConfigService.updateCacheStatus(driveId, true);
        return ResultBean.success();
    }


    @PostMapping("/{driveId}/disable")
    public ResultBean disableCache(@PathVariable("driveId") Integer driveId) {
        driveConfigService.updateCacheStatus(driveId, false);
        return ResultBean.success();
    }


    @GetMapping("/{driveId}/info")
    public ResultBean cacheInfo(@PathVariable("driveId") Integer driveId) {
        CacheInfoDTO cacheInfo = driveConfigService.findCacheInfo(driveId);
        return ResultBean.success(cacheInfo);
    }


    @PostMapping("/{driveId}/refresh")
    public ResultBean refreshCache(@PathVariable("driveId") Integer driveId, String key) throws Exception {
        driveConfigService.refreshCache(driveId, key);
        return ResultBean.success();
    }

    @PostMapping("/{driveId}/auto-refresh/start")
    public ResultBean enableAutoRefresh(@PathVariable("driveId") Integer driveId) {
        driveConfigService.startAutoCacheRefresh(driveId);
        return ResultBean.success();
    }


    @PostMapping("/{driveId}/auto-refresh/stop")
    public ResultBean disableAutoRefresh(@PathVariable("driveId") Integer driveId) {
        driveConfigService.stopAutoCacheRefresh(driveId);
        return ResultBean.success();
    }

    @PostMapping("/{driveId}/clear")
    public ResultBean clearCache(@PathVariable("driveId") Integer driveId) {
        driveConfigService.clearCache(driveId);
        return ResultBean.success();
    }

}