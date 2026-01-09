// import { createApp } from 'vue'
// import './style.css'
// import App from './App.vue'
import {router} from "./router";
import 'virtual:uno.css'

import 'virtual:svg-icons-register'
import SvgIcon from '@/components/svg/SvgIcon.vue'

import {createApp} from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

const app = createApp(App)

app.use(router)
app.use(ElementPlus)
app.component('SvgIcon', SvgIcon)
app.mount('#app')
