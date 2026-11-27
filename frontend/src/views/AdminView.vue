<script setup>
import { onMounted, ref, watch } from 'vue'

import { api, statusText } from '../api.js'
import { Download } from '@lucide/vue'

const props = defineProps({
  tab: { type: String, default: 'dashboard' },
  user: { type: Object, required: true },
})

const stats = ref({})
const windows = ref([])
const categories = ref([])
const users = ref([])
const orders = ref([])

const errorMsg = ref('')
const infoMsg = ref('')
const showWindowModal = ref(false)
const editingWindow = ref(null)
const windowForm = ref(emptyWindow())
const newCategory = ref('')

function emptyWindow() {
  return { name: '', campus: '', floor: '', location: '', merchantId: '', status: 1, businessHours: '' }
}

async function loadDashboard() {
  try {
    stats.value = await api.stats()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function loadCanteen() {
  try {
    windows.value = await api.windows()
    categories.value = await api.categories()
    users.value = await api.users()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function loadUsers() {
  try {
    users.value = await api.users()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function loadOrders() {
  try {
    orders.value = await api.orders()
  } catch (error) {
    errorMsg.value = error.message
  }
}

const merchants = () => users.value.filter(u => u.role === 'merchant')

function openCreateWindow() {
  editingWindow.value = null
  windowForm.value = emptyWindow()
  showWindowModal.value = true
}

function openEditWindow(window) {
  editingWindow.value = window
  windowForm.value = { ...window }
  showWindowModal.value = true
}

async function saveWindow() {
  errorMsg.value = ''
  infoMsg.value = ''
  try {
    const payload = { ...windowForm.value, merchantId: windowForm.value.merchantId || null }
    if (editingWindow.value) {
      await api.updateWindow(editingWindow.value.id, payload)
      infoMsg.value = '窗口已更新'
    } else {
      await api.createWindow(payload)
      infoMsg.value = '窗口已新增'
    }
    showWindowModal.value = false
    await loadCanteen()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function removeWindow(window) {
  if (!confirm(`确定删除窗口「${window.name}」吗？`)) return
  try {
    await api.deleteWindow(window.id)
    await loadCanteen()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function addCategory() {
  if (!newCategory.value.trim()) return
  try {
    await api.createCategory(newCategory.value.trim())
    newCategory.value = ''
    await loadCanteen()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function removeCategory(category) {
  try {
    await api.deleteCategory(category.id)
    await loadCanteen()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function exportCsv() {
  try {
    const csv = await api.exportCsv()
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `订单数据_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    errorMsg.value = error.message
  }
}

function badgeClass(status) {
  return { PENDING_PAY: 'badge-pending', PAID: 'badge-paid', PICKED: 'badge-done', CANCELLED: 'badge-cancel' }[status]
}

const ROLE_TEXT = { student: '学生', merchant: '商家', admin: '管理员' }

onMounted(() => {
  loadDashboard()
  loadCanteen()
  loadOrders()
})

watch(() => props.tab, (tab) => {
  if (tab === 'dashboard') loadDashboard()
  if (tab === 'canteen') loadCanteen()
  if (tab === 'users') loadUsers()
  if (tab === 'orders') loadOrders()
})
</script>

<template>
  <div>
    <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>
    <div v-if="infoMsg" class="alert alert-info">{{ infoMsg }}</div>

    <!-- 数据概览 -->
    <div v-if="tab === 'dashboard'">
      <div class="grid-stats">
        <div class="card stat-card">
          <div class="label">订单总数</div>
          <div class="value">{{ stats.totalOrders ?? 0 }}</div>
          <div class="hint">全部订单</div>
        </div>
        <div class="card stat-card">
          <div class="label">营业收入</div>
          <div class="value">¥{{ Number(stats.revenue ?? 0).toFixed(2) }}</div>
          <div class="hint">已支付订单合计</div>
        </div>
        <div class="card stat-card">
          <div class="label">待取餐</div>
          <div class="value">{{ stats.paid ?? 0 }}</div>
          <div class="hint">已支付等待核验</div>
        </div>
        <div class="card stat-card">
          <div class="label">已完成 / 已取消</div>
          <div class="value">{{ stats.picked ?? 0 }} / {{ stats.cancelled ?? 0 }}</div>
          <div class="hint">取餐完成 / 取消订单</div>
        </div>
      </div>

      <div class="card">
        <h3 style="margin-top: 0">菜品销量排行</h3>
        <table class="table">
          <thead><tr><th>排名</th><th>菜品</th><th>销量</th></tr></thead>
          <tbody>
            <tr v-for="(row, index) in stats.salesRanking || []" :key="row.name">
              <td>{{ index + 1 }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.total }}</td>
            </tr>
            <tr v-if="!(stats.salesRanking || []).length"><td colspan="3" class="empty">暂无销售数据</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 窗口与分类 -->
    <div v-else-if="tab === 'canteen'">
      <div class="toolbar">
        <div class="field" style="margin: 0">
          <label>新增分类</label>
          <input v-model="newCategory" class="input" placeholder="如：盖浇饭" @keyup.enter="addCategory" />
        </div>
        <button class="btn" @click="addCategory">添加分类</button>
        <span style="flex: 1"></span>
        <button class="btn btn-primary" @click="openCreateWindow">+ 新增窗口</button>
      </div>

      <div class="grid-stats" style="grid-template-columns: 2fr 1fr">
        <div class="card" style="padding: 0; overflow: auto">
          <table class="table table-nowrap">
            <thead><tr><th>窗口</th><th>校区</th><th>楼层</th><th>位置</th><th>负责商家</th><th>营业时间</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="w in windows" :key="w.id">
                <td>{{ w.name }}</td>
                <td>{{ w.campus || '-' }}</td>
                <td>{{ w.floor || '-' }}</td>
                <td>{{ w.location || '-' }}</td>
                <td>{{ w.merchantName || '未分配' }}</td>
                <td>{{ w.businessHours || '-' }}</td>
                <td>
                  <span class="badge" :class="w.status === 1 ? 'badge-paid' : 'badge-cancel'">
                    {{ w.status === 1 ? '营业中' : '休息' }}
                  </span>
                </td>
                <td>
                  <div class="flex">
                    <button class="btn btn-sm" @click="openEditWindow(w)">编辑</button>
                    <button class="btn btn-sm btn-danger" @click="removeWindow(w)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="card">
          <h3 style="margin-top: 0">菜品分类</h3>
          <div v-for="c in categories" :key="c.id" class="flex between" style="padding: 6px 0; border-bottom: 1px solid var(--border)">
            <span>{{ c.name }}</span>
            <button class="btn btn-sm btn-danger" @click="removeCategory(c)">删除</button>
          </div>
          <div v-if="!categories.length" class="empty">暂无分类</div>
        </div>
      </div>
    </div>

    <!-- 用户管理 -->
    <div v-else-if="tab === 'users'">
      <div class="card" style="padding: 0; overflow: hidden">
        <table class="table">
          <thead><tr><th>编号</th><th>账号</th><th>姓名</th><th>角色</th><th>手机号</th><th>注册时间</th></tr></thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.username }}</td>
              <td>{{ u.realName || '-' }}</td>
              <td><span class="role-tag">{{ ROLE_TEXT[u.role] || u.role }}</span></td>
              <td>{{ u.phone || '-' }}</td>
              <td>{{ u.createTime }}</td>
            </tr>
            <tr v-if="!users.length"><td colspan="6" class="empty">暂无用户</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 订单记录 -->
    <div v-else>
      <div class="toolbar">
        <span class="muted">共 {{ orders.length }} 条订单</span>
        <span style="flex: 1"></span>
        <button class="btn btn-teal" @click="exportCsv"><Download :size="15" />导出 CSV</button>
      </div>

      <div class="card" style="padding: 0; overflow: hidden">
        <table class="table">
          <thead>
            <tr><th>订单号</th><th>学生</th><th>窗口</th><th>金额</th><th>状态</th><th>取餐码</th><th>预约时段</th><th>下单时间</th></tr>
          </thead>
          <tbody>
            <tr v-for="o in orders" :key="o.id">
              <td>{{ o.orderNo }}</td>
              <td>{{ o.userName }}</td>
              <td>{{ o.windowName }}</td>
              <td>¥{{ Number(o.amount).toFixed(2) }}</td>
              <td><span class="badge" :class="badgeClass(o.status)">{{ statusText(o.status) }}</span></td>
              <td>{{ o.pickupCode || '-' }}</td>
              <td>{{ o.pickupTime || '-' }}</td>
              <td>{{ o.createTime }}</td>
            </tr>
            <tr v-if="!orders.length"><td colspan="8" class="empty">暂无订单</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 窗口表单 -->
    <div v-if="showWindowModal" class="modal-mask" @click.self="showWindowModal = false">
      <div class="modal">
        <h3>{{ editingWindow ? '编辑窗口' : '新增窗口' }}</h3>
        <div class="field">
          <label>窗口名称</label>
          <input v-model="windowForm.name" class="input" placeholder="如：一楼风味窗口" />
        </div>
        <div class="field">
          <label>位置</label>
          <input v-model="windowForm.location" class="input" placeholder="如：学生食堂一楼A区" />
        </div>
        <div class="flex">
          <div class="field" style="flex: 1">
            <label>校区</label>
            <input v-model="windowForm.campus" class="input" placeholder="如：校本部" />
          </div>
          <div class="field" style="flex: 1">
            <label>楼层</label>
            <input v-model="windowForm.floor" class="input" placeholder="如：一楼" />
          </div>
        </div>
        <div class="field">
          <label>负责商家</label>
          <select v-model="windowForm.merchantId" class="select">
            <option value="">未分配</option>
            <option v-for="m in merchants()" :key="m.id" :value="m.id">{{ m.realName || m.username }}</option>
          </select>
        </div>
        <div class="flex">
          <div class="field" style="flex: 1">
            <label>营业时间</label>
            <input v-model="windowForm.businessHours" class="input" placeholder="如：10:30-13:30" />
          </div>
          <div class="field" style="flex: 1">
            <label>状态</label>
            <select v-model="windowForm.status" class="select">
              <option :value="1">营业中</option>
              <option :value="0">休息</option>
            </select>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="showWindowModal = false">取消</button>
          <button class="btn btn-primary" @click="saveWindow">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>
