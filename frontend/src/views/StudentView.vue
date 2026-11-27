<script setup>
import { computed, onMounted, ref, watch } from 'vue'

import { api, statusText } from '../api.js'

const props = defineProps({
  tab: { type: String, default: 'menu' },
  user: { type: Object, required: true },
})

const PICKUP_SLOTS = ['11:30-11:45', '11:45-12:00', '12:00-12:15', '12:15-12:30', '17:30-17:45', '17:45-18:00']
const EMOJI = { '盖浇饭': '🍛', '面食': '🍜', '小吃': '🍗', '饮品': '🥤' }

const windows = ref([])
const categories = ref([])
const dishes = ref([])
const cart = ref([])
const orders = ref([])

const windowId = ref('')
const categoryId = ref('')
const keyword = ref('')
const campus = ref('')
const pickupTime = ref(PICKUP_SLOTS[2])
const remark = ref('')
const errorMsg = ref('')
const infoMsg = ref('')
const loading = ref(false)

const cartTotal = computed(() =>
  cart.value.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0),
)

const campuses = computed(() =>
  [...new Set(windows.value.map(w => w.campus).filter(Boolean))],
)

const filteredDishes = computed(() => {
  if (!campus.value) return dishes.value
  const ids = new Set(windows.value.filter(w => w.campus === campus.value).map(w => w.id))
  return dishes.value.filter(d => ids.has(d.windowId))
})

function emojiFor(dish) {
  return EMOJI[dish.categoryName] || '🍽️'
}

