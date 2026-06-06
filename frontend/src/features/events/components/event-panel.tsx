import { EventCard } from "@/features/events/components/event-card"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/shared/components/ui/card"
import type { EventPanelProps } from "@/features/events/types/event"
import type { ProcessedTransferDto } from "@/features/transfers/types/transfer"

const EMPTY_PROCESSED_TRANSFERS: ProcessedTransferDto[] = []

export function EventPanel({
  title,
  description,
  emptyText,
  events,
  processedTransfers = EMPTY_PROCESSED_TRANSFERS,
  tone,
}: EventPanelProps) {
  const isEmpty = events.length === 0

  return (
    <Card
      className={`grid grid-rows-[auto_1fr] ring-primary/60 ${isEmpty ? "min-h-[280px]" : "min-h-[360px]"}`}
    >
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        {isEmpty ? (
          <div className="flex min-h-full items-center rounded-lg border border-dashed border-border p-4 text-center text-muted-foreground">
            {emptyText}
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {events.map((event) => (
              <EventCard
                event={event}
                key={`${event.eventId}-${event.deliveryState ?? tone}-${event.replayAttempts ?? 0}`}
                processedTransfer={processedTransfers.find(
                  (processedTransfer) => processedTransfer.eventId === event.eventId,
                )}
              />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
