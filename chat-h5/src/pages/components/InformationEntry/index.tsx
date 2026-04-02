import { Modal } from "antd-mobile";
import React from "react";
import styles from "./index.less";
const Index = (props: { isShow: boolean }) => {
  const [visible, setVisible] = React.useState(props.isShow);

  return (
    <>
      <Modal
        visible={visible}
        content="人在天边月上明"
        closeOnAction
        className={styles.InformationModal}
        onClose={() => {
          setVisible(false);
        }}
      />
    </>
  );
};

export default Index;
