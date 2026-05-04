export const statusMaps = {
  user: {
    ACTIVE: ['正常', 'success'],
    DISABLED: ['禁用', 'danger'],
  },
  apply: {
    PENDING: ['待审核', 'warning'],
    APPROVED: ['已通过', 'success'],
    REJECTED: ['已驳回', 'danger'],
  },
  business: {
    OPEN: ['营业中', 'success'],
    CLOSED: ['已关闭', 'muted'],
    PENDING: ['待审核', 'warning'],
    DISABLED: ['已禁用', 'danger'],
  },
  dish: {
    ON_SALE: ['上架中', 'success'],
    SOLD_OUT: ['已售罄', 'warning'],
    OFF_SHELF: ['已下架', 'muted'],
    PENDING: ['待审核', 'info'],
  },
  review: {
    VISIBLE: ['可见', 'success'],
    HIDDEN: ['隐藏', 'warning'],
    DELETED: ['已删除', 'danger'],
  },
  target: {
    DISH: ['菜品', 'info'],
  },
  canteenType: {
    CANTEEN: ['食堂', 'info'],
    CAMPUS_SHOP: ['校内独立店', 'success'],
    PERIPHERY_SHOP: ['校外独立店', 'warning'],
  },
}

export function labelFor(group, value) {
  return statusMaps[group]?.[value]?.[0] || value || '-'
}

export function toneFor(group, value) {
  return statusMaps[group]?.[value]?.[1] || 'muted'
}

export function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

export function formatDate(value) {
  return value ? String(value).slice(0, 10) : '-'
}

export function formatNumber(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

export function formatScore(value) {
  if (value === null || value === undefined || value === '') return '-'
  return Number(value).toFixed(1)
}

export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-'
  return `¥${Number(value).toFixed(2)}`
}
