import { useState, useRef, useEffect, useMemo } from "react";
import { Button } from "antd-mobile";
import "./utils/speechrecognizer";
import { signCallback, config } from "./utils/asrauthentication";

const params = {
  signCallback: signCallback, // 鉴权函数，若直接使用默认鉴权函数。可不传此参数
  // 用户参数
  secretid: config.secretId,
  secretkey: config.secretKey,
  appid: config.appId,
  // 临时密钥参数，非必填
  // token: config.token,
  // 实时识别接口参数
  engine_model_type: "16k_zh", // 因为内置WebRecorder采样16k的数据，所以参数 engineModelType 需要选择16k的引擎，为 '16k_zh'
  // 以下为非必填参数，可跟据业务自行修改
  voice_format: 1,
  hotword_id: "08003a00000000000000000000000000",
  needvad: 1,
  filter_dirty: 1,
  filter_modal: 2,
  filter_punc: 0,
  convert_num_mode: 1,
  word_info: 2,
};
export default function VolcVoiceInput() {
  const [result, setResult] = useState("");
  const speechRecognizer = useRef<any>(null);
  // start ：开始识别/ 结束
  // connecting 建立连接中
  // recognizing 识别中
  const [status, setStatus] = useState<"start" | "connecting" | "recognizing">(
    "start"
  );
  const finalTextRef = useRef("");
  // 开始录音
  const startRecording = async () => {
    finalTextRef.current = "";
    const webAudioSpeechRecognizer = new WebAudioSpeechRecognizer(params);
    speechRecognizer.current = webAudioSpeechRecognizer;
    console.log("speechRecognizer.current ", speechRecognizer.current);

    webAudioSpeechRecognizer.OnRecognitionStart = (res: any) => {
      setStatus("connecting");
    };
    // 一句话开始
    webAudioSpeechRecognizer.OnSentenceBegin = (res: any) => {
      setStatus("recognizing");
    };
    // 识别变化时
    webAudioSpeechRecognizer.OnRecognitionResultChange = (res: any) => {
      setResult(finalTextRef.current + res.result.voice_text_str);
    };
    // 一句话结束
    webAudioSpeechRecognizer.OnSentenceEnd = (res: any) => {
      finalTextRef.current += res.result.voice_text_str;
      setResult(finalTextRef.current);
    };
    // 识别结束
    webAudioSpeechRecognizer.OnRecognitionComplete = (res: any) => {};
    // 识别错误
    webAudioSpeechRecognizer.OnError = (res: any) => {
      endRecording();
    };
    webAudioSpeechRecognizer.start();
  };

  const endRecording = () => {
    if (speechRecognizer.current) {
      speechRecognizer.current.stop();
      speechRecognizer.current = null;
      setStatus("start");
      finalTextRef.current = "";
    }
  };
  return (
    <div style={{ padding: 20 }}>
      <Button
        style={{ width: "100%", height: 55, fontSize: 16, borderRadius: 12 }}
        // color={recording ? "danger" : "primary"}
        onClick={startRecording}
      >
        {status === "start"
          ? "开始识别"
          : status === "connecting"
          ? "建立连接中"
          : "识别中"}
      </Button>
      <Button
        style={{ width: "100%", height: 55, fontSize: 16, borderRadius: 12 }}
        // color={recording ? "danger" : "primary"}
        onClick={endRecording}
      >
        结束
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
