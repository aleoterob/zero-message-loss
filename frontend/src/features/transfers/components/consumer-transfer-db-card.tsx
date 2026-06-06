import { Database } from "lucide-react"

import { formatProcessedAt } from "@/features/transfers/helpers/transfer-display"
import { MetadataField } from "@/shared/components/metadata-field"
import type { ConsumerTransferDbCardProps } from "@/features/transfers/types/transfer"

export function ConsumerTransferDbCard({ processedTransfer }: ConsumerTransferDbCardProps) {
  return (
    <div className="mt-3 rounded-md border border-border bg-accent p-3">
      <div className="mb-2.5 flex items-center gap-1.5 text-sm font-bold text-accent-foreground">
        <Database data-icon="inline-start" />
        <span>Consumer DB confirmed</span>
      </div>
      <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
        <MetadataField label="From" value={processedTransfer.fromAccount} />
        <MetadataField label="To" value={processedTransfer.toAccount} />
        <MetadataField label="Amount" value={`${processedTransfer.amount} ${processedTransfer.currency}`} />
        <MetadataField label="Status" value={processedTransfer.status} />
        <MetadataField label="Transfer ID" value={processedTransfer.transferId} />
        <MetadataField label="Event ID" value={processedTransfer.eventId} />
        <MetadataField label="Processed at" value={formatProcessedAt(processedTransfer.processedAt)} />
      </div>
    </div>
  )
}
