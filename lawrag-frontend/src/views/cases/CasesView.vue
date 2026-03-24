<template>
  <div class="cv-root">

    <!-- ===== Left: Case List Panel ===== -->
    <div class="cv-list-panel">

      <!-- Search + Filter Header -->
      <div class="cv-list-head">
        <div class="cv-search-wrap">
          <svg class="cv-search-icon" viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input
            v-model="searchText"
            class="cv-search-input"
            placeholder="搜索案例标题、关键词…"
            @input="selectedId = null"
          />
          <button v-if="searchText" class="cv-search-clear" @click="searchText = ''; selectedId = null">
            <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
          </button>
        </div>

        <!-- Domain Filters -->
        <div class="cv-domain-filters">
          <button
            v-for="d in domainTags" :key="d.val"
            class="cv-ftag" :class="{ active: activeDomain === d.val }"
            @click="activeDomain = d.val; selectedId = null"
          >{{ d.label }}</button>
        </div>

        <!-- Sort + Result count -->
        <div class="cv-list-meta">
          <span class="cv-result-count">共 <em>{{ filteredCases.length }}</em> 个案例</span>
          <div class="cv-sort-tabs">
            <button v-for="s in sorts" :key="s.val" class="cv-sort-btn" :class="{ active: sortBy === s.val }" @click="sortBy = s.val">{{ s.label }}</button>
          </div>
        </div>
      </div>

      <!-- Case Cards -->
      <div class="cv-cards-scroll">
        <div
          v-for="c in sortedCases" :key="c.id"
          class="cv-card"
          :class="{ active: selectedId === c.id, hot: c.hot }"
          @click="selectCase(c.id)"
        >
          <div class="cv-card-row1">
            <span class="cv-dtag" :style="{ background: domainColor(c.domain) }">{{ c.domain }}</span>
            <span class="cv-outcome-tag" :class="c.outcome">{{ outcomeMap[c.outcome] }}</span>
            <span class="cv-card-year">{{ c.year }}</span>
            <span v-if="c.hot" class="cv-hot-badge">
              <svg viewBox="0 0 20 20" fill="currentColor" width="11" height="11"><path fill-rule="evenodd" d="M12.395 2.553a1 1 0 00-1.45-.385c-.345.23-.614.558-.822.88-.214.33-.403.713-.57 1.116-.334.804-.614 1.768-.84 2.734a31.365 31.365 0 00-.613 3.58 2.64 2.64 0 01-.945-1.067c-.328-.68-.398-1.534-.398-2.654A1 1 0 005.05 6.05 6.981 6.981 0 003 11a7 7 0 1011.95-4.95c-.592-.591-.98-.985-1.348-1.467-.363-.476-.724-1.063-1.207-2.03zM12.12 15.12A3 3 0 017 13s.879.5 2.5.5c0-1 .5-4 1.25-4.5.5 1 .786 1.293 1.371 1.879A2.99 2.99 0 0113 13a2.99 2.99 0 01-.879 2.121z" clip-rule="evenodd"/></svg>
              热门
            </span>
          </div>
          <div class="cv-card-title">{{ c.title }}</div>
          <div class="cv-card-summary">{{ c.summary }}</div>
          <div class="cv-card-row3">
            <span class="cv-card-court">{{ c.court }}</span>
            <div class="cv-card-stats">
              <span>
                <svg viewBox="0 0 20 20" fill="currentColor" width="12" height="12"><path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z"/></svg>
                {{ getComments(c.id).length }}
              </span>
              <span>
                <svg viewBox="0 0 20 20" fill="currentColor" width="12" height="12"><path d="M2 10.5a1.5 1.5 0 113 0v6a1.5 1.5 0 01-3 0v-6zM6 10.333v5.43a2 2 0 001.106 1.79l.05.025A4 4 0 008.943 18h5.416a2 2 0 001.962-1.608l1.2-6A2 2 0 0015.56 8H12V4a2 2 0 00-2-2 1 1 0 00-1 1v.667a4 4 0 01-.8 2.4L6.8 7.933a4 4 0 00-.8 2.4z"/></svg>
                {{ c.likes + (likedIds.includes(c.id) ? 1 : 0) }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="sortedCases.length === 0" class="cv-empty">
          <div class="cv-empty-s">§</div>
          <p>未找到相关案例</p>
        </div>
      </div>
    </div>

    <!-- ===== Right: Detail Panel ===== -->
    <div class="cv-detail-panel" :class="{ shown: selectedCase }">

      <!-- Empty state -->
      <div v-if="!selectedCase" class="cv-detail-empty">
        <div class="cv-de-icon">
          <svg viewBox="0 0 56 56" fill="none" width="56" height="56">
            <line x1="28" y1="6" x2="28" y2="50" stroke="#b8943f" stroke-width="2"/>
            <line x1="8" y1="12" x2="48" y2="12" stroke="#b8943f" stroke-width="2"/>
            <path d="M8 12 L3 28 Q8 34 13 28 L8 12Z" fill="none" stroke="#b8943f" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M48 12 L43 28 Q48 34 53 28 L48 12Z" fill="none" stroke="#b8943f" stroke-width="1.8" stroke-linejoin="round"/>
            <line x1="19" y1="50" x2="37" y2="50" stroke="#b8943f" stroke-width="2"/>
          </svg>
        </div>
        <p class="cv-de-title">选择一个案例</p>
        <p class="cv-de-sub">点击左侧任意案例卡片，查看完整案情、裁判分析与评论</p>
      </div>

      <!-- Case Detail -->
      <template v-else>
        <div class="cv-detail-scroll">

          <!-- Detail Header -->
          <div class="cv-detail-hero">
            <div class="cv-dh-tags">
              <span class="cv-dtag lg" :style="{ background: domainColor(selectedCase.domain) }">{{ selectedCase.domain }}</span>
              <span class="cv-outcome-tag lg" :class="selectedCase.outcome">{{ outcomeMap[selectedCase.outcome] }}</span>
              <span v-if="selectedCase.hot" class="cv-hot-badge">
                <svg viewBox="0 0 20 20" fill="currentColor" width="11" height="11"><path fill-rule="evenodd" d="M12.395 2.553a1 1 0 00-1.45-.385c-.345.23-.614.558-.822.88-.214.33-.403.713-.57 1.116-.334.804-.614 1.768-.84 2.734a31.365 31.365 0 00-.613 3.58 2.64 2.64 0 01-.945-1.067c-.328-.68-.398-1.534-.398-2.654A1 1 0 005.05 6.05 6.981 6.981 0 003 11a7 7 0 1011.95-4.95c-.592-.591-.98-.985-1.348-1.467-.363-.476-.724-1.063-1.207-2.03zM12.12 15.12A3 3 0 017 13s.879.5 2.5.5c0-1 .5-4 1.25-4.5.5 1 .786 1.293 1.371 1.879A2.99 2.99 0 0113 13a2.99 2.99 0 01-.879 2.121z" clip-rule="evenodd"/></svg>
                热门
              </span>
            </div>
            <h2 class="cv-detail-title">{{ selectedCase.title }}</h2>
            <div class="cv-detail-meta-row">
              <span>
                <svg viewBox="0 0 20 20" fill="currentColor" width="13" height="13"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/></svg>
                {{ selectedCase.caseNo }}
              </span>
              <span>
                <svg viewBox="0 0 20 20" fill="currentColor" width="13" height="13"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm3 1h2v2H7V5zm2 4H7v2h2V9zm2-4h2v2h-2V5zm2 4h-2v2h2V9z" clip-rule="evenodd"/></svg>
                {{ selectedCase.court }}
              </span>
              <span>
                <svg viewBox="0 0 20 20" fill="currentColor" width="13" height="13"><path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/></svg>
                {{ selectedCase.year }} 年
              </span>
            </div>
            <p class="cv-detail-summary">{{ selectedCase.summary }}</p>

            <!-- Action bar -->
            <div class="cv-detail-actions">
              <button class="cv-action-btn like" :class="{ liked: likedIds.includes(selectedCase.id) }" @click="toggleLike(selectedCase.id)">
                <svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15"><path d="M2 10.5a1.5 1.5 0 113 0v6a1.5 1.5 0 01-3 0v-6zM6 10.333v5.43a2 2 0 001.106 1.79l.05.025A4 4 0 008.943 18h5.416a2 2 0 001.962-1.608l1.2-6A2 2 0 0015.56 8H12V4a2 2 0 00-2-2 1 1 0 00-1 1v.667a4 4 0 01-.8 2.4L6.8 7.933a4 4 0 00-.8 2.4z"/></svg>
                {{ likedIds.includes(selectedCase.id) ? '已点赞' : '点赞' }}
                <span class="cv-action-count">{{ selectedCase.likes + (likedIds.includes(selectedCase.id) ? 1 : 0) }}</span>
              </button>
              <button class="cv-action-btn ask" @click="goAsk(selectedCase)">
                <svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15"><path d="M2 5a2 2 0 012-2h7a2 2 0 012 2v4a2 2 0 01-2 2H9l-3 3v-3H4a2 2 0 01-2-2V5z"/><path d="M15 7v2a4 4 0 01-4 4H9.828l-1.766 1.767c.28.149.599.233.938.233h2l3 3v-3h2a2 2 0 002-2V9a2 2 0 00-2-2h-1z"/></svg>
                向 AI 咨询此类问题
              </button>
            </div>
          </div>

          <!-- Facts -->
          <div class="cv-section">
            <div class="cv-section-label">
              <span class="cv-section-num">01</span>案情经过
            </div>
            <p class="cv-section-text">{{ selectedCase.facts }}</p>
          </div>

          <!-- Key Points -->
          <div class="cv-section">
            <div class="cv-section-label">
              <span class="cv-section-num">02</span>裁判要点
            </div>
            <div class="cv-keypoints">
              <div v-for="(pt, i) in selectedCase.keyPoints" :key="i" class="cv-kp-item">
                <span class="cv-kp-dot"></span>
                <span>{{ pt }}</span>
              </div>
            </div>
          </div>

          <!-- Law Refs -->
          <div class="cv-section">
            <div class="cv-section-label">
              <span class="cv-section-num">03</span>法律依据
            </div>
            <div class="cv-lawrefs">
              <div v-for="(ref, i) in selectedCase.lawRefs" :key="i" class="cv-lawref-chip">
                <svg viewBox="0 0 20 20" fill="currentColor" width="13" height="13"><path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.396 0 2.706.374 3.831 1.029A7.969 7.969 0 0114.5 14c1.255 0 2.443.29 3.5.804v-10A7.968 7.968 0 0014.5 4c-1.255 0-2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z"/></svg>
                {{ ref }}
              </div>
            </div>
          </div>

          <!-- AI Analysis -->
          <div class="cv-section cv-analysis-section">
            <div class="cv-section-label">
              <span class="cv-section-num">04</span>AI 法律分析
            </div>
            <div class="cv-analysis-box">
              <div class="cv-analysis-avatar">
                <svg viewBox="0 0 56 56" fill="none" width="22" height="22">
                  <line x1="28" y1="6" x2="28" y2="50" stroke="#b8943f" stroke-width="2.5"/>
                  <line x1="8" y1="12" x2="48" y2="12" stroke="#b8943f" stroke-width="2.5"/>
                  <path d="M8 12 L3 28 Q8 34 13 28 L8 12Z" fill="none" stroke="#b8943f" stroke-width="2" stroke-linejoin="round"/>
                  <path d="M48 12 L43 28 Q48 34 53 28 L48 12Z" fill="none" stroke="#b8943f" stroke-width="2" stroke-linejoin="round"/>
                  <line x1="19" y1="50" x2="37" y2="50" stroke="#b8943f" stroke-width="2.5"/>
                </svg>
              </div>
              <p class="cv-analysis-text">{{ selectedCase.aiAnalysis }}</p>
            </div>
          </div>

          <!-- ===== Comments ===== -->
          <div class="cv-section cv-comments-section">
            <div class="cv-section-label">
              <span class="cv-section-num">05</span>用户评论
              <span class="cv-comment-count">{{ getComments(selectedCase.id).length }}</span>
            </div>

            <!-- Comment Input -->
            <div class="cv-comment-input-wrap">
              <div class="cv-comment-avatar self">我</div>
              <div class="cv-comment-input-box">
                <textarea
                  v-model="commentText"
                  class="cv-comment-textarea"
                  placeholder="分享你对本案的看法、疑问或补充…"
                  rows="2"
                  @keydown.ctrl.enter="submitComment"
                ></textarea>
                <div class="cv-comment-input-footer">
                  <span class="cv-comment-hint">Ctrl + Enter 发送</span>
                  <button class="cv-comment-submit" :disabled="!commentText.trim()" @click="submitComment">
                    发表评论
                  </button>
                </div>
              </div>
            </div>

            <!-- Comment List -->
            <div class="cv-comment-list">
              <transition-group name="cv-comment">
                <div v-for="cm in getComments(selectedCase.id)" :key="cm.id" class="cv-comment-item">
                  <div class="cv-comment-avatar" :style="{ background: cm.avatarColor }">{{ cm.avatar }}</div>
                  <div class="cv-comment-body">
                    <div class="cv-comment-head">
                      <span class="cv-comment-name">{{ cm.name }}</span>
                      <span class="cv-comment-role" v-if="cm.role">{{ cm.role }}</span>
                      <span class="cv-comment-time">{{ cm.time }}</span>
                    </div>
                    <p class="cv-comment-text">{{ cm.text }}</p>
                    <div class="cv-comment-actions">
                      <button class="cv-cmt-like" :class="{ active: cm.userLiked }" @click="likeComment(cm)">
                        <svg viewBox="0 0 20 20" fill="currentColor" width="12" height="12"><path d="M2 10.5a1.5 1.5 0 113 0v6a1.5 1.5 0 01-3 0v-6zM6 10.333v5.43a2 2 0 001.106 1.79l.05.025A4 4 0 008.943 18h5.416a2 2 0 001.962-1.608l1.2-6A2 2 0 0015.56 8H12V4a2 2 0 00-2-2 1 1 0 00-1 1v.667a4 4 0 01-.8 2.4L6.8 7.933a4 4 0 00-.8 2.4z"/></svg>
                        {{ cm.likes }}
                      </button>
                      <button class="cv-cmt-reply" @click="replyTo = cm.name; commentText = `回复 @${cm.name}：`">
                        回复
                      </button>
                    </div>
                  </div>
                </div>
              </transition-group>
            </div>
          </div>

        </div>
      </template>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// ===== State =====
const searchText = ref('')
const activeDomain = ref('全部')
const sortBy = ref('default')
const selectedId = ref<number | null>(null)
const likedIds = ref<number[]>([])
const commentText = ref('')
const replyTo = ref('')

// ===== Domain tags =====
const domainTags = [
  { label: '全部', val: '全部' },
  { label: '劳动用工', val: '劳动用工' },
  { label: '婚姻家庭', val: '婚姻家庭' },
  { label: '合同纠纷', val: '合同纠纷' },
  { label: '房产纠纷', val: '房产纠纷' },
  { label: '刑事辩护', val: '刑事辩护' },
  { label: '消费维权', val: '消费维权' },
  { label: '知识产权', val: '知识产权' },
]

const sorts = [
  { label: '默认', val: 'default' },
  { label: '最热', val: 'hot' },
  { label: '最新', val: 'new' },
]

const domainColorMap: Record<string, string> = {
  '劳动用工': '#6b4c17',
  '婚姻家庭': '#7c2d3e',
  '合同纠纷': '#1e4d73',
  '房产纠纷': '#1e5c3a',
  '刑事辩护': '#3d1a73',
  '消费维权': '#6b2e17',
  '知识产权': '#1a3d73',
  '行政法律': '#4a3a17',
}
const domainColor = (d: string) => domainColorMap[d] || '#374151'

const outcomeMap: Record<string, string> = {
  win: '胜诉',
  partial: '部分支持',
  lose: '败诉',
  settle: '调解结案',
}

// ===== Cases Data =====
const cases = reactive([
  {
    id: 1, hot: true,
    domain: '劳动用工', year: '2023', outcome: 'win',
    caseNo: '（2023）京0105民初2341号',
    court: '北京市朝阳区人民法院',
    title: '某科技公司违法解除劳动合同赔偿案',
    summary: '员工因拒绝强制超时加班被以"严重违纪"解除合同，法院认定违法，判决支付双倍赔偿金。',
    facts: '原告李某于2020年入职某科技公司担任软件工程师，公司要求每周强制加班20小时以上。2022年12月，原告以身体健康为由拒绝超时加班，公司随即以"严重违反规章制度"为由单方面解除劳动合同。原告申请劳动仲裁后提起诉讼，仲裁裁决支持原告，公司不服提起诉讼。',
    keyPoints: [
      '用人单位解除劳动合同须有法定事由，"拒绝违法超时加班"不构成严重违纪',
      '《劳动法》第41条规定每日加班不超过3小时、每月不超过36小时，公司要求每周20小时加班严重违法',
      '违法解除劳动合同须支付经济补偿金的2倍作为赔偿金（N×2），工龄3年赔偿额约8.4万元',
    ],
    lawRefs: ['《劳动合同法》第47条', '《劳动合同法》第87条', '《劳动法》第41条'],
    aiAnalysis: '本案核心在于用人单位能否将"拒绝违法超时加班"认定为"严重违反规章制度"。根据《劳动合同法》第39条，严重违纪需有明确规章制度依据且该制度本身合法。公司规章制度中规定的加班时长超出法定上限，属无效条款，员工拒绝执行违法指令不构成违纪。遭遇类似情况，建议劳动者保存加班记录、工资条及公司通知等证据，及时申请劳动仲裁。',
    likes: 142,
  },
  {
    id: 2, hot: true,
    domain: '婚姻家庭', year: '2023', outcome: 'partial',
    caseNo: '（2023）沪0115民初8872号',
    court: '上海市浦东新区人民法院',
    title: '离婚诉讼中婚前房产婚后还贷增值分割案',
    summary: '夫妻一方婚前购房，婚后共同还贷，离婚时增值部分及共同还贷出资依法公平分割。',
    facts: '男方婚前以个人名义全款购买一套住房，婚后双方共同居住。婚后数年内，夫妻以共同财产偿还了部分房贷。离婚诉讼时该房产已升值约200万元，女方主张共同财产分割，男方主张房产为婚前个人财产。',
    keyPoints: [
      '婚前个人财产不因结婚自动转化为夫妻共同财产',
      '婚后以共同财产偿还贷款的部分及对应增值，离婚时应由房产所有方对另一方进行补偿',
      '补偿金额 = 共同还贷金额 ÷ 购房总价 × 当前市值，兼顾公平原则酌情调整',
    ],
    lawRefs: ['《民法典》第1063条', '《民法典》第1087条', '最高法婚姻法司法解释三第7条'],
    aiAnalysis: '婚前房产的归属认定是婚姻财产纠纷中最高频的问题之一。民法典明确婚前财产婚后不转化，但共同还贷部分属于夫妻共同财产的使用，离婚时须予以补偿。实践中，补偿计算需要提供银行还款记录、房屋评估报告，建议离婚前委托有资质的评估机构出具房产价值报告以供法院参考。',
    likes: 98,
  },
  {
    id: 3, hot: false,
    domain: '合同纠纷', year: '2024', outcome: 'win',
    caseNo: '（2024）粤互联网民初1056号',
    court: '广州互联网法院',
    title: '电商平台商家虚假发货三倍赔偿案',
    summary: '消费者下单后商家录入空物流单号虚假发货，法院认定欺诈，支持退款及三倍赔偿。',
    facts: '原告于某电商平台下单购买笔记本电脑，支付8600元。商家在系统中录入虚假物流信息，实际从未发货，物流单号为空单。消费者多次催促无回应，退款申请被拒，投诉平台后仍未解决，遂诉至法院。',
    keyPoints: [
      '虚假录入物流信息欺骗消费者构成欺诈，消费者可主张退款并按购买金额三倍赔偿',
      '三倍赔偿不足500元的，按500元计算（本案8600×3=25800元）',
      '电商平台对平台内欺诈行为知道或应当知道而未采取措施的，承担连带责任',
    ],
    lawRefs: ['《消费者权益保护法》第55条', '《电子商务法》第38条', '《民法典》第148条'],
    aiAnalysis: '电商欺诈维权的关键在于"欺诈"的认定。虚假发货（录入空单号）属于典型欺诈行为，证据保全尤为重要：需截图保存订单页面、物流信息、平台客服记录及商家聊天记录。建议消费者优先通过平台投诉获取处理记录，作为诉讼证据，同时向市场监管部门投诉，提高维权成功率。',
    likes: 76,
  },
  {
    id: 4, hot: false,
    domain: '房产纠纷', year: '2023', outcome: 'win',
    caseNo: '（2023）粤0305民初4412号',
    court: '深圳市南山区人民法院',
    title: '中介隐瞒凶宅信息买方撤销合同赔偿案',
    summary: '中介公司故意隐瞒房屋曾发生命案，买方签约后获悉，法院支持撤销合同并赔偿全部损失。',
    facts: '原告通过中介公司以320万元购买二手房一套，签约并支付首付100万元后，从邻居处得知该房屋两年前发生过命案。向中介核实，对方承认知情但称"不影响居住"。原告诉请撤销合同、退还首付及赔偿中介费和利息损失。',
    keyPoints: [
      '"凶宅"系影响购房决策的重大事项，经纪机构及出卖人负有主动披露义务',
      '故意隐瞒重大不利信息构成欺诈，买方有权依法撤销合同',
      '合同撤销后，出卖方返还购房款，并赔偿因欺诈造成的全部实际损失（含中介费、资金利息等）',
    ],
    lawRefs: ['《民法典》第148条', '《民法典》第157条', '《房地产经纪管理办法》第25条'],
    aiAnalysis: '"凶宅"问题在司法实践中已形成较为稳定的裁判规则——无论买方是否明确询问，卖方及中介均有主动告知义务。购房前建议查询房屋历史信息，向居委会、物业或邻居了解情况。若已签约，一旦发现隐瞒，应立即通过书面方式（微信/邮件）向对方表明知情并表示异议，固定时间节点证据。',
    likes: 113,
  },
  {
    id: 5, hot: true,
    domain: '刑事辩护', year: '2022', outcome: 'win',
    caseNo: '（2022）苏0281刑初206号',
    court: '江苏省昆山市人民法院',
    title: '反杀持刀歹徒正当防卫无罪辩护成功案',
    summary: '被告人遭歹徒持刀袭击夺刀反击致对方死亡，辩护律师援引正当防卫条款，法院认定无罪。',
    facts: '被告人某夜骑车回家途中，遭一名男子驾车蓄意拦截并持刀多次砍击。被告人奋力抢夺凶刀后连续击打对方，对方经抢救无效死亡。公诉机关以故意伤害罪（致人死亡）提起公诉，辩护律师提出正当防卫系无罪辩护主张。',
    keyPoints: [
      '正当防卫须同时满足：起因（不法侵害）、时间（侵害正在进行）、对象（实施侵害者）、主观（防卫意图）四个条件',
      '面对持械严重暴力侵害，为保护人身安全采取防卫行为致侵害人死亡，不属于防卫过当',
      '《刑法》第20条第3款（无限防卫权）：对正在进行行凶、杀人、抢劫、强奸、绑架等暴力犯罪采取防卫行为，造成不法侵害人伤亡，不属于防卫过当，不负刑事责任',
    ],
    lawRefs: ['《刑法》第20条', '最高检《关于依法妥善办理轻伤害案件的指导意见》'],
    aiAnalysis: '正当防卫的认定历来是刑事辩护的难点，司法实践中"防卫过当"认定较为保守。本案的价值在于确立了"面对持械严重暴力不需要等比例防卫"的裁判导向。对于普通民众，遭遇持械暴力侵害时应优先逃跑，若无法逃脱被迫防卫，事后应立即报警、保护现场、寻找目击证人，并委托专业刑事律师处理。',
    likes: 287,
  },
  {
    id: 6, hot: false,
    domain: '消费维权', year: '2024', outcome: 'partial',
    caseNo: '（2024）川0107民初3301号',
    court: '成都市武侯区人民法院',
    title: '健身房预付卡关门跑路消费者集体追偿案',
    summary: '健身俱乐部突然停业、法定代表人失联，34名消费者持卡无法消费，法院支持退还未消费余额。',
    facts: '某健身俱乐部以优惠价格向消费者销售年卡和三年卡。因经营困难突然关门，法定代表人失联。34名持卡消费者组成群体诉讼，要求退还预付款余额合计约18万元。经营者名下财产已被多家债权人申请查封保全。',
    keyPoints: [
      '预付式消费中，经营者停止经营构成根本违约，消费者有权解除合同要求退还未消费余额',
      '经营者资不抵债时，消费者以预付款债权进入破产程序，按比例受偿',
      '预付卡资金应依法纳入第三方监管，经营者违规使用须承担额外赔偿责任',
    ],
    lawRefs: ['《消费者权益保护法》第53条', '《民法典》第563条', '《单用途商业预付卡管理办法》第12条'],
    aiAnalysis: '预付卡"跑路"是高频消费纠纷，消费者追偿难度较大。建议：①充值前查询企业信用（国家企业信用信息公示系统）；②小额多次充值而非一次性大额充值；③保存收据、合同及消费记录；④发现异常立即向市场监管局投诉、申请财产保全；⑤联合其他受害者集体维权，降低诉讼成本。',
    likes: 64,
  },
  {
    id: 7, hot: false,
    domain: '知识产权', year: '2023', outcome: 'win',
    caseNo: '（2023）浙互联网民初2187号',
    court: '杭州互联网法院',
    title: '短视频博主原创内容被搬运著作权侵权赔偿案',
    summary: '原创短视频被他人搬运并配以不同BGM再发布，累计500万播放，法院支持删除及赔偿经济损失。',
    facts: '原告为签约创作者，独立制作美食类短视频，在某平台积累数十万粉丝。其发现多账号将视频完整搬运并关闭原声配以不同BGM后再发布，累计播放量超500万次，被告账号因此获得平台流量激励约3000元。',
    keyPoints: [
      '短视频属著作权法保护的"以类似摄制电影方法创作的作品"，受信息网络传播权保护',
      '未经授权复制、传播他人视频构成侵权，更换背景音乐不改变侵权性质',
      '赔偿金额参考：被告违法获利（3000元）× 惩罚系数 + 维权合理费用，酌定赔偿2万元',
    ],
    lawRefs: ['《著作权法》第52条', '《信息网络传播权保护条例》第2条', '《民法典》第179条'],
    aiAnalysis: '短视频版权维权已成为互联网法院最高频案件类型之一。创作者应尽早在作品中嵌入数字水印或字幕署名，并在发布时截图保存作品ID和发布时间戳。发现侵权后，可通过平台侵权投诉（通常24小时内删除）、向公证处申请网页证据保全，或向互联网法院提起在线诉讼，全程网上办理、效率较高。',
    likes: 89,
  },
  {
    id: 8, hot: false,
    domain: '劳动用工', year: '2024', outcome: 'win',
    caseNo: '（2024）浙0106民初5521号',
    court: '杭州市西湖区人民法院',
    title: '竞业限制协议效力与补偿金缺失争议案',
    summary: '员工离职后被索赔竞业违约金，但公司从未支付补偿金，法院认定协议对员工不产生约束力。',
    facts: '原告某软件公司称，前员工张某离职后加入竞争对手公司，违反竞业限制协议，要求张某支付违约金20万元。张某抗辩称，离职至今公司从未支付任何竞业限制补偿金，协议对其不产生约束力。',
    keyPoints: [
      '竞业限制协议须以用人单位按月支付补偿金为前提，否则劳动者有权拒绝履行',
      '用人单位连续3个月未支付补偿金的，劳动者可书面通知解除竞业限制协议，无需承担违约责任',
      '竞业限制违约金应以实际损失为上限，不得约定明显过高的违约金',
    ],
    lawRefs: ['《劳动合同法》第23条', '《劳动合同法》第24条', '最高法劳动争议司法解释（四）第6条'],
    aiAnalysis: '竞业限制是高科技企业保护商业秘密的常用手段，但实践中大量协议因企业未支付补偿金而失效。劳动者离职后收到竞业限制要求，应首先确认：①公司是否按月支付了竞业限制补偿金；②协议是否明确了补偿金标准（通常不低于离职前12个月月平均工资的30%）。如补偿金从未支付，可发送书面通知解除协议后自由就业。',
    likes: 55,
  },
  {
    id: 9, hot: false,
    domain: '合同纠纷', year: '2023', outcome: 'partial',
    caseNo: '（2023）京0108民初7731号',
    court: '北京市海淀区人民法院',
    title: '装修公司偷工减料使用劣质材料违约赔偿案',
    summary: '装修公司擅自以劣质材料替换合同约定品牌，经鉴定确认违约，法院支持减价及整改赔偿。',
    facts: '业主与某装修公司签订全包装修合同，约定使用特定品牌瓷砖和地板。竣工后业主发现材料与合同不符，委托第三方鉴定机构检测，确认实际用料品牌与规格均低于约定标准，价值差价约3.2万元。业主要求全额退款并拆除重做。',
    keyPoints: [
      '承揽方擅自变更合同约定材料品牌和规格，构成违约，业主可要求修复、减少报酬或赔偿损失',
      '已入住使用的"全部拆除重做"主张，须证明劣质材料对居住存在实质安全隐患，否则法院通常以差价补偿代替',
      '鉴定机构出具的材料差价检测报告可直接作为损失赔偿依据',
    ],
    lawRefs: ['《民法典》第781条', '《民法典》第584条', '《建设工程质量管理条例》第32条'],
    aiAnalysis: '装修纠纷中证据保全至关重要。建议：①装修前对合同约定材料品牌/型号逐项拍照存档；②装修过程中全程拍视频记录；③收房时不要急于签字验收，委托第三方监理；④发现材料不符，立即书面通知施工方（不要仅口头协商），并委托有资质的鉴定机构取证。法院对装修纠纷通常倾向于"减价补偿"而非"推倒重来"，金额以鉴定价差为准。',
    likes: 47,
  },
  {
    id: 10, hot: false,
    domain: '行政法律', year: '2023', outcome: 'win',
    caseNo: '（2023）沪03行终112号',
    court: '上海市第三中级人民法院',
    title: '违规罚款行政处罚决定被撤销行政诉讼案',
    summary: '行政机关在未告知当事人陈述申辩权利的情况下直接作出罚款处罚，法院认定程序违法予以撤销。',
    facts: '原告某餐饮企业因被举报存在食品标签违规问题，行政执法人员到店检查后直接开具罚款通知，金额1.2万元。原告认为从未获得陈述申辩机会，遂申请行政复议被维持后，提起行政诉讼。',
    keyPoints: [
      '行政机关作出行政处罚前，须告知当事人拟作出的处罚内容及依据，保障其陈述申辩权',
      '未履行告知程序直接作出罚款处罚，违反《行政处罚法》第44条，属程序违法',
      '程序违法的行政处罚决定由法院依法予以撤销，行政机关可依法重新作出',
    ],
    lawRefs: ['《行政处罚法》第44条', '《行政处罚法》第45条', '《行政诉讼法》第70条'],
    aiAnalysis: '行政处罚维权的关键在于程序性权利的保护。当事人收到行政处罚决定书时，应重点核查：①是否收到过《行政处罚事先告知书》；②是否有机会提交书面陈述或申辩；③罚款金额是否与违法情节相称。如存在程序瑕疵，可在收到处罚决定书60日内申请行政复议，或6个月内直接提起行政诉讼，程序违法是最有效的撤销理由之一。',
    likes: 38,
  },
])

// ===== Comments Data =====
const commentsMap = reactive<Record<number, any[]>>({
  1: [
    { id: 101, name: '林律师', role: '执业律师', avatar: '林', avatarColor: '#7c5c1e', time: '2024-03-12', text: '这个案子很典型，强制加班问题在互联网行业非常普遍。劳动者要注意保存加班审批记录、打卡记录和工作群消息，这些都是关键证据。', likes: 23, userLiked: false },
    { id: 102, name: '王晓明', role: '', avatar: '王', avatarColor: '#1e4d73', time: '2024-03-15', text: '我也遇到过类似情况，最后拿到了赔偿。建议大家离职前先咨询律师，不要轻易签署任何放弃权利的协议。', likes: 18, userLiked: false },
    { id: 103, name: '匿名用户', role: '', avatar: '匿', avatarColor: '#374151', time: '2024-04-02', text: '请问双倍赔偿的计算基数是什么？是税前工资还是实际到手的工资？', likes: 7, userLiked: false },
  ],
  2: [
    { id: 201, name: '张法官（退休）', role: '法律顾问', avatar: '张', avatarColor: '#7c2d3e', time: '2024-02-18', text: '婚前房产婚后还贷的分割问题，民法典相较于之前的司法解释有所调整，实践中计算方式各地法院也略有差异，建议委托当地律师评估。', likes: 31, userLiked: false },
    { id: 202, name: '小陈', role: '', avatar: '陈', avatarColor: '#1e5c3a', time: '2024-03-08', text: '这种情况下女方能拿到多少补偿，主要取决于婚后还了多少贷款，以及房子涨了多少，是吗？', likes: 12, userLiked: false },
  ],
  5: [
    { id: 501, name: '刑辩律师Lily', role: '刑事律师', avatar: '李', avatarColor: '#3d1a73', time: '2023-11-20', text: '昆山反杀案是正当防卫认定的重要里程碑，之后最高检的指导意见也明确了不能苛求防卫人"等比例"防卫，这是法律进步。', likes: 89, userLiked: false },
    { id: 502, name: '法律学生', role: '法学院在读', avatar: '学', avatarColor: '#1e4d73', time: '2023-12-05', text: '请问老师，如果是在家里遭遇入室抢劫，防卫权是否更宽泛？', likes: 34, userLiked: false },
    { id: 503, name: '普通市民', role: '', avatar: '民', avatarColor: '#374151', time: '2024-01-15', text: '这个案子让我知道遇到歹徒真的可以反抗，之前总担心会被认定为故意伤害。', likes: 56, userLiked: false },
  ],
})

const getComments = (id: number) => commentsMap[id] || []

// ===== Computed =====
const filteredCases = computed(() => {
  let list = [...cases]
  if (activeDomain.value !== '全部') list = list.filter(c => c.domain === activeDomain.value)
  if (searchText.value.trim()) {
    const kw = searchText.value.trim().toLowerCase()
    list = list.filter(c =>
      c.title.toLowerCase().includes(kw) ||
      c.summary.toLowerCase().includes(kw) ||
      c.domain.toLowerCase().includes(kw) ||
      c.keyPoints.some(k => k.toLowerCase().includes(kw))
    )
  }
  return list
})

const sortedCases = computed(() => {
  const list = [...filteredCases.value]
  if (sortBy.value === 'hot') return list.sort((a, b) => b.likes - a.likes)
  if (sortBy.value === 'new') return list.sort((a, b) => Number(b.year) - Number(a.year))
  return list
})

const selectedCase = computed(() => cases.find(c => c.id === selectedId.value) || null)

// ===== Methods =====
const selectCase = (id: number) => {
  selectedId.value = id
  commentText.value = ''
  replyTo.value = ''
}

const toggleLike = (id: number) => {
  const idx = likedIds.value.indexOf(id)
  if (idx === -1) {
    likedIds.value.push(id)
    ElMessage({ message: '点赞成功', type: 'success', duration: 1000 })
  } else {
    likedIds.value.splice(idx, 1)
  }
}

const likeComment = (cm: any) => {
  if (cm.userLiked) {
    cm.likes--
    cm.userLiked = false
  } else {
    cm.likes++
    cm.userLiked = true
  }
}

const submitComment = () => {
  const text = commentText.value.trim()
  if (!text) return
  const caseId = selectedId.value!
  if (!commentsMap[caseId]) commentsMap[caseId] = []
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || '匿名用户'
  const initial = name.charAt(name.length - 1)
  commentsMap[caseId].unshift({
    id: Date.now(),
    name,
    role: '',
    avatar: initial,
    avatarColor: '#b8943f',
    time: new Date().toLocaleDateString('zh-CN'),
    text,
    likes: 0,
    userLiked: false,
  })
  commentText.value = ''
  replyTo.value = ''
  ElMessage({ message: '评论发表成功', type: 'success', duration: 1500 })
}

const goAsk = (c: any) => {
  router.push({ path: '/chat', query: { q: c.title } })
}
</script>

<style scoped>
/* ===== Variables ===== */
.cv-root {
  --gold: #b8943f;
  --gold-light: #d4aa5a;
  --navy: #0c1322;
  --ff-zh: "Songti SC", "Source Han Serif SC", SimSun, STSong, serif;
  --ff-brand: Palatino, "Palatino Linotype", Georgia, serif;

  display: flex;
  height: calc(100vh - var(--header-height, 60px) - 48px);
  gap: 0;
  background: #f7f5f1;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
}

/* ===== Left Panel ===== */
.cv-list-panel {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e0d0;
  background: #fff;
}

.cv-list-head {
  padding: 16px;
  border-bottom: 1px solid #f0ece4;
  flex-shrink: 0;
}

/* Search */
.cv-search-wrap {
  position: relative;
  margin-bottom: 12px;
}
.cv-search-icon {
  position: absolute; left: 10px; top: 50%; transform: translateY(-50%);
  color: #94a3b8; pointer-events: none;
}
.cv-search-input {
  width: 100%; height: 36px;
  padding: 0 32px 0 34px;
  border: 1px solid #e2d9c8;
  border-radius: 8px;
  font-size: 13px;
  color: #1a1a2e;
  background: #faf9f6;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
  box-sizing: border-box;
}
.cv-search-input:focus { border-color: var(--gold); }
.cv-search-input::placeholder { color: #c0b8a8; }
.cv-search-clear {
  position: absolute; right: 8px; top: 50%; transform: translateY(-50%);
  background: none; border: none; cursor: pointer; color: #94a3b8; padding: 2px;
  display: flex; align-items: center;
}
.cv-search-clear:hover { color: #475569; }

/* Domain filters */
.cv-domain-filters {
  display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px;
}
.cv-ftag {
  padding: 4px 12px; border: 1px solid #e2d9c8; border-radius: 16px;
  background: #faf9f6; font-size: 12px; color: #64748b;
  cursor: pointer; transition: all 0.18s; font-family: inherit;
}
.cv-ftag:hover { border-color: var(--gold); color: var(--gold); }
.cv-ftag.active { background: var(--gold); border-color: var(--gold); color: #fff; font-weight: 600; }

/* Meta row */
.cv-list-meta {
  display: flex; align-items: center; justify-content: space-between;
}
.cv-result-count { font-size: 12px; color: #94a3b8; }
.cv-result-count em { font-style: normal; color: var(--gold); font-weight: 700; }
.cv-sort-tabs { display: flex; gap: 2px; }
.cv-sort-btn {
  padding: 3px 10px; border: 1px solid transparent; border-radius: 4px;
  font-size: 12px; color: #94a3b8; background: none; cursor: pointer; transition: all 0.15s; font-family: inherit;
}
.cv-sort-btn:hover { color: #475569; }
.cv-sort-btn.active { border-color: #e2d9c8; color: var(--gold); background: #faf7f0; font-weight: 600; }

/* Cards scroll */
.cv-cards-scroll {
  flex: 1; overflow-y: auto; padding: 8px;
}
.cv-cards-scroll::-webkit-scrollbar { width: 4px; }
.cv-cards-scroll::-webkit-scrollbar-thumb { background: #e2d9c8; border-radius: 2px; }

/* Case card */
.cv-card {
  padding: 14px 14px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s;
  margin-bottom: 4px;
  border-left: 3px solid transparent;
  position: relative;
}
.cv-card:hover { background: #faf7f0; }
.cv-card.active {
  background: #fdf8ed;
  border-left-color: var(--gold);
  box-shadow: inset 0 0 0 1px rgba(184,148,63,0.15);
}

.cv-card-row1 {
  display: flex; align-items: center; gap: 6px; margin-bottom: 7px; flex-wrap: wrap;
}
.cv-dtag {
  display: inline-block; padding: 2px 8px; border-radius: 3px;
  font-size: 11px; font-weight: 600; color: rgba(255,255,255,0.92); letter-spacing: 0.3px;
}
.cv-dtag.lg { padding: 4px 12px; font-size: 12px; }
.cv-outcome-tag {
  font-size: 11px; font-weight: 600; padding: 2px 7px; border-radius: 10px;
}
.cv-outcome-tag.win     { color: #15803d; background: #dcfce7; }
.cv-outcome-tag.partial { color: #b45309; background: #fef3c7; }
.cv-outcome-tag.lose    { color: #b91c1c; background: #fee2e2; }
.cv-outcome-tag.settle  { color: #1d4ed8; background: #dbeafe; }
.cv-outcome-tag.lg { font-size: 12px; padding: 4px 12px; border-radius: 12px; }
.cv-card-year { font-size: 11px; color: #94a3b8; margin-left: auto; }
.cv-hot-badge {
  display: inline-flex; align-items: center; gap: 3px;
  font-size: 10px; font-weight: 700; color: #ea580c;
  background: #fff7ed; border: 1px solid #fed7aa;
  padding: 1px 6px; border-radius: 10px; letter-spacing: 0.3px;
}
.cv-card-title {
  font-size: 13px; font-weight: 700; color: #1a1a2e;
  line-height: 1.5; margin-bottom: 5px;
}
.cv-card-summary {
  font-size: 12px; color: #64748b; line-height: 1.6;
  overflow: hidden; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  margin-bottom: 8px;
}
.cv-card-row3 {
  display: flex; align-items: center; justify-content: space-between;
}
.cv-card-court { font-size: 11px; color: #94a3b8; }
.cv-card-stats {
  display: flex; align-items: center; gap: 10px;
  font-size: 11px; color: #94a3b8;
}
.cv-card-stats span {
  display: flex; align-items: center; gap: 3px;
}

/* Empty */
.cv-empty { text-align: center; padding: 60px 20px; color: #c0b8a8; }
.cv-empty-s { font-size: 56px; font-family: var(--ff-brand); color: rgba(184,148,63,0.15); line-height: 1; margin-bottom: 8px; }
.cv-empty p { font-size: 13px; }

/* ===== Right Panel ===== */
.cv-detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #faf9f6;
}

/* Empty state */
.cv-detail-empty {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 12px;
  color: #94a3b8;
}
.cv-de-icon {
  width: 80px; height: 80px; background: rgba(184,148,63,0.08);
  border: 1px solid rgba(184,148,63,0.15); border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
}
.cv-de-title { font-size: 16px; font-weight: 600; color: #64748b; margin: 0; }
.cv-de-sub { font-size: 13px; color: #94a3b8; text-align: center; max-width: 240px; line-height: 1.6; margin: 0; }

/* Detail scroll */
.cv-detail-scroll {
  flex: 1; overflow-y: auto; padding: 0;
}
.cv-detail-scroll::-webkit-scrollbar { width: 4px; }
.cv-detail-scroll::-webkit-scrollbar-thumb { background: #e2d9c8; border-radius: 2px; }

/* Detail Hero */
.cv-detail-hero {
  background: var(--navy);
  padding: 28px 32px 24px;
  position: relative;
  overflow: hidden;
}
.cv-detail-hero::after {
  content: '§';
  position: absolute; right: -12px; bottom: -20px;
  font-size: 140px; font-family: var(--ff-brand);
  color: rgba(184,148,63,0.05); line-height: 1; pointer-events: none;
}
.cv-dh-tags { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; flex-wrap: wrap; }
.cv-detail-title {
  font-family: var(--ff-zh);
  font-size: 20px; font-weight: 700; color: #fff;
  margin: 0 0 12px; line-height: 1.4;
}
.cv-detail-meta-row {
  display: flex; align-items: center; gap: 16px; flex-wrap: wrap;
  font-size: 12px; color: rgba(255,255,255,0.4); margin-bottom: 14px;
}
.cv-detail-meta-row span { display: flex; align-items: center; gap: 5px; }
.cv-detail-summary { font-size: 13px; color: rgba(255,255,255,0.6); line-height: 1.7; margin: 0 0 20px; }

/* Action bar */
.cv-detail-actions { display: flex; gap: 10px; flex-wrap: wrap; position: relative; z-index: 2; }
.cv-action-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 18px; border-radius: 6px; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; font-family: inherit; border: 1px solid transparent;
  letter-spacing: 0.3px;
}
.cv-action-btn.like {
  background: rgba(255,255,255,0.06); border-color: rgba(255,255,255,0.12); color: rgba(255,255,255,0.6);
}
.cv-action-btn.like:hover { border-color: var(--gold); color: var(--gold); }
.cv-action-btn.like.liked { background: rgba(184,148,63,0.15); border-color: var(--gold); color: var(--gold); }
.cv-action-btn.ask { background: var(--gold); color: var(--navy); border-color: var(--gold); }
.cv-action-btn.ask:hover { background: var(--gold-light); transform: translateY(-1px); }
.cv-action-count { font-weight: 400; opacity: 0.7; }

/* Sections */
.cv-section { padding: 22px 32px; border-bottom: 1px solid #f0ece4; background: #fff; }
.cv-section:last-child { border-bottom: none; }
.cv-analysis-section { background: #faf9f6; }
.cv-comments-section { background: #fff; }

.cv-section-label {
  display: flex; align-items: center; gap: 10px;
  font-size: 13px; font-weight: 700; color: #334155;
  margin-bottom: 14px; letter-spacing: 0.3px;
}
.cv-section-num {
  font-size: 11px; font-weight: 700; color: var(--gold);
  font-family: var(--ff-brand); letter-spacing: 1px;
}
.cv-section-text { font-size: 13px; color: #475569; line-height: 1.85; margin: 0; }

/* Key points */
.cv-keypoints { display: flex; flex-direction: column; gap: 10px; }
.cv-kp-item {
  display: flex; align-items: flex-start; gap: 10px;
  font-size: 13px; color: #334155; line-height: 1.7;
}
.cv-kp-dot {
  width: 6px; height: 6px; border-radius: 50%; background: var(--gold);
  flex-shrink: 0; margin-top: 7px;
}

/* Law refs */
.cv-lawrefs { display: flex; flex-wrap: wrap; gap: 8px; }
.cv-lawref-chip {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; border-radius: 6px;
  background: #faf7f0; border: 1px solid rgba(184,148,63,0.2);
  font-size: 12px; color: #7a5c20; font-weight: 500;
}

/* AI Analysis */
.cv-analysis-box {
  display: flex; gap: 12px; align-items: flex-start;
  background: #fff; border: 1px solid rgba(184,148,63,0.2);
  border-left: 3px solid var(--gold);
  border-radius: 8px; padding: 16px;
}
.cv-analysis-avatar {
  width: 36px; height: 36px; flex-shrink: 0;
  background: #faf7f0; border: 1px solid rgba(184,148,63,0.2);
  border-radius: 10px; display: flex; align-items: center; justify-content: center;
}
.cv-analysis-text { font-size: 13px; color: #475569; line-height: 1.85; margin: 0; }

/* Comments */
.cv-comment-count {
  display: inline-flex; align-items: center; justify-content: center;
  background: #f1f5f9; color: #64748b;
  width: 20px; height: 20px; border-radius: 10px; font-size: 11px; font-weight: 700;
}

/* Comment input */
.cv-comment-input-wrap {
  display: flex; gap: 10px; margin-bottom: 20px;
}
.cv-comment-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: #fff; flex-shrink: 0;
  margin-top: 2px;
}
.cv-comment-avatar.self { background: var(--navy); }
.cv-comment-input-box { flex: 1; }
.cv-comment-textarea {
  width: 100%; padding: 10px 12px; border: 1px solid #e2d9c8; border-radius: 8px;
  font-size: 13px; color: #334155; resize: none; outline: none;
  transition: border-color 0.2s; font-family: inherit;
  background: #faf9f6; line-height: 1.6; box-sizing: border-box;
}
.cv-comment-textarea:focus { border-color: var(--gold); background: #fff; }
.cv-comment-textarea::placeholder { color: #c0b8a8; }
.cv-comment-input-footer {
  display: flex; align-items: center; justify-content: space-between; margin-top: 8px;
}
.cv-comment-hint { font-size: 11px; color: #c0b8a8; }
.cv-comment-submit {
  padding: 6px 18px; background: var(--gold); color: var(--navy);
  font-size: 13px; font-weight: 700; border: none; border-radius: 6px;
  cursor: pointer; transition: all 0.2s; font-family: inherit;
}
.cv-comment-submit:hover:not(:disabled) { background: var(--gold-light); }
.cv-comment-submit:disabled { opacity: 0.4; cursor: not-allowed; }

/* Comment list */
.cv-comment-list { display: flex; flex-direction: column; gap: 16px; }
.cv-comment-item { display: flex; gap: 10px; }
.cv-comment-body { flex: 1; }
.cv-comment-head {
  display: flex; align-items: center; gap: 8px; margin-bottom: 5px; flex-wrap: wrap;
}
.cv-comment-name { font-size: 13px; font-weight: 700; color: #1a1a2e; }
.cv-comment-role {
  font-size: 10px; font-weight: 600; color: var(--gold);
  background: rgba(184,148,63,0.1); padding: 1px 7px; border-radius: 10px;
}
.cv-comment-time { font-size: 11px; color: #94a3b8; margin-left: auto; }
.cv-comment-text { font-size: 13px; color: #475569; line-height: 1.7; margin: 0 0 8px; }
.cv-comment-actions { display: flex; align-items: center; gap: 12px; }
.cv-cmt-like {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 12px; color: #94a3b8; background: none; border: none;
  cursor: pointer; transition: color 0.2s; font-family: inherit; padding: 0;
}
.cv-cmt-like:hover { color: var(--gold); }
.cv-cmt-like.active { color: var(--gold); }
.cv-cmt-reply {
  font-size: 12px; color: #94a3b8; background: none; border: none;
  cursor: pointer; transition: color 0.2s; font-family: inherit; padding: 0;
}
.cv-cmt-reply:hover { color: #475569; }

/* Comment transition */
.cv-comment-enter-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.cv-comment-enter-from { opacity: 0; transform: translateY(-8px); }
</style>
