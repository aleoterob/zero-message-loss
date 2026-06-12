import { useEffect } from 'react';

import type { AgentWidgetProps } from '@/features/agent-widget/types/agent-widget';
import { env } from '@/shared/config/env';

const AGENT_WIDGET_LOGOUT_EVENT = 'zero-message-loss:agent-widget-logout';
const AGENT_WIDGET_SCRIPT_ID = 'zero-message-loss-agent-widget-script';

function normalizePathname(pathname: string): string {
  if (pathname.length > 1 && pathname.endsWith('/')) {
    return pathname.slice(0, -1);
  }
  return pathname;
}

function isLoginRoute(pathname: string): boolean {
  const normalizedPath = normalizePathname(pathname);
  return normalizedPath === '/login' || normalizedPath.startsWith('/login/');
}

function setAgentWidgetEmbedVisibility(visible: boolean): void {
  const display = visible ? '' : 'none';
  document
    .getElementById('aura-chat-widget-container')
    ?.style.setProperty('display', display);
  document
    .getElementById('aura-chat-widget-iframe-container')
    ?.style.setProperty('display', display);
}

function teardownAgentWidget(): void {
  document.getElementById(AGENT_WIDGET_SCRIPT_ID)?.remove();
  document.getElementById('aura-chat-widget-container')?.remove();
  document.getElementById('aura-chat-widget-iframe-container')?.remove();
}

function isAgentEmbedSubtreeRoot(node: HTMLElement): boolean {
  return (
    node.id === 'aura-chat-widget-container' ||
    node.id === 'aura-chat-widget-iframe-container' ||
    node.querySelector('#aura-chat-widget-container') !== null ||
    node.querySelector('#aura-chat-widget-iframe-container') !== null
  );
}

export function useAgentWidget({ enabled }: AgentWidgetProps): void {
  const onLogin = isLoginRoute(window.location.pathname);

  useEffect(() => {
    const onLogout = (): void => {
      teardownAgentWidget();
    };

    window.addEventListener(AGENT_WIDGET_LOGOUT_EVENT, onLogout);
    return () =>
      window.removeEventListener(AGENT_WIDGET_LOGOUT_EVENT, onLogout);
  }, []);

  useEffect(() => {
    if (onLogin || !enabled) {
      teardownAgentWidget();
      return undefined;
    }

    setAgentWidgetEmbedVisibility(true);

    if (document.getElementById(AGENT_WIDGET_SCRIPT_ID)) {
      return undefined;
    }

    const widgetScript = document.createElement('script');
    widgetScript.id = AGENT_WIDGET_SCRIPT_ID;
    widgetScript.src = env.agentWidgetScriptSrc;
    widgetScript.async = true;
    document.head.appendChild(widgetScript);

    return undefined;
  }, [enabled, onLogin]);

  useEffect(() => {
    if (!onLogin && enabled) {
      return undefined;
    }

    const observer = new MutationObserver((mutations) => {
      for (const mutation of mutations) {
        for (const node of Array.from(mutation.addedNodes)) {
          if (!(node instanceof HTMLElement)) {
            continue;
          }
          if (isAgentEmbedSubtreeRoot(node)) {
            teardownAgentWidget();
            return;
          }
        }
      }
    });

    observer.observe(document.body, { childList: true, subtree: true });
    teardownAgentWidget();

    return () => {
      observer.disconnect();
    };
  }, [enabled, onLogin]);
}
