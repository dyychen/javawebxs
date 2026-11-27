<script setup>
import { ref } from 'vue'

import { api } from '../api.js'

const emit = defineEmits(['login'])

const mode = ref('login')
const username = ref('')
const password = ref('')
const realName = ref('')
const phone = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function submit() {
  errorMsg.value = ''
  loading.value = true
  try {
    const data = mode.value === 'login'
      ? await api.login(username.value, password.value)
      : await api.register({ username: username.value, password: password.value, realName: realName.value, phone: phone.value })
    localStorage.setItem('canteen_user', JSON.stringify(data))
    emit('login', data)
  } catch (error) {
    errorMsg.value = error.message
  } finally {
    loading.value = false
  }
}

function fill(account, pwd) {
  username.value = account
  password.value = pwd
}
</script>

<template>
  <div class="login-wrap">
    <div class="card login-card">
      <h1>校园食堂订餐系统</h1>
      <div class="tip">{{ mode === 'login' ? '请使用账号登录' : '注册学生账号' }}</div>

      <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>

      <form @submit.prevent="submit">
        <div class="field">
          <label>账号</label>
          <input v-model="username" class="input" placeholder="请输入账号" />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="password" type="password" class="input" placeholder="请输入密码" />
        </div>
        <template v-if="mode === 'register'">
          <div class="field">
            <label>姓名</label>
            <input v-model="realName" class="input" placeholder="真实姓名" />
          </div>
          <div class="field">
            <label>手机号</label>
            <input v-model="phone" class="input" placeholder="选填" />
          </div>
        </template>
        <button class="btn btn-primary" style="width: 100%" :disabled="loading">
          {{ loading ? '请稍候...' : mode === 'login' ? '登 录' : '注 册' }}
        </button>
      </form>

      <div class="flex between mt">
        <span class="muted">没有账号？</span>
        <a href="javascript:void(0)" @click="mode = mode === 'login' ? 'register' : 'login'">
          {{ mode === 'login' ? '注册学生账号' : '返回登录' }}
        </a>
      </div>

      <div class="login-roles">
        演示账号（点击填入）：<br />
        <a href="javascript:void(0)" @click="fill('student1', '123456')">学生 student1 / 123456</a><br />
        <a href="javascript:void(0)" @click="fill('merchant1', '123456')">商家 merchant1 / 123456</a><br />
        <a href="javascript:void(0)" @click="fill('admin', 'admin123')">管理员 admin / admin123</a>
      </div>
    </div>
  </div>
</template>
