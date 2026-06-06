import type { TransferEventDto } from "@/features/events/types/event"
import type { ProcessedTransferDto } from "@/features/transfers/types/transfer"

export interface RightPanelProps {
  events: TransferEventDto[]
  processedTransfers: ProcessedTransferDto[]
}
