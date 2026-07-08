import React, { useState } from 'react';
import { Card, Form, Input, Button, Typography, message } from 'antd';
import { MailOutlined, LockOutlined, SafetyCertificateOutlined, GoogleOutlined, GithubOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const { Title, Text } = Typography;

const Login = () => {
  const [loading, setLoading] = useState(false);
  const { login, darkMode } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      await login(values.email, values.password);
      message.success('Successfully logged in!');
      navigate('/');
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Login failed. Please check your credentials.';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-zinc-950 p-5">
      <Card
        className="w-full max-w-md shadow-2xl rounded-2xl border-0"
        classNames={{ body: "p-8" }}
      >
        <div className="text-center mb-6">
          <SafetyCertificateOutlined className="text-5xl text-blue-500 mb-3" />
          <Title level={2} className="!m-0">IncidentIQ</Title>
          <Text type="secondary">Sign in to manage IT incidents</Text>
        </div>

        <Form
          name="login_form"
          initialValues={{ remember: true }}
          onFinish={onFinish}
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'Please input your email!' },
              { type: 'email', message: 'Please enter a valid email address!' }
            ]}
          >
            <Input prefix={<MailOutlined className="text-gray-400" />} placeholder="Email Address" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please input your password!' }]}
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="Password"
            />
          </Form.Item>

          <Form.Item className="mb-0">
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
              className="h-11 rounded-lg font-medium"
            >
              Sign In
            </Button>
          </Form.Item>
        </Form>

        <div className="relative my-6 text-center">
          <span className="bg-white dark:bg-zinc-900 px-3 text-gray-400 text-sm">Or sign in with</span>
          <div className="absolute top-1/2 left-0 right-0 h-[1px] bg-gray-200 dark:bg-zinc-800 -z-10"></div>
        </div>

        <div className="flex gap-4">
          <Button
            type="default"
            icon={<GoogleOutlined className="text-red-500" />}
            href="http://localhost:8081/oauth2/authorization/google"
            block
            className="h-11 rounded-lg font-medium border-gray-300 hover:border-red-500 hover:text-red-500 flex justify-center items-center gap-2"
          >
            Google
          </Button>
          <Button
            type="default"
            icon={<GithubOutlined className="text-slate-800 dark:text-white" />}
            href="http://localhost:8081/oauth2/authorization/github"
            block
            className="h-11 rounded-lg font-medium border-gray-300 hover:border-slate-800 hover:text-slate-800 dark:hover:border-white dark:hover:text-white flex justify-center items-center gap-2"
          >
            GitHub
          </Button>
        </div>

        <div className="text-center mt-5">
          <Text type="secondary">Don't have an account? </Text>
          <Link to="/register" className="text-blue-500 hover:text-blue-600 font-medium">Register now</Link>
        </div>
      </Card>
    </div>
  );
};

export default Login;
