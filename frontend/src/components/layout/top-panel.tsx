import { useState } from "react"
import {
  AlertCircle,
  CheckCircle2,
  ChevronsUpDown,
  Loader2,
  Pause,
  Play,
  RotateCcw,
  SendHorizontal,
  Siren,
} from "lucide-react"

import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { useCreateTransfer } from "@/hooks/use-create-transfer"
import { useConsumerControl } from "@/hooks/use-consumer-control"
import type { AccountOption } from "@/types/transfer"
import type {
  AccountComboboxProps,
  ConsumerControlsProps,
  SubmitNoticeProps,
  TopPanelProps,
} from "@/types/top-panel"

const dummyAccounts: AccountOption[] = [
  { id: "ACC001", label: "ACC001 - Payroll Account" },
  { id: "ACC002", label: "ACC002 - Savings Account" },
  { id: "ACC003", label: "ACC003 - Vendor Payments" },
  { id: "ACC004", label: "ACC004 - Treasury Account" },
  { id: "ACC005", label: "ACC005 - Operations Account" },
]

export function TopPanel({ accounts = dummyAccounts }: TopPanelProps) {
  const {
    amount,
    createTransfer,
    fromAccount,
    setAmount,
    setFromAccount,
    setToAccount,
    submitMessage,
    submitState,
    toAccount,
  } = useCreateTransfer()
  const consumerControl = useConsumerControl()

  return (
    <Card className="transfer-card">
      <CardHeader>
        <CardTitle>Create Transfer</CardTitle>
        <CardDescription>
          Submit a banking transfer and watch the outbox event arrive through Kafka.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className="transfer-form" onSubmit={createTransfer}>
          <AccountCombobox
            accounts={accounts}
            id="from-account"
            label="From account"
            value={fromAccount}
            onChange={setFromAccount}
          />
          <AccountCombobox
            accounts={accounts}
            id="to-account"
            label="To account"
            value={toAccount}
            onChange={setToAccount}
          />
          <div className="field-stack">
            <Label htmlFor="amount">Amount</Label>
            <Input
              id="amount"
              min="0.01"
              step="0.01"
              type="number"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
            />
          </div>
          <Button
            className="submit-button"
            disabled={submitState === "loading" || fromAccount === toAccount || Number(amount) <= 0}
            type="submit"
          >
            {submitState === "loading" ? (
              <Loader2 data-icon="inline-start" />
            ) : (
              <SendHorizontal data-icon="inline-start" />
            )}
            Create transfer
          </Button>
        </form>
        <SubmitNotice state={submitState} message={submitMessage} />
        <ConsumerControls
          actionState={consumerControl.actionState}
          status={consumerControl.status}
          statusMessage={consumerControl.statusMessage}
          onFailProcessing={consumerControl.failProcessing}
          onPause={consumerControl.pause}
          onRestoreProcessing={consumerControl.restoreProcessing}
          onResume={consumerControl.resume}
        />
      </CardContent>
    </Card>
  )
}

function AccountCombobox({ accounts, id, label, value, onChange }: AccountComboboxProps) {
  const [open, setOpen] = useState(false)
  const selectedAccount = accounts.find((account) => account.id === value)

  return (
    <div className="field-stack">
      <Label htmlFor={id}>{label}</Label>
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            aria-expanded={open}
            className="account-trigger"
            id={id}
            type="button"
            variant="outline"
          >
            <span>{selectedAccount?.label ?? "Select account"}</span>
            <ChevronsUpDown data-icon="inline-end" />
          </Button>
        </PopoverTrigger>
        <PopoverContent align="start" className="account-popover">
          <Command>
            <CommandInput placeholder="Search account..." />
            <CommandList>
              <CommandEmpty>No account found.</CommandEmpty>
              <CommandGroup>
                {accounts.map((account) => (
                  <CommandItem
                    data-checked={account.id === value}
                    key={account.id}
                    onSelect={() => {
                      onChange(account.id)
                      setOpen(false)
                    }}
                    value={account.label}
                  >
                    {account.label}
                  </CommandItem>
                ))}
              </CommandGroup>
            </CommandList>
          </Command>
        </PopoverContent>
      </Popover>
    </div>
  )
}

function SubmitNotice({ state, message }: SubmitNoticeProps) {
  if (state === "idle" || state === "loading") {
    return null
  }

  return (
    <p className={`submit-notice ${state}`}>
      {state === "success" ? <CheckCircle2 data-icon="inline-start" /> : <AlertCircle data-icon="inline-start" />}
      {message}
    </p>
  )
}

function ConsumerControls({
  actionState,
  status,
  statusMessage,
  onFailProcessing,
  onPause,
  onRestoreProcessing,
  onResume,
}: ConsumerControlsProps) {
  const isLoading = actionState === "loading"
  const statusTone = status?.failProcessing ? "danger" : status?.paused ? "paused" : "ready"
  const statusLabel = status?.failProcessing
    ? "Failure mode enabled"
    : status?.paused
      ? "Consumer paused"
      : "Consumer running"

  return (
    <div className="consumer-controls">
      <div className={`consumer-status ${statusTone}`}>
        <span>{statusLabel}</span>
        <small>{status?.failProcessing ? "DLT replay waiting" : "Automatic replay ready"}</small>
      </div>
      <div className="control-actions">
        <Button disabled={isLoading || status?.paused === true} onClick={onPause} type="button" variant="outline">
          <Pause data-icon="inline-start" />
          Pause
        </Button>
        <Button disabled={isLoading || status?.paused === false} onClick={onResume} type="button" variant="outline">
          <Play data-icon="inline-start" />
          Resume
        </Button>
        <Button
          disabled={isLoading || status?.failProcessing === true}
          onClick={onFailProcessing}
          type="button"
          variant="outline"
        >
          <Siren data-icon="inline-start" />
          Fail mode
        </Button>
        <Button
          disabled={isLoading || status?.failProcessing === false}
          onClick={onRestoreProcessing}
          type="button"
          variant="outline"
        >
          <RotateCcw data-icon="inline-start" />
          Restore
        </Button>
      </div>
      {statusMessage ? <p className={`control-message ${actionState}`}>{statusMessage}</p> : null}
    </div>
  )
}