async function loadDishes() {
  try {
    dishes.value = await api.dishes({
      windowId: windowId.value,
      categoryId: categoryId.value,
      keyword: keyword.value,
    })
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function loadCart() {
  try {
    cart.value = await api.cart(props.user.id)
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function loadOrders() {
  try {
    orders.value = await api.myOrders(props.user.id)
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function addToCart(dish) {
  errorMsg.value = ''
  infoMsg.value = ''
  try {
    await api.addToCart(props.user.id, dish.id, 1)
    await loadCart()
    infoMsg.value = `已加入购物车：${dish.name}`
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function changeQuantity(item, delta) {
  const next = item.quantity + delta
  try {
    if (next <= 0) {
      await api.removeCart(item.id)
    } else {
      await api.updateCart(item.id, next)
    }
    await loadCart()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function submitOrder() {
  errorMsg.value = ''
  infoMsg.value = ''
  if (!cart.value.length) {
    errorMsg.value = '购物车为空，请先添加菜品'
    return
  }
  loading.value = true
  try {
    const created = await api.createOrder(props.user.id, pickupTime.value, remark.value)
    const text = created.map(o => `订单 ${o.orderNo}，取餐码 ${o.pickupCode}`).join('；')
    infoMsg.value = `下单成功！${text}`
    remark.value = ''
    await loadCart()
    await loadOrders()
  } catch (error) {
    errorMsg.value = error.message
  } finally {
    loading.value = false
  }
}

async function pay(order) {
  try {
    await api.payOrder(order.id)
    await loadOrders()
  } catch (error) {
    errorMsg.value = error.message
  }
}

async function cancel(order) {
  try {
    await api.cancelOrder(order.id)
    await loadOrders()
  } catch (error) {
    errorMsg.value = error.message
  }
}

function badgeClass(status) {
  return {
    PENDING_PAY: 'badge-pending',
    PAID: 'badge-paid',
    PICKED: 'badge-done',
    CANCELLED: 'badge-cancel',
  }[status]
}

onMounted(async () => {
  try {
    windows.value = await api.windows()
    categories.value = await api.categories()
  } catch (error) {
    errorMsg.value = error.message
  }
  await loadDishes()
  await loadCart()
  await loadOrders()
})

watch(() => props.tab, (tab) => {
  if (tab === 'orders') loadOrders()
})
</script>

<template>
  <!-- 在线点餐 -->
  <div v-if="tab === 'menu'">
    <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>
    <div v-if="infoMsg" class="alert alert-info">{{ infoMsg }}</div>

    <div class="toolbar">
      <div class="field" style="margin: 0">
        <label>食堂窗口</label>
        <select v-model="windowId" class="select" @change="loadDishes">
          <option value="">全部窗口</option>
          <option v-for="w in windows" :key="w.id" :value="w.id">{{ w.name }}</option>
        </select>
      </div>
      <div class="field" style="margin: 0">
        <label>校区</label>
        <select v-model="campus" class="select">
          <option value="">全部校区</option>
          <option v-for="c in campuses" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
      <div class="field" style="margin: 0">
        <label>分类</label>
        <select v-model="categoryId" class="select" @change="loadDishes">
          <option value="">全部分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </div>
      <div class="field" style="margin: 0">
        <label>关键字</label>
        <input v-model="keyword" class="input" placeholder="搜索菜品" @keyup.enter="loadDishes" />
      </div>
      <button class="btn" @click="loadDishes">搜索</button>
    </div>

    <div class="grid-dishes">
      <div v-for="dish in filteredDishes" :key="dish.id" class="card dish-card">
        <div class="dish-emoji">{{ emojiFor(dish) }}</div>
        <div class="dish-name">{{ dish.name }}</div>
        <div class="dish-meta">
          <span>{{ dish.windowName }}</span>
          <span>库存 {{ dish.stock }}</span>
        </div>
        <div class="flex between">
          <span class="dish-price">¥{{ Number(dish.price).toFixed(2) }}</span>
          <button class="btn btn-primary btn-sm" :disabled="dish.status !== 1 || dish.stock <= 0" @click="addToCart(dish)">
            {{ dish.status !== 1 ? '已下架' : dish.stock <= 0 ? '售罄' : '加入购物车' }}
          </button>
        </div>
      </div>
      <div v-if="!filteredDishes.length" class="empty">暂无菜品</div>
    </div>

    <!-- 购物车 -->
    <div class="card mt" v-if="cart.length">
      <div class="flex between">
        <h3 style="margin: 0">购物车</h3>
        <button class="btn btn-sm" @click="api.clearCart(user.id).then(loadCart)">清空</button>
      </div>
      <table class="table mt">
        <thead>
          <tr><th>菜品</th><th>单价</th><th>数量</th><th>小计</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="item in cart" :key="item.id">
            <td>{{ item.dishName }}</td>
            <td>¥{{ Number(item.price).toFixed(2) }}</td>
            <td>
              <div class="flex">
                <button class="btn btn-sm" @click="changeQuantity(item, -1)">-</button>
                <span>{{ item.quantity }}</span>
                <button class="btn btn-sm" @click="changeQuantity(item, 1)">+</button>
              </div>
            </td>
            <td>¥{{ (Number(item.price) * item.quantity).toFixed(2) }}</td>
            <td><button class="btn btn-sm btn-danger" @click="changeQuantity(item, -item.quantity)">删除</button></td>
          </tr>
        </tbody>
      </table>

      <div class="toolbar mt">
        <div class="field" style="margin: 0">
          <label>预约取餐时段</label>
          <select v-model="pickupTime" class="select">
            <option v-for="slot in PICKUP_SLOTS" :key="slot" :value="slot">{{ slot }}</option>
          </select>
        </div>
        <div class="field" style="margin: 0">
          <label>备注</label>
          <input v-model="remark" class="input" placeholder="如：少辣、不要葱" />
        </div>
        <div class="flex" style="gap: 16px">
          <span>合计：<strong class="dish-price">¥{{ cartTotal.toFixed(2) }}</strong></span>
          <button class="btn btn-primary" :disabled="loading" @click="submitOrder">
            {{ loading ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 我的订单 -->
  <div v-else>
    <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>
    <div v-if="infoMsg" class="alert alert-info">{{ infoMsg }}</div>
    <div v-if="!orders.length" class="card empty">还没有订单，去点餐吧</div>
    <div v-for="order in orders" :key="order.id" class="card mt">
      <div class="flex between wrap">
        <div>
          <strong>{{ order.orderNo }}</strong>
          <span class="muted">　{{ order.windowName }}　预约 {{ order.pickupTime || '未选择' }}</span>
        </div>
        <div class="flex">
          <span class="badge" :class="badgeClass(order.status)">{{ statusText(order.status) }}</span>
          <button v-if="order.status === 'PENDING_PAY'" class="btn btn-teal btn-sm" @click="pay(order)">模拟支付</button>
          <button v-if="order.status === 'PENDING_PAY'" class="btn btn-sm btn-danger" @click="cancel(order)">取消</button>
        </div>
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
      <div class="flex between mt">
        <span class="muted" v-if="order.pickupCode">取餐码：<strong>{{ order.pickupCode }}</strong></span>
        <span class="muted" v-else>订单已取消</span>
        <span>实付：<strong class="dish-price">¥{{ Number(order.amount).toFixed(2) }}</strong></span>
      </div>
    </div>
  </div>
</template>
