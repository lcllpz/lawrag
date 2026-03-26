import { useEffect, useLayoutEffect, useRef, useState } from "react";

type ScrollBehaviorType = "auto" | "smooth";

interface MessageLike {
  id?: string | number;
  content?: string;
}

interface UseChatAutoScrollOptions {
  bottomOffset?: number;
}

export function useChatAutoScroll<T extends MessageLike>(
  messages: T[],
  options: UseChatAutoScrollOptions = {}
) {
  const { bottomOffset = 40 } = options;

  const containerRef = useRef<HTMLDivElement | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  const initializedRef = useRef(false);
  const shouldAutoScrollRef = useRef(true);

  const [isAtBottom, setIsAtBottom] = useState(true);

  const lastMessage = messages[messages.length - 1];
  const lastMessageContent = lastMessage?.content;

  function getDistanceToBottom() {
    const el = containerRef.current;
    if (!el) return 0;
    return el.scrollHeight - el.scrollTop - el.clientHeight;
  }
  // 如果离底部小于某个阈值，就认为“已经在底部了”
  // 会出问题：

  // 有 padding
  // 有 gap
  // 有图片加载
  // 有小数像素
  // iOS 滚动误差

  // 👉 几乎永远不会刚好等于 0
  function checkIsAtBottom() {
    return getDistanceToBottom() <= bottomOffset;
  }

  function scrollToBottom(behavior: ScrollBehaviorType = "auto") {
    const bottomEl = bottomRef.current;
    if (bottomEl) {
      bottomEl.scrollIntoView({
        behavior,
        block: "end",
      });
      return;
    }

    const el = containerRef.current;
    if (!el) return;
    el.scrollTo({
      top: el.scrollHeight,
      behavior,
    });
  }

  function handleScroll() {
    const atBottom = checkIsAtBottom();
    shouldAutoScrollRef.current = atBottom;
    setIsAtBottom(atBottom);
  }

  // 1. 首次进入：无动画到底
  useLayoutEffect(() => {
    if (initializedRef.current) return;
    if (!messages.length) return;

    initializedRef.current = true;

    const el = containerRef.current;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }

    shouldAutoScrollRef.current = true;
    setIsAtBottom(true);
  }, [messages.length]);

  // 2 & 3. 消息变化
  useEffect(() => {
    if (!initializedRef.current) return;

    if (shouldAutoScrollRef.current) {
      scrollToBottom("auto");
    }
  }, [lastMessageContent]);

  // 2. 用户发送消息后，你可以手动调用这个，强制到底
  function scrollToBottomAfterSend() {
    shouldAutoScrollRef.current = true;
    setIsAtBottom(true);
    scrollToBottom("smooth");
  }

  return {
    containerRef,
    bottomRef,
    handleScroll,
    isAtBottom,
    scrollToBottom,
    scrollToBottomAfterSend,
  };
}
