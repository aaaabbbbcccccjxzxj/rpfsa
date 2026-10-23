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
const Comment = () => import('../components/work/Comment.vue')
const Recharge = () => import('../components/work/Recharge.vue')
const routes = [
    {
        path: '/',
        component: AdminLogin
    },
    {
        path: '/home',
        component: Home,
        // 守卫必须显式放行或返回重定向目标：原先只调 router.replace 而不调 next()，
        // 会让导航一直处于 pending 状态，未登录访问后台页面时界面卡住无响应。
        beforeEnter: () => (sessionStorage.getItem('adminToken')
            ? true
            : { path: '/', replace: true }),
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
            },
            {
                path: 'comment',
                component: Comment
            },
            {
                path: 'recharge',
                component: Recharge
            }
        ]
    },

]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})



export default router