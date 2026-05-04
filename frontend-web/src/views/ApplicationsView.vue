<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Check, RotateCcw, Search, X } from 'lucide-vue-next'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LoadingState from '@/components/LoadingState.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import PageShell from '@/components/PageShell.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api } from '@/services/api'
import { formatDateTime, labelFor } from '@/utils/format'

const filters = reactive({ applyStatus: '' })
const page = ref(1)
const size = 10
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const approveState = reactive({ open: false, item: null })
const rejectState = reactive({ open: false, item: null, reason: '' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await api.merchantApplications({ ...filters, page: page.value, size })
    records.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function reset() {
  filters.applyStatus = ''
  search()
}

async function approve() {
  await api.approveApplication(approveState.item.merchantId)
  approveState.open = false
  await load()
}

async function reject() {
  if (!rejectState.reason.trim()) return
  await api.rejectApplication(rejectState.item.merchantId, rejectState.reason.trim())
  rejectState.open = false
  rejectState.reason = ''
  await load()
}

function locationText(item) {
  if (item.canteenType === 'CANTEEN') {
    return `${item.canteenName || '-'} / ${item.stallName || '-'}`
  }
  return `${labelFor('canteenType', item.canteenType)} · ${item.canteenName || item.merchantName || '-'}`
}

onMounted(load)
</script>

<template>
  <PageShell title="入驻审核" description="处理卖家入驻申请，审核通过后将授予商家权限。">
    <form class="filter-bar" @submit.prevent="search">
      <select v-model="filters.applyStatus">
        <option value="">全部申请</option>
        <option value="PENDING">待审核</option>
        <option value="APPROVED">已通过</option>
        <option value="REJECTED">已驳回</option>
      </select>
      <button class="primary-button" type="submit"><Search :size="16" />筛选</button>
      <button class="secondary-button" type="button" @click="reset"><RotateCcw :size="16" />重置</button>
    </form>

    <LoadingState :loading="loading" :error="error" :empty="!records.length">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>商家</th>
              <th>位置</th>
              <th>状态</th>
              <th>申请时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.merchantId">
              <td>
                <strong>{{ item.merchantName }}</strong>
                <small>{{ item.description || '无描述' }}</small>
              </td>
              <td>{{ locationText(item) }}</td>
              <td><StatusBadge group="apply" :value="item.applyStatus" /></td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button
                    class="text-button"
                    type="button"
                    :disabled="item.applyStatus !== 'PENDING'"
                    @click="approveState.open = true; approveState.item = item"
                  >
                    <Check :size="15" />通过
                  </button>
                  <button
                    class="text-danger"
                    type="button"
                    :disabled="item.applyStatus !== 'PENDING'"
                    @click="rejectState.open = true; rejectState.item = item"
                  >
                    <X :size="15" />驳回
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar v-model:page="page" :size="size" :total="total" @update:page="load" />
    </LoadingState>

    <ConfirmDialog
      :open="approveState.open"
      title="审核通过"
      :message="`确认通过 ${approveState.item?.merchantName || ''} 的入驻申请？`"
      @close="approveState.open = false"
      @confirm="approve"
    />

    <ModalDialog :open="rejectState.open" title="驳回申请" @close="rejectState.open = false">
      <label class="field">
        <span>驳回原因</span>
        <textarea v-model="rejectState.reason" rows="4" placeholder="请填写原因，商家将看到该说明"></textarea>
      </label>
      <footer class="modal-actions">
        <button class="secondary-button" type="button" @click="rejectState.open = false">取消</button>
        <button class="danger-button" type="button" :disabled="!rejectState.reason.trim()" @click="reject">确认驳回</button>
      </footer>
    </ModalDialog>
  </PageShell>
</template>
