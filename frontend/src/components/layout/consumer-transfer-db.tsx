import { Database } from "lucide-react"

import type { ProcessedTransferDto } from "@/types/transfer"

interface ConsumerTransferDbProps {
  processedTransfer: ProcessedTransferDto
}

export function ConsumerTransferDb({ processedTransfer }: ConsumerTransferDbProps) {
  return (
    <div className="consumer-transfer-db">
      <div className="consumer-transfer-db__header">
        <Database data-icon="inline-start" />
        <span>Consumer DB confirmed</span>
      </div>
      <div className="consumer-transfer-db__grid">
        <DbField label="From" value={processedTransfer.fromAccount} />
        <DbField label="To" value={processedTransfer.toAccount} />
        <DbField label="Amount" value={`${processedTransfer.amount} ${processedTransfer.currency}`} />
        <DbField label="Status" value={processedTransfer.status} />
        <DbField label="Transfer ID" value={processedTransfer.transferId} />
        <DbField label="Event ID" value={processedTransfer.eventId} />
        <DbField label="Processed at" value={formatProcessedAt(processedTransfer.processedAt)} />
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
