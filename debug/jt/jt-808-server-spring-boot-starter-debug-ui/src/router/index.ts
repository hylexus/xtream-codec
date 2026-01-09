import {createRouter, createWebHistory} from 'vue-router'
import MessageDecoding from "../pages/message-decoding.vue";
import Home from "../pages/home.vue";
import MessageEncoding from "../pages/message-encoding.vue";
import FieldTest from "@/pages/field-test.vue";
import DynamicEncoding from "@/pages/dynamic-encoding.vue";

const routes = [
    {path: '/home', component: Home},
    {path: '/message-decoding', component: MessageDecoding},
    {path: '/message-encoding', component: MessageEncoding},
    {path: '/dynamic-encoding', component: DynamicEncoding},
    {path: '/field-test', component: FieldTest},
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
})
