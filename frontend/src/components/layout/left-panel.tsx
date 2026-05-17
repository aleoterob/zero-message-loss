import { EventPanel } from "@/components/layout/event-panel"
import type { LeftPanelProps } from "@/types/left-panel"

export function LeftPanel({ events, processedTransfers }: LeftPanelProps) {
  return (
    <EventPanel
      description="Events received from GET /events/stream"
      emptyText="Create a transfer to see the normal event stream."
      events={events}
      processedTransfers={processedTransfers}
      title="Live Transfers"
      tone="live"
    />
  )
}
