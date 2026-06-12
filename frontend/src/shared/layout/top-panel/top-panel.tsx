import { useCreateTransfer } from "@/features/transfers/hooks/use-create-transfer"
import { dummyAccounts } from "@/features/transfers/constants/dummy-accounts"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/shared/components/ui/card"
import { ConsumerControls } from "@/shared/layout/top-panel/consumer-controls"
import { SubmitNotice } from "@/shared/layout/top-panel/submit-notice"
import { TransferForm } from "@/shared/layout/top-panel/transfer-form"
import type { TopPanelProps } from "@/features/transfers/types/top-panel"

export function TopPanel({ accounts = dummyAccounts, consumerControl, onClearPanels }: TopPanelProps) {
  const transferForm = useCreateTransfer()

  return (
    <Card className="mx-auto w-full max-w-295 ring-primary/60" size="sm">
      <CardHeader className="gap-0.5">
        <CardTitle>Create Transfer</CardTitle>
        <CardDescription>
          Submit a banking transfer and watch the outbox event arrive through Kafka.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <TransferForm accounts={accounts} transferForm={transferForm} />
        <SubmitNotice state={transferForm.submitState} message={transferForm.submitMessage} />
        <ConsumerControls
          actionState={consumerControl.actionState}
          isAvailable={consumerControl.isAvailable}
          status={consumerControl.status}
          statusMessage={consumerControl.statusMessage}
          onClearPanels={onClearPanels}
          onFailProcessing={consumerControl.failProcessing}
          onRestoreProcessing={consumerControl.restoreProcessing}
        />
      </CardContent>
    </Card>
  )
}
