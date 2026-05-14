import { useReducer, type FormEvent } from "react"

import { env } from "@/config/env"
import type { TransferFormAction, TransferFormState } from "@/types/top-panel"

const initialTransferFormState: TransferFormState = {
  fromAccount: "ACC001",
  toAccount: "ACC002",
  amount: "1500.00",
  submitState: "idle",
  submitMessage: "",
}

function transferFormReducer(state: TransferFormState, action: TransferFormAction): TransferFormState {
  switch (action.type) {
    case "setFromAccount":
      return { ...state, fromAccount: action.value }
    case "setToAccount":
      return { ...state, toAccount: action.value }
    case "setAmount":
      return { ...state, amount: action.value }
    case "submitStarted":
      return { ...state, submitState: "loading", submitMessage: "" }
    case "submitSucceeded":
      return { ...state, submitState: "success", submitMessage: action.message }
    case "submitFailed":
      return { ...state, submitState: "error", submitMessage: action.message }
  }
}

export function useCreateTransfer() {
  const [formState, dispatch] = useReducer(transferFormReducer, initialTransferFormState)
  const { amount, fromAccount, submitMessage, submitState, toAccount } = formState

  async function createTransfer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    dispatch({ type: "submitStarted" })

    try {
      const response = await fetch(env.transferApiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          fromAccount,
          toAccount,
          amount: Number(amount),
          currency: "ARS",
        }),
      })

      if (!response.ok) {
        throw new Error(`Transfer API returned ${response.status}`)
      }

      dispatch({
        type: "submitSucceeded",
        message: "Transfer created. Watch the live stream for the event.",
      })
    } catch (error) {
      dispatch({
        type: "submitFailed",
        message: error instanceof Error ? error.message : "Could not create transfer.",
      })
    }
  }

  return {
    amount,
    createTransfer,
    fromAccount,
    setAmount: (value: string) => dispatch({ type: "setAmount", value }),
    setFromAccount: (value: string) => dispatch({ type: "setFromAccount", value }),
    setToAccount: (value: string) => dispatch({ type: "setToAccount", value }),
    submitMessage,
    submitState,
    toAccount,
  }
}
