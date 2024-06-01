<template>
    <div>
        <page-nav></page-nav>
        <el-divider content-position="center">'websocket + stomp' 聊天室 demo</el-divider>
        <div class="chatRoomTag">
            <div class="userTag">
                <!-- 头部操作栏 -->
                <div class="headerHandleTag">
                    <div class="onlineChatTag">
                        <el-button type="primary" icon="el-icon-switch-button" size="mini" plain @click="userFirstOnlineHandle()" class="onlineChatTag">
                            {{ this.userFirst.isOnline ? '下线' : '上线' }}</el-button>
                    </div>
                    <div class="chatUserTag">
                        <el-tag type="" size="medium" class="chatUserTag">
                            <i class="el-icon-chat-dot-square"></i> {{ this.userFirst.chatRoomTitle }}
                        </el-tag>
                    </div>
                    <div class="userNameTag">
                        <el-tag type="" size="medium" class="userNameTag">
                            <i class="el-icon-user-solid"></i> {{ this.userFirst.userName }}
                        </el-tag>
                    </div>
                </div>
                <!-- 聊天室主体栏 -->
                <div class="bodyHandleTag">
                    <!-- 好友列表 -->
                    <div class="friendListTag">
                        <el-table :data="this.userFirst.friendList" :show-header="false" :stripe="true" height="750" style="width: 100%">
                            <el-table-column label="好友列表" show-overflow-tooltip	 width="120">
                                <template slot-scope="scope">
                                    <i class="el-icon-user"></i>
                                    <el-button type="text" size="mini" @click="userFirstOpenFriendRoomHandle(scope.$index, scope.row)">
                                        {{ scope.row.type == 2 ? '(群)' : '' }}{{ scope.row.name }}</el-button>
                                </template>
                            </el-table-column>
                        </el-table>         
                    </div>
                    <!-- 聊天消息主体 -->
                    <div class="chatRoomMsgTag">
                        <div class="selectLoginUserTag" v-if="!this.userFirst.isOnline">
                            <el-button plain @click="userFirstSelectUserListHandle()">点击获取可用账户</el-button>
                            <el-select v-model="userFirst.uuid" placeholder="请选择一个账号..." style="width: 150px;">
                                <el-option  v-for="item in this.userFirst.allUserList" :key="item.uuid" :label="item.name" :value="item.uuid"></el-option>
                            </el-select>
                            <el-button type="primary" icon="el-icon-switch-button" size="medium" plain @click="userFirstOnlineHandle()" class="onlineChatTag">上线</el-button>
                        </div>
                        <!-- 消息展示区域 -->
                        <div ref="chatMsgTag" class="chatMsgTag" v-if="this.userFirst.isOnline">
                            <ul ref="recMsgRef1" class="recMsgTag">
                                <li v-for="(item, index) in this.userFirst.chatMsgList" :key="index" 
                                    :style="{'text-align': (userFirst.uuid == item.senderUuid) ? 'right' : 'left'}">
                                    <span span v-if="userFirst.uuid !== item.senderUuid" class="pointTag"></span>
                                    <span style="color: rgb(159, 110, 207);font-size: 12px;">{{item.senderName + ' (' + item.sendTime + ')' }}</span>
                                    <span span v-if="userFirst.uuid == item.senderUuid" class="pointTag"></span>
                                    <br/><span style="color: rgb(123, 12, 12);font-family: Arial, Helvetica, sans-serif;">{{ item.sendContent }}</span>
                                </li>
                            </ul> 
                        </div>
                        <!-- 发送消息区域 -->
                        <div class="sendMsgTag" v-if="this.userFirst.isOnline">
                            <el-input v-model="userFirst.sendMsg" size="small" @keydown.enter.native="userFirstSendMsgHandle()" placeholder="输入聊天..." style="width: 85%;"></el-input>
                            <el-button size="small" @click="userFirstSendMsgHandle()">发送</el-button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="userTag">
                <!-- 头部操作栏 -->
                <div class="headerHandleTag">
                    <div class="onlineChatTag">
                        <el-button type="primary" icon="el-icon-switch-button" size="mini" plain @click="userSecondOnlineHandle()" class="onlineChatTag">
                            {{ this.userSecond.isOnline ? '下线' : '上线' }}</el-button>
                    </div>
                    <div class="chatUserTag">
                        <el-tag type="" size="medium" class="chatUserTag">
                            <i class="el-icon-chat-dot-square"></i> {{ this.userSecond.chatRoomTitle }}
                        </el-tag>
                    </div>
                    <div class="userNameTag">
                        <el-tag type="" size="medium" class="userNameTag">
                            <i class="el-icon-user-solid"></i> {{ this.userSecond.userName }}
                        </el-tag>
                    </div>
                </div>
                <!-- 聊天室主体栏 -->
                <div class="bodyHandleTag">
                    <!-- 好友列表 -->
                    <div class="friendListTag">
                        <el-table :data="this.userSecond.friendList" :show-header="false" :stripe="true" height="750" style="width: 100%">
                            <el-table-column label="好友列表" show-overflow-tooltip	 width="120">
                                <template slot-scope="scope">
                                    <i class="el-icon-user"></i>
                                    <el-button type="text" size="mini" @click="userSecondOpenFriendRoomHandle(scope.$index, scope.row)">
                                        {{ scope.row.type == 2 ? '(群)' : '' }}{{ scope.row.name }}</el-button>
                                </template>
                            </el-table-column>
                        </el-table>         
                    </div>
                    <!-- 聊天消息主体 -->
                    <div class="chatRoomMsgTag">
                        <div class="selectLoginUserTag" v-if="!this.userSecond.isOnline">
                            <el-button plain @click="userSecondSelectUserListHandle()">点击获取可用账户</el-button>
                            <el-select v-model="userSecond.uuid" placeholder="请选择一个账号..." style="width: 150px;">
                                <el-option  v-for="item in this.userSecond.allUserList" :key="item.uuid" :label="item.name" :value="item.uuid"></el-option>
                            </el-select>
                            <el-button type="primary" icon="el-icon-switch-button" size="medium" plain @click="userSecondOnlineHandle()" class="onlineChatTag">上线</el-button>
                        </div>
                        <!-- 消息展示区域 -->
                        <div ref="chatMsgTag" class="chatMsgTag" v-if="this.userSecond.isOnline">
                            <ul ref="recMsgRef2" class="recMsgTag">
                                <li v-for="(item, index) in this.userSecond.chatMsgList" :key="index" 
                                    :style="{'text-align': (userSecond.uuid == item.senderUuid) ? 'right' : 'left'}">
                                    <span span v-if="userSecond.uuid !== item.senderUuid" class="pointTag"></span>
                                    <span style="color: rgb(159, 110, 207);font-size: 12px;">{{item.senderName + ' (' + item.sendTime + ')' }}</span>
                                    <span span v-if="userSecond.uuid == item.senderUuid" class="pointTag"></span>
                                    <br/><span style="color: rgb(123, 12, 12);font-family: Arial, Helvetica, sans-serif;">{{ item.sendContent }}</span>
                                </li>
                            </ul> 
                        </div>
                        <!-- 发送消息区域 -->
                        <div class="sendMsgTag" v-if="this.userSecond.isOnline">
                            <el-input v-model="userSecond.sendMsg" size="small" @keydown.enter.native="userSecondSendMsgHandle()" placeholder="输入聊天..." style="width: 85%;"></el-input>
                            <el-button size="small" @click="userSecondSendMsgHandle()">发送</el-button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="userTag">
                <!-- 头部操作栏 -->
                <div class="headerHandleTag">
                    <div class="onlineChatTag">
                        <el-button type="primary" icon="el-icon-switch-button" size="mini" plain @click="userThirdOnlineHandle()" class="onlineChatTag">
                            {{ this.userThird.isOnline ? '下线' : '上线' }}</el-button>
                    </div>
                    <div class="chatUserTag">
                        <el-tag type="" size="medium" class="chatUserTag">
                            <i class="el-icon-chat-dot-square"></i> {{ this.userThird.chatRoomTitle }}
                        </el-tag>
                    </div>
                    <div class="userNameTag">
                        <el-tag type="" size="medium" class="userNameTag">
                            <i class="el-icon-user-solid"></i> {{ this.userThird.userName }}
                        </el-tag>
                    </div>
                </div>
                <!-- 聊天室主体栏 -->
                <div class="bodyHandleTag">
                    <!-- 好友列表 -->
                    <div class="friendListTag">
                        <el-table :data="this.userThird.friendList" :show-header="false" :stripe="true" height="750" style="width: 100%">
                            <el-table-column label="好友列表" show-overflow-tooltip	 width="120">
                                <template slot-scope="scope">
                                    <i class="el-icon-user"></i>
                                    <el-button type="text" size="mini" @click="userThirdOpenFriendRoomHandle(scope.$index, scope.row)">
                                        {{ scope.row.type == 2 ? '(群)' : '' }}{{ scope.row.name }}</el-button>
                                </template>
                            </el-table-column>
                        </el-table>         
                    </div>
                    <!-- 聊天消息主体 -->
                    <div class="chatRoomMsgTag">
                        <div class="selectLoginUserTag" v-if="!this.userThird.isOnline">
                            <el-button plain @click="userThirdSelectUserListHandle()">点击获取可用账户</el-button>
                            <el-select v-model="userThird.uuid" placeholder="请选择一个账号..." style="width: 150px;">
                                <el-option  v-for="item in this.userThird.allUserList" :key="item.uuid" :label="item.name" :value="item.uuid"></el-option>
                            </el-select>
                            <el-button type="primary" icon="el-icon-switch-button" size="medium" plain @click="userThirdOnlineHandle()" class="onlineChatTag">上线</el-button>
                        </div>
                        <!-- 消息展示区域 -->
                        <div ref="chatMsgTag" class="chatMsgTag" v-if="this.userThird.isOnline">
                            <ul ref="recMsgRef3" class="recMsgTag">
                                <li v-for="(item, index) in this.userThird.chatMsgList" :key="index" 
                                    :style="{'text-align': (userThird.uuid == item.senderUuid) ? 'right' : 'left'}">
                                    <span span v-if="userThird.uuid !== item.senderUuid" class="pointTag"></span>
                                    <span style="color: rgb(159, 110, 207);font-size: 12px;">{{item.senderName + ' (' + item.sendTime + ')' }}</span>
                                    <span span v-if="userThird.uuid == item.senderUuid" class="pointTag"></span>
                                    <br/><span style="color: rgb(123, 12, 12);font-family: Arial, Helvetica, sans-serif;">{{ item.sendContent }}</span>
                                </li>
                            </ul> 
                        </div>
                        <!-- 发送消息区域 -->
                        <div class="sendMsgTag" v-if="this.userThird.isOnline">
                            <el-input v-model="userThird.sendMsg" size="small" @keydown.enter.native="userThirdSendMsgHandle()" placeholder="输入聊天..." style="width: 85%;"></el-input>
                            <el-button size="small" @click="userThirdSendMsgHandle()">发送</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
