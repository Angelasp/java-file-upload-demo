package com.alcedo.file.upload.service.impl;

import com.alcedo.file.upload.config.MinioUtil;
import com.alcedo.file.upload.utils.ResultEntity;
import com.alcedo.file.upload.utils.Uuid;
import com.alcedo.file.upload.domain.vo.PartFileIndexVo;
import com.alcedo.file.upload.service.FileUploadMinioService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName: FileUploadMinioServiceImpl
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description:
 */
@Slf4j
@Service
public class FileUploadMinioServiceImpl implements FileUploadMinioService {

    @Autowired
    private MinioUtil minioUtil;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public ResultEntity<Boolean> fileUploadMinio(MultipartFile file) {
        String objectName = Uuid.generateUniqueId(8) + "_" + file.getOriginalFilename();

        try {
            Boolean aBoolean = minioUtil.uploadFile(file, "test", objectName);
            log.info("文件上传是否完成：{}", aBoolean);
            return ResultEntity.success(aBoolean, "文件上传完成！");
        } catch (Exception e) {
            log.info("文件上传异常！", e);
            return ResultEntity.error(false, "文件上传异常！");
        }
    }

    @Override
    public ResultEntity<Boolean> filePartUploadMinio(MultipartFile file, String partFile, String bucketName) {

        try {
            PartFileIndexVo partFileIndexVo = JSONObject.parseObject(partFile, PartFileIndexVo.class);
            String objectName = "temp/" + partFileIndexVo.getFileUid() + "/"
                    + partFileIndexVo.getFileName().substring(0, partFileIndexVo.getFileName().lastIndexOf(".")) + "_"
                    + partFileIndexVo.getPartIndex() + ".temp";
            //上传文件分片
            Boolean partState = minioUtil.uploadFile(file, bucketName, objectName);

            if (partState) {

                Long result = redisTemplate.opsForValue().increment("partCount:" + partFileIndexVo.getFileUid());

                if (result.equals(partFileIndexVo.getPartTotalNum().longValue())) {

                    log.info("{}:开始合并分片请求...", partFileIndexVo.getFileName());
                    //开始合并分片
                    Boolean complete = minioUtil.uploadFileComplete(bucketName, "temp/" + partFileIndexVo.getFileUid(),
                            partFileIndexVo.getFileName(), partFileIndexVo.getPartTotalNum());
                    //移除文件分片上传情况，这个也应该在redis中
                    Boolean delete = false;
                    if (complete) {
                        delete = redisTemplate.delete("partCount:" + partFileIndexVo.getFileUid());
                        log.info("{}:分片文件合并完成！", partFileIndexVo.getFileName());
                    }
                    return ResultEntity.success(complete && delete, partFileIndexVo.getFileUid());
                }
            }
            return ResultEntity.success(partState, partFileIndexVo.getPartIndex().toString());
        } catch (Exception e) {
            log.info("文件上传异常！", e);
            return ResultEntity.error("文件上传异常！");
        }
    }

}
