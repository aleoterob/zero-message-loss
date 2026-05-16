import type { MetadataFieldProps } from "@/types/transfer"

export function MetadataField({ label, value }: MetadataFieldProps) {
  return (
    <div className="min-w-0">
      <span className="mb-0.5 block text-xs font-bold uppercase text-muted-foreground">{label}</span>
      <strong className="block min-w-0 truncate text-sm font-semibold" title={value}>
        {value}
      </strong>
    </div>
  )
}
