import { useAgentWidget } from '@/features/agent-widget/hooks/use-agent-widget'
import type { AgentWidgetProps } from '@/features/agent-widget/types/agent-widget'

export function AgentWidget({ enabled }: AgentWidgetProps) {
  useAgentWidget({ enabled })

  return null
}
