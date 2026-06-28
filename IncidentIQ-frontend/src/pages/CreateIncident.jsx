import React, { useState } from 'react';
import { Card, Form, Input, Button, Modal, Spin, Typography, Space, message, notification, Upload } from 'antd';
import { PlusCircleOutlined, RobotOutlined, SmileOutlined, UploadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { uploadAttachment } from '../services/attachmentService';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

const CreateIncident = () => {
  const [loading, setLoading] = useState(false);
  const [showAiModal, setShowAiModal] = useState(false);
  const [aiModalTitle, setAiModalTitle] = useState('Analyzing Incident Details');
  const [aiModalText, setAiModalText] = useState('Our Gemini AI agent is classifying the category and severity level based on your description...');
  const [fileList, setFileList] = useState([]);
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const uploadProps = {
    onRemove: (file) => {
      const index = fileList.indexOf(file);
      const newFileList = fileList.slice();
      newFileList.splice(index, 1);
      setFileList(newFileList);
    },
    beforeUpload: (file) => {
      const isLt20M = file.size / 1024 / 1024 < 20;
      if (!isLt20M) {
        message.error('File must be smaller than 20MB!');
        return Upload.LIST_IGNORE;
      }
      setFileList([...fileList, file]);
      return false;
    },
    fileList,
  };

  const onFinish = async (values) => {
    setAiModalTitle('Analyzing Incident Details');
    setAiModalText('Our Gemini AI agent is classifying the category and severity level based on your description...');
    setLoading(true);
    setShowAiModal(true);
    try {
      const response = await api.post('/v1/incidents', {
        title: values.title,
        description: values.description,
      });

      const createdIncident = response.data;

      // Handle queued attachment uploads
      if (fileList.length > 0) {
        setAiModalTitle('Uploading Attachments');
        setAiModalText(`Uploading ${fileList.length} attachment(s) to secure S3 storage...`);
        try {
          await Promise.all(fileList.map(file => uploadAttachment(createdIncident.id, file)));
        } catch (uploadError) {
          console.error('Failed to upload attachments during incident creation', uploadError);
          message.error('Failed to upload some attachments. You can re-upload them in the Attachments tab.');
        }
      }

      setShowAiModal(false);
      setFileList([]);

      // Display dynamic details computed by Gemini
      notification.open({
        message: 'Incident Created Successfully!',
        description: (
          <div>
            <p><strong>ID:</strong> #{createdIncident.id}</p>
            <p>
              <strong>AI Classification:</strong><br />
              Priority: <span className={`font-bold ${createdIncident.priority === 'CRITICAL' || createdIncident.priority === 'HIGH' ? 'text-red-500' : 'text-green-500'}`}>{createdIncident.priority}</span><br />
              Category: <span className="font-bold text-blue-500">{createdIncident.category}</span>
            </p>
          </div>
        ),
        icon: <SmileOutlined className="text-green-500" />,
        duration: 8,
      });

      form.resetFields();
      navigate('/incidents/my');
    } catch (error) {
      console.error('Failed to create incident', error);
      setShowAiModal(false);
      const errorMsg = error.response?.data?.message || 'Failed to submit incident. Please check your network and try again.';
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-[700px] mx-auto p-4">
      <Card
        title={
          <Space>
            <PlusCircleOutlined className="text-blue-500 text-xl" />
            <span className="text-lg font-semibold">Report New Incident</span>
          </Space>
        }
        className="rounded-lg shadow-sm border-zinc-100 dark:border-zinc-800"
      >
        <Paragraph type="secondary" className="mb-6">
          Describe the IT problem or request. Our IncidentIQ AI agent will automatically review the description, classify the technical category, and assign a priority severity level.
        </Paragraph>

        <Form
          form={form}
          name="create_incident"
          onFinish={onFinish}
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="title"
            label="Incident Title"
            rules={[
              { required: true, message: 'Please enter a summary title!' },
              { min: 5, message: 'Title must be at least 5 characters long!' }
            ]}
          >
            <Input placeholder="Example: VPN connection dropping frequently" />
          </Form.Item>

          <Form.Item
            name="description"
            label="Detailed Description"
            rules={[
              { required: true, message: 'Please describe the incident details!' },
              { min: 10, message: 'Please provide more details (at least 10 characters)!' },
              { max: 2000, message: 'Description cannot exceed 2000 characters!' }
            ]}
          >
            <TextArea
              rows={6}
              placeholder="Please provide steps to reproduce, error messages, and context. Be as detailed as possible to allow accurate AI classification."
            />
          </Form.Item>

          <Form.Item label="Incident Attachments (Optional)">
            <Upload {...uploadProps} listType="picture" multiple>
              <Button icon={<UploadOutlined />}>Select Attachment Files</Button>
            </Upload>
          </Form.Item>

          <Form.Item className="!mb-0 text-right">
            <Space>
              <Button onClick={() => navigate('/')}>Cancel</Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                File Ticket
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      {/* AI Processing Overlay Dialog */}
      <Modal
        open={showAiModal}
        footer={null}
        closable={false}
        centered
        classNames={{ body: "p-8 text-center" }}
      >
        <Space orientation="vertical" size="large" className="w-full">
          <Spin size="large" indicator={<RobotOutlined className="text-5xl text-blue-500" spin />} />
          <div>
            <Title level={4}>{aiModalTitle}</Title>
            <Text type="secondary">
              {aiModalText}
            </Text>
          </div>
        </Space>
      </Modal>
    </div>
  );
};

export default CreateIncident;
