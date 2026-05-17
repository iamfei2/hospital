<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-left">
        <div class="login-left-content">
          <h2>医院检查数据管理平台</h2>
          <p>统一管理、智能分析、安全追溯</p>
          <div class="features">
            <div class="feature-item">
              <i class="el-icon-document-checked"></i>
              <span>多类型检查数据</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-data-analysis"></i>
              <span>智能统计分析</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-lock"></i>
              <span>数据安全追溯</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-form-container">
          <h3 class="welcome-text">欢迎登录</h3>
          <p class="login-desc">请输入您的账号密码登录系统</p>
          
          <el-form
              :model="loginForm"
              :rules="rules"
              ref="loginForm"
              class="login-form"
          >
            <el-form-item prop="loginAccount">
              <el-input
                  type="text"
                  autocomplete="off"
                  size="large"
                  v-model="loginForm.loginAccount"
                  placeholder="请输入账号"
                  prefix-icon="el-icon-user"
                  @keyup.enter.native="confirm"
              ></el-input>
            </el-form-item>
            
            <el-form-item prop="loginPassword">
              <el-input
                  type="password"
                  show-password
                  autocomplete="off"
                  size="large"
                  v-model="loginForm.loginPassword"
                  placeholder="请输入密码"
                  prefix-icon="el-icon-lock"
                  @keyup.enter.native="confirm"
              ></el-input>
            </el-form-item>
            
            <el-form-item prop="captcha">
              <div class="captcha-row">
                <el-input
                    type="text"
                    autocomplete="off"
                    size="large"
                    v-model="loginForm.captcha"
                    placeholder="验证码"
                    class="captcha-input"
                    @keyup.enter.native="confirm"
                ></el-input>
                <canvas 
                    class="captcha-canvas" 
                    @click="refreshCaptcha" 
                    title="点击刷新" 
                    ref="captchaCanvas" 
                    width="120" 
                    height="44"
                ></canvas>
              </div>
            </el-form-item>
            
            <div class="login-options">
              <el-checkbox v-model="rememberMe" size="small">记住账号</el-checkbox>
              <span class="register-link" @click="showRegisterDialog">没有账号？立即注册</span>
            </div>
            
            <el-button
                type="primary"
                @click="confirm"
                :loading="loading"
                :disabled="loading"
                class="login-btn"
                size="large"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form>
          
          <div class="login-footer">
            <span>© 2026 医院检查数据管理平台</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog
        title="用户注册"
        :visible.sync="registerDialogVisible"
        width="420px"
        :close-on-click-modal="false"
        append-to-body
    >
      <el-form
          :model="registerForm"
          :rules="registerRules"
          ref="registerForm"
          label-width="80px"
      >
        <el-form-item label="账号" prop="loginAccount">
          <el-input
              v-model="registerForm.loginAccount"
              placeholder="请输入账号"
              maxlength="20"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="loginPassword">
          <el-input
              type="password"
              v-model="registerForm.loginPassword"
              placeholder="请输入密码"
              maxlength="20"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              type="password"
              v-model="registerForm.confirmPassword"
              placeholder="请再次输入密码"
              maxlength="20"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="userName">
          <el-input
              v-model="registerForm.userName"
              placeholder="请输入姓名"
              maxlength="20"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="registerDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registerLoading">注 册</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { encryptPassword, encryptData, decryptData } from '@/utils/crypto'
import { Message } from 'element-ui'

