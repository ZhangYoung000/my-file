package cn.zyity.zfile.controller.home;

import cn.hutool.core.util.StrUtil;
import cn.zyity.zfile.model.entity.StorageConfig;
import cn.zyity.zfile.repository.StorageConfigRepository;
import cn.zyity.zfile.repository.SysUserReposity;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
public class FileController {

    @Value("${zfile.debug}")
    private Boolean debug;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DriveContext driveContext;

    @Resource
    private DriveConfigService driveConfigService;

    @Resource
    private FilterConfigService filterConfigService;

    @Resource
    private StorageConfigRepository storageConfigRepository;

    @Resource
    private LocalServiceImpl localService;


    /**
     * 获取所有已启用的驱动器
     *
     * @return  所有已启用驱动器
     */
    @GetMapping("/drive/list")
    public ResultBean drives() {
        return ResultBean.success(driveConfigService.listOnlyEnable());
    }

    /**
     * 获取某个驱动器下, 指定路径的数据
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   path
     *          路径
     *
     * @param   password
     *          文件夹密码, 某些文件夹需要密码才能访问, 当不需要密码时, 此参数可以为空
     *
     * @return  当前路径下所有文件及文件夹
     */
    @GetMapping("/list/{driveId}")
    public ResultBean list(@PathVariable(name = "driveId") Integer driveId,
                           @RequestParam(defaultValue = "/") String path,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false) String orderBy,
                           @RequestParam(required = false, defaultValue = "asc") String orderDirection) throws Exception {
        AbstractBaseFileService fileService = driveContext.get(driveId);
        List<FileItemDTO> fileItemList = fileService.fileList(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR));

        // 创建副本, 防止排序和过滤对原数据产生影响
        List<FileItemDTO> copyList = new ArrayList<>(fileItemList);

        System.out.println("fileList:"+copyList);
       /* for (int i = 0; i < copyList.size(); i++) {
            FileItemDTO file = copyList.get(i);
            if (!String.valueOf(driveId).equals(file.getUrl().split("/")[5])) {
                copyList.remove(i);
                i--;
            }
        }
        System.out.println("fileList final:"+copyList);*/

        // 校验密码, 如果校验不通过, 则返回错误消息
        VerifyResult verifyResult = verifyPassword(copyList, driveId, path, password);
        if (!verifyResult.isPassed()) {
            return ResultBean.error(verifyResult.getMsg(), verifyResult.getCode());
        }

        // 过滤掉驱动器配置的表达式中要隐藏的数据
        filterFileList(copyList, driveId);

        // 按照自然排序
        copyList.sort(new FileComparator(orderBy, orderDirection));
        // 开始获取参数信息
        SystemFrontConfigDTO systemConfig = systemConfigService.getSystemFrontConfig(driveId);
        DriveConfig driveConfig = driveConfigService.findById(driveId);
        systemConfig.setDebugMode(debug);
        systemConfig.setDefaultSwitchToImgMode(driveConfig.getDefaultSwitchToImgMode());

        // 如果不是 FTP 模式，则尝试获取当前文件夹中的 README 文件，有则读取，没有则停止
        if (!Objects.equals(driveConfig.getType(), StorageTypeEnum.FTP)) {
            fileItemList.stream()
                    .filter(fileItemDTO -> Objects.equals(ZFileConstant.README_FILE_NAME, fileItemDTO.getName()))
                    .findFirst()
                    .ifPresent(fileItemDTO -> {
                        String readme = HttpUtil.getTextContent(fileItemDTO.getUrl());
                        systemConfig.setReadme(readme);
                    });
        }

        return ResultBean.successData(new FileListDTO(copyList, systemConfig));
    }
    @GetMapping("/list/findAllFile")
    public ResultBean list(@RequestParam(defaultValue = "/") String path,
                           @RequestParam(required = false) String orderBy,
                           @RequestParam(required = false, defaultValue = "asc") String orderDirection) throws Exception {
//        AbstractBaseFileService fileService = driveContext.get(driveId);
        List<FileItemDTO> fileItemList = localService.fileListAll(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR));

        // 创建副本, 防止排序和过滤对原数据产生影响
        List<FileItemDTO> copyList = new ArrayList<>(fileItemList);
        System.out.println(copyList );
        // 校验密码, 如果校验不通过, 则返回错误消息
       /* VerifyResult verifyResult = verifyPassword(copyList, driveId, path, password);
        if (!verifyResult.isPassed()) {
            return ResultBean.error(verifyResult.getMsg(), verifyResult.getCode());
        }*/

        // 过滤掉驱动器配置的表达式中要隐藏的数据
//        filterFileList(copyList, driveId);

        // 按照自然排序
        copyList.sort(new FileComparator(orderBy, orderDirection));



        // 开始获取参数信息
        /*SystemFrontConfigDTO systemConfig = systemConfigService.getSystemFrontConfig(driveId);
        DriveConfig driveConfig = driveConfigService.findById(driveId);
        systemConfig.setDebugMode(debug);
        systemConfig.setDefaultSwitchToImgMode(driveConfig.getDefaultSwitchToImgMode());

        // 如果不是 FTP 模式，则尝试获取当前文件夹中的 README 文件，有则读取，没有则停止
        if (!Objects.equals(driveConfig.getType(), StorageTypeEnum.FTP)) {
            fileItemList.stream()
                    .filter(fileItemDTO -> Objects.equals(ZFileConstant.README_FILE_NAME, fileItemDTO.getName()))
                    .findFirst()
                    .ifPresent(fileItemDTO -> {
                        String readme = HttpUtil.getTextContent(fileItemDTO.getUrl());
                        systemConfig.setReadme(readme);
                    });
        }*/

