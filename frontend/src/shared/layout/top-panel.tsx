import { useState } from 'react';
import {
  AlertCircle,
  CheckCircle2,
  ChevronsUpDown,
  Loader2,
  RotateCcw,
  SendHorizontal,
  Siren,
  Trash2,
} from 'lucide-react';

import { useCreateTransfer } from '@/features/transfers/hooks/use-create-transfer';
import { useConsumerControl } from '@/features/transfers/hooks/use-consumer-control';
import {
  getConsumerStatusDescription,
  getConsumerStatusLabel,
  getConsumerStatusTone,
} from '@/features/transfers/helpers/consumer-control-display';
import { dummyAccounts } from '@/features/transfers/constants/dummy-accounts';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/shared/components/ui/command';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { cn } from '@/shared/lib/utils';
import type {
  AccountComboboxProps,
  ConsumerControlsProps,
  SubmitNoticeProps,
  TopPanelProps,
} from '@/features/transfers/types/top-panel';

export function TopPanel({
  accounts = dummyAccounts,
  onClearPanels,
}: TopPanelProps) {
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
  } = useCreateTransfer();
  const consumerControl = useConsumerControl();

  return (
    <Card className="mx-auto w-full max-w-295 ring-primary/60" size="sm">
      <CardHeader className="gap-0.5">
        <CardTitle>Create Transfer</CardTitle>
        <CardDescription>
          Submit a banking transfer and watch the outbox event arrive through
          Kafka.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form
          className="grid grid-cols-1 items-end gap-3.5 md:grid-cols-[minmax(220px,1fr)_minmax(220px,1fr)_minmax(140px,0.6fr)_auto]"
          onSubmit={createTransfer}
        >
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
          <div className="flex min-w-0 flex-col gap-2">
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
            className="min-w-[150px] w-full hover:opacity-85 md:w-auto"
            disabled={
              submitState === 'loading' ||
              fromAccount === toAccount ||
              Number(amount) <= 0
            }
            type="submit"
          >
            {submitState === 'loading' ? (
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
          onClearPanels={onClearPanels}
          onFailProcessing={consumerControl.failProcessing}
          onRestoreProcessing={consumerControl.restoreProcessing}
        />
      </CardContent>
    </Card>
  );
}

function AccountCombobox({
  accounts,
  id,
  label,
  value,
  onChange,
}: AccountComboboxProps) {
  const [open, setOpen] = useState(false);
  const selectedAccount = accounts.find((account) => account.id === value);

  return (
    <div className="flex min-w-0 flex-col gap-2">
      <Label htmlFor={id}>{label}</Label>
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            aria-expanded={open}
            className="w-full min-w-0 justify-between"
            id={id}
            type="button"
            variant="outline"
          >
            <span className="min-w-0 truncate">
              {selectedAccount?.label ?? 'Select account'}
            </span>
            <ChevronsUpDown data-icon="inline-end" />
          </Button>
        </PopoverTrigger>
        <PopoverContent align="start" className="w-[280px] p-0">
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
                      onChange(account.id);
                      setOpen(false);
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
  );
}

function SubmitNotice({ state, message }: SubmitNoticeProps) {
  if (state === 'idle' || state === 'loading') {
    return null;
  }

  return (
    <p
      className={cn(
        'mt-4 flex items-center gap-2 text-sm',
        state === 'success' ? 'text-accent-foreground' : 'text-destructive',
      )}
    >
      {state === 'success' ? (
        <CheckCircle2 data-icon="inline-start" />
      ) : (
        <AlertCircle data-icon="inline-start" />
      )}
      {message}
    </p>
  );
}

function ConsumerControls({
  actionState,
  status,
  statusMessage,
  onClearPanels,
  onFailProcessing,
  onRestoreProcessing,
}: ConsumerControlsProps) {
  const isLoading = actionState === 'loading';
  const statusTone = getConsumerStatusTone(status);
  const statusLabel = getConsumerStatusLabel(status);
  const statusDescription = getConsumerStatusDescription(status);

  return (
    <div className="mt-3 grid grid-cols-1 items-center gap-3 border-t border-border pt-3 md:grid-cols-[minmax(180px,0.8fr)_minmax(0,1.8fr)]">
      <div
        className={cn(
          'flex min-w-0 flex-col gap-0.5 rounded-md border p-2.5',
          statusTone === 'danger' && 'border-destructive bg-destructive/10',
          statusTone === 'ready' && 'border-border bg-accent',
        )}
      >
        <span className="text-sm font-bold">{statusLabel}</span>
        <small className="text-xs text-muted-foreground">
          {statusDescription}
        </small>
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
            'col-span-full m-0 text-sm text-muted-foreground',
            actionState === 'error' && 'text-destructive',
          )}
        >
          {statusMessage}
        </p>
      ) : null}
    </div>
  );
}
