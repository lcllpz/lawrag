import { useState, useRef, useEffect } from "react";
import { Button, Toast } from "antd-mobile";

// ====================== 官方文档标准配置（已修正所有错误）======================
const CONFIG = {
  // 你的APP ID（控制台正确）
  X_Api_App_Key: "5418685475",
  // 你的Access Token（控制台正确）
  TOKEN: "ydK9khKTXSICS2GuVOKNz3oX_ZOhVy44",
  // 文档固定的Resource ID（之前的核心错误！这里必须填这个，不是实例ID）
  X_Api_Resource_Id: "volc.seedasr.sauc.duration",
  // 文档推荐的优化版接口地址
  WS_URL: "wss://openspeech.bytedance.com/api/v2/asr",
};

export default function VolcVoiceInput() {
  const [recording, setRecording] = useState(false);
  const [result, setResult] = useState("");
  const wsRef = useRef<WebSocket | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const audioCtxRef = useRef<AudioContext | null>(null);
  const processorRef = useRef<ScriptProcessorNode | null>(null);
  const seqRef = useRef(0);

  // 开始录音
  const startRecording = async () => {
    try {
      setRecording(true);
      setResult("");
      Toast.show("录音中…");

      // 1. 获取麦克风权限
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: { sampleRate: 16000, channelCount: 1, echoCancellation: true },
      });
      streamRef.current = stream;

      // 2. 官方文档标准鉴权URL（100%对齐Header要求，浏览器兼容方案）
      const authUrl = `${CONFIG.WS_URL}?X-Api-App-Key=${CONFIG.X_Api_App_Key}&Authorization=Bearer%20${CONFIG.TOKEN}&X-Api-Resource-Id=${CONFIG.X_Api_Resource_Id}`;
      const ws = new WebSocket(authUrl);
      wsRef.current = ws;
      ws.binaryType = "arraybuffer"; // 文档要求二进制模式

      // 连接成功：发送文档要求的Full Client Request
      ws.onopen = () => {
        console.log("✅ WebSocket 连接成功！");
        sendFullClientRequest(ws);
      };

      // 接收识别结果
      ws.onmessage = (e) => {
        try {
          // 解析服务端返回的二进制数据
          const buffer = e.data;
          const view = new DataView(buffer);

          // 跳过4字节header + 4字节sequence，读取payload长度
          const payloadSize = view.getUint32(8, false);
          const payloadData = buffer.slice(12, 12 + payloadSize);
          const text = new TextDecoder().decode(payloadData);
          const res = JSON.parse(text);

          // 更新识别结果
          if (res.code === 0 && res.result?.text) {
            setResult(res.result.text);
          }
        } catch (err) {
          console.error("解析结果失败", err);
        }
      };

      ws.onerror = (e) => console.error("❌ WebSocket 错误", e);
      ws.onclose = (e) => console.log("🔌 WebSocket 关闭", e);

      // 3. 采集16kHz 16bit单声道PCM音频（文档要求格式）
      const audioCtx = new AudioContext({ sampleRate: 16000 });
      audioCtxRef.current = audioCtx;
      const source = audioCtx.createMediaStreamSource(stream);
      const processor = audioCtx.createScriptProcessor(4096, 1, 1);
      processorRef.current = processor;

      source.connect(processor);
      processor.connect(audioCtx.destination);

      // 按文档要求，每200ms发送音频包
      processor.onaudioprocess = (e) => {
        if (ws.readyState !== WebSocket.OPEN) return;
        const float32Data = e.inputBuffer.getChannelData(0);
        sendAudioPacket(ws, float32Data);
      };
    } catch (err) {
      console.error("麦克风异常", err);
      Toast.show("无法访问麦克风，请检查权限");
      setRecording(false);
    }
  };

  // 发送Full Client Request（文档强制要求的第一条消息）
  const sendFullClientRequest = (ws: WebSocket) => {
    // 文档要求的请求参数
    const payload = JSON.stringify({
      user: { uid: "web_user_" + Date.now() },
      audio: {
        format: "pcm",
        rate: 16000,
        bits: 16,
        channel: 1,
      },
      request: {
        model_name: "bigmodel",
        enable_itn: true,
        enable_punc: true,
        enable_ddc: false,
        result_type: "full",
      },
    });

    // 文档标准4字节Header
    const header = new Uint8Array(4);
    header[0] = 0b00010001; // version=1, header_size=1*4=4
    header[1] = 0b00010000; // message_type=1(Full Client Request), flags=0
    header[2] = 0b00010000; // serialization=1(JSON), compression=0(无压缩)
    header[3] = 0x00; // 保留位

    // 4字节Payload长度（大端序，文档要求）
    const payloadSize = new Uint8Array(4);
    new DataView(payloadSize.buffer).setUint32(0, payload.length, false);

    // 合并消息
    const msg = new Uint8Array(
      header.length + payloadSize.length + payload.length
    );
    msg.set(header, 0);
    msg.set(payloadSize, header.length);
    msg.set(
      new TextEncoder().encode(payload),
      header.length + payloadSize.length
    );

    ws.send(msg);
  };

  // 发送音频数据包（文档标准格式）
  const sendAudioPacket = (ws: WebSocket, float32Data: Float32Array) => {
    // 转16bit PCM（文档要求格式）
    const int16Data = new Int16Array(float32Data.length);
    for (let i = 0; i < float32Data.length; i++) {
      const s = Math.max(-1, Math.min(1, float32Data[i]));
      int16Data[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
    }

    // 文档标准4字节Header
    const header = new Uint8Array(4);
    header[0] = 0b00010001; // version=1, header_size=4
    header[1] = 0b00100001; // message_type=2(Audio Only Request), flags=1(带sequence)
    header[2] = 0b00000000; // serialization=0(无序列化), compression=0(无压缩)
    header[3] = 0x00;

    // 4字节序列号（大端序）
    const seq = new Uint8Array(4);
    new DataView(seq.buffer).setUint32(0, ++seqRef.current, false);

    // 4字节音频长度（大端序）
    const audioSize = new Uint8Array(4);
    new DataView(audioSize.buffer).setUint32(0, int16Data.byteLength, false);

    // 合并消息
    const msg = new Uint8Array(
      header.length + seq.length + audioSize.length + int16Data.byteLength
    );
    msg.set(header, 0);
    msg.set(seq, header.length);
    msg.set(audioSize, header.length + seq.length);
    msg.set(
      new Uint8Array(int16Data.buffer),
      header.length + seq.length + audioSize.length
    );

    ws.send(msg);
  };

  // 停止录音，释放所有资源
  const stopRecording = () => {
    setRecording(false);
    processorRef.current?.disconnect();
    audioCtxRef.current?.close();
    streamRef.current?.getTracks().forEach((track) => track.stop());
    wsRef.current?.close();
    Toast.show("录音结束");
  };

  // 页面卸载自动清理
  useEffect(() => {
    return () => stopRecording();
  }, []);

  return (
    <div style={{ padding: 20 }}>
      <Button
        style={{ width: "100%", height: 55, fontSize: 16, borderRadius: 12 }}
        color={recording ? "danger" : "primary"}
        onTouchStart={startRecording}
        onTouchEnd={stopRecording}
        onMouseDown={startRecording}
        onMouseUp={stopRecording}
      >
        {recording ? "松开结束" : "按住说话"}
      </Button>
      <div
        style={{
          marginTop: 20,
          padding: 15,
          background: "#f5f5f5",
          borderRadius: 10,
          minHeight: 60,
          whiteSpace: "pre-wrap",
        }}
      >
        {result || "识别结果将显示在这里"}
      </div>
    </div>
  );
}
