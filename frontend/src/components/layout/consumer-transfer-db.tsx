import { Database } from "lucide-react"

import type { ProcessedEventDto } from "@/types/transfer"

interface ConsumerTransferDbProps {
  processedEvent: ProcessedEventDto
}

export function ConsumerTransferDb({ processedEvent }: ConsumerTransferDbProps) {
  return (
    <div className="consumer-transfer-db">
      <div className="consumer-transfer-db__header">
        <Database data-icon="inline-start" />
        <span>Consumer DB confirmed</span>
      </div>
      <div className="consumer-transfer-db__grid">
        <DbField label="Event ID" value={processedEvent.eventId} />
        <DbField label="Transfer ID" value={processedEvent.transferId} />
        <DbField label="Processed at" value={formatProcessedAt(processedEvent.processedAt)} />
      </div>
    </div>
  )
}

function DbField({ label, value }: { label: string; value: string }) {
  return (
    <div className="consumer-transfer-db__field">
      <span>{label}</span>
      <strong title={value}>{value}</strong>
    </div>
  )
}

function formatProcessedAt(value: string) {
  const timestamp = Number(value)
  if (Number.isFinite(timestamp)) {
    return new Date(timestamp / 1000).toLocaleString()
  }

  const date = new Date(value)
  if (!Number.isNaN(date.getTime())) {
    return date.toLocaleString()
  }

  return value
}
