package com.alcedo.file.upload.controller;

import com.alcedo.file.upload.domain.vo.DiskFileIndexVo;
import com.alcedo.file.upload.domain.vo.PointFileIndexVo;
import com.alcedo.file.upload.service.FileUploadService;
import com.alcedo.file.upload.utils.ResultEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @ClassName: FileUploadController
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/fileUpload")
@CrossOrigin
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 单文件上传
     *
     * @param file
     */
    @PostMapping("/singleFileUpload")
    public ResultEntity<Boolean> singleFileUpload(@RequestParam("file") MultipartFile file) {
        log.info("单文件上传操作,文件大小为:{}", file.getSize());
        ResultEntity<Boolean> result = fileUploadService.singleFileUpload(file);
        return result;
    }

    /**
     * 多文件上传
     *
     * @param files
     */
    @PostMapping("/multipleFileUpload")
    public ResultEntity multipleFileUpload(@RequestParam("files") MultipartFile[] files) {
        log.info("多文件上传操作,文件个数:{}", files.length);
        ResultEntity result = fileUploadService.multipleFileUpload(files);
        return result;
    }

    /**
     * 单文件分片上传
     *
     * @param filePart  单次分片的文件
     * @param partIndex 当前分片定位
     * @param partNum   文件分片总数
     * @param fileName  单次分片的文件名称
     */
    @PostMapping("/singleFilePartUpload")
    public ResultEntity<Boolean> singleFilePartUpload(@RequestParam("filePart") MultipartFile filePart,
                                                @RequestParam("partIndex") Integer partIndex,
                                                @RequestParam("partNum") Integer partNum,
                                                @RequestParam("fileName") String fileName,
                                                @RequestParam("fileUid") String fileUid) {
        log.info("单文件分片上传,总片数:{},分片数:{},文件名:{},大小:{},uid:{}", partNum, partIndex, fileName, filePart.getSize(), fileUid);
        ResultEntity<Boolean> result = fileUploadService.singleFilePartUpload(filePart, partIndex, partNum, fileName, fileUid);
        return result;
    }

    /**
     * 多文件分片上传
     *
     * @param filePart  单次分片的文件
     * @param partIndex 当前分片定位
     * @param partNum   文件分片总数
     * @param fileName  单次分片的文件名称
     */
    @PostMapping("/multipleFilePartUpload")
    public ResultEntity<String> multipleFilePartUpload(@RequestParam("filePart") MultipartFile filePart,
                                                 @RequestParam("partIndex") Integer partIndex,
                                                 @RequestParam("partNum") Integer partNum,
                                                 @RequestParam("fileName") String fileName,
                                                 @RequestParam("fileUid") String fileUid) {
        log.info("多文件分片上传,总片数:{},分片数:{},文件名:{},大小:{},uid:{}", partNum, partIndex, fileName, filePart.getSize(), fileUid);
        ResultEntity<String> result = fileUploadService.multipleFilePartUpload(filePart, partIndex, partNum, fileName, fileUid);
        return result;
    }

    /**
     * 多文件(分片)秒传
     *
     * @param filePart  单次分片的文件
     * @param fileInfo  当前分片相关信息
     * @param fileOther 所有不需要上传的文件信息，包括文件索引
     */
    @PostMapping("/multipleFilePartFlashUpload")
    public ResultEntity<String> multipleFilePartFlashUpload(@RequestParam("filePart") MultipartFile filePart,
                                                      @RequestParam("fileInfo") String fileInfo,
                                                      @RequestParam("fileOther") String fileOther) {
        log.info("多文件(分片)秒传,文件大小:{};文件信息:{};其他信息:{}", filePart.getSize(), fileInfo, fileOther);

        ResultEntity<String> result = fileUploadService.multipleFilePartFlashUpload(filePart, fileInfo, fileOther);
        return result;
    }

    /**
     * 单文件(分片)断点上传
     *
     * @param filePart 单次分片的文件
     * @param fileInfo 当前相关信息
     */
    @PostMapping("/singleFilePartPointUpload")
    public ResultEntity<String> singleFilePartPointUpload(@RequestParam("filePart") MultipartFile filePart,
                                                    @RequestParam("fileInfo") String fileInfo) {
        ResultEntity<String> result = fileUploadService.singleFilePartPointUpload(filePart, fileInfo);
        return result;
    }

    /**
     * 检测秒传文件上传服务器中存在的文件，即不需要上传的文件
     */
    @PostMapping("/checkDiskFile")
    public ResultEntity<List<DiskFileIndexVo>> checkDiskFile(@RequestBody List<DiskFileIndexVo> upLoadFileListMd5) {
        log.info("检测服务器磁盘文件和需要上传的md5值");
        ResultEntity<List<DiskFileIndexVo>> result = fileUploadService.checkDiskFile(upLoadFileListMd5);
        return result;
    }

    /**
     * 检测断点上传文件在务器中上传进度
     */
    @PostMapping("/checkUploadFileIndex")
    public ResultEntity<PointFileIndexVo> checkUploadFileIndex(@RequestBody PointFileIndexVo pointFileIndexVo) {
        log.info("检测服务器中文件上传进度，{}", pointFileIndexVo);
        ResultEntity<PointFileIndexVo> result = fileUploadService.checkUploadFileIndex(pointFileIndexVo);
        return result;
    }

    /**
     * 测试接口
     */
    @PostMapping("/test")
    public ResultEntity<String> test() {
        log.info("测试接口");
        return ResultEntity.success("测试通过");
    }
}
