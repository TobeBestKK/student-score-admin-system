import api from './index'

export interface Student {
  id: number
  studentNo: string
  name: string
  gender: number
  classId: number
  className: string | null
  phone: string | null
  email: string | null
  enrollmentYear: number | null
}

export interface StudentQuery {
  grade?: number
  classId?: number
  keyword?: string
  page?: number
  size?: number
}

export interface StudentCreate {
  studentNo: string
  name: string
  gender?: number
  classId: number
  phone?: string
  email?: string
  enrollmentYear?: number
}

export interface StudentUpdate {
  studentNo?: string
  name?: string
  gender?: number
  classId?: number
  phone?: string
  email?: string
  enrollmentYear?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function fetchStudents(params?: StudentQuery): Promise<PageResponse<Student>> {
  return api.get('/students', { params })
}

export function fetchAllStudents(): Promise<Student[]> {
  return api.get('/students/all')
}

export function fetchStudentById(id: number): Promise<Student> {
  return api.get(`/students/${id}`)
}

export function createStudent(data: StudentCreate): Promise<Student> {
  return api.post('/students', data)
}

export function updateStudent(id: number, data: StudentUpdate): Promise<Student> {
  return api.put(`/students/${id}`, data)
}

export function deleteStudent(id: number): Promise<void> {
  return api.delete(`/students/${id}`)
}