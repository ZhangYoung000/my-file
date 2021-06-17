package cn.zyity.zfile.controller.user;

import cn.hutool.crypto.SecureUtil;
import cn.zyity.zfile.context.DriveContext;
import cn.zyity.zfile.model.constant.ZFileConstant;
import cn.zyity.zfile.model.entity.DriveConfig;
import cn.zyity.zfile.model.entity.StorageConfig;
import cn.zyity.zfile.model.entity.SysUser;
import cn.zyity.zfile.model.entity.SysUserRole;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.model.support.ResultBean;
import cn.zyity.zfile.repository.DriverConfigRepository;
import cn.zyity.zfile.repository.StorageConfigRepository;
import cn.zyity.zfile.repository.SysUserReposity;
import cn.zyity.zfile.service.SystemConfigService;
import cn.zyity.zfile.service.impl.LocalServiceImpl;
import cn.zyity.zfile.util.FileUtil;
import cn.zyity.zfile.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.ws.resources.HttpserverMessages;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 本地存储 Controller
 *
 */
@Controller
public class LocalController {

    @Resource
    private DriveContext driveContext;

    @Resource
    private SysUserReposity userReposity;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private DriverConfigRepository driverConfigRepository;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private StorageConfigRepository storageConfigRepository;


    /**
     * 本地存储下载指定文件
     *
     * @return 文件
     */
    @Secured("ROLE_USER")
    @GetMapping("/file/**")
    @ResponseBody
    public ResponseEntity<Object> downAttachment( final HttpServletRequest request) throws Exception {
        Object _userDriveId = request.getSession().getAttribute("driverId");
        if (_userDriveId == null) {
            throw new Exception("驱动id空值");
        }
        int userDriverId =Integer.parseInt ((String) _userDriveId);
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        System.out.println("path:"+path);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        System.out.println("bestMatchPattern:"+bestMatchPattern);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);
        System.out.println("filePath:"+filePath);
        LocalServiceImpl localService = (LocalServiceImpl) driveContext.get(userDriverId);
        return FileUtil.export(new File(StringUtils.removeDuplicateSeparator(localService.getFilePath() + ZFileConstant.PATH_SEPARATOR + filePath)));
    }


    @PostMapping("/register")
    @ResponseBody
    public void register( HttpServletResponse response, @RequestParam("username") String username, @RequestParam("password") String password) {
        System.out.println("=======register param=====");
        System.out.println(username);
        System.out.println(password);
        response.setContentType("application/json;charset=utf-8");

        PrintWriter out = null;
        try {
            out = response.getWriter();
            List<SysUser> users = userReposity.findByUsername(username);
            if (users != null && users.size() > 0) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(objectMapper.writeValueAsString(ResultBean.error("该用户名已存在！")));
            } else {

                DriveConfig driveConfig = new DriveConfig();
//                driveConfig.setId(0);
                driveConfig.setEnable(true);
                driveConfig.setAutoRefreshCache(false);
                driveConfig.setDefaultSwitchToImgMode(false);
                driveConfig.setEnableCache(false);
                driveConfig.setName(username);
                driveConfig.setSearchEnable(false);
                driveConfig.setSearchEnable(false);
                driveConfig.setSearchIgnoreCase(false);
                driveConfig.setType(StorageTypeEnum.LOCAL);

                driverConfigRepository.save(driveConfig);
                int driverId = driveConfig.getId();
                System.out.println("driverId:"+driverId);
                String rootPath = systemConfigService.getRootPath();

                StorageConfig storageConfig = new StorageConfig("filePath", "文件路径");
//                storageConfig.setId(0);
                storageConfig.setType(StorageTypeEnum.LOCAL);
                storageConfig.setDriveId(driverId);
                String userDir = rootPath + "/" + username;
                File file = new File(userDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                storageConfig.setValue(userDir);
                storageConfigRepository.save(storageConfig);

//              存储用户
                SysUser user = new SysUser();
//                user.setId(0);
                user.setUsername(username);
                user.setPsd(SecureUtil.md5(password));
                user.setDriverId(driverId);
                userReposity.save(user);
                driveContext.init(driverId);
//              存储用户角色
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(2);
                out.write(objectMapper.writeValueAsString(ResultBean.success("注册成功！")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }


    }

    @ResponseBody
    @PostMapping("/update-pwd")
    @Secured("ROLE_USER")
    public ResultBean updatePsd(HttpServletRequest request,String oldPassword, String password) throws Exception {
        Object _userDriveId = request.getSession().getAttribute("driverId");
        if (_userDriveId == null) {
            throw new Exception("驱动id空值");
        }
        int userDriverId =Integer.parseInt ((String) _userDriveId);
        SysUser user = userReposity.findFirstByDriverId(userDriverId);
        if (user == null) {
            return ResultBean.error("驱动有误！");
        }
        if (user.getPsd().equals(SecureUtil.md5(oldPassword))) {
            user.setPsd(SecureUtil.md5(password));
            userReposity.save(user);
            return ResultBean.success();
        }
        return ResultBean.error("旧密码不正确！");
    }


}