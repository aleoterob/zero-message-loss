import { EventPanel } from "@/components/layout/event-panel"
import type { RightPanelProps } from "@/types/right-panel"

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
