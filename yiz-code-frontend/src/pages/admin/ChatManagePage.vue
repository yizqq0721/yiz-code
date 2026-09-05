<template>
  <div id="chatManagePage">
    <!-- 搜索表单（后端仅支持按应用ID查询） -->
    <a-form layout="inline" :model="searchForm" @finish="doSearch">
      <a-form-item label="应用ID">
        <a-input v-model:value="searchForm.appId" placeholder="输入应用ID" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="false"
      :scroll="{ x: 1400 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'message'">
          <a-tooltip :title="record.message">
            <div class="message-text">{{ record.message }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'messageType'">
          <a-tag :color="record.messageType === 'user' ? 'blue' : 'green'">
            {{ record.messageType === 'user' ? '用户消息' : 'AI消息' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="primary" size="small" @click="viewAppChat(record.appId)">
            查看对话
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- 加载更多（游标分页） -->
    <div v-if="appId" class="load-more">
      <span class="load-more-total">共 {{ total }} 条</span>
      <a-button type="link" :loading="loading" :disabled="!hasMore" @click="loadMore">
        加载更多
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { formatTime } from '@/utils/time'
// 大整数安全解析：修复后端雪花 ID（19 位 Long）在 JSON 解析时精度丢失的问题，
// 保证对话记录中的 id/appId 为精确字符串，"查看对话"跳转和 ID 列展示才正确
import { bigIntSafeTransformResponse } from '@/utils/jsonBigInt'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
    fixed: 'left',
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    width: 300,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    width: 100,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    width: 80,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right',
  },
]

// 搜索条件（后端接口只支持应用ID）
const searchForm = reactive({
  appId: '',
})

// 数据
const data = ref<API.ChatHistory[]>([])
const total = ref(0)
const loading = ref(false)

// 当前查询的应用ID
const appId = ref<number>()

// 游标：最后一条记录的创建时间
const lastCreateTime = ref<string>()

// 是否还有更多数据
const hasMore = computed(() => data.value.length < total.value)

// 获取数据（游标分页，后端不支持页码跳转）
const fetchData = async (isLoadMore = false) => {
  if (!appId.value || loading.value) return
  loading.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId.value,
      pageSize: 10,
    }
    // 加载更多时，传递最后一条消息的创建时间作为游标
    if (isLoadMore && lastCreateTime.value) {
      params.lastCreateTime = lastCreateTime.value
    }
    // 【修复雪花 ID 精度丢失】
    // 对话记录中的 id/appId 是 19 位 Long，默认 JSON 解析会被四舍五入，
    // 导致"查看对话"用错误 ID 跳转、表格 ID 列显示错误。
    // 通过 transformResponse 用 json-bigint 解析响应，id/appId 保持为精确字符串。
    const res = await listAppChatHistory(params, {
      transformResponse: [bigIntSafeTransformResponse],
    })
    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records ?? []
      data.value = isLoadMore ? [...data.value, ...records] : records
      total.value = res.data.data.totalRow ?? 0
      // 更新游标
      const lastRecord = records[records.length - 1]
      if (lastRecord?.createTime) {
        lastCreateTime.value = lastRecord.createTime
      }
    } else {
      message.error('获取数据失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const doSearch = () => {
  // 【修复雪花 ID 精度丢失】
  // 应用 ID 是 19 位 Long，绝不能用 Number() 转换后查询：超出 JS 安全整数范围会被舍入，
  // 导致查错应用。查询时保留原始字符串（后端会自动转换），Number 仅用于格式校验。
  const id = searchForm.appId.trim()
  const numId = Number(id)
  if (!id || !Number.isFinite(numId) || numId <= 0) {
    message.warning('请输入正确的应用ID')
    return
  }
  appId.value = id as unknown as number
  // 重置游标和数据
  lastCreateTime.value = undefined
  data.value = []
  total.value = 0
  fetchData()
}

// 加载更多
const loadMore = () => {
  fetchData(true)
}

// 查看应用对话
// record.appId 经 json-bigint 解析后是精确的 ID 字符串，跳转的对话页 URL 才是正确的
const viewAppChat = (id: number | undefined) => {
  if (id) {
    router.push(`/app/chat/${id}`)
  }
}
</script>

<style scoped>
#chatManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.message-text {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.load-more {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.load-more-total {
  color: #999;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