// import { Client, Message } from '@stomp/stompjs';

export default {
    name: 'StudyDemoChatRoom',

    data() {
        return {
            urlPrefix: 'http://localhost:8083',
            socket: null,
            stompClient: null,
            subscription: null,
            userFirst:{
                isOnline: false,
                stompClient: null,
                subscription: null,
                checkSub: null,
                chatRoomTitle: '聊天室',//当前正在聊天的聊天室
                uuid: '',//当前登录的用户账户
                userName: 'admin',//当前登录的用户名
                sendMsg: '',//当前输入的聊天消息
                allUserList:[],//当前可选的登录账号
                openFriend: {},//打开的当前聊天室
                friendList: [],//好友列表
                chatMsgList: []//当前聊天室的聊天内容
            },
            userSecond:{
                isOnline: false,
                stompClient: null,
                chatRoomTitle: '聊天室',
                uuid: '',
                userName: 'admin',
                sendMsg: '',
                allUserList:[],
                openFriend: {},
                friendList: [],
                chatMsgList: []
            },
            userThird:{
                isOnline: false,
                stompClient: null,
                chatRoomTitle: '聊天室',
                uuid: '',
                userName: 'admin',
                sendMsg: '',
                allUserList:[],
                openFriend: {},
                friendList: [],
                chatMsgList: []
            },
            openFriend: {
                uuid: '',
                name: '',
                type: ''
            },
            chatMsg: {
                senderUuid: '',//发送者uuid
                senderName: '',//发送者姓名
                sendTime: '',//发送时间
                sendContent: '',//发送内容
                receiver: '',//接受者，即当前登录用户uuid
                msgType: ''//消息类型，单聊还是群聊
            }
        };
    },

    created() {
        // this.socket = new WebSocket('ws://localhost:8080/chat');
        // this.stompClient = Stomp.client('ws://localhost:8083/chat')
        // 添加beforeunload事件监听器
        window.addEventListener('load', this.handlePageRefresh); 
    },
    beforeDestroy() {
        // 在组件销毁前移除beforeunload事件监听器
        window.removeEventListener('load', this.handlePageRefresh);
    },

    mounted() {
       
    },

    methods: {
        handlePageRefresh(){
            let url = this.urlPrefix + "/chatRoom/cleanAllChatRoomData"
            let uuids = [];
            // uuids.push(this.userFirst.uuid)
            // uuids.push(this.userSecond.uuid)
            // uuids.push(this.userThird.uuid)
            // console.log(uuids)
            // this.$post(url,uuids,null,resp => {
            //     let res = resp.data
            //     if(res.code == 200){
            //     }else{
            //     }
            // })
        },
        //第一个用户聊天室
        userFirstSelectUserListHandle(){//获取当前可登录用户
           this.getUserListHandle(this.userFirst)
        },
        async userFirstOnlineHandle(){//用户上线和下线
            //如果已经在线，则是下线操作
            if(this.userFirst.isOnline){
                this.userOffline(this.userFirst)
                Object.assign(this.userFirst, this.$options.data().userFirst)
                return
            }
            //上线操作
            if(!(this.userFirst.uuid == null)){
                this.userOnline(this.userFirst)
            }
        },
        userFirstOpenFriendRoomHandle(index,row){//选取好友列表进行聊天
            this.userFirst.chatMsgList = []
            this.userFirst.openFriend = row
            this.userFirst.chatRoomTitle = (row.type == 1 ? "" : "(群)") + row.name
            this.openFriendRoomHandle(this.userFirst,row)
        },
        userFirstSendMsgHandle(){//发送消息
            this.sendMsgHandle(this.userFirst)
        },
        //第二个用户聊天室
        userSecondSelectUserListHandle(){//获取当前可登录用户
            this.getUserListHandle(this.userSecond)
        },
        async userSecondOnlineHandle(){//用户上线和下线
            //如果已经在线，则是下线操作
            if(this.userSecond.isOnline){
                this.userOffline(this.userSecond)
                Object.assign(this.userSecond, this.$options.data().userSecond)
                return
            }
            //上线操作
            if(!(this.userSecond.uuid == null)){
                this.userOnline(this.userSecond)
            }
        },
        userSecondOpenFriendRoomHandle(index,row){//选取好友列表进行聊天
            this.userSecond.chatMsgList = []
            this.userSecond.openFriend = row
            this.userSecond.chatRoomTitle = (row.type == 1 ? "" : "(群)") + row.name
            this.openFriendRoomHandle(this.userSecond,row)
        },
        userSecondSendMsgHandle(){//发送消息
            this.sendMsgHandle(this.userSecond)
        },
        //第三个用户聊天室
        userThirdSelectUserListHandle(){//获取当前可登录用户
            this.getUserListHandle(this.userThird)
        },
        async userThirdOnlineHandle(){//用户上线和下线
            //如果已经在线，则是下线操作
            if(this.userThird.isOnline){
                this.userOffline(this.userThird)
                Object.assign(this.userThird, this.$options.data().userThird)
                return
            }
            //上线操作
            if(!(this.userThird.uuid == null)){
                this.userOnline(this.userThird)
            }
        },
        userThirdOpenFriendRoomHandle(index,row){//选取好友列表进行聊天
            this.userThird.chatMsgList = []
            this.userThird.openFriend = row
            this.userThird.chatRoomTitle = (row.type == 1 ? "" : "(群)") + row.name
            this.openFriendRoomHandle(this.userThird,row)
        },
        userThirdSendMsgHandle(){//发送消息
            this.sendMsgHandle(this.userThird)
        },
        //公共方法
        getUserListHandle(user){//获取可用账号
            let url = this.urlPrefix + "/chatRoom/getAllUserList"
            this.$post(url,null,null,resp => {
                let res = resp.data
                if(res.code == 200){
                    user.allUserList = res.data
                }else{
                    this.$message.error(res.msg)
                }
            })
        },
        connetChatRoom(stompClient,user){//websocket连接
            return new Promise((resolve, reject) => {
                // 设置WebSocket连接以及Stomp客户端方式1
                // const socket = new WebSocket('ws://localhost:8080/chat');
                // this.stompClient = Stomp.over(socket);
                // 创建Stomp客户端方式2
                // this.stompClient = Stomp.client('ws://localhost:8083/chat')

                // 连接成功的回调函数
                const onConnect = (frame) => {
                    console.log('连接成功:',frame);
                    resolve(true)
                };
                // 连接错误的回调函数
                const onError  = (error) => {
                    console.log('连接错误:',error);
                    resolve(false)
                };
                // 连接断开的回调函数
                const onDisconnect   = (offLine) => {
                    console.log('连接断开:',offLine);
                };

                // 建立连接
                stompClient.connect({"Authorization": user.uuid, "user": JSON.stringify(user)}, onConnect, onError);

                // 监听客户端断开事件
                stompClient.onDisconnect = onDisconnect;
                
            })
        },
        userOnline(user){//用户上线
            let url = this.urlPrefix + "/chatRoom/getUserData"
                this.$post(url,{uuid:user.uuid},null,async resp => {
                    let res = resp.data
                    if(res.code == 200){
                        user.stompClient = Stomp.client('ws://localhost:8083/chat')
                        console.log("连接对象：",user.stompClient)
                        let connect = await this.connetChatRoom(user.stompClient,{uuid: user.uuid,name: user.userName,type: res.data.user.type})
                        if(connect){
                            console.log("返回数据：",res.data)
                            user.friendList = res.data.friendList
                            user.uuid = res.data.user.uuid
                            user.userName = res.data.user.name
                            // this.userFirst.type = res.data.user.type
                            user.isOnline = !user.isOnline
                            this.subscribeChatRoom(user.stompClient,{uuid:user.uuid,name:user.userName,type:'1'})
                        }else{
                            return
                        }
                    }else{
                        this.$message.error(res.msg)
                        return
                    }
                })
        },
        userOffline(user){//用户下线
            let url = this.urlPrefix + "/chatRoom/offLine"
            user.stompClient.disconnect(() => {
                console.log(user.uuid,' 下线了！');
                //告诉服务器下线通知
                this.$post(url,{uuid:user.uuid},null,resp => {
                    let res = resp.data
                    if(res.code == 200){
                        this.$message.success(res.msg)
                    }else{
                        this.$message.error(res.msg)
                        return
                    }
                })
            });
            user.isOnline = !user.isOnline
        },
        openFriendRoomHandle(user,row){//打开好友聊天室
            if(row.type == 1){
                console.log(user.uuid," 当前选中的是好友:",row.name)
                var destination = '/own/' + user.uuid + '/' + row.uuid + '/messages';
                this.subscribeChatRoomMssages(user,destination)
            }else{
                console.log(user.uuid," 当前选中的是群:",row.name)
                var destination = '/topic/' + row.uuid + '/queue/messages';
                 //监听来自当前群消息
                 this.subscribeChatRoomMssages(user,destination)
            }
            let url = this.urlPrefix + "/chatRoom/getUserRoomMsg"
            let userRoomMsg = {
                uuid: user.uuid,
                roomId: row.uuid,
                type: row.type
            }
            //去获取历史聊天记录
            this.$post(url,userRoomMsg,null,resp => {
                let res = resp.data
                if(res.code == 200){
                    user.chatMsgList = [...res.data, ...user.chatMsgList]
                    console.log("历史聊天消息：",user.chatMsgList)
                    this.scrollToBottom()
                }else{
                    this.$message.error(res.msg)
                }
            })
        },
        sendMsgHandle(user){//发送消息
            if(user.openFriend.uuid == undefined) {
                console.log("发送的消息无接收者,发送者:",user.uuid)
                return
            }
            if(user.sendMsg == '' || user.sendMsg == undefined) {
                console.log("发送的消息无数据,发送者:",user.uuid)
                this.scrollToBottom()
                return
            }
            let message = {
                senderUuid: user.uuid,//发送者uuid,即当前登录用户uuid
                senderName: user.userName,//发送者姓名
                sendTime: this.$moment().format('MM-DD HH:mm:ss'),//发送时间
                sendContent: user.sendMsg,//发送内容
                receiverUuid: user.openFriend.uuid,//接受者uuid，选中当前聊天室的用户uuid
                receiverName: user.openFriend.name,//接受者名称，选中当前聊天室的用户名称
                msgType: user.openFriend.type//消息类型，单聊还是群聊,当前选中当前聊天室的类型
            };
            console.log(user.uuid," 发送的消息:",JSON.stringify(message))
            //如果是群发消息,那就不在添加自己发送的消息到列表
            user.openFriend.type == 1 ? user.chatMsgList.push(message) : ''
            user.stompClient.send('/app/user.sendMessage', {}, JSON.stringify(message));
            user.sendMsg = ''
            this.scrollToBottom()
        },
        subscribeChatRoomMssages(user,destination){//监听聊天室消息
            user.subscription = user.stompClient.subscribe(destination, function (message) {
                let msg = JSON.parse(message.body)
                let info = "新消息 => 当前账号:" + user.uuid + " ; 路径" + destination + " ; 接收者: " + msg.receiverUuid + " ; 发送者: " + msg.senderUuid;
                console.log(info)
                let chatMsg = {
                    senderUuid: msg.senderUuid,//发送者uuid
                    senderName: msg.senderName,//发送者姓名
                    sendTime: msg.sendTime,//发送时间
                    sendContent: msg.sendContent,//发送内容
                    receiverUuid: msg.receiverUuid,//接受者，即当前登录用户uuid
                    receiverName: msg.receiverName,
                    msgType: msg.msgType//消息类型，单聊还是群聊
                }
                if(msg.msgType == 1){
                    user.openFriend.uuid == msg.senderUuid ? user.chatMsgList.push(chatMsg) : ''
                }else{
                    user.openFriend.uuid == msg.receiverUuid ? user.chatMsgList.push(chatMsg) : ''
                }
                this.scrollToBottom()
            });
        },
        subscribeChatRoom(stompClient,user){//测试接口
            stompClient.subscribe('/own/A004/A003/messages', function (message) {
                // console.log('/own/测试接口消息: ' + message.body);
                // 处理接收到的消息
            });

            stompClient.subscribe('/user/A004/A003/messages', function (message) {
                // console.log('/user/测试接口消息: ' + message.body);
                // 处理接收到的消息
            });
        },
        scrollToBottom() {
            this.$nextTick(() => {
                
                var container1 = this.$refs.recMsgRef1;
                if(container1){
                    container1.scrollTop = container1.scrollHeight;
                }

                var container2 = this.$refs.recMsgRef2;
                if(container2){
                    container2.scrollTop = container2.scrollHeight;
                }

                var container3 = this.$refs.recMsgRef3;
                if(container3){
                    container3.scrollTop = container3.scrollHeight;
                }

                console.log(container1," = ",container2," = ",container3)
            });
        }
    }
};
</script>

