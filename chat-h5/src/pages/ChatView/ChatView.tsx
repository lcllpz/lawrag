import { useEffect, useId, useRef, useState } from "react";
import { Button, Popup, TextArea, Toast } from "antd-mobile";
import { marked } from "marked";
import { chatApi } from "@/services/chat";
import { favoriteApi } from "@/services/favorite";
import { templateApi } from "@/services/template";
import type {
  ChatMessageUI,
  Conversation,
  SourceRef,
  MessageRole,
} from "@/types/chat";
import { v4 as uuidv4 } from "uuid";
import styles from "./ChatView.less";
import { useChatAutoScroll } from "./hooks";
import VoiceInput from "./components/Voice";
import { PhonebookOutline } from "antd-mobile-icons";
import Login from "./components/Login";

type ChatMessageUIWithUI = ChatMessageUI & { showSources?: boolean };

const safeJsonParse = (value: any) => {
  if (value == null) return null;
  if (typeof value !== "string") return value;
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
};

const normalizeSources = (sources: any): SourceRef[] | null => {
  const parsed = safeJsonParse(sources);
  if (!Array.isArray(parsed)) return null;
  return parsed as SourceRef[];
};

const normalizeRetrievalLog = (retrievalLog: any): any => {
  const parsed = safeJsonParse(retrievalLog);
  return parsed ?? null;
};

