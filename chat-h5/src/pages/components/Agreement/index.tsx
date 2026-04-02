import { Button, Modal, NavBar, Popup, Radio, Space } from "antd-mobile";
import React, { useEffect, useRef, useState } from "react";
import styles from "./index.less";
import { marked } from "marked";
const Index = (props: {
  visible: boolean;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;
}) => {
  const { visible, setVisible } = props;
  const agreementContent = useRef<any>(null);

  useEffect(() => {
    if (visible && agreementContent.current) {
      const dom = agreementContent.current as Element;
      loadAndRenderMarkdown(dom);
    }
  }, [visible]);

  const loadAndRenderMarkdown = async (markdownContent: Element) => {
    const url = `/agreement/agreement.md`;
    const response = await fetch(url);
    const text = await response.text();
    const content = await marked.parse(text); // 返回 HTML
    markdownContent.innerHTML = content;
  };
  return (
    <>
      <Popup
        position="left"
        visible={visible}
        bodyStyle={{ width: "100%" }}
        className={styles.agreementPoup}
        forceRender
      >
        <div className={styles.agreementPoupContainer}>
          <div className={styles.agreementPoupBody}>
            <NavBar onBack={() => setVisible(false)}>用户协议/隐私条例</NavBar>
          </div>
          <div className={styles.agreementContent} ref={agreementContent}></div>
        </div>
      </Popup>

      <Popup
        className={styles.agreementFooterPoup}
        position="bottom"
        mask={false}
        visible={visible}
        forceRender
        bodyStyle={{
          borderTopLeftRadius: "16px",
          borderTopRightRadius: "16px",
          backdropFilter: "blur(28px)",
          background: "rgba(228, 228, 244, 0.4)",
        }}
      >
        <div className={styles.agreementFooterContent}>
          <label className={styles.agreement}>
            <Radio />
            <div
              style={{
                marginLeft: "6px",
              }}
            >
              我已阅读并同意<span>《用户协议》</span>和<span>《隐私政策》</span>
            </div>
          </label>
          <div className={styles.footerBox}>
            <Button size="large" className={styles.cancelButton}>
              取消
            </Button>
            <Button
              color="primary"
              size="large"
              className={styles.confirmButton}
            >
              确定
            </Button>
          </div>
        </div>
      </Popup>
    </>
  );
};

export default Index;
