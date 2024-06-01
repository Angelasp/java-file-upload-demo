<template>
  <div id="app">
    <FileUpload></FileUpload>
    <router-view></router-view>
  </div>
</template>

<script>
import FileUpload from './components/FileUpload.vue'
export default {
  components: { FileUpload },
  name: 'App'
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
</style>
