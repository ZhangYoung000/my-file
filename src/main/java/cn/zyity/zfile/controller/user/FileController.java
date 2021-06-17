package cn.zyity.zfile.controller.user;

import cn.hutool.core.util.StrUtil;
import cn.zyity.zfile.model.entity.StorageConfig;
import cn.zyity.zfile.repository.StorageConfigRepository;
import cn.zyity.zfile.service.impl.LocalServiceImpl;
import com.alibaba.fastjson.JSON;
import cn.zyity.zfile.context.DriveContext;
import cn.zyity.zfile.exception.PasswordVerifyException;
import cn.zyity.zfile.model.constant.ZFileConstant;
import cn.zyity.zfile.model.dto.FileItemDTO;
import cn.zyity.zfile.model.dto.FileListDTO;
import cn.zyity.zfile.model.dto.SystemFrontConfigDTO;
import cn.zyity.zfile.model.entity.DriveConfig;
import cn.zyity.zfile.model.enums.StorageTypeEnum;
import cn.zyity.zfile.model.support.ResultBean;
import cn.zyity.zfile.model.support.VerifyResult;
import cn.zyity.zfile.service.DriveConfigService;
import cn.zyity.zfile.service.FilterConfigService;
import cn.zyity.zfile.service.SystemConfigService;
import cn.zyity.zfile.service.base.AbstractBaseFileService;
import cn.zyity.zfile.util.FileComparator;
import cn.zyity.zfile.util.HttpUtil;
import cn.zyity.zfile.util.StringUtils;
import com.sun.xml.internal.ws.resources.HttpserverMessages;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 前台文件管理
 */
@Slf4j
@RequestMapping("/api")
@RestController
//@Secured("ROLE_USER")
//      TODO:删除user下的此controller，前端url修改为admin下的controller

public class FileController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DriveContext driveContext;


    @Resource
    private StorageConfigRepository storageConfigRepository;

    @Resource
    private LocalServiceImpl localService;


    /**
     * 获取所有已启用的驱动器
     *
     * @return  所有已启用驱动器
     */
/*
    @GetMapping("/drive/list")
    public ResultBean drives() {
        return ResultBean.success(driveConfigService.listOnlyEnable());
    }
*/

    /**
     * 获取某个驱动器下, 指定路径的数据
     *
     * @param path        路径
     * @return 当前路径下所有文件及文件夹
     */
    //      todo:delete param driveId in this method

    @GetMapping("/list")
    public ResultBean list(HttpServletRequest request,
                           @RequestParam(defaultValue = "/") String path,
                           @RequestParam(required = false) String orderBy,
                           @RequestParam(required = false, defaultValue = "asc") String orderDirection) throws Exception {
        Object _userDriveId = request.getSession().getAttribute("driverId");
        if (_userDriveId == null) {
            throw new Exception("驱动id空值");
        }
        int userDriveId =Integer.parseInt ((String) _userDriveId);
        AbstractBaseFileService fileService = driveContext.get(userDriveId);
        List<FileItemDTO> fileItemList = null;
        if (StringUtils.isNullOrEmpty(orderBy) || StringUtils.isNullOrEmpty(orderDirection)) {
            fileItemList = fileService.fileList(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR));
        } else {
            fileItemList = fileService.fileList(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR), orderBy, orderDirection);
        }

        // 开始获取参数信息
        SystemFrontConfigDTO systemConfig = systemConfigService.getSystemFrontConfig(userDriveId);
        return ResultBean.successData(new FileListDTO(fileItemList, systemConfig));
    }



    @GetMapping("/updateFilename")
    public ResultBean updateFilename(HttpServletRequest request,String path, String oldName, String newName, Integer driverId) throws Exception{
        Object _userDriveId = request.getSession().getAttribute("driverId");
        if (_userDriveId == null) {
            throw new Exception("驱动id空值");
        }
        int userDriverId =Integer.parseInt ((String) _userDriveId);
        List<StorageConfig> byDriveId = storageConfigRepository.findByDriveId(userDriverId);
        if (byDriveId == null || byDriveId.size() != 1) {
            return  ResultBean.error("修改失败！");
        }
        String basePath = byDriveId.get(0).getValue();
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
    public ResultBean deleteFile(HttpServletRequest request,String path,String name,Integer driverId)throws Exception {
        Object _userDriveId = request.getSession().getAttribute("driverId");
        if (_userDriveId == null) {
            throw new Exception("驱动id空值");
        }
        int userDriverId =Integer.parseInt ((String) _userDriveId);
        List<StorageConfig> byDriveId = storageConfigRepository.findByDriveId(userDriverId);
        if (byDriveId == null || byDriveId.size() != 1) {
            return  ResultBean.error("删除失败！");
        }
        String basePath = byDriveId.get(0).getValue();
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


    @PostMapping("/uploadFile")
    public ResultBean uploadFile(HttpServletRequest request) throws Exception{
        Object _userDriveId = request.getSession().getAttribute("driverId");
        if (_userDriveId == null) {
            throw new Exception("驱动id空值");
        }
        return uploadFlieAsync(request);
    }

    @Async(value = "myThreadPool")
    public ResultBean uploadFlieAsync(HttpServletRequest request) {
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            Iterator<String> itr = multipartRequest.getFileNames();

            if (itr.hasNext()) {

                /** 页面控件的文件流* */
                MultipartFile mpf = multipartRequest.getFile("file");
                String path = multipartRequest.getParameter("path");
                String _driverId = multipartRequest.getParameter("driverId");
                int driverId = Integer.parseInt(_driverId);
                String fileName = multipartRequest.getParameter("fileName");
                List<StorageConfig> byDriveId = storageConfigRepository.findByDriveId(driverId);
                if (byDriveId == null || byDriveId.size() != 1) {
                    return  ResultBean.error("上传失败，驱动有误！");
                }
                String basePath = byDriveId.get(0).getValue();
                if (basePath.endsWith("/")) {
                    basePath = basePath.substring(0, basePath.length() - 1);
                }
                System.out.println("fileName:" + fileName);
                fileName =basePath + path + "/" + fileName;
                System.out.println("upload-》文件保存全路径" + fileName);
                File file = new File(fileName);
                try {
                    FileCopyUtils.copy(mpf.getBytes(), file);
                    System.out.println("上传成功！");
                    return ResultBean.success("上传成功");
                } catch (IOException e) {

                    System.out.println("上传失败！");
                    e.printStackTrace();
                    return  ResultBean.error("上传失败！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return  ResultBean.error("上传失败！");
        }
        return  ResultBean.error("上传失败！");
    }

}