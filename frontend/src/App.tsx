import { Box } from "lucide-react"

import { LeftPanel } from "@/components/layout/left-panel"
import { RightPanel } from "@/components/layout/right-panel"
import { TopPanel } from "@/components/layout/top-panel"
import { env } from "@/config/env"
import { useEventStream } from "@/hooks/use-event-stream"
import { useProcessedTransferStream } from "@/hooks/use-processed-transfer-stream"

function App() {
  const liveEvents = useEventStream(env.eventsStreamUrl)
  const dltEvents = useEventStream(env.eventsDltUrl)
  const processedTransfers = useProcessedTransferStream(env.eventsProcessedUrl)

  return (
    <main className="grid min-h-screen grid-rows-[auto_auto_auto] p-5 md:p-6">
      <section className="mx-auto mb-6 flex w-full max-w-[1180px] flex-col items-start gap-6 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-2 text-xs font-bold uppercase text-muted-foreground">Zero Message Loss</p>
          <h1 className="m-0 flex items-center gap-3 text-3xl font-semibold leading-none md:text-4xl">
            <Box className="size-8 shrink-0 translate-y-0.5 text-primary md:size-9" aria-hidden="true" />
            <span>Banking transfer event monitor</span>
          </h1>
        </div>
        <div className="flex flex-wrap gap-2 md:justify-end" aria-label="Service ports">
          <span className="rounded-full border border-primary px-2.5 py-1.5 text-xs text-muted-foreground">
            Producer 8081
          </span>
          <span className="rounded-full border border-primary px-2.5 py-1.5 text-xs text-muted-foreground">
            Message Ops 8085
          </span>
          <span className="rounded-full border border-primary px-2.5 py-1.5 text-xs text-muted-foreground">
            Frontend 5173
          </span>
        </div>
      </section>

      <TopPanel />

      <section className="mx-auto mt-4 grid w-full max-w-[1180px] grid-cols-1 gap-4 md:grid-cols-2">
        <LeftPanel events={liveEvents} processedTransfers={processedTransfers} />
        <RightPanel events={dltEvents} processedTransfers={processedTransfers} />
      </section>
    </main>
  )
}

export default App
