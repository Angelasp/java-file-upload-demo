package com.alcedo.file.upload.service.impl;

import com.alcedo.file.upload.executor.MultipleFileTaskExecutor;
import com.alcedo.file.upload.executor.PartMergeFlashTaskExecutor;
import com.alcedo.file.upload.executor.PartMergeTaskExecutor;
import com.alcedo.file.upload.service.FileUploadService;
import com.alcedo.file.upload.utils.ResultEntity;
import com.alcedo.file.upload.domain.vo.DiskFileIndexVo;
import com.alcedo.file.upload.domain.vo.PointFileIndexVo;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName: FileUploadServiceImpl
 * @Author:  Alcedo
 * @CreateTime: 2023-05-16
 * @Description:
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private final Map<String, Integer> isMergePart = new HashMap<>();

    private final Map<String, List<String>> uploadProgress = new HashMap<>();

    private static List<DiskFileIndexVo> diskFileIndexVos;

    ThreadPoolExecutor partMergeTask = new ThreadPoolExecutor(10, 15,
            30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4));

    /**
     * 单文件上传
     * 直接将传入的文件通过io流形式直接写入(服务器)指定路径下
     *
     * @param file 上传的文件
     * @return
     */
    @Override
    public ResultEntity<Boolean> singleFileUpload(MultipartFile file) {
        //实际情况下，这些路径都应该是服务器上面存储文件的路径
        String filePath = System.getProperty("user.dir") + "\\file\\";
        File dir = new File(filePath);
        if (!dir.exists()) dir.mkdir();

        if (file == null) {
            return ResultEntity.error(false, "上传文件为空！");
        }
        InputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            String filename = file.getOriginalFilename();
            fileOutputStream = new FileOutputStream(filePath + filename);
            fileInputStream = file.getInputStream();

            byte[] buf = new byte[1024 * 8];
            int length;
            while ((length = fileInputStream.read(buf)) != -1) {//读取fis文件输入字节流里面的数据
                fileOutputStream.write(buf, 0, length);//通过fos文件输出字节流写出去
            }
            log.info("单文件上传完成！文件路径:{},文件名:{},文件大小:{}", filePath, filename, file.getSize());
            return ResultEntity.success(true, "单文件上传完成！");
        } catch (IOException e) {
            return ResultEntity.error(true, "单文件上传失败！");
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream.flush();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 多文件上传
     * 直接将传入的多个文件通过io流形式直接写入(服务器)指定路径下
     * 写入指定路径下是通过多线程进行文件写入的，文件写入线程执行功能就和上面单文件写入是一样的
     *
     * @param files 上传的所有文件
     * @return
     */
    @Override
    public ResultEntity<Boolean> multipleFileUpload(MultipartFile[] files) {
        //实际情况下，这些路径都应该是服务器上面存储文件的路径
        String filePath = System.getProperty("user.dir") + "\\file\\";
        File dir = new File(filePath);
        if (!dir.exists()) dir.mkdir();

        if (files.length == 0) {
            return ResultEntity.error(false, "上传文件为空！");
        }
        ArrayList<String> uploadFiles = new ArrayList<>();
        try {

            ArrayList<Future<String>> futures = new ArrayList<>();
            //使用多线程来完成对每个文件的写入
            for (MultipartFile file : files) {
                futures.add(partMergeTask.submit(new MultipleFileTaskExecutor(filePath, file)));
            }

            //这里主要用于监听各个文件写入线程是否执行结束
            int count = 0;
            while (count != futures.size()) {
                for (Future<String> future : futures) {
                    if (future.isDone()) {
                        uploadFiles.add(future.get());
                        count++;
                    }
                }
                Thread.sleep(1);
            }
            log.info("多文件上传完成！文件路径:{},文件信息:{}", filePath, uploadFiles);
            return ResultEntity.success(true, "多文件上传完成！");
        } catch (Exception e) {
            log.error("多文件分片上传失败!", e);
            return ResultEntity.error(true, "多文件上传失败！");
        }

    }

    /**
     * 单文件分片上传
     * 直接将传入的文件分片通过io流形式写入(服务器)指定临时路径下
     * 然后判断是否分片都上传完成，如果所有分片都上传完成的话，就把临时路径下的分片文件通过流形式读入合并并从新写入到(服务器)指定文件路径下
     * 最后删除临时文件和临时文件夹，临时文件夹是通过文件的uuid进行命名的
     *
     * @param filePart  分片文件
     * @param partIndex 当前分片值
     * @param partNum   所有分片数
     * @param fileName  当前文件名称
     * @param fileUid   当前文件uuid
     * @return
     */
    @Override
    public ResultEntity<Boolean> singleFilePartUpload(MultipartFile filePart, Integer partIndex, Integer partNum, String fileName, String fileUid) {
        //实际情况下，这些路径都应该是服务器上面存储文件的路径
        String filePath = System.getProperty("user.dir") + "\\file\\";//文件存放路径
        String tempPath = filePath + "temp\\" + fileUid;//临时文件存放路径
        File dir = new File(tempPath);
        if (!dir.exists()) dir.mkdirs();

        //生成一个临时文件名
        String tempFileNamePath = tempPath + "\\" + fileName + "_" + partIndex + ".part";
        try {
            //将分片存储到临时文件夹中
            filePart.transferTo(new File(tempFileNamePath));

            File tempDir = new File(tempPath);
            File[] tempFiles = tempDir.listFiles();

            one:
            if (partNum.equals(Objects.requireNonNull(tempFiles).length)) {
                //需要校验一下,表示已有异步程序正在合并了;如果是分布式这个校验可以加入redis的分布式锁来完成
                if (isMergePart.get(fileUid) != null) {
                    break one;
                }
                isMergePart.put(fileUid, tempFiles.length);
                System.out.println("所有分片上传完成，预计总分片：" + partNum + "; 实际总分片:" + tempFiles.length);

                FileOutputStream fileOutputStream = new FileOutputStream(filePath + fileName);
                //这里如果分片很多的情况下，可以采用多线程来执行
                for (int i = 0; i < partNum; i++) {
                    //读取分片数据，进行分片合并
                    FileInputStream fileInputStream = new FileInputStream(tempPath + "\\" + fileName + "_" + i + ".part");
                    byte[] buf = new byte[1024 * 8];//8MB
                    int length;
                    while ((length = fileInputStream.read(buf)) != -1) {//读取fis文件输入字节流里面的数据
                        fileOutputStream.write(buf, 0, length);//通过fos文件输出字节流写出去
                    }
                    fileInputStream.close();
                }
                fileOutputStream.flush();
                fileOutputStream.close();

                // 删除临时文件夹里面的分片文件 如果使用流操作且没有关闭输入流，可能导致删除失败
                for (int i = 0; i < partNum; i++) {
                    boolean delete = new File(tempPath + "\\" + fileName + "_" + i + ".part").delete();
                    File file = new File(tempPath + "\\" + fileName + "_" + i + ".part");
//                    System.out.println(i + " ; 是否删除：" + delete + " ; 是否还存在：" + file.exists());
                }
                //在删除对应的临时文件夹
                if (Objects.requireNonNull(tempDir.listFiles()).length == 0) {
                    boolean delete = tempDir.delete();
//                    System.out.println("文件夹 " + tempPath + "是否删除：" + delete);
                }
                isMergePart.remove(fileUid);
            }

        } catch (Exception e) {
            log.error("单文件分片上传失败!", e);
            return ResultEntity.error(false, "单文件分片上传失败");
        }
        //通过返回成功的分片值，来验证分片是否有丢失
        return ResultEntity.success(true, partIndex.toString());
    }

    /**
     * 多文件分片上传
     * 先将所有文件分片读入到(服务器)指定临时路径下，每个文件的分片文件的临时文件夹都是已文件的uuid进行命名的
     * 然后判断对已经上传所有分片的文件进行合并，此处是通过多线程对每一个文件的分片文件进行合并的
     * 最后对已经合并完成的分片临时文件和文件夹进行删除
     *
     * @param filePart  分片文件
     * @param partIndex 当前分片值
     * @param partNum   总分片数
     * @param fileName  当前文件名称
     * @param fileUid   当前文件uuid
     * @return
     */
    @Override
    public ResultEntity<String> multipleFilePartUpload(MultipartFile filePart, Integer partIndex, Integer partNum, String fileName, String fileUid) {
        //实际情况下，这些路径都应该是服务器上面存储文件的路径
        String filePath = System.getProperty("user.dir") + "\\file\\";//文件存放路径
        String tempPath = filePath + "temp\\" + fileUid;//临时文件存放路径
        File dir = new File(tempPath);
        if (!dir.exists()) dir.mkdirs();
        //生成一个临时文件名
        String tempFileNamePath = tempPath + "\\" + fileName + "_" + partIndex + ".part";
        try {
            filePart.transferTo(new File(tempFileNamePath));

            File tempDir = new File(tempPath);
            File[] tempFiles = tempDir.listFiles();
            //如果临时文件夹中分片数量和实际分片数量一致的时候，就需要进行分片合并
            one:
            if (partNum.equals(tempFiles.length)) {
                //需要校验一下,表示已有异步程序正在合并了;如果是分布式这个校验可以加入redis的分布式锁来完成
                if (isMergePart.get(fileUid) != null) {
                    break one;
                }
                isMergePart.put(fileUid, tempFiles.length);
                System.out.println(fileName + ":所有分片上传完成，预计总分片：" + partNum + "; 实际总分片:" + tempFiles.length);

                //使用多线程来完成对每个文件的合并
                Future<Integer> submit = partMergeTask.submit(new PartMergeTaskExecutor(filePath, tempPath, fileName, partNum));
                System.out.println("上传文件名:" + fileName + "; 总大小：" + submit.get());
                isMergePart.remove(fileUid);
            }
        } catch (Exception e) {
            log.error("{}:多文件分片上传失败!", fileName, e);
            return ResultEntity.error("", "多文件分片上传失败");
        }
        //通过返回成功的分片值，来验证分片是否有丢失
        return ResultEntity.success(partIndex.toString(), fileUid);
    }

    /**
     * 多文件(分片)秒传
     * 通过对比已有的文件分片md5值和需要上传文件分片的MD5值，
     * 在文件分片合并的时候，对已有的文件进行地址索引，对没有的文件进行临时文件写入
     * 最后合并的时候根据不同的文件分片进行文件读取写入
     *
     * @param filePart  上传没有的分片文件
     * @param fileInfo  当前分片文件相关信息
     * @param fileOther 已存在文件分片相关信息
     * @return
     */
    @Override
    public ResultEntity<String> multipleFilePartFlashUpload(MultipartFile filePart, String fileInfo, String fileOther) {
        DiskFileIndexVo upFileInfo = JSONObject.parseObject(fileInfo, DiskFileIndexVo.class);
        List<DiskFileIndexVo> notUpFileInfoList = JSON.parseArray(fileOther, DiskFileIndexVo.class);
        //实际情况下，这些路径都应该是服务器上面存储文件的路径
        String filePath = System.getProperty("user.dir") + "\\file\\";//文件存放路径
        //正常情况下，这个临时文件也应该放入(服务器)非临时文件夹中，这样方便下次其他文件上传查找是否曾经上传过类似的
        //当前demo是单独存放在临时文件夹中，文件合并完成之后直接删除的
        String tempPath = filePath + "temp\\" + upFileInfo.getFileUid();//临时文件存放路径

        File dir = new File(tempPath);
        if (!dir.exists()) dir.mkdirs();
        //生成一个临时文件名
        String tempFileNamePath = tempPath + "\\" + upFileInfo.getFileName() + "_" + upFileInfo.getPartIndex() + ".part";

        try {
            filePart.transferTo(new File(tempFileNamePath));

            File tempDir = new File(tempPath);
            File[] tempFiles = tempDir.listFiles();
            notUpFileInfoList = notUpFileInfoList.stream().filter(e ->
                    upFileInfo.getFileUid().equals(e.getFileUid())).collect(Collectors.toList());
            //如果临时文件夹中分片数量和实际分片数量一致的时候，就需要进行分片合并
            one:
            if ((upFileInfo.getPartNum() - notUpFileInfoList.size()) == tempFiles.length) {
                //需要校验一下,表示已有异步程序正在合并了;如果是分布式这个校验可以加入redis的分布式锁来完成
                if (isMergePart.get(upFileInfo.getFileUid()) != null) {
                    break one;
                }
                isMergePart.put(upFileInfo.getFileUid(), tempFiles.length);
                System.out.println(upFileInfo.getFileName() + ":所有分片上传完成，预计总分片：" + upFileInfo.getPartNum()
                        + "; 实际总分片:" + tempFiles.length + "; 已存在分片数:" + notUpFileInfoList.size());

                //使用多线程来完成对每个文件的合并
                Future<Integer> submit = partMergeTask.submit(
                        new PartMergeFlashTaskExecutor(filePath, upFileInfo, notUpFileInfoList));
//                System.out.println("上传文件名:" + upFileInfo.getFileName() + "; 总大小：" + submit.get());
                isMergePart.remove(upFileInfo.getFileUid());
            }
        } catch (Exception e) {
            log.error("{}:多文件(分片)秒传失败!", upFileInfo.getFileName(), e);
            return ResultEntity.error("", "多文件(分片)秒传失败！");
        }
        //通过返回成功的分片值，来验证分片是否有丢失
        return ResultEntity.success(upFileInfo.getPartIndex().toString(), upFileInfo.getFileUid());
    }

    /**
     * 根据传入需要上传的文件片段的md5值来对比服务器中的文件的md5值,将已有对应的md5值的文件过滤出来，
     * 通知前端或者自行出来这些文件，即为不需要上传的文件分片，并将已有的文件分片地址索引返回给前端进行出来
     *
     * @param upLoadFileListMd5 原本需要上传文件的索引分片信息
     * @return
     */
    @Override
    public ResultEntity<List<DiskFileIndexVo>> checkDiskFile(List<DiskFileIndexVo> upLoadFileListMd5) {
//        upLoadFileListMd5.forEach(System.out::println);
        List<DiskFileIndexVo> notUploadFile;
        try {
            //后端服务器已经存在的分片md5值集合
//            List<DiskFileIndexVo> diskFileMd5IndexList = getDiskFileMd5Index();
            List<DiskFileIndexVo> diskFileMd5IndexList = diskFileIndexVos;

            notUploadFile = upLoadFileListMd5.stream().filter(uf -> diskFileMd5IndexList.stream().anyMatch(
                    df -> {
                        if (df.getFileMd5().equals(uf.getFileMd5())) {
                            uf.setFileIndex(df.getFileName());//不需要上传文件的地址索引
                            return true;
                        }
                        return false;
                    })).collect(Collectors.toList());
//            notUploadFile = notUploadFile.stream().collect(Collectors.collectingAndThen(
//                    Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DiskFileIndexVo::getFileMd5))), ArrayList::new));

            log.info("过滤出不需要上传的文件分片:{}", notUploadFile);
        } catch (Exception e) {
            log.error("上传文件检测异常!", e);
            return ResultEntity.error("上传文件检测异常！");
        }
        return ResultEntity.success(notUploadFile);
    }

    /**
     * 根据文件uuid(md5生成的)来判断此文件在服务器中是否未上传完整，
     * 如果没上传完整，则返回相关上传进度等信息
     *
     * @param pointFileIndexVo
     * @return
     */
    @Override
    public ResultEntity<PointFileIndexVo> checkUploadFileIndex(PointFileIndexVo pointFileIndexVo) {
        try {
            List<String> list = uploadProgress.get(pointFileIndexVo.getFileMd5());
            if (list == null) list = new ArrayList<>();
            pointFileIndexVo.setParts(list);
            System.out.println("已上传部分：" + list);
            return ResultEntity.success(pointFileIndexVo);
        } catch (Exception e) {
            log.error("上传文件检测异常!", e);
            return ResultEntity.error("上传文件检测异常！");
        }
    }

    /**
     * 单文件(分片)断点上传
     *
     * @param filePart 需要上传的分片文件
     * @param fileInfo 当前需要上传的分片文件信息，如uuid，文件名，文件总分片数量等
     * @return
     */
    @Override
    public ResultEntity<String> singleFilePartPointUpload(MultipartFile filePart, String fileInfo) {
        PointFileIndexVo pointFileIndexVo = JSONObject.parseObject(fileInfo, PointFileIndexVo.class);
        //实际情况下，这些路径都应该是服务器上面存储文件的路径
        String filePath = System.getProperty("user.dir") + "\\file\\";//文件存放路径
        String tempPath = filePath + "temp\\" + pointFileIndexVo.getFileMd5();//临时文件存放路径
        File dir = new File(tempPath);
        if (!dir.exists()) dir.mkdirs();

        //生成一个临时文件名
        String tempFileNamePath = tempPath + "\\" + pointFileIndexVo.getFileName() + "_" + pointFileIndexVo.getPartIndex() + ".part";
        try {
            //将分片存储到临时文件夹中
            filePart.transferTo(new File(tempFileNamePath));

            List<String> partIndex = uploadProgress.get(pointFileIndexVo.getFileMd5());
            if (Objects.isNull(partIndex)) {
                partIndex = new ArrayList<>();
            }
            partIndex.add(pointFileIndexVo.getPartIndex().toString());
            uploadProgress.put(pointFileIndexVo.getFileMd5(), partIndex);

            File tempDir = new File(tempPath);
            File[] tempFiles = tempDir.listFiles();

            one:
            if (pointFileIndexVo.getPartNum().equals(Objects.requireNonNull(tempFiles).length)) {
                //需要校验一下,表示已有异步程序正在合并了;如果是分布式这个校验可以加入redis的分布式锁来完成
                if (isMergePart.get(pointFileIndexVo.getFileMd5()) != null) {
                    break one;
                }
                isMergePart.put(pointFileIndexVo.getFileMd5(), tempFiles.length);
                System.out.println("所有分片上传完成，预计总分片：" + pointFileIndexVo.getPartNum() + "; 实际总分片:" + tempFiles.length);
                //读取分片数据，进行分片合并
                FileOutputStream fileOutputStream = new FileOutputStream(filePath + pointFileIndexVo.getFileName());
                //这里如果分片很多的情况下，可以采用多线程来执行
                for (int i = 0; i < pointFileIndexVo.getPartNum(); i++) {
                    FileInputStream fileInputStream = new FileInputStream(tempPath + "\\" + pointFileIndexVo.getFileName() + "_" + i + ".part");
                    byte[] buf = new byte[1024 * 8];//8MB
                    int length;
                    while ((length = fileInputStream.read(buf)) != -1) {//读取fis文件输入字节流里面的数据
                        fileOutputStream.write(buf, 0, length);//通过fos文件输出字节流写出去
                    }
                    fileInputStream.close();
                }
                fileOutputStream.flush();
                fileOutputStream.close();

                // 删除临时文件夹里面的分片文件 如果使用流操作且没有关闭输入流，可能导致删除失败
                for (int i = 0; i < pointFileIndexVo.getPartNum(); i++) {
                    boolean delete = new File(tempPath + "\\" + pointFileIndexVo.getFileName() + "_" + i + ".part").delete();
                    File file = new File(tempPath + "\\" + pointFileIndexVo.getFileName() + "_" + i + ".part");
//                    System.out.println(i + " ; 是否删除：" + delete + " ; 是否还存在：" + file.exists());
                }
                //在删除对应的临时文件夹
                if (Objects.requireNonNull(tempDir.listFiles()).length == 0) {
                    boolean delete = tempDir.delete();
//                    System.out.println("文件夹 " + tempPath + "是否删除：" + delete);
                }
                isMergePart.remove(pointFileIndexVo.getFileMd5());
                uploadProgress.remove(pointFileIndexVo.getFileMd5());
            }

        } catch (Exception e) {
            log.error("单文件分片上传失败!", e);
            return ResultEntity.error(pointFileIndexVo.getFileMd5(), "单文件分片上传失败");
        }
        //通过返回成功的分片值，来验证分片是否有丢失
        return ResultEntity.success(pointFileIndexVo.getFileMd5(), pointFileIndexVo.getPartIndex().toString());
    }

    /**
     * 获取(服务器)指定文件存储路径下所有文件MD5值
     * 实际情况下，每一个文件的md5值都是单独保存在数据库或者其他存储机制中的，
     * 不需要每次都去读取文件然后获取md5值，这样多次io读取很耗性能
     *
     * @return
     * @throws Exception
     */
    @Bean
    private List<DiskFileIndexVo> getDiskFileMd5Index() throws Exception {
        String filePath = System.getProperty("user.dir") + "\\file\\part\\";
        File saveFileDir = new File(filePath);
        if (!saveFileDir.exists()) saveFileDir.mkdirs();

        List<DiskFileIndexVo> diskFileIndexVoList = new ArrayList<>();
        File[] listFiles = saveFileDir.listFiles();
        if (listFiles == null) return diskFileIndexVoList;

        for (File listFile : listFiles) {
            String fileName = listFile.getName();
            FileInputStream fileInputStream = new FileInputStream(filePath + fileName);
            String md5DigestAsHex = DigestUtils.md5DigestAsHex(fileInputStream);

            DiskFileIndexVo diskFileIndexVo = new DiskFileIndexVo();
            diskFileIndexVo.setFileName(fileName);
            diskFileIndexVo.setFileMd5(md5DigestAsHex);
            diskFileIndexVoList.add(diskFileIndexVo);
            fileInputStream.close();
        }
//        diskFileIndexVoList.forEach(e -> {
//            System.out.println(e.getFileName() + " = " + e.getFileMd5());
//        });
        diskFileIndexVos = diskFileIndexVoList;
        log.info("服务器磁盘所有文件 {}", diskFileIndexVoList);
        return diskFileIndexVoList;
    }
}
