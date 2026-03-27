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
      target: "http://localhost:8080",
      changeOrigin: true,
      // ws: true,
      // pathRewrite: { "^/api": "" },
    },
  },
  // https: {
  //   hosts: ["localhost", "127.0.0.1", "192.168.1.10"],
  //   http2: false,
  // },
  // https: {},
});
