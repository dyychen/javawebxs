<script setup>
import { onMounted, ref, watch } from 'vue'

import { api, statusText } from '../api.js'
import { Plus } from '@lucide/vue'

const props = defineProps({
  tab: { type: String, default: 'dishes' },
  user: { type: Object, required: true },
})

const windows = ref([])
const categories = ref([])
const dishes = ref([])
const orders = ref([])

const errorMsg = ref('')
const infoMsg = ref('')
const showModal = ref(false)
const editing = ref(null)
const form = ref(emptyForm())

const verifyCodes = ref({})

function emptyForm() {
  return { windowId: '', categoryId: '', name: '', price: 0, stock: 0, status: 1, image: '' }
}

async function loadAll() {
  try {
    windows.value = await api.windowsByMerchant()
    categories.value = await api.categories()
    dishes.value = await api.dishes({ merchantId: props.user.id })
    if (!form.value.windowId && windows.value.length) {
      form.value.windowId = windows.value[0].id
    }
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

function openCreate() {
  editing.value = null
  form.value = emptyForm()
  if (windows.value.length) form.value.windowId = windows.value[0].id
  showModal.value = true
}

function openEdit(dish) {
  editing.value = dish
  form.value = { ...dish }
  showModal.value = true
}

async function saveDish() {
  errorMsg.value = ''
  infoMsg.value = ''
  try {
    const payload = { ...form.value, price: Number(form.value.price), stock: Number(form.value.stock) }
    if (editing.value) {
      await api.updateDish(editing.value.id, payload)
      infoMsg.value = '菜品已更新'
    } else {
      await api.createDish(payload)
      infoMsg.value = '菜品已新增'
    }
    showModal.value = false
    await loadAll()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function changeStock(dish, delta) {
  try {
    await api.changeStock(dish.id, delta)
    await loadAll()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function toggleStatus(dish) {
  try {
    await api.updateDish(dish.id, { status: dish.status === 1 ? 0 : 1 })
    await loadAll()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function removeDish(dish) {
  if (!confirm(`确定删除菜品「${dish.name}」吗？`)) return
  try {
    await api.deleteDish(dish.id)
    await loadAll()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function verify(order) {
  const code = (verifyCodes.value[order.id] || '').trim()
  if (!code) {
    errorMsg.value = '请输入取餐码'
    return
  }
  errorMsg.value = ''
  infoMsg.value = ''
  try {
    await api.verifyOrder(order.id, code)
    infoMsg.value = `订单 ${order.orderNo} 核验成功，已完成取餐`
    verifyCodes.value[order.id] = ''
    await loadOrders()
  } catch (error) {
    errorMsg.value = error.message
  }
}

function badgeClass(status) {
  return { PENDING_PAY: 'badge-pending', PAID: 'badge-paid', PICKED: 'badge-done', CANCELLED: 'badge-cancel' }[status]
}

onMounted(() => {
  loadAll()
  loadOrders()
})

watch(() => props.tab, (tab) => {
  if (tab === 'orders') loadOrders()
})
</script>

<template>
  <div>
    <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>
    <div v-if="infoMsg" class="alert alert-info">{{ infoMsg }}</div>

    <!-- 菜品管理 -->
    <div v-if="tab === 'dishes'">
      <div class="toolbar">
        <div class="field" style="margin: 0">
          <label>我的窗口</label>
          <select class="select" :value="form.windowId" @change="e => form.windowId = Number(e.target.value)">
            <option v-for="w in windows" :key="w.id" :value="w.id">{{ w.name }}（{{ w.location }}）</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="openCreate"><Plus :size="15" />新增菜品</button>
      </div>

      <div class="card" style="padding: 0; overflow: hidden">
        <table class="table">
          <thead>
            <tr><th>编号</th><th>菜品</th><th>分类</th><th>所属窗口</th><th>价格</th><th>库存</th><th>销量</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="dish in dishes" :key="dish.id">
              <td>{{ dish.id }}</td>
              <td>{{ dish.name }}</td>
              <td>{{ dish.categoryName || '-' }}</td>
              <td>{{ dish.windowName }}</td>
              <td>¥{{ Number(dish.price).toFixed(2) }}</td>
              <td>
                <div class="flex">
                  <button class="btn btn-sm" @click="changeStock(dish, -1)">-</button>
                  <span>{{ dish.stock }}</span>
                  <button class="btn btn-sm" @click="changeStock(dish, 1)">+</button>
                </div>
              </td>
              <td>{{ dish.sales }}</td>
              <td>
                <span class="badge" :class="dish.status === 1 ? 'badge-paid' : 'badge-cancel'">
                  {{ dish.status === 1 ? '在售' : '下架' }}
                </span>
              </td>
              <td>
                <div class="flex wrap">
                  <button class="btn btn-sm" @click="openEdit(dish)">编辑</button>
                  <button class="btn btn-sm" @click="toggleStatus(dish)">{{ dish.status === 1 ? '下架' : '上架' }}</button>
                  <button class="btn btn-sm btn-danger" @click="removeDish(dish)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="!dishes.length"><td colspan="9" class="empty">暂无菜品</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 订单核验 -->
    <div v-else>
      <div v-if="!orders.length" class="card empty">暂无订单</div>
      <div v-for="order in orders" :key="order.id" class="card mt">
        <div class="flex between wrap">
          <div>
            <strong>{{ order.orderNo }}</strong>
            <span class="muted">　学生：{{ order.userName }}　预约 {{ order.pickupTime || '未选择' }}</span>
          </div>
          <span class="badge" :class="badgeClass(order.status)">{{ statusText(order.status) }}</span>
        </div>
        <table class="table mt">
          <thead><tr><th>菜品</th><th>单价</th><th>数量</th></tr></thead>
          <tbody>
            <tr v-for="item in order.items" :key="item.id">
              <td>{{ item.dishName }}</td>
              <td>¥{{ Number(item.price).toFixed(2) }}</td>
              <td>{{ item.quantity }}</td>
            </tr>
          </tbody>
        </table>
        <div class="flex mt wrap">
          <input v-model="verifyCodes[order.id]" class="input" style="width: 160px" placeholder="输入学生取餐码" />
          <button class="btn btn-teal" :disabled="order.status !== 'PAID'" @click="verify(order)">
            核验取餐
          </button>
          <span class="muted">金额 ¥{{ Number(order.amount).toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 新增 / 编辑菜品 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ editing ? '编辑菜品' : '新增菜品' }}</h3>
        <div class="field">
          <label>所属窗口</label>
          <select v-model="form.windowId" class="select">
            <option v-for="w in windows" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <div class="field">
          <label>分类</label>
          <select v-model="form.categoryId" class="select">
            <option value="">不分类</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <div class="field">
          <label>菜品名称</label>
          <input v-model="form.name" class="input" placeholder="如：红烧肉盖浇饭" />
        </div>
        <div class="flex">
          <div class="field" style="flex: 1">
            <label>价格</label>
            <input v-model="form.price" type="number" step="0.5" class="input" />
          </div>
          <div class="field" style="flex: 1">
            <label>库存</label>
            <input v-model="form.stock" type="number" class="input" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="saveDish">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>
