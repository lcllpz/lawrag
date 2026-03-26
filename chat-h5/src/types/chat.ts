export interface Conversation {
  id: number;
  title: string;
  messageCount: number;
  lastActive: string;
  createTime: string;
}

export type MessageRole = "user" | "assistant";

export interface SourceRef {
  index: number;
  name: string;
  chapter?: string;
  pageNumber?: number;
  content: string;
  score?: number;
}

export interface Message {
  id: number;
  conversationId: number;
  role: MessageRole;
  content: string;
  sources: SourceRef[] | null;
  retrievalLog: any;
  feedback: number;
  isFallback: number;
  createTime: string;
}

// 前端 UI 状态扩展（SSE 流式占位用）
export interface ChatMessageUI {
  tempId: number | string;
  role: MessageRole;
  content: string;
  conversationId?: number;
  sources: SourceRef[] | null;
  retrievalLog: any;
  feedback: number;
  isFallback?: number;
  id?: number;
  isStreaming: boolean;
}
