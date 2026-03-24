<template>
  <div class="auth-root">
    <div class="grid-bg"></div>
    <div class="orb orb-a"></div>
    <div class="orb orb-b"></div>

    <div class="auth-card">

      <!-- ═══ LEFT PANEL ═══ -->
      <div class="panel-l">
        <div class="top-accent"></div>
        <div class="deco-sect">§</div>

        <div class="panel-l-body">
          <div class="brand">
            <svg class="brand-scales" viewBox="0 0 52 56" fill="none">
              <line x1="26" y1="4" x2="26" y2="49" stroke="currentColor" stroke-width="2"/>
              <line x1="4"  y1="14" x2="48" y2="14" stroke="currentColor" stroke-width="2"/>
              <circle cx="26" cy="12" r="2.5" fill="currentColor"/>
              <line x1="4"  y1="14" x2="4"  y2="29" stroke="currentColor" stroke-width="1.5"/>
              <path d="-1 29 Q4 36 9 29"    stroke="currentColor" stroke-width="1.5" fill="none"/>
              <line x1="48" y1="14" x2="48" y2="29" stroke="currentColor" stroke-width="1.5"/>
              <path d="M42 29 Q48 36 54 29" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M18 49 L34 49"       stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
              <path d="M15 53 L37 53"       stroke="currentColor" stroke-width="2"   stroke-linecap="round"/>
            </svg>
            <span class="brand-name">AdmissionRAG</span>
          </div>

          <div class="eyebrow-en">INTELLIGENT ADMISSION ASSISTANT</div>

          <h1 class="zh-headline">智能招生<br>问答平台</h1>

          <p class="intro-text">
            融合多路向量检索、BM25全文检索与大语言模型，
            基于招生章程与录取资料提供有据可查的专业招生信息解答。
          </p>

          <div class="gold-rule"></div>

          <ul class="feat-list">
            <li v-for="f in features" :key="f">
              <span class="feat-em">—</span>{{ f }}
            </li>
          </ul>

          <p class="panel-foot">© 2026 AdmissionRAG &nbsp;·&nbsp; 智能招生问答平台</p>
        </div>
      </div>

      <!-- ═══ RIGHT PANEL ═══ -->
      <div class="panel-r">
        <div class="form-wrap">
          <p class="form-eyebrow">ADMISSIONRAG PLATFORM</p>
          <h2 class="form-title">欢迎登录</h2>
          <div class="title-rule"></div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            size="large"
            @keyup.enter="handleLogin"
          >
            <div class="field-block">
              <span class="field-label">用户名</span>
              <el-form-item prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  clearable
                  class="flat-input"
                />
              </el-form-item>
            </div>

            <div class="field-block">
              <span class="field-label">密码</span>
              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                  class="flat-input"
                />
              </el-form-item>
            </div>

            <div class="row-opts">
              <el-checkbox v-model="rememberMe" class="check-me">记住我</el-checkbox>
              <el-button link @click="router.push('/forgot-password')" class="link-forgot">
                忘记密码
              </el-button>
            </div>

            <button
              class="submit-btn"
              :class="{ 'is-loading': loading }"
              :disabled="loading"
              type="button"
              @click="handleLogin"
            >
              <span v-if="!loading">立&emsp;即&emsp;登&emsp;录</span>
              <span v-else class="dot-loader"><i></i><i></i><i></i></span>
            </button>
          </el-form>

          <p class="reg-row">
            还没有账号？
            <router-link to="/register" class="reg-link">立即注册</router-link>
          </p>

          <div class="demo-zone">
            <div class="demo-sep">
              <span class="sep-line"></span>
              <span class="sep-txt">DEMO ACCOUNTS</span>
              <span class="sep-line"></span>
            </div>
            <div
              v-for="acc in demoAccounts"
              :key="acc.username"
              class="demo-row"
              @click="fillAccount(acc)"
            >
              <span class="d-badge" :data-admin="acc.type === 'danger'">{{ acc.label }}</span>
              <code class="d-cred">{{ acc.username }} / {{ acc.password }}</code>
              <span class="d-tap">↵</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router    = useRouter()
const route     = useRoute()
const userStore = useUserStore()

const formRef    = ref<FormInstance>()
const loading    = ref(false)
const rememberMe = ref(false)

const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码',   trigger: 'blur' }]
}

const features = [
  '多路召回 + RRF 融合检索算法',
  'Cross-Encoder 精准重排序',
  '招生信息安全兜底与风险识别机制',
  '来源可溯，每条引用有据可查'
]

const demoAccounts = [
  { username: 'admin',  password: '123456', label: '管理员', type: 'danger' },
  { username: 'user01', password: '123456', label: '用户',   type: '' }
]