//        return ResultBean.successData(new FileListDTO(copyList, systemConfig));
        return ResultBean.successData(copyList);
    }


    /**
     * 获取指定路径下的文件信息内容
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   path
     *          文件全路径
     *
     * @return  该文件的名称, 路径, 大小, 下载地址等信息.
     */
    @GetMapping("/directlink/{driveId}")
    public ResultBean directlink(@PathVariable(name = "driveId") Integer driveId, String path) {
        AbstractBaseFileService fileService = driveContext.get(driveId);
        return ResultBean.successData(fileService.getFileItem(path));
    }

    @GetMapping("/mkdir")
    public ResultBean mkdir(String dirPath,Integer driverId) {
        List<StorageConfig> byDriveId = storageConfigRepository.findByDriveId(driverId);
        if (byDriveId == null || byDriveId.size() != 1) {
            return  ResultBean.error("创建失败！");
        }
        String basePath = byDriveId.get(0).getValue();
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
       /* System.out.println("=====basePath====");
        System.out.println(basePath);

        System.out.println("==========mkDir Param=========");
        System.out.println(dirPath+"   "+driverId);*/
//        创建文件夹
        File dir = new File(basePath + dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return  ResultBean.success("创建成功！");

    }
    @GetMapping("/updateFilename")
    public ResultBean updateFilename(String path,String oldName,String newName,Integer driverId) {
        /*System.out.println("========params=======");
        System.out.println(path);
        System.out.println(oldName);
        System.out.println(newName);
        System.out.println(driverId);*/
        List<StorageConfig> byDriveId = storageConfigRepository.findByDriveId(driverId);
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
    @GetMapping("/admin/updateFilename")
    public ResultBean updateFilenameAdmin(String path,String oldName,String newName) {
        /*System.out.println("========params=======");
        System.out.println(path);
        System.out.println(oldName);
        System.out.println(newName);
        System.out.println(driverId);*/
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
    public ResultBean deleteFile(String path,String name,Integer driverId) {
        List<StorageConfig> byDriveId = storageConfigRepository.findByDriveId(driverId);
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
    @GetMapping("/admin/deleteFile")
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


    @PostMapping("/uploadFile")
    public ResultBean uploadFile(HttpServletRequest request) {
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
    /**
     * 校验密码
     * @param   fileItemList
     *          文件列表
     * @param   driveId
     *          驱动器 ID
     * @param   path
     *          请求路径
     * @param   inputPassword
     *          用户输入的密码
     * @return 是否校验通过
     */
    private VerifyResult verifyPassword(List<FileItemDTO> fileItemList, Integer driveId, String path, String inputPassword) {

        AbstractBaseFileService fileService = driveContext.get(driveId);

        for (FileItemDTO fileItemDTO : fileItemList) {
            if (ZFileConstant.PASSWORD_FILE_NAME.equals(fileItemDTO.getName())) {
                String expectedPasswordContent;
                try {
                    expectedPasswordContent = HttpUtil.getTextContent(fileItemDTO.getUrl());
                } catch (HttpClientErrorException httpClientErrorException) {
                    log.trace("尝试重新获取密码文件缓存中链接后仍失败, driveId: {}, path: {}, inputPassword: {}, passwordFile:{} ",
                            driveId, path, inputPassword, JSON.toJSONString(fileItemDTO), httpClientErrorException);
                    try {
                        String pwdFileFullPath = StringUtils.removeDuplicateSeparator(fileItemDTO.getPath() + ZFileConstant.PATH_SEPARATOR + fileItemDTO.getName());
                        FileItemDTO pwdFileItem = fileService.getFileItem(pwdFileFullPath);
                        expectedPasswordContent = HttpUtil.getTextContent(pwdFileItem.getUrl());
                    } catch (Exception e) {
                        throw new PasswordVerifyException("此文件夹未加密文件夹, 但密码检查异常, 请联系管理员检查密码设置", e);
                    }
                }

                if (matchPassword(expectedPasswordContent, inputPassword)) {
                    break;
                }

                if (StrUtil.isEmpty(inputPassword)) {
                    return VerifyResult.fail("此文件夹需要密码.", ResultBean.REQUIRED_PASSWORD);
                }
                return VerifyResult.fail("密码错误.", ResultBean.INVALID_PASSWORD);
            }
        }

        return VerifyResult.success();
    }


    /**
     * 校验两个密码是否相同, 忽略空白字符
     *
     * @param   expectedPasswordContent
     *          预期密码
     *
     * @param   password
     *          实际输入密码
     *
     * @return  是否匹配
     */
    private boolean matchPassword(String expectedPasswordContent, String password) {
        if (Objects.equals(expectedPasswordContent, password)) {
            return true;
        }

        if (expectedPasswordContent == null) {
            return false;
        }

        if (password == null) {
            return false;
        }

        expectedPasswordContent = expectedPasswordContent.replace("\n", "").trim();
        password = password.replace("\n", "").trim();
        return Objects.equals(expectedPasswordContent, password);
    }


    /**
     * 过滤文件列表, 去除密码, 文档文件和此驱动器通过规则过滤的文件.
     *
     * @param   fileItemList
     *          文件列表
     * @param   driveId
     *          驱动器 ID
     */
    private void filterFileList(List<FileItemDTO> fileItemList, Integer driveId) {
        if (fileItemList == null) {
            return;
        }

        fileItemList.removeIf(
                fileItem -> ZFileConstant.PASSWORD_FILE_NAME.equals(fileItem.getName())
                        || ZFileConstant.README_FILE_NAME.equals(fileItem.getName())
                        || filterConfigService.filterResultIsHidden(driveId, StringUtils.concatUrl(fileItem.getPath(), fileItem.getName()))
        );
    }

}