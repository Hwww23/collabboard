"use client"

import { useEffect, useRef, useState } from "react"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { BoardDto, TaskDto, getBoard, getToken } from "./api"

const API = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"

export interface BoardEvent {
  type: "TASK_MOVED" | "TASK_CREATED" | "TASK_DELETED"
  boardId: number
  payload: unknown
  triggeredBy: string
}

export function useBoard(boardId: number) {
  const [board, setBoard] = useState<BoardDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [lastEvent, setLastEvent] = useState<BoardEvent | null>(null)
  const clientRef = useRef<Client | null>(null)

  // Load initial board data
  useEffect(() => {
    getBoard(boardId).then(b => {
      setBoard(b)
      setLoading(false)
    })
  }, [boardId])

  // Connect to WebSocket
  useEffect(() => {
    const token = getToken()
    if (!token) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API}/ws`),
      onConnect: () => {
        console.log("WebSocket connected")

        client.subscribe(`/topic/board/${boardId}`, (msg) => {
          const event: BoardEvent = JSON.parse(msg.body)
          console.log("WebSocket event received:", event.type, event)
          setLastEvent(event)

          if (event.type === "TASK_MOVED") {
            const task = event.payload as TaskDto
            setBoard(prev => {
              if (!prev?.columns) return prev
              const updated = prev.columns.map(col => ({
                ...col,
                tasks: col.tasks.filter(t => t.id !== task.id)
              }))
              const targetCol = updated.find(c => c.id === task.columnId)
              if (targetCol) {
                targetCol.tasks.splice(task.position, 0, task)
              }
              return { ...prev, columns: updated }
            })
          }

          if (event.type === "TASK_CREATED") {
            const task = event.payload as TaskDto
            setBoard(prev => {
              if (!prev?.columns) return prev
              return {
                ...prev,
                columns: prev.columns.map(col =>
                  col.id === task.columnId
                    ? { ...col, tasks: [...col.tasks, task] }
                    : col
                )
              }
            })
          }
        })
      },
      onDisconnect: () => console.log("WebSocket disconnected")
    })

    client.activate()
    clientRef.current = client

    return () => { client.deactivate() }
  }, [boardId])

  function sendMove(taskId: number, targetColumnId: number, newPosition: number) {
    const token = getToken()
    if (!clientRef.current?.connected || !token) return

    clientRef.current.publish({
      destination: `/app/board/${boardId}/move`,
      headers: { token },
      body: JSON.stringify({ taskId, targetColumnId, newPosition })
    })

    // Optimistic update
    setBoard(prev => {
      if (!prev?.columns) return prev
      let movedTask: TaskDto | null = null

      const updated = prev.columns.map(col => ({
        ...col,
        tasks: col.tasks.filter(t => {
          if (t.id === taskId) { movedTask = t; return false }
          return true
        })
      }))

      const targetCol = updated.find(c => c.id === targetColumnId)
      if (targetCol && movedTask) {
        targetCol.tasks.splice(newPosition, 0, {
          ...(movedTask as TaskDto),
          columnId: targetColumnId
        })
      }

      return { ...prev, columns: updated }
    })
  }

  function sendCreateTask(title: string, description: string, columnId: number) {
    const token = getToken()
    if (!clientRef.current?.connected || !token) return

    clientRef.current.publish({
      destination: `/app/board/${boardId}/task/create`,
      headers: { token },
      body: JSON.stringify({ title, description, columnId })
    })
  }

  return { board, loading, lastEvent, sendMove, sendCreateTask, setBoard }
}