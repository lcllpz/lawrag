import { Button, Form, Input, Popup } from "antd-mobile";
import React, { useEffect, useMemo } from "react";
import styles from "./index.less";
import CloseOutline from "antd-mobile-icons/es/CloseOutline";
import PhonebookOutline from "antd-mobile-icons/es/PhonebookOutline";
const Login = (props: { isShow: boolean }) => {
  const [visible, setVisible] = React.useState(props.isShow);
  useEffect(() => {
    setVisible(props.isShow);
  }, [props.isShow]);
  const options = useMemo(() => {
    return [
      {
        id: 1,
        name: "游客ID",
        discrible: "游客登录",
      },
    ];
  }, []);
  return (
    <div>
      <Popup
        visible={visible}
        onMaskClick={() => {
          //   setVisible(false);
        }}
        className={styles.loginPopup}
      >
        <div className={styles.loginPopupBody}>
          <div className={styles.headerBox}>
            <div className={styles.titleBox}>
              <span>使用申请</span>
              <CloseOutline className={styles.icon} />
            </div>
            <div className={styles.describe}>
              <span>游客登录后将不保留历史记录</span>
              <div className={styles.describeBg}></div>
            </div>
          </div>
          <div className={styles.contentBox}>
            <Form layout="horizontal">
              <Form.Item
                name="phone"
                required={false}
                label={<img src="/phoneIcon.svg"></img>}
                rules={[{ required: true, message: "姓名不能为空" }]}
              >
                <Input onChange={console.log} placeholder="请输入姓名" />
              </Form.Item>
            </Form>
            <Button block type="submit" color="primary" size="large">
              提交
            </Button>
          </div>
        </div>
      </Popup>
    </div>
  );
};

export default Login;
