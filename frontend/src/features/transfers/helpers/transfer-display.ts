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
