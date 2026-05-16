import type { ConsumerStatus } from "@/types/top-panel"

export type ConsumerStatusTone = "danger" | "paused" | "ready"

export function getConsumerStatusTone(status?: ConsumerStatus | null): ConsumerStatusTone {
  if (status?.failProcessing) {
    return "danger"
  }

  if (status?.paused) {
    return "paused"
  }

  return "ready"
}

export function getConsumerStatusLabel(status?: ConsumerStatus | null) {
  if (status?.failProcessing) {
    return "Failure mode enabled"
  }

  if (status?.paused) {
    return "Consumer paused"
  }

  return "Consumer running"
}

export function getConsumerStatusDescription(status?: ConsumerStatus | null) {
  return status?.failProcessing ? "DLT replay waiting" : "Automatic replay ready"
}
