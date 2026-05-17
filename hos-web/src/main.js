import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/global.css';
import axios from "axios";
import VueRouter from 'vue-router';
import router from './router'


// 配置axios: 开发环境用localhost，生产/Docker用相对路径
axios.defaults.baseURL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8090';

// 请求拦截器 - 添加Token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理Token过期
axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response && error.response.status === 401) {
      // Token过期，清除本地存储并跳转登录页
      localStorage.removeItem('token');
      localStorage.removeItem('CurUser');
      router.push('/');
    }
    return Promise.reject(error);
  }
);

Vue.prototype.$axios = axios;
Vue.config.productionTip = false
Vue.use(VueRouter);
Vue.use(ElementUI,{ size: 'small'});

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
