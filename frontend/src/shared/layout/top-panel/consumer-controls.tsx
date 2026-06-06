import { RotateCcw, Siren, Trash2 } from "lucide-react"

import {
  getConsumerStatusDescription,
  getConsumerStatusLabel,
  getConsumerStatusTone,
} from "@/features/transfers/helpers/consumer-control-display"
import { Button } from "@/shared/components/ui/button"
import { cn } from "@/shared/lib/utils"
import type { ConsumerControlsProps } from "@/features/transfers/types/top-panel"

export function ConsumerControls({
  actionState,
  status,
  statusMessage,
  onClearPanels,
  onFailProcessing,
  onRestoreProcessing,
}: ConsumerControlsProps) {
  const isLoading = actionState === "loading"
  const statusTone = getConsumerStatusTone(status)
  const statusLabel = getConsumerStatusLabel(status)
  const statusDescription = getConsumerStatusDescription(status)

  return (
    <div className="mt-3 grid grid-cols-1 items-center gap-3 border-t border-border pt-3 md:grid-cols-[minmax(180px,0.8fr)_minmax(0,1.8fr)]">
      <div
        className={cn(
          "flex min-w-0 flex-col gap-0.5 rounded-md border p-2.5",
          statusTone === "danger" && "border-destructive bg-destructive/10",
          statusTone === "ready" && "border-border bg-accent",
        )}
      >
        <span className="text-sm font-bold">{statusLabel}</span>
        <small className="text-xs text-muted-foreground">{statusDescription}</small>
      </div>
      <div className="flex flex-wrap gap-2 md:justify-end">
        <Button
          disabled={isLoading || status?.failProcessing === true}
          onClick={onFailProcessing}
          type="button"
          variant="primaryOutline"
        >
          <Siren data-icon="inline-start" />
          Fail mode
        </Button>
        <Button
          disabled={isLoading || status?.failProcessing === false}
          onClick={onRestoreProcessing}
          type="button"
          variant="primaryOutline"
        >
          <RotateCcw data-icon="inline-start" />
          Restore
        </Button>
        <Button onClick={onClearPanels} type="button" variant="primaryOutline">
          <Trash2 data-icon="inline-start" />
          Clear panels
        </Button>
      </div>
      {statusMessage ? (
        <p
          className={cn(
            "col-span-full m-0 text-sm text-muted-foreground",
            actionState === "error" && "text-destructive",
          )}
        >
          {statusMessage}
        </p>
      ) : null}
    </div>
  )
}
