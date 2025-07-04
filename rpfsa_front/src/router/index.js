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
                beforeEnter: (to, from, next) => {
                    if (sessionStorage.getItem("user")) next()
                    else router.replace('/login')
                },
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
