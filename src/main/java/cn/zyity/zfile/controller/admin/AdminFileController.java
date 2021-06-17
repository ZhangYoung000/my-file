package cn.zyity.zfile.controller.admin;

import cn.zyity.zfile.context.DriveContext;
import cn.zyity.zfile.model.constant.ZFileConstant;
import cn.zyity.zfile.model.dto.FileItemDTO;
import cn.zyity.zfile.model.dto.FileListDTO;
import cn.zyity.zfile.model.dto.SystemFrontConfigDTO;
import cn.zyity.zfile.model.entity.DriveConfig;
import cn.zyity.zfile.model.entity.StorageConfig;
import cn.zyity.zfile.model.entity.SysUser;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.model.support.ResultBean;
import cn.zyity.zfile.model.support.VerifyResult;
import cn.zyity.zfile.repository.StorageConfigRepository;
import cn.zyity.zfile.service.DriveConfigService;
import cn.zyity.zfile.service.FilterConfigService;
import cn.zyity.zfile.service.SystemConfigService;
import cn.zyity.zfile.service.base.AbstractBaseFileService;
import cn.zyity.zfile.service.impl.LocalServiceImpl;
import cn.zyity.zfile.util.FileComparator;
import cn.zyity.zfile.util.FileUtil;
import cn.zyity.zfile.util.HttpUtil;
import cn.zyity.zfile.util.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/file")
@Secured("ROLE_ADMIN")
public class AdminFileController {
    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DriveContext driveContext;

    @Resource
    private LocalServiceImpl localService;

    @GetMapping("/list/{driveId}")
    public ResultBean list(@PathVariable(name = "driveId") Integer driveId,
                           @RequestParam(defaultValue = "/") String path,
                           @RequestParam(required = false) String orderBy,
                           @RequestParam(required = false, defaultValue = "asc") String orderDirection) throws Exception {
        AbstractBaseFileService fileService = driveContext.get(driveId);
        List<FileItemDTO> fileItemList = null;
        if (StringUtils.isNullOrEmpty(orderBy) || StringUtils.isNullOrEmpty(orderDirection)) {
            fileItemList = fileService.fileList(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR));
        } else {
            fileItemList = fileService.fileList(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR), orderBy, orderDirection);
        }
        // 开始获取参数信息
        SystemFrontConfigDTO systemConfig = systemConfigService.getSystemFrontConfig(driveId);
        return ResultBean.successData(new FileListDTO(fileItemList, systemConfig));
    }

    @GetMapping("/list/fileList")
    public ResultBean list(@RequestParam(defaultValue = "/") String path,
                           @RequestParam(required = false) String orderBy,
                           @RequestParam(required = false, defaultValue = "asc") String orderDirection) throws Exception {
        List<FileItemDTO> fileItemList = localService.fileListAll(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR),orderBy,orderDirection);
        return ResultBean.successData(fileItemList);
    }

    @GetMapping("/updateFilename")
    public ResultBean updateFilenameAdmin(String path,String oldName,String newName) {
        String basePath = systemConfigService.getRootPath();
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        boolean flag = new File(basePath+path + oldName).renameTo(new File(basePath+path + newName));
        if (flag) {
            return ResultBean.success("修改成功！");
        }
        return ResultBean.error("修改失败！");

    }

    @GetMapping("/deleteFile")
    public ResultBean deleteFileAdmin(String path,String name) {
        String basePath = systemConfigService.getRootPath();
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        File file = new File(basePath + path + name);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                return  ResultBean.error("删除失败！");
            }
            return ResultBean.success("删除成功！");
        }
        boolean flag = file.delete();
        if (flag) {
            return ResultBean.success("删除成功！");
        }
        return ResultBean.error("删除失败！");
    }

    /*
    管理员下载文件
    * */
    @GetMapping("/file/download")
    @ResponseBody
    public ResponseEntity<Object>  download(HttpServletRequest request, String path) throws Exception {
        return FileUtil.export(new File(StringUtils.removeDuplicateSeparator(systemConfigService.getRootPath() + ZFileConstant.PATH_SEPARATOR + path)));
    }
    }
