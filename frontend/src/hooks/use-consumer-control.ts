import { useEffect, useState } from "react"

import { env } from "@/config/env"
import type { ConsumerActionState, ConsumerStatus } from "@/types/top-panel"

export function useConsumerControl() {
  const [status, setStatus] = useState<ConsumerStatus | null>(null)
  const [actionState, setActionState] = useState<ConsumerActionState>("idle")
  const [statusMessage, setStatusMessage] = useState("")

  async function request(path: string, message: string, method = "POST") {
    setActionState("loading")

    try {
      const response = await fetch(`${env.consumerControlUrl}${path}`, { method })
      if (!response.ok) {
        throw new Error(`Consumer control returned ${response.status}`)
      }

      const nextStatus = (await response.json()) as ConsumerStatus
      setStatus(nextStatus)
      setStatusMessage(message)
      setActionState("idle")
    } catch (error) {
      setActionState("error")
      setStatusMessage(error instanceof Error ? error.message : "Consumer control request failed.")
    }
  }

  useEffect(() => {
    const abortController = new AbortController()

    async function loadStatus() {
      try {
        const response = await fetch(`${env.consumerControlUrl}/status`, {
          method: "GET",
          signal: abortController.signal,
        })
        if (!response.ok) {
          throw new Error(`Consumer control returned ${response.status}`)
        }

        const nextStatus = (await response.json()) as ConsumerStatus
        setStatus(nextStatus)
      } catch (error) {
        if (error instanceof DOMException && error.name === "AbortError") {
          return
        }

        setActionState("error")
        setStatusMessage(error instanceof Error ? error.message : "Consumer control request failed.")
      }
    }

    void loadStatus()

    return () => abortController.abort()
  }, [])

  return {
    actionState,
    failProcessing: () => request("/fail-processing", "Failure mode enabled."),
    restoreProcessing: () => request("/restore-processing", "Processing restored. Replay is ready."),
    status,
    statusMessage,
  }
}