export default function ChatView() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [convLoading, setConvLoading] = useState(false);
  const [currentConvId, setCurrentConvId] = useState<number | null>(null);

  const [messages, setMessages] = useState<ChatMessageUIWithUI[]>([]);

  const { containerRef, bottomRef, handleScroll, scrollToBottomAfterSend } =
    useChatAutoScroll(messages, {
      bottomOffset: 40,
    });

  const [inputText, setInputText] = useState("");
  const [isStreaming, setIsStreaming] = useState(false);
  const [isRecording, setIsRecording] = useState(false);

  const messagesContainerRef = useRef<HTMLDivElement | null>(null);

  const mediaStreamRef = useRef<MediaStream | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const scriptProcessorRef = useRef<ScriptProcessorNode | null>(null);
  const speechWsRef = useRef<WebSocket | null>(null);
  const actualRateRef = useRef<number>(0);
  const voiceConfirmedTextRef = useRef<string>(""); // 与后端 confirmedText 语义对齐（客户端兜底）

  // 移动端会话抽屉（窄屏时隐藏左侧栏）
  const [sidebarDrawerVisible, setSidebarDrawerVisible] = useState(false);

  // ===== 快捷模板 =====
  const [quickTemplates, setQuickTemplates] = useState<
    Array<{ id?: number; title: string; content: string; category: string }>
  >([]);
  const [quickPanelVisible, setQuickPanelVisible] = useState(false);

  // ===== 收藏/反馈 =====
  const [favoritedMsgIds, setFavoritedMsgIds] = useState<Set<number>>(
    new Set()
  );
  const [favoriteIdMap, setFavoriteIdMap] = useState<Map<number, number>>(
    new Map()
  ); // messageId -> favoriteId

  // ===== 检索日志 =====
  const [retrievalLogVisible, setRetrievalLogVisible] = useState(false);
  const [currentRetrievalLog, setCurrentRetrievalLog] = useState<any>(null);

  // const scrollToBottom = () => {
  //   const el = messagesContainerRef.current;

  //   if (!el) return;
  //   console.log("el", el.scrollHeight);

  //   el.scrollTo({
  //     top: el.scrollHeight,
  //     behavior: "smooth",
  //   });
  // };

  const loadConversations = async () => {
    setConvLoading(true);
    try {
      const res = await chatApi.listConversations({ size: 50 });
      setConversations(res?.records || []);
    } catch (e: any) {
      Toast.show(e?.message || "加载会话失败");
    } finally {
      setConvLoading(false);
    }
  };

  const loadHistory = async (convId: number) => {
    setConvLoading(true);
    try {
      const res = await chatApi.getHistory(convId, { size: 100 });
      const records = res?.records || [];
      setMessages(
        records.map((m: any) => ({
          id: m.id,
          conversationId: m.conversationId,
          role: m.role as MessageRole,
          content: m.content || "",
          sources: normalizeSources(m.sources),
          retrievalLog: normalizeRetrievalLog(m.retrievalLog),
          feedback: m.feedback ?? 0,
          isFallback: m.isFallback ?? 0,
          createTime: m.createTime,
          tempId: Number.isFinite(m.id) ? Number(m.id) : Date.now(),
          isStreaming: false,
          showSources: false,
        }))
      );
      setCurrentConvId(convId);
    } catch (e: any) {
      Toast.show(e?.message || "加载历史失败");
    } finally {
      setConvLoading(false);
    }
  };

  const handleSelectConversation = (convId: number) => {
    if (isStreaming) {
      Toast.show("生成中，请稍候");
      return;
    }
    setSidebarDrawerVisible(false);
    loadHistory(convId);
  };

  useEffect(() => {
    loadConversations();
    loadTemplates();
    loadFavoriteStatus();
    return () => {};
  }, []);

  const loadTemplates = async () => {
    try {
      const list = await templateApi.getEnabled();
      setQuickTemplates(list || []);
    } catch {
      // 忽略模板加载失败
    }
  };

  const loadFavoriteStatus = async () => {
    try {
      const res: any = await favoriteApi.getList({ current: 1, size: 200 });
      const records: any[] = res?.records || [];
      const nextMsgIds = new Set<number>();
      const nextIdMap = new Map<number, number>();
      for (const fav of records) {
        nextMsgIds.add(fav.messageId);
        nextIdMap.set(fav.messageId, fav.id);
      }
      setFavoritedMsgIds(nextMsgIds);
      setFavoriteIdMap(nextIdMap);
    } catch {
      // 忽略收藏拉取失败
    }
  };

  const newConversation = () => {
    setCurrentConvId(null);
    setMessages([]);
    setInputText("");
    setIsStreaming(false);
  };

  const deleteConversation = async (convId: number) => {
    if (!window.confirm("确认删除该对话？")) return;
    try {
      await chatApi.deleteConversation(convId);
      Toast.show("已删除");
      if (currentConvId === convId) newConversation();
      await loadConversations();
    } catch (e: any) {
      Toast.show(e?.message || "删除失败");
    }
  };

  const sendMessage = async (text: string) => {
    const content = text.trim();
    if (!content || isStreaming) return;
    if (!navigator.onLine) {
      Toast.show("当前离线，无法发送");
      return;
    }

    setInputText("");
    setIsStreaming(true);

    const userMsgTempId = uuidv4();
    const assistantTempId = uuidv4();

    const userMsg: ChatMessageUIWithUI = {
      tempId: userMsgTempId,
      role: "user",
      content,
      sources: null,
      retrievalLog: null,
      feedback: 0,
      isStreaming: false,
      showSources: false,
      conversationId: currentConvId ?? undefined,
    };

    const assistantMsg: ChatMessageUIWithUI = {
      tempId: assistantTempId,
      role: "assistant",
      content: "",
      sources: null,
      retrievalLog: null,
      feedback: 0,
      isStreaming: true,
      showSources: false,
      conversationId: currentConvId ?? undefined,
    };
    scrollToBottomAfterSend();

    setMessages((prev) => [...prev, userMsg, assistantMsg]);
    // debugger;

    // 建立 SSE 连接
    const params = new URLSearchParams({ message: content });
    if (currentConvId) params.append("conversationId", String(currentConvId));

    const token = localStorage.getItem("token") || "";

    const esUrl = `/api/chat/stream?${params.toString()}&token=${token}`;
    // evtSourceRef.current?.close();
    const evt = new EventSource(esUrl);
    // evtSourceRef.current = evt;

    const onToken = (e: MessageEvent) => {
      const data = safeJsonParse(e.data) || {};
      const delta: string = data.content || "";
      if (!delta) return;
      console.log("delta", delta);

      setMessages((prev) =>
        prev.map((m) =>
          m.tempId === assistantTempId
            ? { ...m, content: m.content + delta }
            : m
        )
      );
    };

    const onDone = (e: MessageEvent) => {
      const data = safeJsonParse(e.data) || {};
      const nextSources = normalizeSources(data.sources);
      const nextRetrievalLog = normalizeRetrievalLog(data.retrievalLog);

      setMessages((prev) =>
        prev.map((m) =>
          m.tempId === assistantTempId
            ? {
                ...m,
                isStreaming: false,
                id: data.messageId,
                sources: nextSources,
                retrievalLog: nextRetrievalLog,
                conversationId: data.conversationId ?? m.conversationId,
              }
            : m
        )
      );

      const nextConvId = data.conversationId;
      if (nextConvId) setCurrentConvId(nextConvId);

      setIsStreaming(false);
      evt.close();
      // evtSourceRef.current = null;
      loadConversations().catch(() => {});
    };

    const onError = (err) => {
      console.log("err", err);

      setMessages((prev) =>
        prev.map((m) =>
          m.tempId === assistantTempId
            ? {
                ...m,
                isStreaming: false,
                content: m.content || "抱歉，生成失败，请重试。",
              }
            : m
        )
      );
      setIsStreaming(false);
      // evtSourceRef.current?.close();
      // evtSourceRef.current = null;
      // Toast.show("生成失败，请重试");
    };

    evt.addEventListener("token", onToken as any);
    evt.addEventListener("done", onDone as any);
    evt.addEventListener("error", onError as any);
  };

  /** 线性插值降采样：把 inputRate 的 Float32 降到 16000Hz */
  const downsampleTo16k = (
    buf: Float32Array,
    inputRate: number
  ): Float32Array => {
    if (inputRate === 16000) return buf;
    const ratio = inputRate / 16000;
    const outLen = Math.floor(buf.length / ratio);
    const out = new Float32Array(outLen);
    for (let i = 0; i < outLen; i++) {
      const pos = i * ratio;
      const lo = Math.floor(pos);
      const hi = Math.min(lo + 1, buf.length - 1);
      out[i] = buf[lo] + (buf[hi] - buf[lo]) * (pos - lo);
    }
    return out;
  };

  const startVoiceRecording = async () => {
    // try {
    //   mediaStreamRef.current = await navigator.mediaDevices.getUserMedia({
    //     audio: true,
    //   });
    // } catch {
    //   Toast.show("无法访问麦克风，请检查浏览器权限设置");
    //   return;
    // }
    // const stream = mediaStreamRef.current;
    // if (!stream) return;
    // // 使用设备原生采样率，避免要求 16000Hz 后静默回退
    // const audioContext = new AudioContext();
    // audioContextRef.current = audioContext;
    // actualRateRef.current = audioContext.sampleRate;
    // const source = audioContext.createMediaStreamSource(stream);
    // const scriptProcessor = audioContext.createScriptProcessor(4096, 1, 1);
    // scriptProcessorRef.current = scriptProcessor;
    // const token = localStorage.getItem("token") || "";
    // const wsUrl = `${location.protocol === "https:" ? "wss" : "ws"}://${
    //   location.host
    // }/api/speech/ws?token=${encodeURIComponent(token)}`;
    // const speechWs = new WebSocket(wsUrl);
    // speechWs.binaryType = "arraybuffer";
    // speechWsRef.current = speechWs;
    // voiceConfirmedTextRef.current = "";
    // speechWs.onopen = () => {
    //   setIsRecording(true);
    //   // 将实际采样率告知后端，由后端填入 DashScope run-task
    //   speechWs.send(`START:${actualRateRef.current}`);
    // };
    // speechWs.onmessage = (e) => {
    //   try {
    //     const data = JSON.parse(e.data as string);
    //     if (data.type === "started") {
    //       // 服务就绪，前端开始录音发送（scriptProcessor 已在跑）
    //     } else if (data.type === "transcript" && data.text != null) {
    //       // 实时更新输入框
    //       setInputText(data.text);
    //       if (data.isFinal) {
    //         voiceConfirmedTextRef.current = data.text;
    //       }
    //     } else if (data.type === "finished") {
    //       setInputText(data.text || voiceConfirmedTextRef.current);
    //       setIsRecording(false);
    //     } else if (data.type === "error") {
    //       Toast.show(data.message || "语音识别出错");
    //       setIsRecording(false);
    //     }
    //   } catch {
    //     // 非 JSON 忽略
    //   }
    // };
    // speechWs.onclose = () => {
    //   setIsRecording(false);
    // };
    // speechWs.onerror = () => {
    //   Toast.show("语音识别服务连接失败");
    //   setIsRecording(false);
    // };
    // // 音频处理：Float32 -> 降采样(16k) -> Int16 PCM -> WS 发送
    // scriptProcessor.onaudioprocess = (e: AudioProcessingEvent) => {
    //   if (speechWsRef.current?.readyState !== WebSocket.OPEN) return;
    //   const raw = e.inputBuffer.getChannelData(0);
    //   const resampled = downsampleTo16k(raw, actualRateRef.current);
    //   const int16 = new Int16Array(resampled.length);
    //   for (let i = 0; i < resampled.length; i++) {
    //     const s = Math.max(-1, Math.min(1, resampled[i]));
    //     int16[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
    //   }
    //   speechWsRef.current.send(int16.buffer);
    // };
    // source.connect(scriptProcessor);
    // // 连接到 destination 防止 Chrome 自动垃圾回收（静音输出）
    // scriptProcessor.connect(audioContext.destination);
  };

  const stopVoiceRecording = () => {
    const ws = speechWsRef.current;
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send("STOP");
      // 短暂等待 finish-task 结果后关闭
      setTimeout(() => ws.close(), 800);
    }

    scriptProcessorRef.current?.disconnect();
    audioContextRef.current?.close().catch(() => {});
    mediaStreamRef.current?.getTracks().forEach((t) => t.stop());

    scriptProcessorRef.current = null;
    audioContextRef.current = null;
    mediaStreamRef.current = null;
    speechWsRef.current = null;
    setIsRecording(false);
  };

  const toggleVoice = async () => {
    if (isRecording) stopVoiceRecording();
    else await startVoiceRecording();
  };

  useEffect(() => {
    return () => {
      stopVoiceRecording();
    };
  }, []);

  const handleSend = () => {
    if (isRecording) stopVoiceRecording();
    sendMessage(inputText);
  };

  const copyContent = async (content: string) => {
    try {
      await navigator.clipboard.writeText(content);
      Toast.show("已复制");
    } catch {
      // 兜底：只要复制成功体验即可
      const textarea = document.createElement("textarea");
      textarea.value = content;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand("copy");
      document.body.removeChild(textarea);
      Toast.show("已复制");
    }
  };

  const submitFeedback = async (msg: any, rating: number) => {
    if (!msg?.id) return;
    try {
      await chatApi.submitFeedback(msg.id, rating);
      setMessages((prev) =>
        prev.map((m) =>
          m.tempId === msg.tempId ? { ...m, feedback: rating } : m
        )
      );
      Toast.show(rating === 1 ? "感谢您的反馈！" : "已记录，我们会持续改进");
    } catch {
      Toast.show("反馈失败");
    }
  };

  const toggleFavorite = async (msg: any) => {
    if (!msg?.id || !msg?.conversationId) return;

    const isFav = favoritedMsgIds.has(msg.id);
    if (isFav) {
      const favId = favoriteIdMap.get(msg.id);
      if (!favId) return;
      try {
        await favoriteApi.remove(favId);
        setFavoritedMsgIds((prev) => {
          const next = new Set(prev);
          next.delete(msg.id);
          return next;
        });
        setFavoriteIdMap((prev) => {
          const next = new Map(prev);
          next.delete(msg.id);
          return next;
        });
        Toast.show("已取消收藏");
      } catch {
        Toast.show("取消收藏失败");
      }
    } else {
      try {
        await favoriteApi.add({
          messageId: msg.id,
          conversationId: msg.conversationId,
        });
        // 由于 add 接口没有返回 favoriteId，这里简单刷新收藏列表以拿到 id
        await loadFavoriteStatus();
        Toast.show("收藏成功");
      } catch {
        Toast.show("收藏失败");
      }
    }
  };

  const showRetrievalLog = (msg: any) => {
    setCurrentRetrievalLog(msg?.retrievalLog || null);
    setRetrievalLogVisible(true);
  };

  const renderMarkdown = (content: string) => {
    if (!content) return "";
    return marked.parse(content) as string;
  };

  useEffect(() => {
    const vv = window.visualViewport;
    if (!vv) return;

    const onVisualViewportResize = () => {
      const container = document.querySelector("#ChatView") as HTMLElement;
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
    return () => vv.removeEventListener("resize", onVisualViewportResize);
  }, []);
  return (
    <div id="ChatView" className={styles.ChatView}>
      <div className={styles.mobileTopBar}>
        <span className={styles.mobileTopBarTitle}>AI招⽣智能体</span>
        <img src="/CSR.svg" alt="" />
      </div>
      <div className={styles.main} ref={containerRef} onScroll={handleScroll}>
        {messages.length === 0 ? (
          <div className={styles.welcome}>
            {quickTemplates.length > 0 ? (
              <div className={styles.quickQuestions}>
                {quickTemplates.slice(0, 5).map((tpl) => (
                  <div
                    key={tpl.id ?? tpl.content}
                    className={styles.quickQItem}
                    onClick={() => {
                      if (isRecording) {
                        Toast.show("正在录音中，请先停止");
                        return;
                      }
                      sendMessage(tpl.content);
                    }}
                  >
                    <span className={styles.quickQMark}>§</span>
                    {tpl.title}
                  </div>
                ))}
              </div>
            ) : null}
          </div>
        ) : (
          <div className={styles.messages} ref={messagesContainerRef}>
            {messages.map((msg) => {
              if (msg.role === "user") {
                return (
                  <div key={msg.tempId} className={styles.userMsg}>
                    <div className={styles.userBubble}>{msg.content}</div>
                  </div>
                );
              }

              return (
                <div key={msg.tempId} className={styles.aiMsg}>
                  <div className={styles.aiAvatar}>A</div>
                  <div className={styles.aiBody}>
                    <div
                      className={styles.aiBubble}
                      dangerouslySetInnerHTML={{
                        __html: renderMarkdown(msg.content),
                      }}
                    />

                    {msg.sources &&
                    msg.sources.length > 0 &&
                    !msg.isStreaming ? (
                      <div className={styles.sourcesPanel}>
                        <div className={styles.sourcesHeader}>
                          参考文献（{msg.sources.length}）
                          <Button
                            size="small"
                            fill="none"
                            color="primary"
                            className={styles.sourcesToggle}
                            onClick={() =>
                              setMessages((prev) =>
                                prev.map((m) =>
                                  m.tempId === msg.tempId
                                    ? { ...m, showSources: !m.showSources }
                                    : m
                                )
                              )
                            }
                          >
                            {msg.showSources ? "收起" : "展开"}
                          </Button>
                        </div>
                        {msg.showSources ? (
                          <div className={styles.sourcesList}>
                            {msg.sources.slice(0, 5).map((src, i) => (
                              <div key={i} className={styles.sourceItem}>
                                <div className={styles.sourceIndex}>
                                  [{src.index}]
                                </div>
                                <div className={styles.sourceContent}>
                                  {src.content}
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : null}
                      </div>
                    ) : null}

                    {!msg.isStreaming && (
                      <div className={styles.messageActions}>
                        <button
                          className={styles.actionBtn}
                          onClick={() => submitFeedback(msg, 1)}
                        >
                          有用
                        </button>
                        <button
                          className={styles.actionBtn}
                          onClick={() => submitFeedback(msg, -1)}
                        >
                          没用
                        </button>
                        <button
                          className={styles.actionBtn}
                          onClick={() => showRetrievalLog(msg)}
                        >
                          检索过程
                        </button>
                        <button
                          className={styles.actionBtn}
                          onClick={() => copyContent(msg.content)}
                        >
                          复制
                        </button>
                        <button
                          className={styles.actionBtn}
                          style={{
                            color: favoritedMsgIds.has(msg.id as number)
                              ? "#b8943f"
                              : undefined,
                          }}
                          onClick={() => toggleFavorite(msg)}
                        >
                          {favoritedMsgIds.has(msg.id as number)
                            ? "取消收藏"
                            : "收藏"}
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              );
            })}

            {isStreaming ? (
              <div className={styles.streamingDots}>...</div>
            ) : null}
            {/* 占位 */}
            <div ref={bottomRef} />
          </div>
        )}
      </div>
      <div className={styles.footerBox}>
        <div className={styles.ChatInputBox}>
          <TextArea
            className={styles.inputArea}
            value={inputText}
            placeholder={
              isRecording
                ? "正在聆听，请说话..."
                : isStreaming
                ? "正在生成中，请稍候..."
                : "有什么问题尽管问我~"
            }
            rows={1}
            autoSize={{ minRows: 1, maxRows: 6 }}
            disabled={isStreaming || isRecording}
            onChange={(v: string) => setInputText(v)}
            // onKeyDown={(e: any) => {
            //   if (e.key === "Enter" && !e.shiftKey) {
            //     e.preventDefault();
            //     handleSend();
            //   }
            // }}
          />
          <div className={styles.sendAction}>
            {/* <Button
            size="small"
            fill="none"
            color="primary"
            onClick={() => setQuickPanelVisible(true)}
            disabled={isStreaming || isRecording}
          >
            快捷模板
          </Button> */}
            <div className={styles.left}>
              <div className={styles.item}>
                <img src="/phone.svg" alt="" />
                <span>AI通话</span>
              </div>
              <div className={styles.itemGap}> </div>
              <div className={styles.item}>
                <img src="/msg.svg" alt="" />
                <span>信息录入</span>
              </div>
            </div>
            <div className={styles.fight}>
              <img src="/speech.svg" alt="" />
              <img src="/send.svg" alt="" />
              {/* <Button
              size="mini"
              fill="none"
              color={isRecording ? "danger" : "primary"}
              disabled={isStreaming}
              onClick={toggleVoice}
              className={
                isRecording ? styles.voiceButtonRecording : styles.voiceButton
              }
            >
              {isRecording ? "停止录音" : "语音输入"}
            </Button> */}
              {/* <Button
              color="primary"
              fill="none"
              disabled={!inputText.trim() || isStreaming}
              onClick={handleSend}
              size="mini"
            >
              发送
            </Button> */}
            </div>
          </div>
        </div>
        <div className={styles.agreement}>
          您同意用户协议和隐私政策 内容由AI生成
        </div>
      </div>

      <Login isShow={true}></Login>
    </div>
  );
}
