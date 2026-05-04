<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Plus, RotateCcw, Search } from 'lucide-vue-next'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LoadingState from '@/components/LoadingState.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import PageShell from '@/components/PageShell.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api } from '@/services/api'
import { formatScore } from '@/utils/format'

const filters = reactive({ type: '', status: '' })
const records = ref([])
const loading = ref(false)
const error = ref('')
const editing = ref(null)
const formOpen = ref(false)
const confirm = reactive({ open: false, item: null })
const form = reactive({
  campusId: 1,
  name: '',
  type: 'CANTEEN',
  location: '',
  description: '',
  openingHours: '',
  openingStartTime: '06:00',
  openingEndTime: '21:00',
  status: 'OPEN',
})
const timeOptions = buildTimeOptions()

function fill(item = null) {
  editing.value = item
  form.campusId = item?.campusId || 1
  form.name = item?.name || ''
  form.type = item?.type || 'CANTEEN'
  form.location = item?.location || ''
  form.description = item?.description || ''
  form.openingHours = item?.openingHours || ''
  fillOpeningTimes(form.openingHours)
  form.status = item?.status || 'OPEN'
  formOpen.value = true
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    records.value = await api.canteens(filters)
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

function reset() {
  filters.type = ''
  filters.status = ''
  load()
}

async function save() {
  if (form.openingStartTime >= form.openingEndTime) {
    window.alert('营业结束时间需晚于开始时间')
    return
  }
  const payload = {
    campusId: Number(form.campusId),
    name: form.name,
    type: form.type,
    location: form.location,
    description: form.description,
    openingHours: `${form.openingStartTime}-${form.openingEndTime}`,
  }
  if (editing.value) {
    await api.updateCanteen(editing.value.id, { ...payload, status: form.status })
  } else {
    await api.createCanteen(payload)
  }
  formOpen.value = false
  await load()
}

async function remove() {
  await api.deleteCanteen(confirm.item.id)
  confirm.open = false
  await load()
}

function buildTimeOptions() {
  const options = []
  for (let hour = 0; hour < 24; hour += 1) {
    for (const minute of [0, 30]) {
      options.push(`${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`)
    }
  }
  return options
}

function fillOpeningTimes(value) {
  const match = String(value || '').match(/^(\d{2}:\d{2})-(\d{2}:\d{2})$/)
  form.openingStartTime = match ? match[1] : '06:00'
  form.openingEndTime = match ? match[2] : '21:00'
}

onMounted(load)
</script>

<template>
  <PageShell title="食堂与独立店铺管理" description="维护食堂，以及不属于食堂窗口的校内、校外独立店铺。">
    <template #actions>
      <button class="primary-button" type="button" @click="fill()"><Plus :size="16" />新增食堂/店铺</button>
    </template>

    <form class="filter-bar" @submit.prevent="load">
      <select v-model="filters.type">
        <option value="">全部类型</option>
        <option value="CANTEEN">食堂</option>
        <option value="CAMPUS_SHOP">校内独立店</option>
        <option value="PERIPHERY_SHOP">校外独立店</option>
      </select>
      <select v-model="filters.status">
        <option value="">全部状态</option>
        <option value="OPEN">营业中</option>
        <option value="CLOSED">已关闭</option>
      </select>
      <button class="primary-button" type="submit"><Search :size="16" />筛选</button>
      <button class="secondary-button" type="button" @click="reset"><RotateCcw :size="16" />重置</button>
    </form>

    <LoadingState :loading="loading" :error="error" :empty="!records.length">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>位置</th>
              <th>营业时间</th>
              <th>状态</th>
              <th>评分</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id">
              <td>
                <strong>{{ item.name }}</strong>
                <small>{{ item.description || '无描述' }}</small>
              </td>
              <td><StatusBadge group="canteenType" :value="item.type" /></td>
              <td>{{ item.location || '-' }}</td>
              <td>{{ item.openingHours || '-' }}</td>
              <td><StatusBadge group="business" :value="item.status" /></td>
              <td>{{ formatScore(item.averageScore) }}</td>
              <td>
                <div class="row-actions">
                  <button class="text-button" type="button" @click="fill(item)">编辑</button>
                  <button class="text-danger" type="button" @click="confirm.open = true; confirm.item = item">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </LoadingState>

    <ModalDialog :open="formOpen" :title="editing ? '编辑食堂/店铺' : '新增食堂/店铺'" wide @close="formOpen = false">
      <form class="form-grid" @submit.prevent="save">
        <label class="field"><span>校区 ID</span><input v-model.number="form.campusId" type="number" min="1" required /></label>
        <label class="field"><span>名称</span><input v-model="form.name" required /></label>
        <label class="field">
          <span>类型</span>
          <select v-model="form.type">
            <option value="CANTEEN">食堂</option>
            <option value="CAMPUS_SHOP">校内独立店</option>
            <option value="PERIPHERY_SHOP">校外独立店</option>
          </select>
        </label>
        <label v-if="editing" class="field">
          <span>状态</span>
          <select v-model="form.status">
            <option value="OPEN">营业中</option>
            <option value="CLOSED">已关闭</option>
            <option value="DISABLED">已禁用</option>
          </select>
        </label>
        <label class="field"><span>位置</span><input v-model="form.location" /></label>
        <label class="field span-2">
          <span>营业时间</span>
          <div class="time-selects">
            <select v-model="form.openingStartTime">
              <option v-for="time in timeOptions" :key="`start-${time}`" :value="time">{{ time }}</option>
            </select>
            <span>至</span>
            <select v-model="form.openingEndTime">
              <option v-for="time in timeOptions" :key="`end-${time}`" :value="time">{{ time }}</option>
            </select>
          </div>
        </label>
        <label class="field span-2"><span>描述</span><textarea v-model="form.description" rows="3"></textarea></label>
        <footer class="modal-actions span-2">
          <button class="secondary-button" type="button" @click="formOpen = false">取消</button>
          <button class="primary-button" type="submit">保存</button>
        </footer>
      </form>
    </ModalDialog>

    <ConfirmDialog
      :open="confirm.open"
      danger
      title="删除食堂/店铺"
      :message="`确认删除 ${confirm.item?.name || ''}？有关联窗口、商家或菜品时后端会拒绝删除。`"
      @close="confirm.open = false"
      @confirm="remove"
    />
  </PageShell>
</template>

<style scoped>
.time-selects {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 10px;
}

.time-selects span {
  color: #7f6b5c;
  font-size: 13px;
  font-weight: 700;
}
</style>
