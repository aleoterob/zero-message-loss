import { useEffect, useState } from "react"

import type { ProcessedTransferDto } from "@/types/transfer"

export function useProcessedTransferStream(url: string) {
  const [transfers, setTransfers] = useState<ProcessedTransferDto[]>([])

  useEffect(() => {
    const source = new EventSource(url)

    source.onmessage = (message) => {
      const transfer = JSON.parse(message.data) as ProcessedTransferDto
      setTransfers((currentTransfers) => {
        const otherTransfers = currentTransfers.filter((currentTransfer) => currentTransfer.eventId !== transfer.eventId)
        return [transfer, ...otherTransfers].slice(0, 20)
      })
    }

    return () => source.close()
  }, [url])

  return transfers
}
