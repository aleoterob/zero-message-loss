import type { EventCardProps } from "@/features/events/types/event"

export function useEventCard({ event, processedTransfer }: EventCardProps) {
  const deliveryState = event.deliveryState ?? (event.isDlt ? "DLT_PENDING" : "LIVE")

  return {
    deliveryState,
    displayedStatus: processedTransfer?.status ?? event.status,
    replayAttempts: event.replayAttempts ?? 0,
    shouldShowConsumerDb: deliveryState !== "DLT_PENDING" && processedTransfer !== undefined,
  }
}
