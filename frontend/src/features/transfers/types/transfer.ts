export interface AccountOption {
  id: string
  label: string
}

export interface ProcessedTransferDto {
  eventId: string
  transferId: string
  fromAccount: string
  toAccount: string
  amount: string
  currency: string
  status: string
  processedAt: string
}

export interface ConsumerTransferDbCardProps {
  processedTransfer: ProcessedTransferDto
}
