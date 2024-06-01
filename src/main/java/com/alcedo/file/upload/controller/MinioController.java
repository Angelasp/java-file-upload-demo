package com.alcedo.file.upload.controller;

import com.alcedo.file.upload.service.FileUploadMinioService;
import com.alcedo.file.upload.utils.ResultEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName: MinioController
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/minio")
@CrossOrigin
@ConditionalOnProperty(prefix = "autoconfigure", value = "isMinio", matchIfMissing = false)
public class MinioController {

    @Autowired
    private FileUploadMinioService fileUploadMinioService;

    @Value("${minio.config.bucketName}")
    private String bucketName;

    /**
     * 单文件上传
     *
     * @param file
     */
    @PostMapping("/singleFileUploadMinio")
    public ResultEntity<Boolean> singleFileUploadMinio(@RequestParam("file") MultipartFile file) {
        log.info("minio单文件上传操作,文件大小为:{}", file.getSize());
        ResultEntity<Boolean> result = fileUploadMinioService.fileUploadMinio(file);
        return result;
    }

    /**
     * 文件分片上传
     *
     * @param file
     */
    @PostMapping("/partFileUploadMinio")
    public ResultEntity<Boolean> partFileUploadMinio(@RequestParam("file") MultipartFile file, @RequestParam("partFileIndex") String partFileIndexVo) {
        log.info("minio文件分片上传操作,文件大小为:{}", partFileIndexVo);
        ResultEntity<Boolean> result = fileUploadMinioService.filePartUploadMinio(file, partFileIndexVo, bucketName);
        return result;
    }

    /**
     * 测试接口
     */
    @PostMapping("/fileTest")
    public ResultEntity<String> fileTest() {
        log.info("测试接口");
        return ResultEntity.success("测试通过");
    }
}
