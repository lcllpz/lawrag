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
            placeholder="搜索招生问题标题、关键词…"
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
          <span class="cv-result-count">共 <em>{{ filteredCases.length }}</em> 条招生经验</span>
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
          <p>未找到相关招生经验</p>
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
        <p class="cv-de-title">选择一条招生经验</p>
        <p class="cv-de-sub">点击左侧任意卡片，查看完整背景、建议要点与评论</p>
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
              <span class="cv-section-num">01</span>背景情况
            </div>
            <p class="cv-section-text">{{ selectedCase.facts }}</p>
          </div>

          <!-- Key Points -->
          <div class="cv-section">
            <div class="cv-section-label">
              <span class="cv-section-num">02</span>关键建议
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
              <span class="cv-section-num">03</span>参考依据
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
              <span class="cv-section-num">04</span>AI 招生分析
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
                  placeholder="分享你对该招生问题的看法、疑问或补充…"
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
  { label: '本科招生', val: '本科招生' },
  { label: '研究生招生', val: '研究生招生' },
  { label: '志愿填报', val: '志愿填报' },
  { label: '调剂政策', val: '调剂政策' },
  { label: '选科要求', val: '选科要求' },
  { label: '奖助学金', val: '奖助学金' },
]

const sorts = [
  { label: '默认', val: 'default' },
  { label: '最热', val: 'hot' },
  { label: '最新', val: 'new' },
]

const domainColorMap: Record<string, string> = {
  '本科招生': '#1e4d73',
  '研究生招生': '#3d1a73',
  '志愿填报': '#1e5c3a',
  '调剂政策': '#7c2d3e',
  '选科要求': '#4a3a17',
  '奖助学金': '#6b2e17',
}
const domainColor = (d: string) => domainColorMap[d] || '#374151'

const outcomeMap: Record<string, string> = {
  win: '建议可行',
  partial: '条件受限',
  lose: '风险较高',
  settle: '需进一步核实',
}

// ===== Cases Data =====
const cases = reactive([
  {
    id: 1, hot: true,
    domain: '本科招生', year: '2025', outcome: 'win',
    caseNo: '招生经验 #A001',
    court: '某省本科批',
    title: '6200 位次考生冲稳保志愿组合成功上岸',
    summary: '考生按“冲2稳4保3”配置志愿，并结合专业组选科要求，最终被第一稳妥志愿录取。',
    facts: '考生为物理类，位次约6200，目标专业为计算机相关。咨询重点是志愿梯度如何配置，避免滑档与退档。',
    keyPoints: [
      '按近三年位次波动划分冲稳保，避免只看分数线',
      '优先核对专业组选科限制，防止“分数够但选科不符”',
      '服从专业调剂可以明显降低退档风险',
    ],
    lawRefs: ['招生章程（当年版）', '招生计划（省份分组）', '历年投档位次数据'],
    aiAnalysis: '该条经验的关键是“位次优先 + 规则校验”。在招生咨询中，分数只是表象，位次和专业组规则才是核心决策依据。',
    likes: 142,
  },
  {
    id: 2, hot: true,
    domain: '志愿填报', year: '2025', outcome: 'partial',
    caseNo: '招生经验 #A002',
    court: '新高考省份',
    title: '高分考生因不服从调剂被退档的复盘',
    summary: '考生只填热门专业且不服从调剂，虽进档但因专业计划满额被退档，后续只能参加征集志愿。',
    facts: '考生位次较好，但填报策略过于集中在热门专业组，且“是否服从调剂”选择了否，导致录取风险被放大。',
    keyPoints: [
      '高分不等于零风险，专业计划容量是硬约束',
      '志愿表中需设置一定比例的保底选项',
      '是否服从调剂需结合目标院校与风险偏好综合判断',
    ],
    lawRefs: ['平行志愿投档规则', '高校招生章程（调剂条款）', '征集志愿政策说明'],
    aiAnalysis: '该条经验提醒：志愿填报是风险管理过程。合理配置“冲稳保”比单纯追逐热门专业更关键。',
    likes: 98,
  },
  {
    id: 3, hot: false,
    domain: '研究生招生', year: '2025', outcome: 'settle',
    caseNo: '招生经验 #A003',
    court: '硕士统考',
    title: '初试高分但复试准备不足导致总分逆转',
    summary: '考生初试领先，但复试面试和英语表达准备不足，综合成绩被后来者反超。',
    facts: '考生只关注初试分数，忽视复试权重和院校复试细则，最终落入待录取边缘。',
    keyPoints: [
      '提前准备英文自我介绍与专业问答框架',
      '复试成绩占比较高时，不能只看初试排名',
      '联系导师与信息收集应合规、克制并以官方流程为准',
    ],
    lawRefs: ['研究生复试录取办法', '学院复试细则', '综合成绩计算公式'],
    aiAnalysis: '该经验说明“初试只是门票，复试决定上限”。制定复试节奏和模拟训练计划能显著提升确定性。',
    likes: 76,
  },
  {
    id: 4, hot: false,
    domain: '奖助学金', year: '2025', outcome: 'win',
    caseNo: '招生经验 #A004',
    court: '高校资助体系',
    title: '家庭经济困难考生通过奖助组合完成就读',
    summary: '考生结合国家助学贷款、校内奖学金与勤工助学，显著降低了入学经济压力。',
    facts: '家长重点关注“学费是否可承受”，希望在不放弃目标专业的前提下评估奖助政策可行性。',
    keyPoints: [
      '提前确认学费、住宿费与生活费的年度预算',
      '奖助学金政策需看申请时间窗口与评审条件',
      '国家助学贷款与校内资助可组合使用，降低现金流压力',
    ],
    lawRefs: ['高校奖助学金管理办法', '国家助学贷款政策', '新生入学资助说明'],
    aiAnalysis: '该经验强调“先算清总成本，再看资助组合”。多数院校都提供多层次资助，不应仅凭学费数字放弃目标院校。',
    likes: 113,
  },
])

// ===== Comments Data =====
const commentsMap = reactive<Record<number, any[]>>({
  1: [
    { id: 101, name: '张老师', role: '升学指导', avatar: '张', avatarColor: '#7c5c1e', time: '2025-03-12', text: '这个志愿结构很实用，建议每个梯度都放1-2个可接受专业，避免“只冲不保”。', likes: 23, userLiked: false },
    { id: 102, name: '李家长', role: '', avatar: '李', avatarColor: '#1e4d73', time: '2025-03-15', text: '我们按位次法重排志愿后，孩子最终录到了更合适的专业。', likes: 18, userLiked: false },
  ],
  2: [
    { id: 201, name: '王同学', role: '考研考生', avatar: '王', avatarColor: '#7c2d3e', time: '2025-02-18', text: '复试准备确实不能拖，尤其是英文问答和专业基础题。', likes: 31, userLiked: false },
    { id: 202, name: '赵老师', role: '研招咨询', avatar: '赵', avatarColor: '#1e5c3a', time: '2025-03-08', text: '建议同学们按目标院校复试权重倒推备考计划。', likes: 12, userLiked: false },
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
