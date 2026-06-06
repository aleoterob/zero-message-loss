import { useState } from "react"
import { ChevronsUpDown } from "lucide-react"

import { Button } from "@/shared/components/ui/button"
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/shared/components/ui/command"
import { Label } from "@/shared/components/ui/label"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/shared/components/ui/popover"
import type { AccountComboboxProps } from "@/features/transfers/types/top-panel"

export function AccountCombobox({ accounts, id, label, value, onChange }: AccountComboboxProps) {
  const [open, setOpen] = useState(false)
  const selectedAccount = accounts.find((account) => account.id === value)

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
            <span className="min-w-0 truncate">{selectedAccount?.label ?? "Select account"}</span>
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
