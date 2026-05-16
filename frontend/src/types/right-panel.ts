import type { ProcessedTransferDto, TransferEventDto } from "@/types/transfer"

export interface RightPanelProps {
  events: TransferEventDto[]
  processedTransfers: ProcessedTransferDto[]
}
