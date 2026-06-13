import api from './index'

export interface LoginRequest {
  username: string
  password: string
  role: string
}

export interface UserInfo {
  id: number
  username: string
  name: string
  role: string
  roleCode: string
  className: string
  studentNo: string
  teacherNo: string
}

export interface LoginResponse {
  success: boolean
  message: string
  token: string
  userInfo: UserInfo
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  return api.post('/auth/login', request)
}