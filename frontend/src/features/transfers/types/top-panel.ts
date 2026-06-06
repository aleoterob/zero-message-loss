import type { AccountOption } from "@/features/transfers/types/transfer"

export type SubmitState = "idle" | "loading" | "success" | "error"

export interface TopPanelProps {
  accounts?: AccountOption[]
  onClearPanels: () => void
}

export interface AccountComboboxProps {
  accounts: AccountOption[]
  id: string
  label: string
  value: string
  onChange: (value: string) => void
}

export interface SubmitNoticeProps {
  state: SubmitState
  message: string
}

export type ConsumerActionState = "idle" | "loading" | "error"

export interface ConsumerStatus {
  failProcessing: boolean
}

export interface TransferFormState {
  fromAccount: string
  toAccount: string
  amount: string
  submitState: SubmitState
  submitMessage: string
}

export type TransferFormAction =
  | { type: "setFromAccount"; value: string }
  | { type: "setToAccount"; value: string }
  | { type: "setAmount"; value: string }
  | { type: "submitStarted" }
  | { type: "submitSucceeded"; message: string }
  | { type: "submitFailed"; message: string }

export interface ConsumerControlsProps {
  actionState: ConsumerActionState
  status: ConsumerStatus | null
  statusMessage: string
  onFailProcessing: () => void
  onRestoreProcessing: () => void
  onClearPanels: () => void
}
