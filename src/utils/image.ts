/**
 * 图片URL处理工具
 * 将相对路径转换为完整的URL
 */
export function getImageUrl(imagePath: string): string {
  if (!imagePath) {
    return getImageUrl('/images/default.jpg')
  }
  
  // 如果已经是完整URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  
  // 如果是相对路径，拼接API基础URL
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  
  // 如果路径已经以/api开头，直接使用
  if (imagePath.startsWith('/api/')) {
    return baseURL.replace('/api', '') + imagePath
  }
  
  // 如果路径以/开头，需要加上/api前缀（因为后端context-path是/api）
  if (imagePath.startsWith('/')) {
    return baseURL + imagePath
  }
  
  // 否则作为相对路径处理
  return baseURL + '/images/' + imagePath
}

