// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
Vue.use(ElementUI);

//导入axios
import axios from "axios";
//导入MD5
import SparkMD5  from "spark-md5"
import md5 from 'js-md5';
Vue.prototype.$md5 = md5;

import moment from 'moment';
Vue.prototype.$moment = moment;

import Stomp from 'stompjs';

import SockJS from 'sockjs-client';
// 设置WebSocket连接以及Stomp客户端方式1
// const socket = new WebSocket('ws://localhost:8080/chat');
// const stompClient = Stomp.over(socket);
// Vue.prototype.$stompClient = stompClient;
// 创建Stomp客户端方式2
// Vue.prototype.$stompClient = Stomp.client('ws://localhost:8080/chat')

Vue.config.productionTip = false

// axios.interceptors.request.use(config =>{
//   config.url = "/demo" + config.url
//   return config;
// })

Vue.prototype.$get = function(url, data, header, callback){//这就相当于在Vue类库中添加了一个$get()方法
  axios({
    url: url,
    method: 'get',
    params: data, //params属性表示参数在url地址后面，SpringMvc可以通过RequestParam指定参数名进行读取
    headers: header,
  }).then(function (resp) {
    if(typeof callback === 'function'){
      callback(resp)
    }
  }).catch(error => {})
}

Vue.prototype.$post = function(url, data, header, callback){//这就相当于在Vue类库中添加了一个$get()方法
  axios({
    url: url,
    method: 'post',
    data: data, //data表示参数在请求（body）中，后端可以通过流直接读取，SpringMvc可以使用RequestBody注解获取
    headers: header,
    dataType:"json",
  }).then(function (resp) {
    if(typeof callback === 'function'){
      callback(resp)
    }
  }).catch(error => {})
}

//用来文件下载的
Vue.prototype.$downFile = function(url, data, header, callback){//这就相当于在Vue类库中添加了一个$get()方法
  axios({
    url: url,
    method: 'post',
    data: data, //data表示参数在请求（body）中，后端可以通过流直接读取，SpringMvc可以使用RequestBody注解获取
    headers: header,
    dataType:"json",
    responseType:'blob' //在请求中加上这一行，特别重要,用于表示返回来的数据是文件流，否则解析文件会有乱码
  }).then(function (resp) {
    if(typeof callback === 'function'){
      callback(resp)
    }
  }).catch(error => {})
}

//用来文件上传的
Vue.prototype.$upFile = function(url, data, header, callback){//这就相当于在Vue类库中添加了一个$get()方法
  let token = {
    'Content-Type': 'multipart/form-data'
  }
  let headers = Object.assign(token, header)
  axios({
    url: url,
    method: 'post',
    data: data, //data表示参数在请求（body）中，后端可以通过流直接读取，SpringMvc可以使用RequestBody注解获取
    headers: headers,
    dataType:"json"
  }).then(function (resp) {
    if(typeof callback === 'function'){
      callback(resp)
    }
  }).catch(error => {})
}

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
