import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'
// 大整数安全解析：修复后端雪花 ID（19 位 Long）在 JSON 解析时精度丢失的问题，
// 保证 loginUser.id 为精确字符串，供全站权限比较（如 isOwner）使用
import { bigIntSafeTransformResponse } from '@/utils/jsonBigInt'

/**
 * 登录用户信息
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 获取登录用户信息
  async function fetchLoginUser() {
    try {
      // 【修复雪花 ID 精度丢失】
      // 用户 ID 是 19 位 Long，默认 JSON 解析会把 loginUser.id 四舍五入，
      // 导致与其他页面的精确 ID 比较时不一致（如 isOwner 判断、权限校验）。
      // 通过 transformResponse 用 json-bigint 解析响应，loginUser.id 保持为精确字符串。
      const res = await getLoginUser({
        transformResponse: [bigIntSafeTransformResponse],
      })
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      }
    } catch (error) {
      // 后端不可用等情况时降级为未登录，避免路由守卫抛异常导致页面无法渲染
      console.error('获取登录用户信息失败：', error)
    }
  }

  // 更新登录用户信息
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
