<script setup>
import { computed, ref } from 'vue'

import { api } from './api.js'
import {
  ClipboardCheck,
  LayoutDashboard,
  LogOut,
  ReceiptText,
  Store,
  UserRound,
  UtensilsCrossed,
} from '@lucide/vue'
import LoginView from './views/LoginView.vue'
import StudentView from './views/StudentView.vue'
import MerchantView from './views/MerchantView.vue'
import AdminView from './views/AdminView.vue'

const user = ref(JSON.parse(localStorage.getItem('canteen_user') || 'null'))
const activeTab = ref('')

const navMap = {
  student: [
    { key: 'menu', label: '在线点餐', icon: UtensilsCrossed },
    { key: 'orders', label: '我的订单', icon: ReceiptText },
  ],
  merchant: [
    { key: 'dishes', label: '菜品管理', icon: Store },
    { key: 'orders', label: '订单核验', icon: ClipboardCheck },
  ],
  admin: [
    { key: 'dashboard', label: '数据概览', icon: LayoutDashboard },
    { key: 'canteen', label: '窗口与分类', icon: Store },
    { key: 'users', label: '用户管理', icon: UserRound },
    { key: 'orders', label: '订单记录', icon: ReceiptText },
  ],
}

const navItems = computed(() => (user.value ? navMap[user.value.role] || [] : []))

const viewComponent = computed(() => {
  if (!user.value) return LoginView
  return { student: StudentView, merchant: MerchantView, admin: AdminView }[user.value.role]
})

function onLogin(data) {
  user.value = data
  activeTab.value = navItems.value[0]?.key || ''
}

function logout() {
  api.logout().catch(() => {})
  localStorage.removeItem('canteen_user')
  user.value = null
}
</script>

<template>
  <LoginView v-if="!user" @login="onLogin" />

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="logo"><UtensilsCrossed :size="18" /></span>
        <span>食堂订餐系统</span>
      </div>
      <button
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeTab === item.key }"
        @click="activeTab = item.key"
      >
        <component :is="item.icon" :size="17" :stroke-width="1.9" />
        <span>{{ item.label }}</span>
      </button>
      <div class="sidebar-footer">
        {{ user.realName || user.username }}
      </div>
    </aside>

    <main class="main">
      <div class="topbar">
        <div>
          <h1>{{ navItems.find(n => n.key === activeTab)?.label || '首页' }}</h1>
          <div class="sub">校园食堂线上订餐与错峰取餐系统</div>
        </div>
        <div class="user-chip">
          <span class="role-tag">{{ user.role === 'admin' ? '管理员' : user.role === 'merchant' ? '商家' : '学生' }}</span>
          <span>{{ user.realName || user.username }}</span>
          <button class="btn btn-sm" @click="logout"><LogOut :size="14" />退出</button>
        </div>
      </div>

      <component :is="viewComponent" :tab="activeTab" :user="user" />
    </main>
  </div>
</template>
