import type { TransferEventDto } from "@/features/events/types/event"
import type { ProcessedTransferDto } from "@/features/transfers/types/transfer"

export interface LeftPanelProps {
  events: TransferEventDto[]
  processedTransfers: ProcessedTransferDto[]
}
