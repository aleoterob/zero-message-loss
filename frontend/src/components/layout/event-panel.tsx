import { ArrowRight } from "lucide-react"

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import type { EventCardProps, EventPanelProps } from "@/types/transfer"

const eventTimeFormatter = new Intl.DateTimeFormat(undefined, {
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
})

export function EventPanel({ title, description, emptyText, events, tone }: EventPanelProps) {
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
                tone={tone}
              />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

function EventCard({ event, tone }: EventCardProps) {
  const deliveryState = event.deliveryState ?? (event.isDlt ? "DLT_PENDING" : "LIVE")

  return (
    <Card className={`event-card ${tone} ${deliveryState.toLowerCase().replaceAll("_", "-")}`}>
      <CardHeader>
        <CardTitle>{truncateUuid(event.transferId)}</CardTitle>
        <CardDescription>{formatDate(event.createdAt)}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="route-line">
          <span>{event.fromAccount}</span>
          <ArrowRight data-icon="inline-start" />
          <span>{event.toAccount}</span>
        </div>
        <div className="event-meta">
          <span>
            {event.amount} {event.currency}
          </span>
          <span>{event.status}</span>
          <span>{formatDeliveryState(deliveryState)}</span>
          {event.replayAttempts ? <span>Replay attempts {event.replayAttempts}</span> : null}
        </div>
      </CardContent>
    </Card>
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
