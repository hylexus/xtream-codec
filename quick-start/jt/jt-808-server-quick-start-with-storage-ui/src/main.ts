// import { createApp } from 'vue'
// import './style.css'
// import App from './App.vue'
import 'virtual:uno.css'
// createApp(App).mount('#app')


import {createApp} from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

const app = createApp(App)

app.use(ElementPlus)
app.mount('#app')