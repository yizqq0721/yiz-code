// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /Health */
export async function healthcheck(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/Health', {
    method: 'GET',
    ...(options || {}),
  })
}
