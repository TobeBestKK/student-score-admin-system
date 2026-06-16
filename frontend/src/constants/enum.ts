export const WARNING_LEVEL = {
  NORMAL: '普通提醒',
  FOCUS: '重点关注',
  SEVERE: '严重预警',
} as const

export const WARNING_TYPE = {
  LOW_SCORE: '单科低分',
  LOW_AVERAGE: '学期平均分偏低',
  LOW_RANKING: '总分排名靠后',
  FAILED_COURSES: '累计不及格',
  FAIL: '不及格',
} as const

export const GRADE_LEVEL = {
  EXCELLENT: '优秀',
  GOOD: '良好',
  MEDIUM: '中等',
  PASS: '及格',
} as const

export const EXAM_TYPE = {
  MIDTERM: '期中',
  FINAL: '期末',
} as const

export const REMARK = {
  EXCELLENT: '优',
  GOOD: '良',
  MEDIUM: '中',
  POOR: '差',
} as const
