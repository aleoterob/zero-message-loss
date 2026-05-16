import { useEffect, useState } from "react"

import type { ProcessedEventDto } from "@/types/transfer"

export function useProcessedEventStream(url: string) {
  const [events, setEvents] = useState<ProcessedEventDto[]>([])

  useEffect(() => {
    const source = new EventSource(url)

    source.onmessage = (message) => {
      const event = JSON.parse(message.data) as ProcessedEventDto
      setEvents((currentEvents) => {
        const otherEvents = currentEvents.filter((currentEvent) => currentEvent.eventId !== event.eventId)
        return [event, ...otherEvents].slice(0, 20)
      })
    }

    return () => source.close()
  }, [url])

  return events
}
