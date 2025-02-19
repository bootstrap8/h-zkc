import {createRouter, createWebHistory, createWebHashHistory} from 'vue-router'

const routes = [
    {
        path: '',
        name: '缺省',
        component: () => import('@/views/config/main.vue')
    },
    {
        path: '/login',
        name: '登录',
        component: () => import('@/views/config/login.vue')
    },
    {
        path: '/main',
        name: '配置中心UI控制台',
        meta: {breadcrumb: ['配置管理', 'UI控制台']},
        component: () => import('@/views/config/main.vue')
    },
    {
        path: '/query',
        name: '配置查询',
        component: () => import('@/views/config/query.vue')
    },
    {
        path: '/history',
        name: '历史操作查询',
        component: () => import('@/views/config/history.vue')
    },
    {
        path: '/back',
        name: '备份恢复',
        component: () => import('@/views/config/back.vue')
    },
    {
        path: '/refresh',
        name: '刷新配置',
        component: () => import('@/views/config/refresh.vue')
    },
    {
        path: '/role',
        name: '角色管理',
        component: () => import('@/views/config/Role.vue')
    },
    {
        path: '/user',
        name: '用户管理',
        component: () => import('@/views/config/User.vue')
    },
    {
        path: '/menu',
        name: '菜单管理',
        component: () => import('@/views/config/Menu.vue')
    }
]

const router = createRouter({
    // history: createWebHistory(process.env.BASE_URL),
    history: createWebHashHistory(process.env.BASE_URL),
    routes
})

// router.beforeEach((to, from, next) => {
//     if (to.query.alllow=='true') {
//         next();
//     } else {
//         return false
//     }
// });

export default router