const fillAccount = (acc: { username: string; password: string }) => {
  form.username = acc.username
  form.password = acc.password
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login({ username: form.username, password: form.password })
    ElMessage.success('登录成功，欢迎使用 AdmissionRAG！')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // error handled in request.ts
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ─── Variables ─── */
.auth-root {
  --navy:      #0c1322;
  --navy-s:    #111a2c;
  --navy-m:    #18243a;
  --border:    #1e2e46;
  --gold:      #b8943f;
  --gold-l:    #d4af58;
  --gold-d:    rgba(184,148,63,.13);
  --ivory:     #f0e6cc;
  --ivory-d:   #8c7f6a;
  --slate:     #5a6a80;
  --ff-serif:  'Palatino Linotype', Palatino, 'Book Antiqua', Georgia, serif;
  --ff-zh:     'Songti SC', 'STSong', 'FangSong', 'SimSun', serif;
  --ff-body:   'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei UI', 'Microsoft YaHei', sans-serif;
  --ff-mono:   'SF Mono', 'Cascadia Code', Consolas, monospace;

  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--navy);
  background-image:
    linear-gradient(rgba(255,255,255,.022) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,.022) 1px, transparent 1px);
  background-size: 50px 50px;
  font-family: var(--ff-body);
  position: relative;
  overflow: hidden;
}

.grid-bg { position: absolute; inset: 0; pointer-events: none; }

.orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(90px);
}
.orb-a {
  width: 560px; height: 560px;
  top: -220px; left: -120px;
  background: radial-gradient(circle, rgba(18,38,82,.9), transparent);
  opacity: .7;
}
.orb-b {
  width: 420px; height: 420px;
  bottom: -120px; right: -80px;
  background: radial-gradient(circle, rgba(184,148,63,.18), transparent);
}

/* ─── Card ─── */
.auth-card {
  display: flex;
  width: min(960px, calc(100vw - 40px));
  min-height: 580px;
  border: 1px solid var(--border);
  box-shadow: 0 40px 100px rgba(0,0,0,.65), inset 0 0 0 1px rgba(255,255,255,.03);
  animation: cardUp .55s cubic-bezier(.22,.68,0,1.1) both;
}
@keyframes cardUp {
  from { opacity: 0; transform: translateY(30px) scale(.975); }
  to   { opacity: 1; transform: translateY(0)   scale(1); }
}

/* ─── Left Panel ─── */
.panel-l {
  flex: 1.15;
  background: var(--navy-s);
  position: relative;
  overflow: hidden;
  border-right: 1px solid var(--border);
}
.top-accent {
  height: 3px;
  background: linear-gradient(90deg, var(--gold) 0%, transparent 70%);
}
.deco-sect {
  position: absolute;
  right: -28px; top: 45%;
  transform: translateY(-50%);
  font-size: 300px;
  color: var(--gold);
  opacity: .035;
  font-family: var(--ff-serif);
  font-style: italic;
  line-height: 1;
  pointer-events: none;
  user-select: none;
}
.panel-l-body {
  padding: 44px 48px;
  height: calc(100% - 3px);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  animation: fromLeft .55s .1s both;
}
@keyframes fromLeft {
  from { opacity: 0; transform: translateX(-18px); }
  to   { opacity: 1; transform: translateX(0); }
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 30px;
}
.brand-scales {
  width: 40px; height: 44px;
  color: var(--gold);
  flex-shrink: 0;
}
.brand-name {
  font-family: var(--ff-serif);
  font-style: italic;
  font-size: 30px;
  font-weight: 700;
  color: var(--ivory);
  letter-spacing: .07em;
}

.eyebrow-en {
  font-size: 10px;
  letter-spacing: .22em;
  color: var(--gold);
  text-transform: uppercase;
  font-weight: 600;
  margin-bottom: 18px;
}
.zh-headline {
  font-family: var(--ff-zh);
  font-size: 31px;
  font-weight: 600;
  color: var(--ivory);
  line-height: 1.45;
  margin-bottom: 18px;
}
.intro-text {
  font-size: 13.5px;
  color: var(--ivory-d);
  line-height: 1.9;
  margin-bottom: 26px;
}
.gold-rule {
  height: 1px;
  background: linear-gradient(90deg, var(--gold) 0%, transparent 55%);
  margin-bottom: 22px;
}
.feat-list {
  list-style: none;
  padding: 0; margin: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.feat-list li {
  display: flex;
  align-items: baseline;
  gap: 10px;
  font-size: 13.5px;
  color: #7a8fa6;
}
.feat-em {
  color: var(--gold);
  font-size: 16px;
  flex-shrink: 0;
  line-height: 1;
}
.panel-foot {
  margin-top: 30px;
  font-size: 11px;
  color: var(--slate);
  letter-spacing: .04em;
}

/* ─── Right Panel ─── */
.panel-r {
  width: 380px;
  background: var(--navy-m);
  display: flex;
  align-items: center;
}
.form-wrap {
  width: 100%;
  padding: 44px 40px;
  animation: fromRight .55s .15s both;
}
@keyframes fromRight {
  from { opacity: 0; transform: translateX(18px); }
  to   { opacity: 1; transform: translateX(0); }
}

.form-eyebrow {
  font-size: 10px;
  letter-spacing: .22em;
  color: var(--gold);
  text-transform: uppercase;
  font-weight: 600;
  margin-bottom: 10px;
}
.form-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--ivory);
  margin-bottom: 12px;
}
.title-rule {
  width: 38px; height: 2px;
  background: var(--gold);
  margin-bottom: 30px;
}

