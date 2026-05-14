import { useEffect, useState } from "react"

import type { TransferEventDto } from "@/types/transfer"

export function useEventStream(url: string) {
  const [events, setEvents] = useState<TransferEventDto[]>([])

  useEffect(() => {
    const source = new EventSource(url)

    source.onmessage = (message) => {
      const event = JSON.parse(message.data) as TransferEventDto
      setEvents((currentEvents) => [event, ...currentEvents].slice(0, 20))
    }

    return () => source.close()
  }, [url])

  return events
}
