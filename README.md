# java-file-upload-demo 对应的前端vue2_demo页面代码，至于怎么搭建前端脚手架和启动vue2框架项目就网上很多搭建教程

# 多种上传文件功能主要演示：
# 1、如果使用了minio请本地安装minio对象存储服务，下载如windows版本minio.exe文件执行以下命令：
<pre>
windows版本启动：
D:\minio>set MINIO_ROOT_USER=admin
D:\minio>set MINIO_ROOT_PASSWORD=12345678
D:\minio>minio.exe server D:\minio
</pre>
<pre>
Linux版本启动：
./minio server /data --console-address ":9001" --address ":9000" --access-key your-access-key --secret-key your-secret-key
</pre>
# 2、单文件、多文件、单文件分片、多文件分片、多文件(md5)分片秒传、单文件分片断点续传等上传方案到项目本地磁盘位置；
# 3、单文件、多文件分片上传至minio文件服务器中的例子；此测试需要在配置文件中将各配置加载项设置为true，加上redis配置项
# 4、文件上传并添加了上传的进度条，以方便前端上传文件能知道完成进度情况;

### Nodejs前端需要安装的前端插件
# npm install axios
# npm install element-ui
# npm install spark-md5


![web-index.png](web-index.png)

#### 前端页面主要代码(含js)如下：
```
<template>
  <div class="affix-container">
    <el-divider content-position="center"><h2>文件上传测试页面</h2></el-divider>
    <div class="uploadFileTag">
      <!-- 单文件上传 -->
      <div class="fileUploadStyle">
        <h3>单文件上传</h3>
        <el-upload ref="upload" name="file" action="#" :on-change="selectSingleFile" :on-remove="removeSingleFile"
          :file-list="singleFile.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success"
            @click="singleFileUpload">点击进行单文件上传</el-button>
          <div slot="tip" class="el-upload__tip">主要用于测试单文件上传</div>
        </el-upload>
      </div>
      <!-- 多文件上传 -->
      <div class="fileUploadStyle">
        <h3>多文件上传</h3>
        <el-upload ref="upload" name="files" action="#" :on-change="selectMultipleFile" :on-remove="removeMultipleFile"
          :file-list="multipleFile.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success"
            @click="multipleFileUpload">点击进行多文件上传</el-button>
          <div slot="tip" class="el-upload__tip">主要用于测试多文件上传</div>
        </el-upload>
      </div>
      <!-- 单文件分片上传 -->
      <div class="fileUploadStyle">
        <h3>单文件分片上传</h3>
        <el-upload ref="upload" name="files" action="#" :on-change="selectSinglePartFile"
          :on-remove="removeSingleFilePart" :file-list="singleFilePart.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success"
            @click="singleFilePartUpload">点击进行单文件分片上传</el-button>
          <div slot="tip" class="el-upload__tip">主要用于测试单文件分片上传</div>
        </el-upload>
        <el-progress :text-inside="true" class="progress" :stroke-width="26" :percentage="singlePartFileProgress" />
      </div>
      <!-- 多文件分片上传 -->
      <div class="fileUploadStyle">
        <h3>多文件分片上传</h3>
        <el-upload ref="upload" name="files" action="#" :on-change="selectMultiplePartFile"
          :on-remove="removeMultiplePartFile" :file-list="multipleFilePart.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success"
            @click="multipleFilePartUpload">点击进行多文件分片上传</el-button>
          <div slot="tip" class="el-upload__tip">主要用于测试多文件分片上传</div>
        </el-upload>
      </div>

      <!-- 多文件(分片)秒传 -->
      <div class="fileUploadStyle">
        <h3>多文件(分片MD5值)秒传</h3>
        <el-upload ref="upload" name="files" action="#" :on-change="selectMultiplePartFlashFile"
          :on-remove="removeMultiplePartFlashFile" :file-list="multipleFilePartFlash.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success"
            @click="multipleFilePartFlashUpload">点击进行文件秒传</el-button>
          <div slot="tip" class="el-upload__tip">主要用于测试多文件(分片MD5值)秒传</div>
        </el-upload>
      </div>
      <!-- 单文件(分片)断点上传 -->
      <div class="fileUploadStyle">
        <h3>单文件(分片)断点上传</h3>
        <el-upload ref="upload" name="files" action="#" :on-change="selectSingleFilePartPoint"
          :on-remove="removeSingleFilePartPoint" :file-list="singleFilePartPoint.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" :type="singleFilePartPoint.isStop ? 'info':'success'"
            @click="singleFilePartPointUpload">{{ singleFilePartPoint.isStop ? '点击继续文件断点上传' : '点击进行文件断点上传' }}</el-button>
          <!-- <el-button style="margin-left: 10px;" size="mini" type="info" :disabled = "!singleFilePartPoint.isStop"  @click="singleFilePartPoint.isStop = false">{{ singleFilePartPoint.isStop ? '继续上传' : '正在上传' }}</el-button> -->
          <div slot="tip" class="el-upload__tip">主要用于测试单文件(分片)断点上传</div>
        </el-upload>
      </div>
      <!-- 单文件上传Minio -->
      <div class="fileUploadStyle">
        <h3>单文件上传Minio</h3>
        <el-upload ref="upload" name="files" action="#" :on-change="selectSingleFileMinio"
          :on-remove="removeSingleFileMinio" :file-list="singleFileMinio.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success"
            @click="singleFileUploadMinio">点击进行单文件分片上传Minio</el-button>
          <div slot="tip" class="el-upload__tip">主要用于测试单文件上传Minio</div>
        </el-upload>
      </div>
      <!-- 多文件分片上传Minio -->
      <div class="fileUploadStyle">
        <h3>多文件分片上传Minio</h3>
        <el-upload ref="upload" name="files" action="#" :on-change="selectMultiplePartFileMinio"
          :on-remove="removeMultiplePartFileMinio" :file-list="multipleFilePartMinio.fileList" :auto-upload="false">
          <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          <el-button style="margin-left: 10px;" size="small" type="success"
            @click="multipleFilePartUploadMinio">点击进行多文件分片上传Minio</el-button>
          <div slot="tip" class="el-upload__tip">主要用于测试多文件分片上传Minio</div>
        </el-upload>
        <el-progress :text-inside="true" class="minio-progress" :stroke-width="26" :percentage="minioProgress" />
      </div>
    </div>
  </div>
</template>
<script>
import SparkMD5 from "spark-md5"
export default {
  name: 'StudyDemoFileUpload',
  data() {
    return {
      minioProgress: 0,
      singlePartFileProgress: 0,
      urlPrefix: 'http://localhost:8086',
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      singleFile: {
        file: '',
        fileList: []
      },
      multipleFile: {
        fileList: []
      },
      singleFilePart: {
        file: '',
        fileList: [],
        partIndex: 0,  // 当前分片
        partNum: 0, // 总分片数
        fileName: '',   // 文件名
        fileUid: '',   // 文件uuid
      },
      multipleFilePart: {
        fileList: [],
        fileData: []
      },
      multipleFilePartFlash: {
        fileList: [],
        fileData: [],
        filePartUploadInfo: [],
        filePartUploadData: []
      },
      singleFilePartPoint: {
        file: '',
        fileList: [],
        fileData: [],
        partNum: 0, // 总分片数
        fileName: '',   // 文件名
        fileUid: '',   // 文件uuid
        fileMd5: '',   // 文件Md5
        isStop: false,
        randomCount: 0
      },
      singleFileMinio: {
        file: '',
        fileList: []
      },
      multipleFilePartMinio: {
        fileList: [],
        fileData: []
      },
    };
  },

  mounted() {

  },

  methods: {
    testRequest() {
      let url = this.urlPrefix + "/fileUpload/fileTest";
      this.$post(url, null, null, resp => {
        let res = resp.data;
        if (res.data) {
          this.$message.success(res.msg)
        } else {
          this.$message.error("测试失败")
        }
      })
    },
    // 下面是单文件上传的一些方法
    selectSingleFile(file, fileList) {
      this.singleFile.file = file
      this.singleFile.fileList = []
      this.singleFile.fileList.push(file)
      console.log("单文件上传->选中的文件：", this.singleFile.file)
    },
    removeSingleFile(file, fileList) {
      this.singleFile.file = ''
      console.log("单文件上传->移除选中的文件：", this.singleFile.file)
    },
    singleFileUpload() {
      let url = this.urlPrefix + "/fileUpload/singleFileUpload";
      var fileParam = new FormData();
      fileParam.append("file", this.singleFile.file.raw);

      this.$upFile(url, fileParam, null, resp => {
        let res = resp.data;
        if (res.data) {
          this.$message.success(res.msg)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    // 下面是多文件上传的一些方法
    selectMultipleFile(file, fileList) {
      this.multipleFile.fileList.push(file)
      console.log("多文件上传->选中的文件：", this.multipleFile.fileList)
    },
    removeMultipleFile(file, fileList) {
      console.log("多文件上传->移除选中的文件：", this.multipleFile.fileList)
    },
    multipleFileUpload() {
      let url = this.urlPrefix + "/fileUpload/multipleFileUpload";
      var fileParam = new FormData();
      this.multipleFile.fileList.forEach((val, index) => {
        fileParam.append("files", val.raw);
      });

      this.$upFile(url, fileParam, null, resp => {
        let res = resp.data;
        if (res.data) {
          this.$message.success(res.msg)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    // 下面是单文件分片上传的一些方法
    selectSinglePartFile(file, fileList) {
      this.singleFilePart.file = file
      this.singleFilePart.fileList = []
      this.singleFilePart.fileList.push(file)
      this.singleFilePart.fileName = file.name
      console.log("单文件分片上传->选中的文件：", this.singleFilePart.file)
    },
    removeSingleFilePart(file, fileList) {
      this.singleFilePart.file = ''
      console.log("单文件分片上传->移除选中的文件：", this.singleFilePart.file)
    },
    singleFilePartUpload() {
      let url = this.urlPrefix + "/fileUpload/singleFilePartUpload";
      let partSize = 8; //MB
      let exeCount = 1;
      this.singleFilePart.fileUid = this.singleFilePart.file.uid
      this.singleFilePart.partNum = Math.ceil(this.singleFilePart.file.size / 1024 / 1024 / partSize) // 8MB一片
      for (let i = 0; i < this.singleFilePart.partNum; i++) {
        var fileParam = new FormData();
        fileParam.append('filePart', this.singleFilePart.file.raw.slice(i * 1024 * 1024 * partSize, (i + 1) * 1024 * 1024 * partSize))
        fileParam.append('partIndex', i)
        fileParam.append('partNum', this.singleFilePart.partNum)
        fileParam.append('fileName', this.singleFilePart.fileName)
        fileParam.append('fileUid', this.singleFilePart.fileUid)

        this.$upFile(url, fileParam, null, resp => {
          let res = resp.data;
          //判断最后一个分片文件传完之后，后台检验文件上传情况

          this.singlePartFileProgress = (100 / (this.singleFilePart.partNum)) * exeCount;
              exeCount++;

          if (res.code === 200) {
            if (i == res.msg) {
              var info = '当前分片 ' + i + ' 上传成功！' + ';返回的分片是 ' + res.msg
              console.log(info)
            } else {
              var info = '当前分片 ' + i + ' 上传失败！' + ';返回的分片是 ' + res.msg
              console.log(info)
            }
            if (res.msg == this.singleFilePart.partNum - 1) {
              this.singlePartFileProgress = 100
              this.$message.success('单文件分片上传完成')
            }
          } else {
            this.$message.error(res.msg)
          }
        })
      }
    },
    // 下面多文件分片上传的一些方法
    selectMultiplePartFile(file, fileList) {
      this.multipleFilePart.fileList.push(file)
      console.log("多文件分片上传->选中的文件：", this.multipleFilePart.fileList)
    },
    removeMultiplePartFile(file, fileList) {
      this.multipleFilePart.fileList = this.multipleFilePart.fileList.filter(item => item.uid != file.uid);
      console.log("多文件分片上传->移除选中的文件：", this.multipleFilePart.fileList)
    },
    multipleFilePartUpload() {
      let fileUploadCount = 0;
      let url = this.urlPrefix + "/fileUpload/multipleFilePartUpload";
      let partSize = 8; //MB
      for (let i = 0; i < this.multipleFilePart.fileList.length; i++) {
        let partNum = Math.ceil(this.multipleFilePart.fileList[i].size / 1024 / 1024 / partSize)
        for (let j = 0; j < partNum; j++) {
          let partFileUid = '';
          var fileParam = new FormData();
          fileParam.append('filePart', this.multipleFilePart.fileList[i].raw.slice(j * 1024 * 1024 * partSize, (j + 1) * 1024 * 1024 * partSize))
          fileParam.append('partIndex', j)
          fileParam.append('partNum', partNum)
          fileParam.append('fileName', this.multipleFilePart.fileList[i].name)
          fileParam.append('fileUid', this.multipleFilePart.fileList[i].uid)
          partFileUid = this.multipleFilePart.fileList[i].uid
          this.$upFile(url, fileParam, null, resp => {
            let res = resp.data;
            //判断最后一个分片文件传完之后，后台检验文件上传情况
            if (res.code == 200) {
              if (j == res.data) {
                var info = '当前分片上传成功!当前分片:' + j + ';返回分片:' + res.data + ';当前uid:' + partFileUid + ';返回uid:' + res.msg
                console.log(info)
              } else {
                var info = '当前分片上传失败!当前分片:' + j + ';返回分片:' + res.data + ';当前uid:' + partFileUid + ';返回uid:' + res.msg
                console.log(info)
              }
              if (res.data == partNum - 1) {
                fileUploadCount++;
                var info = this.multipleFilePart.fileList[i].name + '：上传成功!uid:' + partFileUid
                console.log(info)
                if (fileUploadCount == this.multipleFilePart.fileList.length - 1) {
                  console.log('多文件分片上传完成')
                  this.$message.success('多文件分片上传完成')
                }
              }
            } else {
              var info = '多文件分片上传失败!当前分片:' + j + ';当前uid:' + partFileUid
              console.log(info)
              this.$message.error(res.msg)
            }
          })
        }
      }

    },
    // 下面多文件(分片)秒传的一些方法
    async selectMultiplePartFlashFile(file, fileList) {
      this.multipleFilePartFlash.fileList.push(file)
      console.log("多文件(分片)秒传->选中的文件：", this.multipleFilePartFlash.fileList)
      let partSize = 8; //MB
      let partNum = Math.ceil(file.size / 1024 / 1024 / partSize)
      for (let j = 0; j < partNum; j++) {
        let filePartFlash = {
          "file": '',
          "fileName": '',
          "fileUid": '',
          "fileMd5": '',
          "partIndex": '',
          "partNum": ''
        }
        let partFile = file.raw.slice(j * 1024 * 1024 * partSize, (j + 1) * 1024 * 1024 * partSize)
        filePartFlash.fileName = file.name
        filePartFlash.fileUid = file.uid
        filePartFlash.partIndex = j
        filePartFlash.partNum = partNum
        filePartFlash.fileMd5 = SparkMD5.ArrayBuffer.hash(await this.readFile(partFile))
        this.multipleFilePartFlash.filePartUploadInfo.push(filePartFlash)
        filePartFlash.file = partFile
        this.multipleFilePartFlash.filePartUploadData.push(filePartFlash)
      }
      console.log("多文件(分片)秒传->选中的分片：", this.multipleFilePartFlash.filePartUploadInfo)
    },
    removeMultiplePartFlashFile(file, fileList) {
      this.multipleFilePartFlash.fileList = this.multipleFilePartFlash.fileList.filter(item => item.uid != file.uid);
      this.multipleFilePartFlash.filePartUploadInfo = this.multipleFilePartFlash.filePartUploadInfo.filter(item => item.fileUid != file.uid);
      this.multipleFilePartFlash.filePartUploadData = this.multipleFilePartFlash.filePartUploadData.filter(item => item.fileUid != file.uid);
      console.log("多文件(分片)秒传->移除选中的文件：", this.multipleFilePartFlash.fileList)
      console.log("多文件(分片)秒传->移除选中的分片后：", this.multipleFilePartFlash.filePartUploadInfo)
    },
    multipleFilePartFlashUpload() {
      let fileCheckUrl = this.urlPrefix + "/fileUpload/checkDiskFile";
      let fileUploadUrl = this.urlPrefix + "/fileUpload/multipleFilePartFlashUpload";

      let fileUploadCount = 0;
      let notUploadFilePart = []

      this.$post(fileCheckUrl, this.multipleFilePartFlash.filePartUploadInfo, null, resp => {
        let res = resp.data;
        if (res.code == 200) {
          //后端返回来不需要上传的分片相关信息，包括这些分片在后端服务器的存储索引
          notUploadFilePart = res.data
          //不需要上传的分片文件
          // notUploadFilePart = filePartUploadInfo.filter(item => notUploadFilePart.some(n => n.fileUid == item.fileUid && n.partIndex == item.partIndex))
          //需要上传的分片文件
          this.multipleFilePartFlash.filePartUploadData = this.multipleFilePartFlash.filePartUploadData.filter(item => !notUploadFilePart.some(n => n.fileUid == item.fileUid && n.partIndex == item.partIndex))
          console.log("不需要上传的分片文件：", notUploadFilePart)
          console.log("需要上传的分片文件：", this.multipleFilePartFlash.filePartUploadData)
          for (let i = 0; i < this.multipleFilePartFlash.filePartUploadData.length; i++) {
            var fileParam = new FormData();
            fileParam.append('filePart', this.multipleFilePartFlash.filePartUploadData[i].file)
            fileParam.append('fileInfo', JSON.stringify(this.multipleFilePartFlash.filePartUploadData[i]))
            fileParam.append('fileOther', JSON.stringify(notUploadFilePart))
            //对需要上传的分片上传至后端服务器
            this.$upFile(fileUploadUrl, fileParam, null, resp => {
              let res = resp.data;
              //判断最后一个分片文件传完之后，后台检验文件上传情况

              if (res.code == 200) {
                fileUploadCount++;
                if (res.msg != this.multipleFilePartFlash.filePartUploadData[i].fileUid && i != res.data) {
                  var info = '当前分片上传失败!当前分片:' + j + ';返回分片:' + res.data + ';当前uid:' + this.multipleFilePartFlash.filePartUploadData[i].fileUid + ';返回uid:' + res.msg
                  console.log(info)
                }
                if (fileUploadCount == this.multipleFilePartFlash.filePartUploadData.length) {
                  console.log('多文件分片上传完成')
                  this.$message.success('多文件(分片)秒传完成')
                }
              } else {
                console.log('多文件(分片)秒传失败')
                this.$message.error(res.msg)
              }
            })
          }
        } else {
          this.$message.error(res.msg)
          return
        }
      })
    },
    // 下面单文件(分片)断点上传的一些方法
    async selectSingleFilePartPoint(file, fileList) {
      this.singleFilePartPoint.file = file
      this.singleFilePartPoint.fileList = []
      this.singleFilePartPoint.fileList.push(file)
      this.singleFilePartPoint.fileName = file.name
      this.singleFilePartPoint.fileMd5 = SparkMD5.ArrayBuffer.hash(await this.readFile(this.singleFilePartPoint.file.raw))
      console.log("单文件(分片)断点上传->选中的文件：", this.singleFilePartPoint.fileList)
    },
    removeSingleFilePartPoint(file, fileList) {
      this.singleFilePartPoint.file = ''
      console.log("单文件(分片)断点上传->移除选中的文件：", this.singleFilePartPoint.file)
    },
    async singleFilePartPointUpload() {
      let checkUploadUrl = this.urlPrefix + "/fileUpload/checkUploadFileIndex";
      let fileUploadUrl = this.urlPrefix + "/fileUpload/singleFilePartPointUpload";
      let partSize = 8; //MB

      let partNum = Math.ceil(this.singleFilePartPoint.file.size / 1024 / 1024 / partSize)

      let fileInfo = {}
      fileInfo.fileName = this.singleFilePartPoint.file.name
      fileInfo.fileUid = this.singleFilePartPoint.file.uid
      fileInfo.fileMd5 = this.singleFilePartPoint.fileMd5
      fileInfo.fileSize = this.singleFilePartPoint.file.size
      fileInfo.partNum = partNum

      this.$post(checkUploadUrl, fileInfo, null, resp => {
        let res = resp.data;
        //判断最后一个分片文件传完之后，后台检验文件上传情况
        if (res.code === 200) {
          fileInfo = res.data
          console.log("已上传文件部分：", fileInfo)

          if (this.singleFilePartPoint.randomCount != 0 || fileInfo.parts.length != 0) {
            this.singleFilePartPoint.isStop = false
            console.log("该文件之前被模拟过随机停止的情况,此次上传不再随机停止节点了,直接上传还未上传的文件")
          }

          let random = Math.floor(Math.random() * partNum)

          for (let i = 0; i < partNum; i++) {

            //模拟上传随机被暂停，模拟网络掉线情况等
            if (i == random && this.singleFilePartPoint.randomCount == 0 && fileInfo.parts.length == 0) {
              this.singleFilePartPoint.isStop = true
              this.singleFilePartPoint.randomCount++
              console.log("随机停止分片节点:", random)
              break;
            }
            //如果返回已经上传的partIndex是当前i，则不在上传该part文件
            if (fileInfo.parts.indexOf(i) !== -1) continue;

            fileInfo.partIndex = i
            var fileParam = new FormData();
            fileParam.append('filePart', this.singleFilePartPoint.file.raw.slice(i * 1024 * 1024 * partSize, (i + 1) * 1024 * 1024 * partSize))
            fileParam.append('fileInfo', JSON.stringify(fileInfo))
            this.$upFile(fileUploadUrl, fileParam, null, resp => {
              let res = resp.data;
              //判断最后一个分片文件传完之后，后台检验文件上传情况
              if (res.code === 200) {
                if (i != res.msg) {
                  var info = '当前分片 ' + i + ' 上传失败！' + ';返回的分片是 ' + res.msg
                  console.log(info)
                }
                if (res.msg == partNum - 1) {
                  this.singleFilePartPoint.randomCount = 0
                  this.$message.success('单文件(分片)断点上传完成')
                }
              } else {
                this.$message.error(res.msg)
              }
            })
          }
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    singleFilePartPointStopUpload() {
      this.singleFilePartPoint.isStop = !this.singleFilePartPoint.isStop
    },
    // 下面是单文件上传Minio的一些方法
    selectSingleFileMinio(file, fileList) {
      this.singleFileMinio.file = file
      this.singleFileMinio.fileList = []
      this.singleFileMinio.fileList.push(file)
      console.log("单文件上传Minio->选中的文件：", this.singleFileMinio.file)
    },
    removeSingleFileMinio(file, fileList) {
      this.singleFileMinio.file = ''
      console.log("单文件上传Minio->移除选中的文件：", this.singleFileMinio.file)
    },
    singleFileUploadMinio() {
      let url = this.urlPrefix + "/minio/singleFileUploadMinio";
      var fileParam = new FormData();
      fileParam.append("file", this.singleFileMinio.file.raw);

      this.$upFile(url, fileParam, null, resp => {
        let res = resp.data;
        if (res.data) {
          this.$message.success(res.msg)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    // 下面多文件分片上传Minio的一些方法
    selectMultiplePartFileMinio(file, fileList) {
      this.multipleFilePartMinio.fileList.push(file)
      console.log("多文件分片上传->选中的文件：", this.multipleFilePartMinio.fileList)
    },
    removeMultiplePartFileMinio(file, fileList) {
      this.multipleFilePartMinio.fileList = this.multipleFilePartMinio.fileList.filter(item => item.uid != file.uid);
      console.log("多文件分片上传->移除选中的文件：", this.multipleFilePartMinio.fileList)
    },
    multipleFilePartUploadMinio() {
      let fileUploadCount = 0;
      let url = this.urlPrefix + "/minio/partFileUploadMinio";
      let partSize = 8; //MB
      let partFileUid = [];
      let exeCount = 1;
      for (let i = 0; i < this.multipleFilePartMinio.fileList.length; i++) {
        let partNum = Math.ceil(this.multipleFilePartMinio.fileList[i].size / 1024 / 1024 / partSize)
        let partFileIndex = {
          fileName: this.multipleFilePartMinio.fileList[i].name,
          fileUid: this.multipleFilePartMinio.fileList[i].uid,
          partTotalNum: partNum,
          partIndex: ''
        }
        partFileUid.push(this.multipleFilePartMinio.fileList[i].uid + '')
        for (let j = 0; j < partNum; j++) {
          partFileIndex.partIndex = j
          var fileParam = new FormData();
          fileParam.append('file', this.multipleFilePartMinio.fileList[i].raw.slice(j * 1024 * 1024 * partSize, (j + 1) * 1024 * 1024 * partSize))
          fileParam.append('partFileIndex', JSON.stringify(partFileIndex))

          this.$upFile(url, fileParam, null, resp => {
            let res = resp.data;
            //判断最后一个分片文件传完之后，后台检验文件上传情况
            if (res.code == 200) {

              this.minioProgress = (100 / (partNum)) * exeCount;
              exeCount++;
              // debugger;
              // console.log("========分区数据量："+partNum);
              if (!res.data) {
                var info = '当前分片上传Minio失败!uid:' + partFileUid + ';当前分片:' + j + ';返回分片:' + res.msg
                console.log(info)
              }

              let isUid = partFileUid.includes(res.msg)
              if (isUid) {
                fileUploadCount++;
                if (fileUploadCount == this.multipleFilePartMinio.fileList.length) {
                  console.log('多文件分片上传Minio完成')
                  this.$message.success('多文件分片上传Minio完成')
                  this.minioProgress = 100;
                }
              }


            } else {
              var info = '多文件分片上传Minio失败!当前分片:' + j + ';当前uid:' + partFileUid
              console.log(info)
              this.$message.error(res.msg)
            }
          })
        }
      }
    },
    getFileMd5(file) {
      return new Promise((resolve, reject) => {
        let fileReader = new FileReader()
        fileReader.onload = function (event) {
          let fileMd5 = SparkMD5.ArrayBuffer.hash(event.target.result)
          resolve(fileMd5)
        }
        fileReader.readAsArrayBuffer(file)
      })
    },
    readFile(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = e => resolve(e.target.result);
        reader.onerror = e => reject(e);
        reader.readAsArrayBuffer(file);
      })
    }
  },
};
</script>
