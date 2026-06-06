import { EventPanel } from "@/features/events/components/event-panel"
import type { RightPanelProps } from "@/features/events/types/right-panel"

export function RightPanel({ events, processedTransfers }: RightPanelProps) {
  return (
    <EventPanel
      description="Events received from GET /events/dlt"
      emptyText="No failed messages are waiting for replay."
      events={events}
      processedTransfers={processedTransfers}
      title="Dead Letter Topic / Replay"
      tone="dlt"
    />
  )
}
