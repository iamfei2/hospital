import VueRouter from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'LoginA',
    component: () => import('../components/LoginA')
  },
  {
    path: '/IndexA',
    name: 'IndexA',
    component: () => import('../components/IndexA'),
    children: [
      {
        path: 'profile',
        name: 'Profile',
        meta: { title: '个人信息' },
        component: () => import('../components/ProfileA')
      },
      {
        path: 'home',
        name: 'Home',
        meta: { title: '首页' },
        component: () => import('../components/DefaultA')
      },
      {
        path: 'medicalOrder',
        name: 'MedicalOrder',
        meta: { title: '医嘱信息' },
        component: () => import('../components/MedicalOrderA')
      },
      {
        path: 'labResult',
        name: 'LabResult',
        meta: { title: '检验结果' },
        component: () => import('../components/LabResultA')
      },
      {
        path: 'patientInfo',
        name: 'PatientInfo',
        meta: { title: '患者信息' },
        component: () => import('../components/PatientInfoA')
      },
      {
        path: 'pathology',
        name: 'Pathology',
        meta: { title: '病理检查' },
        component: () => import('../components/PathologyA')
      },
      {
        path: 'check/ct',
        name: 'Ct',
        meta: { title: 'CT检查' },
        component: () => import('../components/CtA')
      },
      {
        path: 'check/mri',
        name: 'Mri',
        meta: { title: '核磁检查' },
        component: () => import('../components/MriA')
      },
      {
        path: 'check/colonoscopy',
        name: 'Enteroscopy',
        meta: { title: '肠镜检查' },
        component: () => import('../components/EnteroscopyA')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        meta: { title: '统计分析' },
        component: () => import('../components/StatisticsA')
      },
      {
        path: 'operationLog',
        name: 'OperationLog',
        meta: { title: '操作日志' },
        component: () => import('../components/OperationLogA')
      },
      {
        path: 'labItemDict',
        name: 'LabItemDict',
        meta: { title: '参考范围管理' },
        component: () => import('../components/LabItemDictA')
      },
      {
        path: 'warningRule',
        name: 'WarningRule',
        meta: { title: '预警规则配置' },
        component: () => import('../components/WarningRuleA')
      },
      {
        path: 'warningRecord',
        name: 'WarningRecord',
        meta: { title: '预警记录查询' },
        component: () => import('../components/WarningRecordA')
      },
      {
        path: 'backup',
        name: 'Backup',
        meta: { title: '数据备份与恢复' },
        component: () => import('../components/BackupA')
      },
      {
        path: 'searchTemplate',
        name: 'SearchTemplate',
        meta: { title: '查询模板管理' },
        component: () => import('../components/SearchTemplateA')
      }
    ]
  },
];

const router = new VueRouter({
  mode: 'history',
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');

  // 登录页直接放行
  if (to.path === '/') {
    next();
    return;
  }

  // 其他页面需要Token
  if (!token) {
    next('/');
    return;
  }

  next();
});

export default router;
