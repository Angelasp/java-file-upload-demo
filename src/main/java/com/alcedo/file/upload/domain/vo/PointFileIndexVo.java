package com.alcedo.file.upload.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName: PointFileIndexVo
 * @Author:  Alcedo
 * @CreateTime: 2022-03-08
 * @Description:
 */
@Data
public class PointFileIndexVo {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件md5值
     */
    private String fileMd5;

    /**
     * 文件大小
     */
    private String fileSize;

    /**
     * 需要上下传文件uid
     */
    private String fileUid;

    private Integer partIndex;

    private Integer partNum;

    private List<String> parts;

}
