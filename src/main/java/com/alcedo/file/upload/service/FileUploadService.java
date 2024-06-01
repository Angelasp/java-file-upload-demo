package com.alcedo.file.upload.service;


import com.alcedo.file.upload.utils.ResultEntity;
import com.alcedo.file.upload.domain.vo.DiskFileIndexVo;
import com.alcedo.file.upload.domain.vo.PointFileIndexVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @ClassName: FileUploadService
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description:
 */
public interface FileUploadService {

    ResultEntity<Boolean> singleFileUpload(MultipartFile file);

    ResultEntity<Boolean> multipleFileUpload(MultipartFile[] files);

    ResultEntity<Boolean> singleFilePartUpload(MultipartFile filePart, Integer partIndex, Integer partNum, String fileName, String fileUid);

    ResultEntity<String> multipleFilePartUpload(MultipartFile filePart, Integer partIndex, Integer partNum, String fileName, String fileUid);

    ResultEntity<String> multipleFilePartFlashUpload(MultipartFile filePart, String fileInfo, String fileOther);

    ResultEntity<List<DiskFileIndexVo>> checkDiskFile(List<DiskFileIndexVo> upLoadFileListMd5);

    ResultEntity<PointFileIndexVo> checkUploadFileIndex(PointFileIndexVo pointFileIndexVo);

    ResultEntity<String> singleFilePartPointUpload(MultipartFile filePart, String fileInfo);
}
