<template>
  <div class="home-page">

    <!-- ===== NAV ===== -->
    <nav class="nav">
      <div class="nav-inner">
        <a class="nav-brand" href="#">
          <svg class="nav-scales" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="20" y1="4" x2="20" y2="36" stroke="#b8943f" stroke-width="1.8"/>
            <line x1="6" y1="8" x2="34" y2="8" stroke="#b8943f" stroke-width="1.8"/>
            <path d="M6 8 L2 20 Q6 24 10 20 L6 8Z" fill="none" stroke="#b8943f" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M34 8 L30 20 Q34 24 38 20 L34 8Z" fill="none" stroke="#b8943f" stroke-width="1.5" stroke-linejoin="round"/>
            <line x1="14" y1="36" x2="26" y2="36" stroke="#b8943f" stroke-width="1.8"/>
          </svg>
          <span class="nav-logo-text"><em>LawRAG</em></span>
        </a>
        <div class="nav-links">
          <a href="#features">核心技术</a>
          <a href="#domains">法律领域</a>
          <a href="#process">使用流程</a>
        </div>
        <div class="nav-actions">
          <router-link to="/login" class="btn-nav-login">登录</router-link>
          <router-link to="/login" class="btn-nav-cta">立即体验</router-link>
        </div>
      </div>
    </nav>

    <!-- ===== HERO ===== -->
    <section class="hero">
      <div class="hero-bg-grid"></div>
      <div class="hero-orb hero-orb-1"></div>
      <div class="hero-orb hero-orb-2"></div>

      <div class="hero-inner">
        <div class="hero-eyebrow">
          <span class="eyebrow-dot"></span>
          <span>RAG · 检索增强生成 · 智能法律咨询</span>
        </div>
        <h1 class="hero-title">
          让法律智识<br/><em class="hero-title-accent">触手可及</em>
        </h1>
        <p class="hero-sub">
          基于向量检索 + BM25 双路召回、Cross-Encoder 精排，<br class="hero-br"/>
          从权威法律文献中为您生成有据可查的咨询回答
        </p>

        <div class="hero-cta-row">
          <router-link to="/login" class="btn-hero-primary">
            <span>开始免费咨询</span>
            <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18"><path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
          </router-link>
          <a href="#features" class="btn-hero-ghost">了解技术原理</a>
        </div>

        <!-- Stats -->
        <div class="hero-stats" ref="statsRef">
          <div class="stat-item">
            <div class="stat-num">
              <CountUp v-if="statsVisible" :end-val="11" :duration="1.5" />
              <span v-else>0</span>
              <span class="stat-unit">+</span>
            </div>
            <div class="stat-label">法律领域覆盖</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">
              <CountUp v-if="statsVisible" :end-val="98" :duration="1.8" />
              <span v-else>0</span>
              <span class="stat-unit">%</span>
            </div>
            <div class="stat-label">召回准确率</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">
              <span class="stat-less">&lt;</span>
              <CountUp v-if="statsVisible" :end-val="3" :duration="1.2" />
              <span v-else>0</span>
              <span class="stat-unit">s</span>
            </div>
            <div class="stat-label">首字节响应</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-num">
              <CountUp v-if="statsVisible" :end-val="100" :duration="2" />
              <span v-else>0</span>
              <span class="stat-unit">%</span>
            </div>
            <div class="stat-label">来源可溯源</div>
          </div>
        </div>
      </div>

      <div class="hero-deco-s" aria-hidden="true">§</div>
    </section>

    <!-- ===== FEATURES ===== -->
    <section class="features" id="features">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-label">CORE TECHNOLOGY</div>
          <h2 class="section-title">五级 RAG 检索管线</h2>
          <p class="section-sub">每一次问答背后，都经历严格的五步检索与精排流程，确保答案有据可查</p>
        </div>

        <div class="pipeline-flow" ref="pipelineRef">
          <div
            v-for="(step, i) in pipelineSteps"
            :key="i"
            class="pipe-step"
            :class="{ visible: pipelineVisible }"
            :style="{ transitionDelay: `${i * 120}ms` }"
          >
            <div class="pipe-num">{{ String(i + 1).padStart(2, '0') }}</div>
            <div class="pipe-icon-wrap" :style="{ background: step.bg }">
              <span v-html="step.icon"></span>
            </div>
            <div class="pipe-content">
              <div class="pipe-title">{{ step.title }}</div>
              <div class="pipe-desc">{{ step.desc }}</div>
            </div>
            <div v-if="i < pipelineSteps.length - 1" class="pipe-arrow">
              <svg viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M8 5l8 7-8 7z"/></svg>
            </div>
          </div>
        </div>

        <div class="feat-cards" ref="cardsRef">
          <div
            v-for="(card, i) in featureCards"
            :key="i"
            class="feat-card"
            :class="{ visible: cardsVisible }"
            :style="{ transitionDelay: `${i * 80}ms` }"
          >
            <div class="feat-card-icon" v-html="card.icon"></div>
            <div class="feat-card-title">{{ card.title }}</div>
            <div class="feat-card-desc">{{ card.desc }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== DOMAINS ===== -->
    <section class="domains" id="domains">
      <div class="section-inner">
        <div class="section-header light">
          <div class="section-label gold">LEGAL DOMAINS</div>
          <h2 class="section-title light">十一大法律领域全覆盖</h2>
          <p class="section-sub light">从婚姻家庭到商事仲裁，专业知识库持续扩充，为您的法律问题提供全方位支持</p>
        </div>

        <div class="domains-grid" ref="domainsRef">
          <div
            v-for="(d, i) in legalDomains"
            :key="i"
            class="domain-card"
            :class="{ visible: domainsVisible }"
            :style="{ transitionDelay: `${i * 60}ms` }"
          >
            <div class="domain-icon" v-html="d.icon"></div>
            <div class="domain-name">{{ d.name }}</div>
            <div class="domain-examples">{{ d.examples }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== PROCESS ===== -->
    <section class="process" id="process">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-label">HOW IT WORKS</div>
          <h2 class="section-title">三步获取专业法律解答</h2>
        </div>

        <div class="process-steps">
          <template v-for="(step, i) in processSteps" :key="i">
            <div class="process-step">
              <div class="process-step-num">{{ String(i + 1).padStart(2, '0') }}</div>
              <div class="process-step-body">
                <div class="process-step-icon" v-html="step.icon"></div>
                <div class="process-step-title">{{ step.title }}</div>
                <div class="process-step-desc">{{ step.desc }}</div>
              </div>
            </div>
            <div v-if="i < processSteps.length - 1" class="process-connector">
              <div class="process-line"></div>
              <svg class="process-arrow" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"/></svg>
            </div>
          </template>
        </div>
      </div>
    </section>

    <!-- ===== CTA ===== -->
    <section class="cta-section">
      <div class="cta-bg-pattern"></div>
      <div class="cta-inner">
        <div class="cta-deco">§</div>
        <h2 class="cta-title">准备好获取您的法律解答了吗？</h2>
        <p class="cta-sub">注册免费账户，即刻体验 AI 驱动的专业法律知识检索</p>
        <router-link to="/login" class="btn-cta-main">
          立即免费体验
          <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18"><path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
        </router-link>
        <p class="cta-disclaimer">不构成正式法律意见 · 仅供知识参考</p>
      </div>
    </section>

    <!-- ===== FOOTER ===== -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <svg class="footer-scales" viewBox="0 0 40 40" fill="none">
            <line x1="20" y1="4" x2="20" y2="36" stroke="#b8943f" stroke-width="1.8"/>
            <line x1="6" y1="8" x2="34" y2="8" stroke="#b8943f" stroke-width="1.8"/>
            <path d="M6 8 L2 20 Q6 24 10 20 L6 8Z" fill="none" stroke="#b8943f" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M34 8 L30 20 Q34 24 38 20 L34 8Z" fill="none" stroke="#b8943f" stroke-width="1.5" stroke-linejoin="round"/>
            <line x1="14" y1="36" x2="26" y2="36" stroke="#b8943f" stroke-width="1.8"/>
          </svg>
          <em>LawRAG</em>
        </div>
        <div class="footer-copy">© 2025 LawRAG · 智能法律咨询平台 · 本平台仅供法律知识参考，不构成法律意见</div>
      </div>
    </footer>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const statsRef = ref<HTMLElement>()
const pipelineRef = ref<HTMLElement>()
const cardsRef = ref<HTMLElement>()
const domainsRef = ref<HTMLElement>()

const statsVisible = ref(false)
const pipelineVisible = ref(false)
const cardsVisible = ref(false)
const domainsVisible = ref(false)

const observers: IntersectionObserver[] = []

const observe = (el: HTMLElement | undefined, setter: (v: boolean) => void, threshold = 0.2) => {
  if (!el) return
  const obs = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) { setter(true); obs.disconnect() }
  }, { threshold })
  obs.observe(el)
  observers.push(obs)
}

