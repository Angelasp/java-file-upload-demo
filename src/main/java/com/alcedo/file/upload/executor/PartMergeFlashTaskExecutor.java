package com.alcedo.file.upload.executor;

import com.alcedo.file.upload.domain.vo.DiskFileIndexVo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @ClassName: ScheduledTask
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description: 多文件(分片)秒传，已存在分片和上传分片文件合并写入执行器
 */
@Slf4j
public class PartMergeFlashTaskExecutor implements Callable<Integer> {

    private String filePath;

    private String tempPath;

    private DiskFileIndexVo upFileInfo;

    private List<DiskFileIndexVo> diskFileIndexVos;

    public PartMergeFlashTaskExecutor() {
        super();
    }

    public PartMergeFlashTaskExecutor(String filePath, DiskFileIndexVo upFileInfo, List<DiskFileIndexVo> diskFileIndexVos) {
        this.filePath = filePath;
        this.tempPath = filePath;
        this.upFileInfo = upFileInfo;
        this.diskFileIndexVos = diskFileIndexVos;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("当前正在合并的文件是：" + upFileInfo.getFileName());
        InputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        Integer partSizeTotal = 0;
        try {
            fileOutputStream = new FileOutputStream(filePath + upFileInfo.getFileName());

            for (int i = 0; i < upFileInfo.getPartNum(); i++) {
                int index = i;
                //获取是上传的临时文件还是服务器已有的临时文件
                if (diskFileIndexVos.size() != 0 && diskFileIndexVos.stream().anyMatch(d -> d.getPartIndex() == index)) {
                    DiskFileIndexVo diskFileIndexVo = diskFileIndexVos.stream().filter(d -> d.getPartIndex() == index).findFirst().get();
                    tempPath = filePath + "part\\" + diskFileIndexVo.getFileIndex();
                } else {
                    tempPath = filePath + "temp\\" + upFileInfo.getFileUid() + "\\" + upFileInfo.getFileName() + "_" + i + ".part";
                }

                fileInputStream = new FileInputStream(tempPath);
                byte[] buf = new byte[1024 * 8];//8MB
                int length;
                while ((length = fileInputStream.read(buf)) != -1) {//读取fis文件输入字节流里面的数据
                    fileOutputStream.write(buf, 0, length);//通过fos文件输出字节流写出去
                    partSizeTotal += length;
                }
                fileInputStream.close();
            }

            tempPath = filePath + "temp\\" + upFileInfo.getFileUid() + "\\";

            //todo 这里可以校验一下文件是否合并完成,并将本次所上传的分片且服务器路径没有的分片文件保存一下;然后在删除临时分片文件

            // 删除临时文件夹里面的分片文件 如果使用流操作且没有关闭输入流，可能导致删除失败
            for (int i = 0; i < upFileInfo.getPartNum(); i++) {
                int index = i;
                if (diskFileIndexVos.stream().noneMatch(d -> d.getPartIndex() == index)) {
                    boolean delete = new File(tempPath + upFileInfo.getFileName() + "_" + i + ".part").delete();
                    File file = new File(tempPath + upFileInfo.getFileName() + "_" + i + ".part");
//                    System.out.println(i + "; 是否删除：" + delete + " ; 是否还存在：" + file.exists());
                }
            }
            //在删除对应的临时文件夹
            File tempDir = new File(tempPath);
            if (tempDir.listFiles().length == 0) {
               tempDir.delete();
            }
        } catch (Exception e) {
            log.error("{}:文件分片合并失败!", upFileInfo.getFileName(), e);
            throw new Exception(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                log.error("{} 文件分片合并完成后,关闭输入输出流错误！", upFileInfo.getFileName(), e);
                e.printStackTrace();
            }
        }
        return partSizeTotal;
    }
}
