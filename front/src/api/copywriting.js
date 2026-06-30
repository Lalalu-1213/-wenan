import request from '../utils/request'

export function generateCopywriting(data) {
  return request({
    url: '/copywriting/generate',
    method: 'post',
    data
  })
}

export function submitBatchTask(data) {
  return request({
    url: '/copywriting/batch/submit',
    method: 'post',
    data
  })
}

export function getBatchTaskStatus(taskId) {
  return request({
    url: `/copywriting/batch/${taskId}`,
    method: 'get'
  })
}

export function getHistory(params) {
  return request({
    url: '/copywriting/history',
    method: 'get',
    params
  })
}

export function getRecentHistory() {
  return request({
    url: '/copywriting/history/recent',
    method: 'get'
  })
}
