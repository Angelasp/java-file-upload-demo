package com.alcedo.file.upload.service;

import com.alcedo.file.upload.utils.ResultEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName: FileUploadMinioService
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description:
 */
public interface FileUploadMinioService {

    ResultEntity<Boolean> fileUploadMinio(MultipartFile file);

    ResultEntity<Boolean> filePartUploadMinio(MultipartFile filePart, String partFileIndexVo,String bucketName);
}
