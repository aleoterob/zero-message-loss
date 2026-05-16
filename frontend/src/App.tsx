import { LeftPanel } from "@/components/layout/left-panel"
import { RightPanel } from "@/components/layout/right-panel"
import { TopPanel } from "@/components/layout/top-panel"
import { env } from "@/config/env"
import { useEventStream } from "@/hooks/use-event-stream"
import { useProcessedEventStream } from "@/hooks/use-processed-event-stream"

function App() {
  const liveEvents = useEventStream(env.eventsStreamUrl)
  const dltEvents = useEventStream(env.eventsDltUrl)
  const processedEvents = useProcessedEventStream(env.eventsProcessedUrl)

  return (
    <main className="app-shell">
      <section className="app-header">
        <div>
          <p className="section-label">Zero Message Loss</p>
          <h1>Banking transfer event monitor</h1>
        </div>
        <div className="status-strip" aria-label="Service ports">
          <span>Producer 8081</span>
          <span>Message Ops 8085</span>
          <span>Frontend 5173</span>
        </div>
      </section>

      <TopPanel />

      <section className="event-grid">
        <LeftPanel events={liveEvents} />
        <RightPanel events={dltEvents} processedEvents={processedEvents} />
      </section>
    </main>
  )
}

export default App
