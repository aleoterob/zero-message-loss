import { ArrowRight, CalendarClock, Hash, RefreshCcw } from "lucide-react"

import { ConsumerTransferDb } from "@/components/layout/consumer-transfer-db"
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import type { EventCardProps, EventPanelProps, ProcessedTransferDto } from "@/types/transfer"

const EMPTY_PROCESSED_TRANSFERS: ProcessedTransferDto[] = []

const eventTimeFormatter = new Intl.DateTimeFormat(undefined, {
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  month: "2-digit",
  second: "2-digit",
  year: "2-digit",
})

export function EventPanel({
  title,
  description,
  emptyText,
  events,
  processedTransfers = EMPTY_PROCESSED_TRANSFERS,
  tone,
}: EventPanelProps) {
  return (
    <Card className="event-panel">
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        {events.length === 0 ? (
          <div className="empty-state">{emptyText}</div>
        ) : (
          <div className="event-list">
            {events.map((event) => (
              <EventCard
                event={event}
                key={`${event.eventId}-${event.deliveryState ?? tone}-${event.replayAttempts ?? 0}`}
                processedTransfer={processedTransfers.find(
                  (processedTransfer) => processedTransfer.eventId === event.eventId,
                )}
                tone={tone}
              />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

function EventCard({ event, processedTransfer, tone }: EventCardProps) {
  const deliveryState = event.deliveryState ?? (event.isDlt ? "DLT_PENDING" : "LIVE")
  const replayAttempts = event.replayAttempts ?? 0
  const shouldShowConsumerDb = deliveryState === "DLT_REPLAYED" && processedTransfer !== undefined

  return (
    <Card className={`event-card ${tone} ${deliveryState.toLowerCase().replaceAll("_", "-")}`} size="sm">
      <CardHeader>
        <CardTitle>
          <span>{event.amount}</span>
          <span>{event.currency}</span>
        </CardTitle>
        <CardDescription>
          <CalendarClock data-icon="inline-start" />
          {formatDate(event.createdAt)}
        </CardDescription>
        <CardAction>
          <span className={`event-state ${deliveryState.toLowerCase().replaceAll("_", "-")}`}>
            {formatDeliveryState(deliveryState)}
          </span>
        </CardAction>
      </CardHeader>
      <CardContent>
        <div className="route-line">
          <span title={event.fromAccount}>{event.fromAccount}</span>
          <ArrowRight data-icon="inline-start" />
          <span title={event.toAccount}>{event.toAccount}</span>
        </div>
        <div className="event-details">
          <EventField label="Transfer ID" value={event.transferId} />
          <EventField label="Event ID" value={event.eventId} />
          <EventField label="Status" value={event.status} />
          <EventField label="Source" value={event.isDlt ? "Dead letter topic" : "Live topic"} />
        </div>
        {shouldShowConsumerDb ? <ConsumerTransferDb processedTransfer={processedTransfer} /> : null}
      </CardContent>
      <CardFooter className="event-footer">
        <span>
          <Hash data-icon="inline-start" />
          {truncateUuid(event.eventId)}
        </span>
        <span>
          <RefreshCcw data-icon="inline-start" />
          Replay attempts {replayAttempts}
        </span>
      </CardFooter>
    </Card>
  )
}

function EventField({ label, value }: { label: string; value: string }) {
  return (
    <div className="event-field">
      <span>{label}</span>
      <strong title={value}>{value}</strong>
    </div>
  )
}

function truncateUuid(value: string) {
  return `${value.slice(0, 8)}...${value.slice(-4)}`
}

function formatDate(epochMillis: number) {
  return eventTimeFormatter.format(new Date(epochMillis))
}

function formatDeliveryState(value: string) {
  return value.toLowerCase().replaceAll("_", " ")
}
