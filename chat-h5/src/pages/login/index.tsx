import { Button, Form, Input } from "antd-mobile";
import React, { useState } from "react";
import styles from "./index.less";
import { EyeInvisibleOutline, EyeOutline } from "antd-mobile-icons";
import { userApi } from "@/services/user";
import { useNavigate } from "umi";
const Index = () => {
  const [visible, setVisible] = useState(false);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState<boolean>(false);
  const navigate = useNavigate();
  const onSubmit = async () => {
    const values = await form.validateFields();
    setLoading(true);
    try {
      const res = await userApi.login({ ...values });
      const { token } = res;
      localStorage.setItem("token", token);
      navigate("/");
      setLoading(false);
    } catch {
      setLoading(false);
    }
  };
  return (
    <div className={styles.loginBox}>
      <Form
        layout="horizontal"
        form={form}
        footer={
          <Button
            block
            color="primary"
            size="large"
            onClick={onSubmit}
            loading={loading}
          >
            登录
          </Button>
        }
        initialValues={{
          username: "user01",
          password: "123456",
        }}
      >
        <Form.Item label="用户名" name="username" rules={[{ required: true }]}>
          <Input placeholder="请输入用户名" clearable />
        </Form.Item>
        <Form.Item
          label="密码"
          name="password"
          rules={[{ required: true }]}
          extra={
            <div className={styles.eye}>
              {!visible ? (
                <EyeInvisibleOutline onClick={() => setVisible(true)} />
              ) : (
                <EyeOutline onClick={() => setVisible(false)} />
              )}
            </div>
          }
        >
          <Input
            placeholder="请输入密码"
            clearable
            type={visible ? "text" : "password"}
          />
        </Form.Item>
      </Form>
    </div>
  );
};

export default Index;
