import JSONbig from 'json-bigint'

/**
 * JSON 解析实例（大整数安全模式）
 *
 * 【背景】后端返回的雪花 ID 是 19 位 Long（如 453799400838299648），
 * 超出了 JS Number 的安全整数范围（Number.MAX_SAFE_INTEGER = 2^53 - 1）。
 * 若用原生 JSON.parse 解析，ID 会被四舍五入（453799400838299648 -> 453799400838299650），
 * 导致后续用该 ID 请求接口时后端查不到数据（404），页面被弹回首页。
 *
 * 【原理】json-bigint 的 storeAsString: true 配置会把超出安全范围的大整数
 * 解析为字符串，从而保证 ID 精确不丢失（安全范围内的普通数字仍保持 number 类型）。
 */
const JSONBig = JSONbig({ storeAsString: true })

/**
 * axios 的 transformResponse 转换函数：使用 json-bigint 解析响应体，
 * 避免大整数（雪花 ID）在 JSON 解析时精度丢失。
 *
 * 【用法】在具体接口调用处通过 options 传入，只影响该请求，不影响全局：
 *   await xxxApi(params, { transformResponse: [bigIntSafeTransformResponse] })
 *
 * 【注意】使用该转换后，响应中超出安全范围的整数（如 id、userId）会变成字符串类型。
 *
 * @param data axios 响应原始数据（字符串）
 * @returns 解析后的对象；非 JSON 内容原样返回（与 axios 默认行为一致）
 */
export const bigIntSafeTransformResponse = (data: unknown): unknown => {
  if (typeof data === 'string') {
    try {
      return JSONBig.parse(data)
    } catch {
      // 响应不是 JSON 字符串（如纯文本）时原样返回
      return data
    }
  }
  return data
}
