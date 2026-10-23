import { createRouter, createWebHashHistory } from "vue-router"

const routes = [
    {
        path: '/',
        redirect: '/introduce',
        component: () => import('../components/home/Rpfsa.vue'),
        children: [
            {
                path: '/introduce',
                component: () => import('../components/homechild/Introduce.vue')
            },
            {
                path: '/about',
                component: () => import('../components/homechild/About.vue')
            },
            {
                path: '/thank',
                component: () => import('../components/homechild/Thank.vue')
            },
            {
                path: '/dogandcat',
                component: () => import('../components/homechild/DogandCat.vue')
            },
            {
                path: '/usercenter',
                component: () => import('../components/homechild/UserCenter.vue'),
                // 守卫必须显式放行或返回重定向目标：原先只调 router.replace 而不调 next()，
                // 会让导航一直处于 pending 状态，未登录访问时页面卡住无响应。
                beforeEnter: () => (sessionStorage.getItem('token')
                    ? true
                    : { path: '/login', replace: true }),
            },
            {
                path: '/community',
                component: () => import('../components/homechild/Community.vue')
            },
            {
                path: '/postdetail/:poid',
                component: () => import('../components/homechild/PostDetail.vue'),
                props: true
            },
            {
                path: '/petdetail/:pid',
                component: () => import('../components/homechild/PetDetail.vue'),
                props: true
            },
            {
                path: '/noticelist/:classic',
                component: () => import('../components/homechild/NoticeList.vue'),
                props: true
            },
            {
                path: '/diary/:pid',
                component: () => import('../components/homechild/Diary.vue'),
                props: true
            },
            {
                path: '/messages',
                component: () => import('../components/homechild/MessageCenter.vue'),
                beforeEnter: () => (sessionStorage.getItem('token')
                    ? true
                    : { path: '/login', replace: true }),
            }
        ]
    },
    {
        path: '/login',
        component: () => import('../components/home/UserLogin.vue'),
    }

]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router