export default {
  name: "LoginA",
  data() {
    const validateCaptcha = (rule, value, callback) => {
      if (!value || value.toLowerCase() !== this.captchaCode.toLowerCase()) {
        callback(new Error('验证码错误'))
      } else {
        callback()
      }
    }
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.loginPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      rememberMe: false,
      captchaCode: '',
      loginForm: {
        loginAccount: '',
        loginPassword: '',
        captcha: ''
      },
      rules: {
        loginAccount: [
          { required: true, message: '请输入账号', trigger: 'blur' }
        ],
        loginPassword: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        captcha: [
          { validator: validateCaptcha, trigger: 'blur' }
        ]
      },
      registerDialogVisible: false,
      registerLoading: false,
      registerForm: {
        loginAccount: '',
        loginPassword: '',
        confirmPassword: '',
        userName: ''
      },
      registerRules: {
        loginAccount: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' }
        ],
        loginPassword: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        userName: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { max: 20, message: '姓名长度不能超过20个字符', trigger: 'blur' }
        ]
      }
    };
  },
  mounted() {
    this.checkRememberMe()
    this.refreshCaptcha()
  },
  methods: {
    refreshCaptcha() {
      const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
      let code = ''
      for (let i = 0; i < 4; i++) {
        code += chars.charAt(Math.floor(Math.random() * chars.length))
      }
      this.captchaCode = code
      this.$nextTick(() => {
        this.drawCaptcha()
      })
    },
    drawCaptcha() {
      const canvas = this.$refs.captchaCanvas
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const width = canvas.width
      const height = canvas.height
      
      ctx.clearRect(0, 0, width, height)
      
      ctx.fillStyle = this.getRandomColor(200, 240)
      ctx.fillRect(0, 0, width, height)
      
      for (let i = 0; i < 4; i++) {
        ctx.strokeStyle = this.getRandomColor(100, 200)
        ctx.lineWidth = Math.random() * 2
        ctx.beginPath()
        ctx.moveTo(Math.random() * width, Math.random() * height)
        ctx.lineTo(Math.random() * width, Math.random() * height)
        ctx.stroke()
      }
      
      for (let i = 0; i < 30; i++) {
        ctx.fillStyle = this.getRandomColor(0, 255)
        ctx.beginPath()
        ctx.arc(Math.random() * width, Math.random() * height, Math.random() * 2, 0, Math.PI * 2)
        ctx.fill()
      }
      
      const chars = this.captchaCode.split('')
      chars.forEach((char, index) => {
        ctx.font = `${16 + Math.random() * 8}px Arial`
        ctx.fillStyle = this.getRandomColor(30, 120)
        ctx.textBaseline = 'middle'
        const x = 10 + index * 25 + (Math.random() - 0.5) * 6
        const y = height / 2 + (Math.random() - 0.5) * 6
        const angle = (Math.random() - 0.5) * 0.3
        ctx.save()
        ctx.translate(x, y)
        ctx.rotate(angle)
        ctx.fillText(char, 0, 0)
        ctx.restore()
      })
    },
    getRandomColor(min, max) {
      const r = Math.floor(Math.random() * (max - min) + min)
      const g = Math.floor(Math.random() * (max - min) + min)
      const b = Math.floor(Math.random() * (max - min) + min)
      return `rgb(${r}, ${g}, ${b})`
    },
    checkRememberMe() {
      const rememberData = localStorage.getItem('rememberMe')
      if (rememberData) {
        try {
          const userData = decryptData(rememberData)
          if (userData && userData.loginAccount) {
            this.loginForm.loginAccount = userData.loginAccount
            this.rememberMe = true
            if (userData.loginPassword) {
              this.loginForm.loginPassword = userData.loginPassword
            }
          }
        } catch (e) {
          localStorage.removeItem('rememberMe')
        }
      }
    },
    saveRememberMe() {
      if (this.rememberMe) {
        const userData = {
          loginAccount: this.loginForm.loginAccount,
          loginPassword: this.loginForm.loginPassword
        }
        localStorage.setItem('rememberMe', encryptData(userData))
      } else {
        localStorage.removeItem('rememberMe')
      }
    },
    confirm() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          
          const loginData = {
            loginAccount: this.loginForm.loginAccount,
            loginPassword: encryptPassword(this.loginForm.loginPassword)
          }
          
          this.$axios.post('/user/login', loginData)
            .then(res => res.data)
            .then(res => {
              this.loading = false
              if (res.code === 200) {
                this.saveRememberMe()
                // 保存Token
                localStorage.setItem('token', res.data.token)
                // 保存用户信息
                sessionStorage.setItem('CurUser', JSON.stringify(res.data.user))
                Message.success('登录成功')
                this.$router.replace('/IndexA')
              } else {
                this.refreshCaptcha()
                Message.error(res.msg || '用户名或密码错误')
              }
            })
            .catch(() => {
              this.loading = false
              this.refreshCaptcha()
              Message.error('登录失败，请稍后重试')
            })
        }
      });
    },
    showRegisterDialog() {
      this.registerDialogVisible = true
      this.$nextTick(() => {
        this.$refs.registerForm.resetFields()
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.registerLoading = true
          
          const registerData = {
            loginAccount: this.registerForm.loginAccount,
            loginPassword: encryptPassword(this.registerForm.loginPassword),
            userName: this.registerForm.userName
          }
          
          this.$axios.post('/user/register', registerData)
            .then(res => res.data)
            .then(res => {
              this.registerLoading = false
              if (res.code === 200) {
                Message.success('注册成功，请登录')
                this.registerDialogVisible = false
              } else {
                Message.error(res.msg || '注册失败')
              }
            })
            .catch(() => {
              this.registerLoading = false
              Message.error('注册失败，请稍后重试')
            })
        }
      })
    }
  }
};
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  background-color: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-wrapper {
  width: 960px;
  height: 500px;
  display: flex;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-left-content {
  color: #fff;
  text-align: left;
}

.login-left-content h2 {
  font-size: 26px;
  font-weight: 600;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.login-left-content p {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.95;
}

.feature-item i {
  font-size: 18px;
}

.login-right {
  width: 440px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form-container {
  width: 100%;
}

.welcome-text {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.login-desc {
  font-size: 13px;
  color: #888;
  margin-bottom: 30px;
}

.login-form {
  width: 100%;
}

.captcha-row {
  display: flex;
  gap: 10px;
}

.captcha-input {
  flex: 1;
}

.captcha-canvas {
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.register-link {
  color: #1890ff;
  cursor: pointer;
  font-size: 13px;
}

.register-link:hover {
  color: #40a9ff;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  font-size: 12px;
  color: #bbb;
}

/* 响应式 */
@media (max-width: 960px) {
  .login-wrapper {
    width: 420px;
    height: auto;
  }
  
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
    padding: 40px 30px;
  }
}
</style>
