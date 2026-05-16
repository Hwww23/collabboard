"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { useBoard } from "@/lib/useBoard"
import { getToken } from "@/lib/api"
import { TaskDto } from "@/lib/api"

export default function BoardPage() {
  const { id } = useParams()
  const router = useRouter()
  const boardId = Number(id)
  const { board, loading, sendMove, sendCreateTask } = useBoard(boardId)

  const [newTaskTitle, setNewTaskTitle] = useState("")
  const [addingToColumn, setAddingToColumn] = useState<number | null>(null)
  const [mounted, setMounted] = useState(false)

  // Must be before any conditional returns
  useEffect(() => {
    setMounted(true)
  }, [])

  // All conditional returns AFTER all hooks
  if (!mounted) return null
  if (!getToken()) { router.push("/login"); return null }
  if (loading) return <div className="p-8">Loading board...</div>
  if (!board) return <div className="p-8">Board not found</div>

  function handleAddTask(columnId: number) {
    if (!newTaskTitle.trim()) return
    sendCreateTask(newTaskTitle, "", columnId)
    setNewTaskTitle("")
    setAddingToColumn(null)
  }

  function handleDragStart(e: React.DragEvent, task: TaskDto) {
    e.dataTransfer.setData("taskId", String(task.id))
    e.dataTransfer.setData("fromColumnId", String(task.columnId))
  }

  function handleDrop(e: React.DragEvent, targetColumnId: number) {
    e.preventDefault()
    const taskId = Number(e.dataTransfer.getData("taskId"))
    const col = board?.columns?.find(c => c.id === targetColumnId)
    const newPosition = col?.tasks.length ?? 0
    sendMove(taskId, targetColumnId, newPosition)
  }

  return (
    <div className="min-h-screen p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <button
            onClick={() => router.push("/boards")}
            className="text-gray-400 hover:text-white text-sm mb-1"
          >
            ← Boards
          </button>
          <h1 className="text-2xl font-bold">{board.name}</h1>
        </div>
        <div className="bg-gray-800 px-3 py-1 rounded-lg text-sm font-mono text-gray-400">
          Invite: <span className="text-white">{board.inviteCode}</span>
        </div>
      </div>

      {/* Columns */}
      <div className="flex gap-4 overflow-x-auto pb-4">
        {board.columns?.map(col => (
          <div
            key={col.id}
            className="bg-gray-900 rounded-xl p-4 min-w-72 w-72 flex-shrink-0 border border-gray-800"
            onDragOver={e => e.preventDefault()}
            onDrop={e => handleDrop(e, col.id)}
          >
            {/* Column header */}
            <div className="flex justify-between items-center mb-3">
              <h2 className="font-semibold">{col.name}</h2>
              <span className="bg-gray-700 text-gray-300 text-xs px-2 py-0.5 rounded-full">
                {col.tasks.length}
              </span>
            </div>

            {/* Tasks */}
            <div className="space-y-2 min-h-8">
              {col.tasks.map(task => (
                <div
                  key={task.id}
                  draggable
                  onDragStart={e => handleDragStart(e, task)}
                  className="bg-gray-800 border border-gray-700 hover:border-blue-500 rounded-lg p-3 cursor-grab active:cursor-grabbing transition-colors"
                >
                  <p className="font-medium text-sm">{task.title}</p>
                  {task.description && (
                    <p className="text-gray-400 text-xs mt-1">{task.description}</p>
                  )}
                  {task.assigneeName && (
                    <span className="text-xs text-blue-400 mt-1 block">
                      → {task.assigneeName}
                    </span>
                  )}
                </div>
              ))}
            </div>

            {/* Add task */}
            {addingToColumn === col.id ? (
              <div className="mt-2 space-y-2">
                <input
                  autoFocus
                  value={newTaskTitle}
                  onChange={e => setNewTaskTitle(e.target.value)}
                  onKeyDown={e => {
                    if (e.key === "Enter") handleAddTask(col.id)
                    if (e.key === "Escape") setAddingToColumn(null)
                  }}
                  placeholder="Task title..."
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-sm outline-none focus:border-blue-500"
                />
                <div className="flex gap-2">
                  <button
                    onClick={() => handleAddTask(col.id)}
                    className="bg-blue-600 hover:bg-blue-700 px-3 py-1 rounded text-sm font-medium"
                  >
                    Add
                  </button>
                  <button
                    onClick={() => setAddingToColumn(null)}
                    className="text-gray-400 hover:text-white px-3 py-1 rounded text-sm"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            ) : (
              <button
                onClick={() => setAddingToColumn(col.id)}
                className="mt-2 w-full text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg py-1 text-sm transition-colors"
              >
                + Add task
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}