const eventTimeFormatter = new Intl.DateTimeFormat(undefined, {
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  month: "2-digit",
  second: "2-digit",
  year: "2-digit",
})

export function truncateUuid(value: string) {
  return `${value.slice(0, 8)}...${value.slice(-4)}`
}

export function formatEventDate(epochMillis: number) {
  return eventTimeFormatter.format(new Date(epochMillis))
}

export function formatDeliveryState(value: string) {
  return value.toLowerCase().replaceAll("_", " ")
}

export function deliveryStateClasses(value: string) {
  switch (value) {
    case "LIVE":
      return "border-primary bg-primary/10 text-primary"
    case "DLT_PENDING":
    case "DLT_REPLAY_FAILED":
      return "border-destructive bg-destructive/10 text-destructive"
    case "DLT_REPLAYED":
      return "border-ring bg-secondary text-secondary-foreground"
    default:
      return "border-border bg-muted text-muted-foreground"
  }
}

export function formatProcessedAt(value: string) {
  const timestamp = Number(value)
  if (Number.isFinite(timestamp)) {
    return new Date(timestamp / 1000).toLocaleString()
  }

  const date = new Date(value)
  if (!Number.isNaN(date.getTime())) {
    return date.toLocaleString()
  }

  return value
}