<style lang="less" scoped>
.chatRoomTag{
    text-align: center;
    width: 100%;
    display: flex;
    flex-direction: row; /*弹性布局的方向*/
    .userTag{
        border: 1px red solid;
        flex-grow: 1;
        height: 800px;
        .headerHandleTag{
            width: 100%;
            display: flex;
            // flex-direction: row; /*弹性布局的方向*/
            .onlineChatTag{
                display: inline-block;
                width: 120px;
            }
            .chatUserTag{
                display: inline-block;
                width: 320px;
            }
            .userNameTag{
                display: inline-block;
                width: 100px;
            }
        }
        .bodyHandleTag{
            width: 100%;
            display: flex;
            .friendListTag{
                width: 120px;
            }
            .chatRoomMsgTag{
                // height: 100px;
                width: 415px;
                .selectLoginUserTag{
                    margin-top: 100px;
                }
                .chatMsgTag{
                    border: 1px red solid;
                    height: 700px;
                    .recMsgTag{
                        list-style-type: none;
                        list-style: none;
                        padding-left: 10px;
                        max-height: 700px;
                        overflow: scroll;
                        font-size: 12px;
                        .pointTag{
                            display: inline-block;
                            width: 3px;
                            height: 6px;
                            border-radius: 50%;
                            background-color: #0251fd;
                        }
                    }
                    .sendMsgTag{
                        display: flex;
                    }
                }
            }
        }
      
    }
}
.chat-room {
  height: 200px;
  overflow-y: scroll;
}
</style>