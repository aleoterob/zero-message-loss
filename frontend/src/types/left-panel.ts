import type { ProcessedTransferDto, TransferEventDto } from "@/types/transfer"

export interface LeftPanelProps {
  events: TransferEventDto[]
  processedTransfers: ProcessedTransferDto[]
}
