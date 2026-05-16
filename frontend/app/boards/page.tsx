"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { getBoards, createBoard, joinBoard, clearAuth, getUser, BoardDto } from "@/lib/api"

export default function BoardsPage() {
  const router = useRouter()
  const [boards, setBoards] = useState<BoardDto[]>([])
  const [newBoardName, setNewBoardName] = useState("")
  const [inviteCode, setInviteCode] = useState("")
  const [loading, setLoading] = useState(true)
  const user = getUser()

  useEffect(() => {
    if (!getUser()) { router.push("/login"); return }
    getBoards().then(setBoards).finally(() => setLoading(false))
  }, [router])

  async function handleCreateBoard(e: React.FormEvent) {
    e.preventDefault()
    if (!newBoardName.trim()) return
    const board = await createBoard(newBoardName)
    setBoards(prev => [...prev, board])
    setNewBoardName("")
  }

  async function handleJoinBoard(e: React.FormEvent) {
    e.preventDefault()
    if (!inviteCode.trim()) return
    try {
      const board = await joinBoard(inviteCode)
      setBoards(prev => [...prev, board])
      setInviteCode("")
      router.push(`/boards/${board.id}`)
    } catch {
      alert("Invalid invite code")
    }
  }

  function handleLogout() {
    clearAuth()
    router.push("/login")
  }

  if (loading) return <div className="p-8">Loading...</div>

  return (
    <div className="min-h-screen p-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">CollabBoard</h1>
        <div className="flex items-center gap-4">
          <span className="text-gray-400">{user?.displayName}</span>
          <button onClick={handleLogout} className="text-sm text-gray-400 hover:text-white border border-gray-700 px-3 py-1 rounded-lg">
            Logout
          </button>
        </div>
      </div>

      {/* Create + Join */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
        <form onSubmit={handleCreateBoard} className="bg-gray-900 p-4 rounded-xl border border-gray-800">
          <h2 className="font-semibold mb-3">Create a board</h2>
          <div className="flex gap-2">
            <input
              value={newBoardName}
              onChange={e => setNewBoardName(e.target.value)}
              placeholder="Board name"
              className="flex-1 bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 outline-none focus:border-blue-500"
            />
            <button type="submit" className="bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg font-semibold">
              Create
            </button>
          </div>
        </form>

        <form onSubmit={handleJoinBoard} className="bg-gray-900 p-4 rounded-xl border border-gray-800">
          <h2 className="font-semibold mb-3">Join a board</h2>
          <div className="flex gap-2">
            <input
              value={inviteCode}
              onChange={e => setInviteCode(e.target.value)}
              placeholder="Invite code"
              className="flex-1 bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 outline-none focus:border-blue-500"
            />
            <button type="submit" className="bg-green-600 hover:bg-green-700 px-4 py-2 rounded-lg font-semibold">
              Join
            </button>
          </div>
        </form>
      </div>

      {/* Board list */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {boards.map(board => (
          <Link key={board.id} href={`/boards/${board.id}`}>
            <div className="bg-gray-900 border border-gray-800 hover:border-blue-500 rounded-xl p-5 cursor-pointer transition-colors">
              <h3 className="font-bold text-lg">{board.name}</h3>
              <p className="text-gray-400 text-sm mt-1">by {board.ownerName}</p>
              <p className="text-gray-600 text-xs mt-2 font-mono">#{board.inviteCode}</p>
            </div>
          </Link>
        ))}
        {boards.length === 0 && (
          <p className="text-gray-500 col-span-3">No boards yet. Create one above!</p>
        )}
      </div>
    </div>
  )
}