/* Fields */
.field-block { margin-bottom: 18px; }
.field-label {
  display: block;
  font-size: 10.5px;
  color: var(--ivory-d);
  letter-spacing: .12em;
  text-transform: uppercase;
  margin-bottom: 7px;
}
.flat-input :deep(.el-input__wrapper) {
  background: transparent !important;
  border: none !important;
  border-bottom: 1px solid var(--border) !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  padding: 2px 0 !important;
  transition: border-color .2s;
}
.flat-input :deep(.el-input__wrapper:hover),
.flat-input :deep(.el-input__wrapper.is-focus) {
  border-bottom-color: var(--gold) !important;
}
.flat-input :deep(.el-input__inner) {
  color: var(--ivory) !important;
  font-size: 14px !important;
  height: 40px;
}
.flat-input :deep(.el-input__inner::placeholder) { color: var(--slate) !important; }
.flat-input :deep(.el-input__prefix)             { color: var(--slate); }
.flat-input :deep(.el-input__suffix-inner)       { color: var(--slate) !important; }

/* Options row */
.row-opts {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 26px;
}
.check-me :deep(.el-checkbox__label) { color: var(--slate); font-size: 13px; }
.check-me :deep(.el-checkbox__inner) { border-color: var(--border); background: transparent; }
.link-forgot { color: var(--gold) !important; font-size: 13px; padding: 0; }
.link-forgot:hover { color: var(--gold-l) !important; }

/* Submit */
.submit-btn {
  width: 100%;
  height: 48px;
  background: var(--gold);
  color: var(--navy);
  border: none;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: .28em;
  text-transform: uppercase;
  font-family: var(--ff-body);
  transition: background .2s, transform .1s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.submit-btn:hover:not(:disabled) { background: var(--gold-l); transform: translateY(-1px); }
.submit-btn:active:not(:disabled) { transform: translateY(0); }
.submit-btn:disabled { opacity: .55; cursor: not-allowed; }

.dot-loader {
  display: inline-flex;
  gap: 5px;
  align-items: center;
}
.dot-loader i {
  width: 6px; height: 6px;
  background: var(--navy);
  border-radius: 50%;
  list-style: none;
  animation: blink 1.2s infinite;
}
.dot-loader i:nth-child(2) { animation-delay: .2s; }
.dot-loader i:nth-child(3) { animation-delay: .4s; }
@keyframes blink {
  0%, 80%, 100% { opacity: .2; }
  40%           { opacity: 1; }
}

.reg-row {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: var(--slate);
}
.reg-link {
  color: var(--gold);
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}
.reg-link:hover { color: var(--gold-l); }

/* Demo */
.demo-zone { margin-top: 26px; }
.demo-sep {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.sep-line { flex: 1; height: 1px; background: var(--border); }
.sep-txt  {
  font-size: 9.5px;
  letter-spacing: .16em;
  color: var(--slate);
  white-space: nowrap;
}
.demo-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border: 1px solid var(--border);
  cursor: pointer;
  transition: border-color .2s, background .2s;
  margin-bottom: 6px;
}
.demo-row:hover { border-color: var(--gold); background: var(--gold-d); }

.d-badge {
  font-size: 9.5px;
  font-weight: 700;
  letter-spacing: .08em;
  padding: 2px 7px;
  border: 1px solid var(--border);
  color: #6a7d93;
  flex-shrink: 0;
}
.d-badge[data-admin="true"] {
  border-color: rgba(184,148,63,.45);
  color: var(--gold);
}
.d-cred {
  flex: 1;
  font-family: var(--ff-mono);
  font-size: 12px;
  color: #627080;
}
.d-tap {
  font-size: 14px;
  color: var(--slate);
  opacity: .5;
}

:deep(.el-form-item) { margin-bottom: 0; }
:deep(.el-form-item__error) {
  color: #e07b5a;
  font-size: 11px;
  padding-top: 4px;
}
</style>
