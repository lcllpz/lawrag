import { Button, Form, Input, Popup, Radio } from "antd-mobile";
import React, { useEffect, useMemo } from "react";
import styles from "./index.less";
import CloseOutline from "antd-mobile-icons/es/CloseOutline";
import PhonebookOutline from "antd-mobile-icons/es/PhonebookOutline";
import Agreement from "../Agreement";
const Login = (props: { isShow: boolean }) => {
  const [visible, setVisible] = React.useState(props.isShow);
  const [open, SetOpen] = React.useState(false);
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
            <Button
              className={styles.submitButton}
              block
              type="submit"
              color="primary"
              size="large"
              style={{
                "--border-color": "16px",
              }}
            >
              提交
            </Button>
            <div className={styles.guestLogin}>游客登录</div>
          </div>
          <div className={styles.agreement}>
            <Radio />
            <div
              style={{
                marginLeft: "6px",
              }}
              onClick={() => SetOpen(true)}
            >
              我已阅读并同意<span>《用户协议》</span>和<span>《隐私政策》</span>
            </div>
          </div>
        </div>
      </Popup>
      <Agreement visible={open} setVisible={SetOpen}></Agreement>
    </div>
  );
};

export default Login;
