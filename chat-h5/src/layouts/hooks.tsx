import { useEffect } from "react";

export const useConfigHooks = () => {
  useEffect(() => {
    // H5 视口：优先使用 100dvh，避免 iOS 地址栏收起导致的高度跳变
    // document.documentElement.style.height = "100dvh";
    // document.body.style.height = "100dvh";

    // 视口 meta：消灭“网页感”并禁用缩放（减少语音/WS 过程中误触）
    const viewportMeta = document.querySelector('meta[name="viewport"]');
    const content =
      "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover";
    if (viewportMeta) {
      viewportMeta.setAttribute("content", content);
    } else {
      const meta = document.createElement("meta");
      meta.name = "viewport";
      meta.content = content;
      document.head.appendChild(meta);
    }

    // Safe Area：适配刘海屏与底部小黑条
    document.documentElement.style.setProperty(
      "--safe-area-inset-top",
      "env(safe-area-inset-top)"
    );
    document.documentElement.style.setProperty(
      "--safe-area-inset-bottom",
      "env(safe-area-inset-bottom)"
    );
    document.documentElement.style.setProperty(
      "--safe-area-inset-left",
      "env(safe-area-inset-left)"
    );
    document.documentElement.style.setProperty(
      "--safe-area-inset-right",
      "env(safe-area-inset-right)"
    );

    // 手势治理（语音通话场景防误触）：屏蔽缩放手势与长按菜单等原生能力
    const prevent = (e: Event) => e.preventDefault();

    // 让 body 不参与滚动，避免 iOS 下拉刷新/橡皮筋从 body 触发
    document.body.style.overflow = "hidden";

    window.addEventListener("gesturestart", prevent as any, { passive: false });
    window.addEventListener("gesturechange", prevent as any, {
      passive: false,
    });
    window.addEventListener("gestureend", prevent as any, { passive: false });

    // 阻断双击缩放与长按菜单
    window.addEventListener("dblclick", prevent as any);
    window.addEventListener("contextmenu", prevent as any);

    // Pinch 缩放（多指）直接阻断
    const preventMultiTouch = (e: TouchEvent) => {
      if (e.touches.length > 1) e.preventDefault();
    };
    // 长按/选择：统一禁止选择与拖拽
    const preventSelect = (e: Event) => e.preventDefault();

    window.addEventListener("touchstart", preventMultiTouch, {
      passive: false,
    });
    window.addEventListener("touchmove", preventMultiTouch, { passive: false });
    document.addEventListener("selectstart", preventSelect as any);
    document.addEventListener("dragstart", preventSelect as any);

    const vv = window.visualViewport;
    if (!vv) return;
    const onVisualViewportResize = () => {
      const container = document.querySelector("#app") as HTMLElement;
      if (container) {
        // 核心：动态设置容器高度为当前“可视区域”的高度
        // 当键盘弹出时，vv.height 会自动变小
        container.style.height = `${vv.height}px`;
        // 键盘弹出，聊天列表自动滚到底部
        // const chatList = document.getElementById("chatList");
        // if (chatList) {
        //   chatList.scrollTop = chatList.scrollHeight;
        // }
        //         chatList.scrollTo({
        //   top: chatList.scrollHeight,
        //   behavior: 'smooth'
        // });
      }
    };
    vv.addEventListener("resize", onVisualViewportResize);

    return () => {
      window.removeEventListener("gesturestart", prevent as any);
      window.removeEventListener("gesturechange", prevent as any);
      window.removeEventListener("gestureend", prevent as any);
      window.removeEventListener("dblclick", prevent as any);
      window.removeEventListener("contextmenu", prevent as any);

      window.removeEventListener("touchstart", preventMultiTouch as any);
      window.removeEventListener("touchmove", preventMultiTouch as any);
      document.removeEventListener("selectstart", preventSelect as any);
      document.removeEventListener("dragstart", preventSelect as any);

      if (!vv) return;
      vv.removeEventListener("resize", onVisualViewportResize as any);
    };
  }, []);
};
