import { Button, Form, Input, Popup, Radio, TextArea } from "antd-mobile";
import React, { useEffect, useMemo } from "react";
import styles from "./index.less";
import CloseOutline from "antd-mobile-icons/es/CloseOutline";
const UserFeedback = (props: { isShow: boolean }) => {
  const [visible, setVisible] = React.useState(props.isShow);
  const [open, SetOpen] = React.useState(false);
  useEffect(() => {
    setVisible(props.isShow);
  }, [props.isShow]);

  return (
    <div>
      <Popup
        visible={visible}
        onMaskClick={() => {
          //   setVisible(false);
        }}
        className={styles.UserFeedback}
      >
        <div className={styles.FeedbackPopupBody}>
          <div className={styles.headerBox}>
            <div className={styles.titleBox}>
              <span>用户反馈</span>
              <CloseOutline className={styles.icon} />
            </div>
            <div className={styles.describe}>
              <span>用户可描述或打电话进行反馈</span>
              <div className={styles.describeBg}></div>
            </div>
          </div>
          <div className={styles.contentBox}>
            <Form layout="horizontal">
              <Form.Item name="phone" required={false}>
                <TextArea
                  placeholder={"用户可输入遇到的问题或建议~"}
                  rows={5}
                  autoSize={{ minRows: 1, maxRows: 6 }}
                  style={{ fontSize: 14 }}
                />
              </Form.Item>
              <Form.Item name="contact">
                <Input
                  onChange={console.log}
                  placeholder="请留下你的联系方式方便后续联系，电话或邮箱"
                />
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
            <div className={styles.pbone}>18988515412</div>
          </div>
        </div>
      </Popup>
    </div>
  );
};

export default UserFeedback;
