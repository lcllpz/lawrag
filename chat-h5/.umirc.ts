import { defineConfig } from "umi";
import path from "path";

export default defineConfig({
  routes: [
    { path: "/", component: "index" },
    { path: "/docs", component: "docs" },
    { path: "/login", component: "@/pages/login" },
  ],
  npmClient: "pnpm",
  alias: {
    "@": path.resolve(__dirname, "../src"),
  },
  proxy: {
    "/api": {
      // target: "http://localhost:8080",
      target: "http://192.168.36.107:8080",
      changeOrigin: true,
      // ws: true,
      // pathRewrite: { "^/api": "" },
    },
    // "/wsApi": {
    //   target: "ws://192.168.36.107:8080",
    //   ws: true, // 关键：开启WebSocket代理
    //   changeOrigin: true,
    // },
  },
  // https: {
  //   hosts: ["localhost", "127.0.0.1", "192.168.1.10"],
  //   http2: false,
  // },
  // https: {},
});
