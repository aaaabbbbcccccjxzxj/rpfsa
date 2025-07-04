//导入相关函数
import { createRouter, createWebHashHistory } from 'vue-router'

//懒加载
const AdminLogin = () => import('../components/AdminLogin.vue')
const Home = () => import('../components/manage/Home.vue')
const User = () => import('../components/work/User.vue')
const Pet = () => import('../components/work/Pet.vue')
const AdminCenter = () => import('../components/work/AdminCenter.vue')
const Adopt = () => import('../components/work/Adopt.vue')
const Post = () => import('../components/work/Post.vue')
const Food = () => import('../components/work/Food.vue')
const Donate = () => import('../components/work/Donate.vue')
const Notice = () => import('../components/work/Notice.vue')
const routes = [
    {
        path: '/',
        component: AdminLogin
    },
    {
        path: '/home',
        component: Home,
        beforeEnter: (to, from, next) => {
            if (sessionStorage.getItem("adminname")) next()
            else router.replace('/')
        },
        children: [
            {
                path: 'user',
                component: User
            },
            {
                path: 'pet',
                component: Pet
            },
            {
                path: 'center',
                component: AdminCenter
            },
            {
                path: 'adopt',
                component: Adopt
            },
            {
                path: 'post',
                component: Post
            },
            {
                path: 'food',
                component: Food
            },
            {
                path: 'donate',
                component: Donate
            },
            {
                path: 'notice',
                component: Notice
            }
        ]
    },

]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})



export default router