onMounted(() => {
  observe(statsRef.value, v => { statsVisible.value = v }, 0.3)
  observe(pipelineRef.value, v => { pipelineVisible.value = v }, 0.1)
  observe(cardsRef.value, v => { cardsVisible.value = v }, 0.1)
  observe(domainsRef.value, v => { domainsVisible.value = v }, 0.05)
})

onUnmounted(() => observers.forEach(o => o.disconnect()))

const pipelineSteps = [
  {
    title: 'Query 改写',
    desc: 'LLM 将模糊口语化提问改写为精准法律检索词',
    bg: 'rgba(184,148,63,0.12)',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>`
  },
  {
    title: '双路召回',
    desc: '向量语义检索 + BM25 关键词检索并行执行',
    bg: 'rgba(59,130,246,0.12)',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>`
  },
  {
    title: 'RRF 融合',
    desc: '互惠排名融合算法合并两路结果，消除偏置',
    bg: 'rgba(16,185,129,0.12)',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z"/></svg>`
  },
  {
    title: 'Cross-Encoder 重排',
    desc: '精排模型对候选文档深度评分，保留最相关片段',
    bg: 'rgba(239,68,68,0.10)',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M3 18h6v-2H3v2zM3 6v2h18V6H3zm0 7h12v-2H3v2z"/></svg>`
  },
  {
    title: 'LLM 流式生成',
    desc: 'Qwen 大模型注入精排上下文，流式输出带引用的回答',
    bg: 'rgba(124,58,237,0.12)',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>`
  }
]

const featureCards = [
  {
    title: '权威法律知识库',
    desc: '涵盖法律法规、司法解释、典型案例，知识库持续扩充，确保答案权威可信',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/></svg>`
  },
  {
    title: '来源可追溯',
    desc: '每条回答均附带文献出处，精确到章节页码，法律依据一目了然',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>`
  },
  {
    title: '流式实时响应',
    desc: '服务端推送技术（SSE）实现打字机效果，无需等待，即看即用',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M13 2.05v2.02c3.95.49 7 3.85 7 7.93 0 3.21-1.81 6-4.72 7.28L13 17v5l7-4-1.22-1.22C21.91 15.1 24 12.15 24 8.99 24 4.14 19.5.3 13 2.05zM11 2.05C4.5.3 0 4.14 0 8.99c0 3.16 2.09 6.11 5.22 7.78L4 18l7 4v-5l-2.28 2.28C6.81 18 5 15.21 5 12c0-4.08 3.05-7.44 7-7.93V2.05z"/></svg>`
  },
  {
    title: '语音输入支持',
    desc: '基于 DashScope Paraformer 的实时语音转文字，解放双手，口述即问',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm-1-9c0-.55.45-1 1-1s1 .45 1 1v6c0 .55-.45 1-1 1s-1-.45-1-1V5zm6 6c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/></svg>`
  },
  {
    title: 'RAG 过程透明',
    desc: '可视化检索日志展示改写、召回、融合、重排全过程，技术完全透明',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>`
  },
  {
    title: '对话历史管理',
    desc: '所有咨询对话云端保存，支持导出 Markdown，随时回顾查阅',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/></svg>`
  }
]

const legalDomains = [
  { name: '民商事法律', examples: '合同纠纷 · 债权债务', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6zm7 13H5v-.23c0-.62.28-1.2.76-1.58C7.47 15.82 9.64 15 12 15s4.53.82 6.24 2.19c.48.38.76.97.76 1.58V19z"/></svg>` },
  { name: '劳动用工法律', examples: '劳动合同 · 工伤赔偿', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M20 6h-2.18c.07-.44.18-.88.18-1.36 0-2.57-2.1-4.64-4.67-4.64-1.4 0-2.65.58-3.55 1.51L8 3.24 6.22 1.51C5.32.58 4.06 0 2.67 0 .1 0-2 2.07-2 4.64c0 .48.11.92.18 1.36H-2c-1.1 0-1.99.9-1.99 2v14c0 1.1.89 2 1.99 2h20c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2z"/></svg>` },
  { name: '婚姻家庭法律', examples: '离婚诉讼 · 财产分割', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 21.593c-5.63-5.539-11-10.297-11-14.402 0-3.791 3.068-5.191 5.281-5.191 1.312 0 4.151.501 5.719 4.457 1.59-3.968 4.464-4.447 5.726-4.447 2.54 0 5.274 1.621 5.274 5.181 0 4.069-5.136 8.625-11 14.402z"/></svg>` },
  { name: '刑事法律', examples: '刑事辩护 · 量刑标准', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/></svg>` },
  { name: '行政法律', examples: '行政复议 · 行政诉讼', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 3L1 9l11 6 9-4.91V17h2V9L12 3zM5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82z"/></svg>` },
  { name: '房产与不动产', examples: '房屋买卖 · 租赁纠纷', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>` },
  { name: '公司与商事法', examples: '股权纠纷 · 公司治理', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z"/></svg>` },
  { name: '知识产权法', examples: '版权保护 · 商标侵权', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M11.5 2C6.81 2 3 5.81 3 10.5S6.81 19 11.5 19h.5v3c4.86-2.34 8-7 8-11.5C20 5.81 16.19 2 11.5 2zm1 14.5h-2v-2h2v2zm0-4h-2c0-3.25 3-3 3-5 0-1.1-.9-2-2-2s-2 .9-2 2h-2c0-2.21 1.79-4 4-4s4 1.79 4 4c0 2.5-3 2.75-3 5z"/></svg>` },
  { name: '诉讼程序法', examples: '起诉流程 · 证据规则', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>` },
  { name: '消费者权益', examples: '产品缺陷 · 退款维权', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z"/></svg>` },
  { name: '综合法律咨询', examples: '多领域交叉 · 政策解读', icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm2.07-7.75l-.9.92C13.45 12.9 13 13.5 13 15h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2h-2c0-2.21 1.79-4 4-4s4 1.79 4 4c0 .88-.36 1.68-.93 2.25z"/></svg>` }
]

const processSteps = [
  {
    title: '描述您的法律问题',
    desc: '用自然语言描述您遇到的法律情况，支持文字或语音输入，无需专业术语',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="32" height="32"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/></svg>`
  },
  {
    title: 'AI 智能检索分析',
    desc: '系统自动改写查询、双路检索法律知识库、精排融合，秒级完成深度法律检索',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="32" height="32"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>`
  },
  {
    title: '获取有据可查的解答',
    desc: '接收附带法律条文出处的专业回答，可查看完整的 RAG 检索过程日志',
    icon: `<svg viewBox="0 0 24 24" fill="currentColor" width="32" height="32"><path d="M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7z"/></svg>`
  }
]
</script>

<style scoped>
.home-page {
  --navy: #0c1322;
  --navy-2: #111b2e;
  --gold: #b8943f;
  --gold-light: #d4aa5a;
  --ivory: #f5edd8;
  --ivory-2: #ede0c4;
  --text-primary: #1a1a2e;
  --text-muted: #6b7280;
  --ff-brand: Palatino, "Palatino Linotype", Georgia, serif;
  --ff-zh: "Songti SC", "Source Han Serif SC", "Noto Serif CJK SC", SimSun, STSong, serif;
  --ff-body: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
  min-height: 100vh;
  background: #faf9f6;
  font-family: var(--ff-body);
  color: var(--text-primary);
  overflow-x: hidden;
}

/* ===== NAV ===== */
.nav {
  position: fixed;
  top: 0; left: 0; right: 0;
  z-index: 100;
  background: rgba(12,19,34,0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(184,148,63,0.2);
}
.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 40px;
}
.nav-brand {
  display: flex; align-items: center; gap: 10px;
  text-decoration: none; flex-shrink: 0;
}
.nav-scales { width: 28px; height: 28px; }
.nav-logo-text {
  font-family: var(--ff-brand);
  font-size: 20px; font-style: italic; color: #fff; letter-spacing: 0.5px;
}
.nav-links { display: flex; align-items: center; gap: 32px; flex: 1; }
.nav-links a {
  color: rgba(255,255,255,0.6); text-decoration: none;
  font-size: 14px; letter-spacing: 0.3px; transition: color 0.2s;
}
.nav-links a:hover { color: var(--gold-light); }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.btn-nav-login {
  color: rgba(255,255,255,0.7); text-decoration: none; font-size: 14px;
  padding: 6px 16px; border: 1px solid rgba(255,255,255,0.15); border-radius: 6px; transition: all 0.2s;
}
.btn-nav-login:hover { border-color: rgba(255,255,255,0.4); color: #fff; }
.btn-nav-cta {
  background: var(--gold); color: var(--navy); text-decoration: none;
  font-size: 14px; font-weight: 600; padding: 7px 20px; border-radius: 6px; transition: background 0.2s;
}
.btn-nav-cta:hover { background: var(--gold-light); }

/* ===== HERO ===== */
.hero {
  position: relative; min-height: 100vh;
  background: var(--navy); display: flex; align-items: center; overflow: hidden;
}
.hero-bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(184,148,63,0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(184,148,63,0.06) 1px, transparent 1px);
  background-size: 60px 60px;
}
.hero-orb { position: absolute; border-radius: 50%; filter: blur(80px); pointer-events: none; }
.hero-orb-1 {
  width: 500px; height: 500px; top: -100px; right: -100px;
  background: radial-gradient(circle, rgba(184,148,63,0.15) 0%, transparent 70%);
}
.hero-orb-2 {
  width: 400px; height: 400px; bottom: -80px; left: -80px;
  background: radial-gradient(circle, rgba(59,130,246,0.10) 0%, transparent 70%);
}
.hero-inner {
  position: relative; z-index: 2; max-width: 1200px;
  margin: 0 auto; padding: 120px 40px 80px; width: 100%;
}
.hero-eyebrow {
  display: flex; align-items: center; gap: 10px;
  font-size: 13px; color: var(--gold); letter-spacing: 1.5px;
  text-transform: uppercase; margin-bottom: 28px;
  animation: fadeUp 0.8s ease both;
}
.eyebrow-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--gold); flex-shrink: 0; }
.hero-title {
  font-family: var(--ff-zh);
  font-size: clamp(48px, 7vw, 80px); font-weight: 700;
  color: #fff; line-height: 1.15; margin: 0 0 24px;
  animation: fadeUp 0.8s 0.1s ease both; letter-spacing: -1px;
}
.hero-title-accent {
  font-style: normal; color: var(--gold); display: inline-block; position: relative;
}
.hero-title-accent::after {
  content: ''; position: absolute; bottom: 4px; left: 0; right: 0;
  height: 2px; background: linear-gradient(90deg, var(--gold), transparent);
}
.hero-sub {
  font-size: 17px; color: rgba(255,255,255,0.55);
  line-height: 1.8; margin: 0 0 40px; animation: fadeUp 0.8s 0.2s ease both;
}
.hero-br { display: none; }
@media (min-width: 768px) { .hero-br { display: block; } }
.hero-cta-row {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 56px; flex-wrap: wrap; animation: fadeUp 0.8s 0.3s ease both;
}
.btn-hero-primary {
  display: inline-flex; align-items: center; gap: 8px;
  background: var(--gold); color: var(--navy); font-weight: 700; font-size: 15px;
  padding: 14px 32px; border-radius: 8px; text-decoration: none; transition: all 0.25s; letter-spacing: 0.3px;
}
.btn-hero-primary:hover {
  background: var(--gold-light); transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(184,148,63,0.35);
}
.btn-hero-ghost {
  color: rgba(255,255,255,0.6); font-size: 14px; text-decoration: none;
  border-bottom: 1px solid rgba(255,255,255,0.2); padding-bottom: 2px; transition: all 0.2s;
}
.btn-hero-ghost:hover { color: #fff; border-color: rgba(255,255,255,0.6); }

/* Stats */
.hero-stats {
  display: flex; align-items: center;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(184,148,63,0.2);
  border-radius: 12px; padding: 24px 32px; max-width: 680px;
  animation: fadeUp 0.8s 0.4s ease both;
}
.stat-item { flex: 1; text-align: center; }
.stat-num {
  font-size: 40px; font-weight: 800; color: var(--gold); line-height: 1;
  display: flex; align-items: flex-end; justify-content: center; gap: 2px;
  font-family: var(--ff-brand);
}
.stat-unit { font-size: 20px; font-weight: 600; margin-bottom: 4px; }
.stat-less { font-size: 22px; margin-bottom: 4px; }
.stat-label { font-size: 12px; color: rgba(255,255,255,0.4); margin-top: 6px; letter-spacing: 0.5px; }
.stat-divider { width: 1px; height: 48px; background: rgba(184,148,63,0.2); flex-shrink: 0; }

.hero-deco-s {
  position: absolute; right: -20px; bottom: 0;
  font-size: clamp(200px, 20vw, 320px); font-family: var(--ff-brand);
  color: rgba(184,148,63,0.04); line-height: 1; pointer-events: none; user-select: none; z-index: 1;
}

/* ===== SECTIONS common ===== */
.section-inner { max-width: 1200px; margin: 0 auto; padding: 100px 40px; }
.section-header { text-align: center; margin-bottom: 60px; }
.section-label { font-size: 11px; font-weight: 700; letter-spacing: 3px; color: var(--gold); margin-bottom: 12px; text-transform: uppercase; }
.section-title { font-family: var(--ff-zh); font-size: clamp(28px, 4vw, 40px); font-weight: 700; color: var(--text-primary); margin: 0 0 16px; line-height: 1.3; }
.section-sub { font-size: 16px; color: var(--text-muted); max-width: 560px; margin: 0 auto; line-height: 1.7; }
.section-header.light .section-title { color: #fff; }
.section-header.light .section-sub { color: rgba(255,255,255,0.5); }
.section-label.gold { color: var(--gold-light); }

/* ===== FEATURES ===== */
.features { background: #faf9f6; }
.pipeline-flow {
  display: flex; align-items: stretch;
  margin-bottom: 64px; background: #fff;
  border: 1px solid #e8e0d0; border-radius: 16px; overflow: hidden;
}
.pipe-step {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  padding: 28px 16px; position: relative;
  opacity: 0; transform: translateY(20px);
  transition: opacity 0.5s ease, transform 0.5s ease;
  border-right: 1px solid #f0e8d8;
}
.pipe-step:last-child { border-right: none; }
.pipe-step.visible { opacity: 1; transform: translateY(0); }
.pipe-num { font-size: 11px; font-weight: 700; color: rgba(184,148,63,0.4); letter-spacing: 1px; margin-bottom: 12px; font-family: var(--ff-brand); }
.pipe-icon-wrap { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-bottom: 12px; color: var(--gold); }
.pipe-title { font-size: 13px; font-weight: 700; color: var(--text-primary); text-align: center; margin-bottom: 6px; }
.pipe-desc { font-size: 11px; color: var(--text-muted); text-align: center; line-height: 1.5; }
.pipe-arrow {
  position: absolute; right: -9px; top: 50%; transform: translateY(-50%);
  z-index: 2; width: 18px; height: 18px; background: var(--gold);
  border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff;
}

.feat-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.feat-card {
  background: #fff; border: 1px solid #e8e0d0; border-radius: 12px; padding: 28px 24px;
  opacity: 0; transform: translateY(20px);
  transition: opacity 0.5s ease, transform 0.5s ease, box-shadow 0.3s, border-color 0.3s;
  border-left: 3px solid transparent;
}
.feat-card.visible { opacity: 1; transform: translateY(0); }
.feat-card:hover { border-left-color: var(--gold); box-shadow: 0 8px 32px rgba(0,0,0,0.08); }
.feat-card-icon { color: var(--gold); margin-bottom: 14px; }
.feat-card-title { font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 8px; }
.feat-card-desc { font-size: 13px; color: var(--text-muted); line-height: 1.6; }

/* ===== DOMAINS ===== */
.domains { background: var(--navy); position: relative; }
.domains::before {
  content: ''; position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(184,148,63,0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(184,148,63,0.05) 1px, transparent 1px);
  background-size: 48px 48px;
}
.domains .section-inner { position: relative; z-index: 2; }
.domains-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.domain-card {
  background: rgba(255,255,255,0.04); border: 1px solid rgba(184,148,63,0.15);
  border-radius: 12px; padding: 20px 18px;
  opacity: 0; transform: translateY(16px);
  transition: opacity 0.45s ease, transform 0.45s ease, background 0.3s, border-color 0.3s;
}
.domain-card.visible { opacity: 1; transform: translateY(0); }
.domain-card:hover { background: rgba(184,148,63,0.08); border-color: rgba(184,148,63,0.4); }
.domain-icon { color: var(--gold); margin-bottom: 10px; }
.domain-name { font-size: 14px; font-weight: 700; color: #fff; margin-bottom: 4px; }
.domain-examples { font-size: 11px; color: rgba(255,255,255,0.35); line-height: 1.5; }

/* ===== PROCESS ===== */
.process { background: var(--ivory); }
.process-steps { display: flex; align-items: center; justify-content: center; }
.process-step { flex: 1; display: flex; flex-direction: column; align-items: center; }
.process-step-num { font-size: 60px; font-weight: 800; color: rgba(184,148,63,0.12); line-height: 1; font-family: var(--ff-brand); margin-bottom: 4px; }
.process-step-body {
  background: #fff; border: 1px solid #e8e0d0; border-radius: 16px;
  padding: 32px 24px; text-align: center; width: 100%;
  border-top: 3px solid var(--gold); transition: box-shadow 0.3s;
}
.process-step-body:hover { box-shadow: 0 12px 40px rgba(0,0,0,0.1); }
.process-step-icon { color: var(--gold); margin: 0 auto 16px; display: flex; justify-content: center; }
.process-step-title { font-family: var(--ff-zh); font-size: 18px; font-weight: 700; color: var(--text-primary); margin-bottom: 10px; }
.process-step-desc { font-size: 13px; color: var(--text-muted); line-height: 1.7; }
.process-connector { display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 0 16px; flex-shrink: 0; }
.process-line { width: 1px; height: 24px; background: var(--gold); opacity: 0.3; }
.process-arrow { color: var(--gold); opacity: 0.5; transform: rotate(90deg); }

/* ===== CTA ===== */
.cta-section { position: relative; background: var(--navy); overflow: hidden; text-align: center; padding: 100px 40px; }
.cta-bg-pattern {
  position: absolute; inset: 0;
  background:
    radial-gradient(ellipse at 30% 50%, rgba(184,148,63,0.12) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 50%, rgba(59,130,246,0.08) 0%, transparent 60%);
}
.cta-inner { position: relative; z-index: 2; max-width: 600px; margin: 0 auto; }
.cta-deco { font-family: var(--ff-brand); font-size: 80px; color: rgba(184,148,63,0.15); line-height: 1; margin-bottom: 16px; }
.cta-title { font-family: var(--ff-zh); font-size: clamp(24px, 4vw, 36px); font-weight: 700; color: #fff; margin: 0 0 16px; }
.cta-sub { font-size: 15px; color: rgba(255,255,255,0.5); margin: 0 0 36px; line-height: 1.7; }
.btn-cta-main {
  display: inline-flex; align-items: center; gap: 10px;
  background: var(--gold); color: var(--navy); font-weight: 700; font-size: 16px;
  padding: 16px 40px; border-radius: 8px; text-decoration: none; transition: all 0.25s; letter-spacing: 0.5px;
}
.btn-cta-main:hover { background: var(--gold-light); transform: translateY(-2px); box-shadow: 0 12px 32px rgba(184,148,63,0.4); }
.cta-disclaimer { font-size: 12px; color: rgba(255,255,255,0.2); margin-top: 20px; letter-spacing: 0.5px; }

/* ===== FOOTER ===== */
.footer { background: #080e1a; border-top: 1px solid rgba(184,148,63,0.15); padding: 28px 40px; }
.footer-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; gap: 20px; flex-wrap: wrap; }
.footer-brand { display: flex; align-items: center; gap: 8px; font-family: var(--ff-brand); font-style: italic; font-size: 16px; color: rgba(255,255,255,0.5); }
.footer-scales { width: 22px; height: 22px; }
.footer-copy { font-size: 12px; color: rgba(255,255,255,0.2); line-height: 1.5; }

/* ===== ANIMATIONS ===== */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ===== RESPONSIVE ===== */
@media (max-width: 1024px) {
  .feat-cards { grid-template-columns: repeat(2, 1fr); }
  .domains-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .nav-links { display: none; }
  .pipeline-flow { flex-direction: column; }
  .pipe-step { border-right: none; border-bottom: 1px solid #f0e8d8; }
  .pipe-arrow { display: none; }
  .feat-cards { grid-template-columns: 1fr; }
  .domains-grid { grid-template-columns: repeat(2, 1fr); }
  .process-steps { flex-direction: column; gap: 20px; }
  .process-connector { flex-direction: row; padding: 0; }
  .process-line { width: 32px; height: 1px; }
  .process-arrow { transform: none; }
  .hero-stats { flex-direction: column; gap: 24px; }
  .stat-divider { width: 80px; height: 1px; }
}
</style>
