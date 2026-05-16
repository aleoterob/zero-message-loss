import type { ProcessedEventDto, TransferEventDto } from "@/types/transfer"

export interface RightPanelProps {
  events: TransferEventDto[]
  processedEvents: ProcessedEventDto[]
}
