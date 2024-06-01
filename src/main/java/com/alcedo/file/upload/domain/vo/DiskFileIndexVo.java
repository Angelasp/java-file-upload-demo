package com.alcedo.file.upload.domain.vo;

import lombok.Data;

/**
 * @ClassName: DiskFileIndexVo
 * @Author:  Alcedo
 * @CreateTime: 2022-03-08
 * @Description:
 */
@Data
public class DiskFileIndexVo {

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

    private Integer partIndex;

    private Integer partNum;

}
