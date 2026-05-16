export interface AccountOption {
  id: string
  label: string
}

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

export interface ProcessedEventDto {
  eventId: string
  transferId: string
  processedAt: string
}

export type EventTone = "live" | "dlt"

export interface EventPanelProps {
  title: string
  description: string
  emptyText: string
  events: TransferEventDto[]
  processedEvents?: ProcessedEventDto[]
  tone: EventTone
}

export interface EventCardProps {
  event: TransferEventDto
  processedEvent?: ProcessedEventDto
  tone: EventTone
}
