<template>
    <div>
        <page-nav></page-nav>
        <el-divider content-position="center">'websocket 测试 demo</el-divider>
        <el-button size="small" @click="connet()">连接</el-button>
        <el-button size="small" @click="quit()">断开</el-button>
        <el-input v-model="sendMsg" size="small" @keydown.enter.native="send()" placeholder="输入聊天..." style="width: 15%;"></el-input>
        <el-button size="small" @click="send()">发送</el-button>
        <el-button size="small" @click="wsPush('A001')">模拟服务端推送消息</el-button>

        <page-nav></page-nav>
        <el-divider content-position="center">'websocket SockJS 测试 demo</el-divider>
        <el-button size="small" @click="wsConnet()">连接</el-button>
        <el-button size="small" @click="wsQuit()">断开</el-button>
        <el-input v-model="sendMsg" size="small" @keydown.enter.native="send()" placeholder="输入聊天..." style="width: 15%;"></el-input>
        <el-button size="small" @click="wsSend()">发送</el-button>
        <el-button size="small" @click="wsPush('A002')">模拟服务端推送消息</el-button>
    </div>
</template>

<script>
// import { Client, Message } from '@stomp/stompjs';

export default {
    name: 'StudyDemoWebSocket',

    data() {
        return {
            urlPrefix: 'http://localhost:8083',
            sendMsg: null,
            stompClient: null,
            subscription: null,
            wsStompClient: null
        };
    },

    created() {
    },

    mounted() {
    },

    methods: {
        wsPush(uuid){
            let url = this.urlPrefix + "/wsChat/wsPush";
            let data = {uuid: uuid}
            this.$get(url,data,null,resp => {
               console.log("推送成功:" + uuid)

            })
        },
        connet(){//websocket连接

            // 设置WebSocket连接以及Stomp客户端方式1
            // const socket = new WebSocket('ws://localhost:8080/chat');
            // this.stompClient = Stomp.over(socket);
            // 创建Stomp客户端方式2
            this.stompClient = Stomp.client('ws://localhost:8083/chat')

            // 连接成功的回调函数
            const onConnect = (frame) => {
                this.$message.success('连接成功')
                this.sub()
                // resolve(true)
            };
            // 连接错误的回调函数
            const onError  = (error) => {
                this.$message.error('连接失败')
                // resolve(false)
            };
            // 连接断开的回调函数
            const onDisconnect   = (offLine) => {
                this.$message.error('连接断开')
            };

            // 建立连接
            this.stompClient.connect({"Authorization": "A001","user": JSON.stringify({uuid:"A001"})}, onConnect, onError);

            // 监听客户端断开事件
            this.stompClient.onDisconnect = onDisconnect;
                
        },
        send(){
            this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({uuid:"A001"}));
        },
        sub(){//测试接口
            const vm = this;
            this.stompClient.subscribe('/websocket/msg/A001', function (message) {
                console.log('websocket => A001接收到反馈的消息: ' + message.body);
                vm.$message.info('[反馈]A001:' + JSON.parse(message.body).sendContent)
                // 处理接收到的消息
            });

            this.stompClient.subscribe('/websocket/public', function (message) {
                console.log('websocket => A001接收到public反馈的消息: ' + message.body);
                // 处理接收到的消息
            });

            this.stompClient.subscribe('/websocket/pushMsg/A001', function (message) {
                console.log('websocket => A001接收到推送的消息: ' + message.body);
                vm.$message.info('[推送]A001:' + JSON.parse(message.body).sendContent)
                // 处理接收到的消息
            });
        },
        quit(){//用户下线
            this.stompClient.disconnect(() => {
                this.$message.error('A001断开连接')
            });
        },

        // ========= SocketJs =========
        wsConnet(){
            
            let url = "ws://localhost:8083/chat";
            // 创建客户端 websocket 的实例
            var socket = new WebSocket(url);
            
            // 连接成功的回调函数
            const onConnect = (frame) => {
                this.$message.success('连接成功')
                this.wsSub()
                // resolve(true)
            };
            // 连接错误的回调函数
            const onError  = (error) => {
                this.$message.error('A001连接失败')
                // resolve(false)
            };
            // 连接断开的回调函数
            const onDisconnect   = (offLine) => {
                this.$message.error('A001连接断开')
            };

            this.wsStompClient = Stomp.over(socket);
            var headers = {
                'x-eb-token': 'eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTAzNTAsInBsYXRmb3JtQ29kZSI6IjIiLCJkZXZpY2VUeXBlIjoiMCIsImV4cCI6MTcxNDEzODA1M30.vc4zjvRyfthegY6eMVaxCKr9lKCWQKWMzl_anqLK2bg',
                "Authorization": "A002",
                "user": JSON.stringify({uuid:"A002"})
            };
            this.wsStompClient.connect(headers,onConnect,onError)

        },
        wsSub(){//测试接口
            const vm = this;
            this.wsStompClient.subscribe('/websocket/msg/A002', function (message) {
                console.log('websocket => A002接收到反馈的消息: ' + message.body);
                vm.$message.info('[反馈]A002:' + JSON.parse(message.body).sendContent)
                // 处理接收到的消息
            });
            
            this.wsStompClient.subscribe('/websocket/public', function (message) {
                console.log('websocket => A002接收到public反馈的消息: ' + message.body);
                // 处理接收到的消息
            });

            this.wsStompClient.subscribe('/websocket/pushMsg/A002', function (message) {
                console.log('websocket => A002接收到推送的消息: ' + message.body);
                vm.$message.info('[推送]A002:' + JSON.parse(message.body).sendContent)
                // 处理接收到的消息
            });
        },
        wsSend(){
            this.wsStompClient.send('/app/chat.sendMessage', {}, JSON.stringify({uuid:"A002"}));
        },
        wsQuit(){
            this.wsStompClient.disconnect(() => {
                this.$message.error('A002连接断开')
            })
        }
    }
    
};
</script>

<style lang="less" scoped>

</style>