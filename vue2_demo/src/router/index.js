import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import FileUpload from '@/components/FileUpload'
import ChatRoom from '@/components/ChatRoom'
import WebSocket from '@/components/Websocket'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
      path: '/FileUpload',
      name: 'FileUpload',
      component: FileUpload
    },
    {
      path: '/ChatRoom',
      name: 'ChatRoom',
      component: ChatRoom
    },
    {
      path: '/WebSocket',
      name: 'WebSocket',
      component: WebSocket
    }
  ]
})
