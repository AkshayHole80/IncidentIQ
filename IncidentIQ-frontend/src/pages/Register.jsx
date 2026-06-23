import React, { useState } from 'react';
import { Card, Form, Input, Button, Select, Typography, message } from 'antd';
import { UserOutlined, MailOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const { Title, Text } = Typography;
const { Option } = Select;

const Register = () => {
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      await register({
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        password: values.password,
        role: values.role
      });
      message.success('Successfully registered and logged in!');
      navigate('/');
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Registration failed. Please try again.';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-zinc-950 p-5">
      <Card
        className="w-full max-w-lg shadow-2xl rounded-2xl border-0"
        classNames={{ body: "p-8" }}
      >
        <div className="text-center mb-6">
          <SafetyCertificateOutlined className="text-5xl text-blue-500 mb-3" />
          <Title level={2} className="!m-0">Create Account</Title>
          <Text type="secondary">Get started with IncidentIQ</Text>
        </div>

        <Form
          name="register_form"
          onFinish={onFinish}
          layout="vertical"
          size="large"
        >
          <div className="flex gap-4">
            <Form.Item
              name="firstName"
              rules={[{ required: true, message: 'Required!' }]}
              className="flex-1"
            >
              <Input prefix={<UserOutlined className="text-gray-400" />} placeholder="First Name" />
            </Form.Item>

            <Form.Item
              name="lastName"
              rules={[{ required: true, message: 'Required!' }]}
              className="flex-1"
            >
              <Input prefix={<UserOutlined className="text-gray-400" />} placeholder="Last Name" />
            </Form.Item>
          </div>

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
            rules={[
              { required: true, message: 'Please input your password!' },
              { min: 6, message: 'Password must be at least 6 characters!' }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="Password"
            />
          </Form.Item>

          <Form.Item
            name="role"
            label="Account Type"
            rules={[{ required: true, message: 'Please select an account type!' }]}
            initialValue="USER"
          >
            <Select placeholder="Select a role">
              <Option value="USER">Standard User (Report incidents)</Option>
              <Option value="SUPPORT_ENGINEER">Support Engineer (Resolve incidents)</Option>
              <Option value="ADMIN">System Administrator (Assign/Close incidents)</Option>
            </Select>
          </Form.Item>

          <Form.Item className="mb-0">
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
              className="h-11 rounded-lg font-medium"
            >
              Create Account
            </Button>
          </Form.Item>
        </Form>

        <div className="text-center mt-5">
          <Text type="secondary">Already have an account? </Text>
          <Link to="/login" className="text-blue-500 hover:text-blue-600 font-medium">Sign In</Link>
        </div>
      </Card>
    </div>
  );
};

export default Register;
