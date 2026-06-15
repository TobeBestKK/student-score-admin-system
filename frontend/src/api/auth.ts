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

export interface ProfileResponse {
  id: number
  username: string
  name: string
  email: string
  phone: string
  position: string
  studentId: string
}

export interface UpdateProfileRequest {
  name?: string
  phone?: string
}

export interface UpdateProfileResponse {
  success: boolean
  message: string
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  return api.post('/auth/login', request)
}

export async function getProfile(params: { userId?: number; studentId?: number; teacherId?: number; username?: string }): Promise<ProfileResponse> {
  return api.get('/profile', { params })
}

export async function updateProfile(params: { studentId?: number; teacherId?: number }, request: UpdateProfileRequest): Promise<UpdateProfileResponse> {
  return api.put('/profile', request, { params })
}