<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Plus, RotateCcw, Search } from 'lucide-vue-next'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LoadingState from '@/components/LoadingState.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import PageShell from '@/components/PageShell.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api } from '@/services/api'

const canteens = ref([])
const filters = reactive({ canteenId: '', status: '' })
const records = ref([])
const loading = ref(false)
const error = ref('')
const editing = ref(null)
const formOpen = ref(false)
const confirm = reactive({ open: false, item: null })
const form = reactive({
  canteenId: '',
  name: '',
  location: '',
  description: '',
  status: 'OPEN',
})

async function loadCanteens() {
  canteens.value = await api.canteens({ type: 'CANTEEN' })
  if (!filters.canteenId && canteens.value.length) {
    filters.canteenId = canteens.value[0].id
  }
}

async function load() {
  if (!filters.canteenId) {
    records.value = []
    error.value = ''
    return
  }
  loading.value = true
  error.value = ''
  try {
    records.value = await api.stalls(filters)
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

function fill(item = null) {
  editing.value = item
  form.canteenId = item?.canteenId || filters.canteenId || canteens.value[0]?.id || ''
  form.name = item?.name || ''
  form.location = item?.location || ''
  form.description = item?.description || ''
  form.status = item?.status || 'OPEN'
  formOpen.value = true
}

function reset() {
  filters.status = ''
  filters.canteenId = canteens.value[0]?.id || ''
  load()
}

async function save() {
  const payload = {
    canteenId: Number(form.canteenId),
    name: form.name,
    location: form.location,
    description: form.description,
  }
  if (editing.value) {
    await api.updateStall(editing.value.id, { ...payload, status: form.status })
  } else {
    await api.createStall(payload)
  }
  formOpen.value = false
  await load()
}

async function remove() {
  await api.deleteStall(confirm.item.id)
  confirm.open = false
  await load()
}

onMounted(async () => {
  await loadCanteens()
  await load()
})
</script>

<template>
  <PageShell title="窗口管理" description="维护食堂下的窗口；独立店铺不在这里管理窗口。">
    <template #actions>
      <button class="primary-button" type="button" :disabled="!canteens.length" @click="fill()"><Plus :size="16" />新增窗口</button>
    </template>

    <form class="filter-bar" @submit.prevent="load">
      <select v-model="filters.canteenId" required>
        <option v-for="item in canteens" :key="item.id" :value="item.id">{{ item.name }}</option>
      </select>
      <select v-model="filters.status">
        <option value="">全部状态</option>
        <option value="OPEN">营业中</option>
        <option value="CLOSED">已关闭</option>
      </select>
      <button class="primary-button" type="submit"><Search :size="16" />筛选</button>
      <button class="secondary-button" type="button" @click="reset"><RotateCcw :size="16" />重置</button>
    </form>

    <LoadingState :loading="loading" :error="error" :empty="!records.length" empty-text="当前食堂暂无窗口">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>窗口</th>
              <th>位置</th>
              <th>商家</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id">
              <td>
                <strong>{{ item.name }}</strong>
                <small>{{ item.description || '无描述' }}</small>
              </td>
              <td>{{ item.location || '-' }}</td>
              <td>{{ item.merchantName || '未绑定' }}</td>
              <td><StatusBadge group="business" :value="item.status" /></td>
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

    <ModalDialog :open="formOpen" :title="editing ? '编辑窗口' : '新增窗口'" wide @close="formOpen = false">
      <form class="form-grid" @submit.prevent="save">
        <label class="field">
          <span>所属食堂</span>
          <select v-model="form.canteenId" required>
            <option v-for="item in canteens" :key="item.id" :value="item.id">{{ item.name }}</option>
          </select>
        </label>
        <label class="field"><span>名称</span><input v-model="form.name" required /></label>
        <label class="field"><span>位置</span><input v-model="form.location" /></label>
        <label v-if="editing" class="field">
          <span>状态</span>
          <select v-model="form.status">
            <option value="OPEN">营业中</option>
            <option value="CLOSED">已关闭</option>
            <option value="DISABLED">已禁用</option>
          </select>
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
      title="删除窗口"
      :message="`确认删除 ${confirm.item?.name || ''}？有关联商家或菜品时后端会拒绝删除。`"
      @close="confirm.open = false"
      @confirm="remove"
    />
  </PageShell>
</template>
