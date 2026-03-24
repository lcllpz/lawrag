<template>
  <div class="register-page">
    <div class="rp-bg-grid"></div>
    <div class="rp-orb rp-orb-1"></div>
    <div class="rp-orb rp-orb-2"></div>

    <!-- Left brand panel -->
    <div class="rp-left">
      <div class="rp-deco-s" aria-hidden="true">§</div>
      <div class="rp-brand">
        <svg class="rp-scales" viewBox="0 0 56 56" fill="none">
          <line x1="28" y1="6" x2="28" y2="50" stroke="#b8943f" stroke-width="2"/>
          <line x1="8" y1="12" x2="48" y2="12" stroke="#b8943f" stroke-width="2"/>
          <path d="M8 12 L3 28 Q8 34 13 28 L8 12Z" fill="none" stroke="#b8943f" stroke-width="1.8" stroke-linejoin="round"/>
          <path d="M48 12 L43 28 Q48 34 53 28 L48 12Z" fill="none" stroke="#b8943f" stroke-width="1.8" stroke-linejoin="round"/>
          <line x1="19" y1="50" x2="37" y2="50" stroke="#b8943f" stroke-width="2"/>
        </svg>
        <div class="rp-brand-name"><em>AdmissionRAG</em></div>
      </div>
      <div class="rp-gold-rule"></div>
      <h2 class="rp-headline">加入平台<br/>开启招生<br/>咨询之旅</h2>
      <ul class="rp-bullets">
        <li><span class="rp-bullet-dash">—</span> 权威招生资料知识库</li>
        <li><span class="rp-bullet-dash">—</span> 双路检索 · 精排融合</li>
        <li><span class="rp-bullet-dash">—</span> 每条回答附带出处</li>
        <li><span class="rp-bullet-dash">—</span> 对话记录云端留存</li>
      </ul>
    </div>

    <!-- Right form panel -->
    <div class="rp-right">
      <div class="rp-form-wrap">
        <div class="rp-form-header">
          <div class="rp-form-label">CREATE ACCOUNT</div>
          <h3 class="rp-form-title">创建新账号</h3>
          <p class="rp-form-sub">加入 AdmissionRAG · 智能招生问答平台</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          label-position="top"
          class="rp-form"
        >
          <el-row :gutter="14">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="3-20 个字符"
                  :prefix-icon="User"
                  class="rp-input"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="昵称" prop="nickname">
                <el-input
                  v-model="form.nickname"
                  placeholder="显示名称（选填）"
                  :prefix-icon="UserFilled"
                  class="rp-input"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="可选，用于找回密码"
              :prefix-icon="Phone"
              class="rp-input"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="6-20 个字符"
              :prefix-icon="Lock"
              show-password
              class="rp-input"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              :prefix-icon="Lock"
              show-password
              class="rp-input"
            />
          </el-form-item>

          <button
            class="rp-submit-btn"
            :class="{ loading }"
            :disabled="loading"
            type="button"
            @click="handleRegister"
          >
            <span v-if="!loading">立 即 注 册</span>
            <span v-else class="rp-loading-dots">
              <span></span><span></span><span></span>
            </span>
          </button>
        </el-form>

        <div class="rp-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="rp-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, UserFilled, Phone, Lock } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPwd = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userApi.register({
      username: form.username,
      password: form.password,
      phone: form.phone || undefined,
      nickname: form.nickname || undefined
    })
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch {
    // 错误已在 request.ts 中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== Variables ===== */
.register-page {
  --navy: #0c1322;
  --navy-panel: #0f1829;
  --gold: #b8943f;
  --gold-light: #d4aa5a;
  --ivory: #f0e6cc;
  --ff-brand: Palatino, "Palatino Linotype", Georgia, serif;
  --ff-zh: "Songti SC", "Source Han Serif SC", "Noto Serif CJK SC", SimSun, STSong, serif;
  --ff-body: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;

  min-height: 100vh;
  display: flex;
  background: var(--navy);
  font-family: var(--ff-body);
  position: relative;
  overflow: hidden;
}

/* Background grid */
.rp-bg-grid {
  position: absolute; inset: 0; pointer-events: none;
  background-image:
    linear-gradient(rgba(184,148,63,0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(184,148,63,0.05) 1px, transparent 1px);
  background-size: 56px 56px;
}

/* Ambient orbs */
.rp-orb {
  position: absolute; border-radius: 50%;
  filter: blur(72px); pointer-events: none;
}
.rp-orb-1 {
  width: 480px; height: 480px; top: -120px; right: 20%;
  background: radial-gradient(circle, rgba(184,148,63,0.1) 0%, transparent 70%);
}
.rp-orb-2 {
  width: 360px; height: 360px; bottom: -80px; left: 30%;
  background: radial-gradient(circle, rgba(59,130,246,0.08) 0%, transparent 70%);
}

/* ===== Left brand panel ===== */
.rp-left {
  position: relative;
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 52px;
  border-right: 1px solid rgba(184,148,63,0.15);
  overflow: hidden;
  animation: slideInLeft 0.7s ease both;
}
@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to   { opacity: 1; transform: translateX(0); }
}

.rp-deco-s {
  position: absolute;
  top: -30px; left: -10px;
  font-size: 240px;
  font-family: var(--ff-brand);
  color: rgba(184,148,63,0.05);
  line-height: 1;
  pointer-events: none;
  user-select: none;
}

.rp-brand {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 28px;
}
.rp-scales { width: 36px; height: 36px; flex-shrink: 0; }
.rp-brand-name {
  font-family: var(--ff-brand);
  font-size: 26px;
  font-style: italic;
  color: rgba(255,255,255,0.9);
  letter-spacing: 0.5px;
}

.rp-gold-rule {
  width: 40px; height: 1px;
  background: linear-gradient(90deg, var(--gold), transparent);
  margin-bottom: 28px;
}

.rp-headline {
  font-family: var(--ff-zh);
  font-size: 30px;
  font-weight: 700;
  color: #fff;
  line-height: 1.5;
  margin: 0 0 32px;
  letter-spacing: 1px;
}

.rp-bullets {
  list-style: none;
  margin: 0; padding: 0;
  display: flex; flex-direction: column; gap: 14px;
}
.rp-bullets li {
  display: flex; align-items: center; gap: 10px;
  font-size: 14px;
  color: rgba(255,255,255,0.5);
  letter-spacing: 0.3px;
}
.rp-bullet-dash {
  color: var(--gold);
  font-size: 18px;
  flex-shrink: 0;
  line-height: 1;
}

/* ===== Right form panel ===== */
.rp-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 60px;
  animation: fadeUp 0.7s 0.15s ease both;
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

.rp-form-wrap {
  width: 100%;
  max-width: 480px;
}

.rp-form-header { margin-bottom: 32px; }

.rp-form-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--gold);
  text-transform: uppercase;
  margin-bottom: 10px;
}

