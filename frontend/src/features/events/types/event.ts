import type { ProcessedTransferDto } from "@/features/transfers/types/transfer"

export interface TransferEventDto {
  eventId: string
  transferId: string
  fromAccount: string
  toAccount: string
  amount: string
  currency: string
  status: string
  createdAt: number
  isDlt: boolean
  deliveryState?: "LIVE" | "DLT_PENDING" | "DLT_REPLAYED" | "DLT_REPLAY_FAILED"
  replayAttempts?: number
}

export type EventTone = "live" | "dlt"

export interface EventPanelProps {
  title: string
  description: string
  emptyText: string
  events: TransferEventDto[]
  processedTransfers?: ProcessedTransferDto[]
  tone: EventTone
}

export interface EventCardProps {
  event: TransferEventDto
  processedTransfer?: ProcessedTransferDto
}
