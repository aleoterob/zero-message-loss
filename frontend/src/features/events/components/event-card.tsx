import { ArrowRight, CalendarClock, Hash, RefreshCcw } from "lucide-react"

import { ConsumerTransferDbCard } from "@/features/transfers/components/consumer-transfer-db-card"
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/shared/components/ui/card"
import { MetadataField } from "@/shared/components/metadata-field"
import {
  deliveryStateClasses,
  formatDeliveryState,
  formatEventDate,
  truncateUuid,
} from "@/features/events/helpers/event-display"
import { useEventCard } from "@/features/events/hooks/use-event-card"
import { cn } from "@/shared/lib/utils"
import type { EventCardProps } from "@/features/events/types/event"

export function EventCard({ event, processedTransfer }: EventCardProps) {
  const { deliveryState, displayedStatus, replayAttempts, shouldShowConsumerDb } = useEventCard({
    event,
    processedTransfer,
  })

  return (
    <Card className="transition-shadow hover:shadow-md" size="sm">
      <CardHeader className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-1.5 pb-1">
        <CardTitle className="flex min-w-0 flex-wrap items-baseline gap-1.5">
          <span className="text-lg font-bold">{event.amount}</span>
          <span className="text-xs font-bold uppercase text-muted-foreground">{event.currency}</span>
        </CardTitle>
        <CardDescription className="flex items-center gap-1.5">
          <CalendarClock data-icon="inline-start" />
          {formatEventDate(event.createdAt)}
        </CardDescription>
        <CardAction>
          <span
            className={cn(
              "inline-flex whitespace-nowrap rounded-sm border px-2 py-1 text-xs font-bold uppercase leading-none",
              deliveryStateClasses(deliveryState),
            )}
          >
            {formatDeliveryState(deliveryState)}
          </span>
        </CardAction>
      </CardHeader>
      <CardContent>
        <div className="flex min-w-0 flex-nowrap items-center gap-2 font-semibold">
          <span
            className="min-w-0 flex-1 truncate rounded-sm border border-border px-2 py-1.5"
            title={event.fromAccount}
          >
            {event.fromAccount}
          </span>
          <ArrowRight data-icon="inline-start" />
          <span
            className="min-w-0 flex-1 truncate rounded-sm border border-border px-2 py-1.5"
            title={event.toAccount}
          >
            {event.toAccount}
          </span>
        </div>
        <div className="mt-3 grid grid-cols-1 gap-2 md:grid-cols-2">
          <MetadataField label="Transfer ID" value={event.transferId} />
          <MetadataField label="Event ID" value={event.eventId} />
          <MetadataField label="Status" value={displayedStatus} />
          <MetadataField label="Source" value={event.isDlt ? "Dead letter topic" : "Live topic"} />
        </div>
        {shouldShowConsumerDb && processedTransfer ? (
          <ConsumerTransferDbCard processedTransfer={processedTransfer} />
        ) : null}
      </CardContent>
      <CardFooter className="flex flex-wrap justify-between gap-2.5 text-xs text-muted-foreground">
        <span className="inline-flex min-w-0 items-center gap-1">
          <Hash data-icon="inline-start" />
          {truncateUuid(event.eventId)}
        </span>
        <span className="inline-flex min-w-0 items-center gap-1">
          <RefreshCcw data-icon="inline-start" />
          Replay attempts {replayAttempts}
        </span>
      </CardFooter>
    </Card>
  )
}
