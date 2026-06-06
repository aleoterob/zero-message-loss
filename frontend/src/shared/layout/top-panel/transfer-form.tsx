import { Loader2, SendHorizontal } from "lucide-react"

import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import { AccountCombobox } from "@/shared/layout/top-panel/account-combobox"
import type { TransferFormProps } from "@/features/transfers/types/top-panel"

export function TransferForm({ accounts, transferForm }: TransferFormProps) {
  const {
    amount,
    createTransfer,
    fromAccount,
    setAmount,
    setFromAccount,
    setToAccount,
    submitState,
    toAccount,
  } = transferForm

  return (
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
  )
}
