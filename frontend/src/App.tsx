import { AgentWidget } from '@/features/agent-widget/components/agent-widget';
import { useEventStream } from '@/features/events/hooks/use-event-stream';
import { useProcessedTransferStream } from '@/features/transfers/hooks/use-processed-transfer-stream';
import { env } from '@/shared/config/env';
import { LeftPanel } from '@/shared/layout/left-panel';
import { RightPanel } from '@/shared/layout/right-panel';
import { TopPanel } from '@/shared/layout/top-panel';

function App() {
  const liveEvents = useEventStream(env.eventsStreamUrl);
  const dltEvents = useEventStream(env.eventsDltUrl);
  const processedTransfers = useProcessedTransferStream(env.eventsProcessedUrl);

  return (
    <div className="min-h-screen w-full">
      <AgentWidget enabled />
      <main className="grid min-h-screen grid-rows-[auto_auto_auto] p-5 md:p-6">
        <section className="mx-auto mb-6 flex w-full max-w-295 flex-col items-start gap-6 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="mb-2 text-xs font-bold uppercase text-muted-foreground">
              Zero Message Loss
            </p>
            <h1 className="m-0 flex items-center gap-3 text-3xl font-semibold leading-none md:text-4xl">
              <img
                src="/apache-kafka-dark.svg"
                alt=""
                className="size-9 shrink-0 translate-y-0.5 md:size-10"
                aria-hidden="true"
              />
              <span>Banking transfer event monitor</span>
            </h1>
          </div>
          <div
            className="flex flex-wrap gap-2 md:justify-end"
            aria-label="Service ports"
          >
            <span className="inline-flex h-7 transform-gpu items-center rounded-full border border-primary/60 px-2.5 text-xs leading-none text-muted-foreground">
              Producer 8081
            </span>
            <span className="inline-flex h-7 transform-gpu items-center rounded-full border border-primary/60 px-2.5 text-xs leading-none text-muted-foreground">
              Message Ops 8085
            </span>
            <span className="inline-flex h-7 transform-gpu items-center rounded-full border border-primary/60 px-2.5 text-xs leading-none text-muted-foreground">
              Frontend 5173
            </span>
          </div>
        </section>

        <TopPanel />

        <section className="mx-auto mt-4 grid w-full max-w-295 grid-cols-1 gap-4 md:grid-cols-2">
          <LeftPanel
            events={liveEvents}
            processedTransfers={processedTransfers}
          />
          <RightPanel
            events={dltEvents}
            processedTransfers={processedTransfers}
          />
        </section>
      </main>
    </div>
  );
}

export default App;
