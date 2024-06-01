package com.alcedo.file.upload.executor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @ClassName: ScheduledTask
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description: 多文件分片上传，分片文件合并写入执行器
 */
@Slf4j
public class PartMergeTaskExecutor implements Callable<Integer> {

    private String filePath;

    private String tempPath;

    private String fileName;

    private Integer partNum;

    public PartMergeTaskExecutor() {
        super();
    }

    public PartMergeTaskExecutor(String filePath, String tempPath, String fileName, Integer partNum) {
        this.filePath = filePath;
        this.tempPath = tempPath;
        this.fileName = fileName;
        this.partNum = partNum;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("当前正在合并的文件是：" + fileName);
        InputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        Integer partSizeTotal = 0;
        try {
            fileOutputStream = new FileOutputStream(filePath + fileName);

            for (int i = 0; i < partNum; i++) {
                //读取分片文件
                fileInputStream = new FileInputStream(tempPath + "\\" + fileName + "_" + i + ".part");
                byte[] buf = new byte[1024 * 8];//8MB
                int length;
                while ((length = fileInputStream.read(buf)) != -1) {//读取fis文件输入字节流里面的数据
                    fileOutputStream.write(buf, 0, length);//通过fos文件输出字节流写出去
                    partSizeTotal += length;
                }
                fileInputStream.close();
            }

            //todo 这里可以校验一下文件是否合并完成,然后在删除临时分片文件

            // 删除临时文件夹里面的分片文件 如果使用流操作且没有关闭输入流，可能导致删除失败
            for (int i = 0; i < partNum; i++) {
                boolean delete = new File(tempPath + "\\" + fileName + "_" + i + ".part").delete();
                File file = new File(tempPath + "\\" + fileName + "_" + i + ".part");
//            System.out.println(i + "; 是否删除：" + delete + " ; 是否还存在：" + file.exists());
            }
            //在删除对应的临时文件夹
            File tempDir = new File(tempPath);
            if (Objects.requireNonNull(tempDir.listFiles()).length == 0) {
                boolean delete = tempDir.delete();
                //System.out.println("文件夹: " + tempPath + ";是否删除: " + delete);
            }
        } catch (Exception e) {
            log.error("{}:文件分片合并失败!", fileName, e);
            throw new Exception(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                log.error("{} 文件分片合并完成后,关闭输入输出流错误！", fileName, e);
                e.printStackTrace();
            }
        }
        return partSizeTotal;
    }
}
