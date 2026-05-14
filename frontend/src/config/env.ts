export const env = {
  transferApiUrl: import.meta.env.VITE_TRANSFER_API_URL,
  eventsStreamUrl: import.meta.env.VITE_EVENTS_STREAM_URL,
  eventsDltUrl: import.meta.env.VITE_EVENTS_DLT_URL,
  consumerControlUrl: import.meta.env.VITE_CONSUMER_CONTROL_URL,
} as const
