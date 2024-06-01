package com.alcedo.file.upload.domain.vo;

import lombok.Data;

/**
 * @ClassName: DiskFileIndexVo
 * @Author:  Alcedo
 * @CreateTime: 2022-03-08
 * @Description:
 */
@Data
public class PartFileIndexVo {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件md5值
     */
    private String fileMd5;

    /**
     * 文件服务器索引
     */
    private String fileIndex;

    /**
     * 需要上下传文件uid
     */
    private String fileUid;

    /**
     * 当前分片
     */
    private Integer partIndex;

    /**
     * 总分片
     */
    private Integer partTotalNum;

}