.rp-form-title {
  font-family: var(--ff-zh);
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px;
  letter-spacing: 0.5px;
}

.rp-form-sub {
  font-size: 13px;
  color: rgba(255,255,255,0.35);
  margin: 0;
  letter-spacing: 0.3px;
}

/* Form overrides */
.rp-form :deep(.el-form-item__label) {
  color: rgba(255,255,255,0.45) !important;
  font-size: 12px !important;
  letter-spacing: 0.5px !important;
  padding-bottom: 6px !important;
}

.rp-input :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.04) !important;
  box-shadow: none !important;
  border: none !important;
  border-bottom: 1px solid rgba(255,255,255,0.12) !important;
  border-radius: 0 !important;
  padding: 0 4px !important;
  transition: border-color 0.25s !important;
}
.rp-input :deep(.el-input__wrapper:hover),
.rp-input :deep(.el-input__wrapper.is-focus) {
  border-bottom-color: var(--gold) !important;
}
.rp-input :deep(.el-input__inner) {
  color: rgba(255,255,255,0.9) !important;
  font-size: 14px !important;
  height: 38px !important;
  background: transparent !important;
}
.rp-input :deep(.el-input__inner::placeholder) { color: rgba(255,255,255,0.2) !important; }
.rp-input :deep(.el-input__prefix) { color: rgba(184,148,63,0.6) !important; }
.rp-input :deep(.el-input__suffix) { color: rgba(255,255,255,0.3) !important; }

/* Submit button */
.rp-submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 8px;
  background: var(--gold);
  color: var(--navy);
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 3px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.25s, transform 0.2s, box-shadow 0.2s;
  font-family: var(--ff-body);
}
.rp-submit-btn:hover:not(:disabled) {
  background: var(--gold-light);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(184,148,63,0.3);
}
.rp-submit-btn:disabled { opacity: 0.7; cursor: not-allowed; }

/* Loading dots */
.rp-loading-dots {
  display: inline-flex; align-items: center; gap: 5px;
}
.rp-loading-dots span {
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--navy); opacity: 0.7;
  animation: dotBounce 1.2s infinite;
}
.rp-loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.rp-loading-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.8); }
  40%            { transform: scale(1.2); }
}

/* Footer */
.rp-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: rgba(255,255,255,0.3);
}
.rp-link {
  color: var(--gold-light);
  text-decoration: none;
  margin-left: 6px;
  font-weight: 500;
  transition: color 0.2s;
}
.rp-link:hover { color: #fff; }

/* Responsive */
@media (max-width: 768px) {
  .rp-left { display: none; }
  .rp-right { padding: 40px 24px; }
}
</style>
