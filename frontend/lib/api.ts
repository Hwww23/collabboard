const API = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"

// ── Types ────────────────────────────────────────────────
export interface AuthResponse {
  token: string
  userId: number
  email: string
  displayName: string
}

export interface TaskDto {
  id: number
  title: string
  description: string
  position: number
  columnId: number
  assigneeId: number | null
  assigneeName: string | null
}

export interface ColumnDto {
  id: number
  name: string
  position: number
  tasks: TaskDto[]
}

export interface BoardDto {
  id: number
  name: string
  inviteCode: string
  ownerName: string
  columns?: ColumnDto[]
}

// ── Auth helpers ─────────────────────────────────────────
export function getToken(): string | null {
  if (typeof window === "undefined") return null
  return sessionStorage.getItem("token")
}

export function getUser(): Omit<AuthResponse, "token"> | null {
  if (typeof window === "undefined") return null
  const raw = sessionStorage.getItem("user")
  return raw ? JSON.parse(raw) : null
}

export function saveAuth(auth: AuthResponse) {
  sessionStorage.setItem("token", auth.token)
  sessionStorage.setItem("user", JSON.stringify({
    userId: auth.userId,
    email: auth.email,
    displayName: auth.displayName
  }))
}

export function clearAuth() {
  sessionStorage.removeItem("token")
  sessionStorage.removeItem("user")
}

// ── API calls ─────────────────────────────────────────────
function authHeaders() {
  const token = getToken()
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  }
}

export async function register(email: string, password: string, displayName: string): Promise<AuthResponse> {
  const res = await fetch(`${API}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, displayName })
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const res = await fetch(`${API}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  })
  if (!res.ok) throw new Error("Invalid email or password")
  return res.json()
}

export async function getBoards(): Promise<BoardDto[]> {
  const res = await fetch(`${API}/api/boards`, { headers: authHeaders() })
  if (!res.ok) throw new Error("Failed to fetch boards")
  return res.json()
}

export async function createBoard(name: string): Promise<BoardDto> {
  const res = await fetch(`${API}/api/boards`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ name })
  })
  if (!res.ok) throw new Error("Failed to create board")
  return res.json()
}

export async function getBoard(id: number): Promise<BoardDto> {
  const res = await fetch(`${API}/api/boards/${id}`, { headers: authHeaders() })
  if (!res.ok) throw new Error("Failed to fetch board")
  return res.json()
}

export async function joinBoard(inviteCode: string): Promise<BoardDto> {
  const res = await fetch(`${API}/api/boards/join/${inviteCode}`, {
    method: "POST",
    headers: authHeaders()
  })
  if (!res.ok) throw new Error("Invalid invite code")
  return res.json()
}

export async function createTask(boardId: number, title: string, description: string, columnId: number): Promise<TaskDto> {
  const res = await fetch(`${API}/api/boards/${boardId}/tasks`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ title, description, columnId })
  })
  if (!res.ok) throw new Error("Failed to create task")
  return res.json()
}

export async function moveTask(boardId: number, taskId: number, targetColumnId: number, newPosition: number): Promise<TaskDto> {
  const res = await fetch(`${API}/api/boards/${boardId}/tasks/move`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify({ taskId, targetColumnId, newPosition })
  })
  if (!res.ok) throw new Error("Failed to move task")
  return res.json()
}