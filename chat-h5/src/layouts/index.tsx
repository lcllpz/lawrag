// import { Outlet } from "umi";
// import { useEffect } from "react";
import { useEffect } from "react";
import "./index.less";
import styles from "./index.less";
import "./global";
// import "antd-mobile/bundle/css-vars-patch.css";
import { Outlet } from "umi";
import { TextArea } from "antd-mobile";
// import 'antd-mobile/es/global'
import VConsole from "vconsole";
import { useConfigHooks } from "./hooks";
new VConsole();
export default function Layout() {
  useConfigHooks();
  return (
    <div id="app" className={styles.container}>
      <Outlet />
    </div>
  );
}
