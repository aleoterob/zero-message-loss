import { AlertCircle, CheckCircle2 } from "lucide-react"

import { cn } from "@/shared/lib/utils"
import type { SubmitNoticeProps } from "@/features/transfers/types/top-panel"

export function SubmitNotice({ state, message }: SubmitNoticeProps) {
  if (state === "idle" || state === "loading") {
    return null
  }

  return (
    <p
      className={cn(
        "mt-4 flex items-center gap-2 text-sm",
        state === "success" ? "text-accent-foreground" : "text-destructive",
      )}
    >
      {state === "success" ? <CheckCircle2 data-icon="inline-start" /> : <AlertCircle data-icon="inline-start" />}
      {message}
    </p>
  )
}
