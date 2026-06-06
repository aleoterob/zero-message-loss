import { useEffect, useState } from "react"

import type { TransferEventDto } from "@/features/events/types/event"

export function useEventStream(url: string) {
  const [events, setEvents] = useState<TransferEventDto[]>([])

  useEffect(() => {
    const source = new EventSource(url)

    source.onmessage = (message) => {
      const event = JSON.parse(message.data) as TransferEventDto
      setEvents((currentEvents) => {
        const otherEvents = currentEvents.filter((currentEvent) => currentEvent.eventId !== event.eventId)
        return [event, ...otherEvents].slice(0, 20)
      })
    }

    return () => source.close()
  }, [url])

  return {
    clearEvents: () => setEvents([]),
    events,
  }
}
