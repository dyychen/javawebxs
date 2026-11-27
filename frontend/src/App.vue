<script setup>
import { computed, ref } from 'vue'

import LoginView from './views/LoginView.vue'
import StudentView from './views/StudentView.vue'
import MerchantView from './views/MerchantView.vue'
import AdminView from './views/AdminView.vue'

const user = ref(JSON.parse(localStorage.getItem('canteen_user') || 'null'))
const activeTab = ref('')

const navMap = {
  student: [
    { key: 'menu', label: '在线点餐', icon: '🍱' },
    { key: 'orders', label: '我的订单', icon: '🧾' },
  ],
  merchant: [
    { key: 'dishes', label: '菜品管理', icon: '🍜' },
    { key: 'orders', label: '订单核验', icon: '✅' },
  ],
  admin: [
    { key: 'dashboard', label: '数据概览', icon: '📊' },
    { key: 'canteen', label: '窗口与分类', icon: '🏬' },
    { key: 'users', label: '用户管理', icon: '👥' },
    { key: 'orders', label: '订单记录', icon: '🧾' },
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
  localStorage.removeItem('canteen_user')
  user.value = null
}
</script>

<template>
  <LoginView v-if="!user" @login="onLogin" />

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="logo">🍚</span>
        <span>食堂订餐系统</span>
      </div>
      <button
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeTab === item.key }"
        @click="activeTab = item.key"
      >
        <span>{{ item.icon }}</span>
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
          <button class="btn btn-sm" @click="logout">退出</button>
        </div>
      </div>

      <component :is="viewComponent" :tab="activeTab" :user="user" />
    </main>
  </div>
</template>
