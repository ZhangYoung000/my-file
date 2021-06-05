package cn.zyity.zfile.model.support;

import cn.zyity.zfile.model.dto.FileItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 */
@Data
@AllArgsConstructor
public class FilePageModel {

    private int totalPage;

    private List<FileItemDTO> fileList;

}