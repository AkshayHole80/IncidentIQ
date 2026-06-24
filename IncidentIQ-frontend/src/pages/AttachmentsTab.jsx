import React, { useState, useEffect, useCallback } from 'react';
import { Table, Upload, Button, Space, Typography, Card, Empty, message, Spin, Tooltip, Tag, Popconfirm } from 'antd';
import { 
  InboxOutlined, 
  DownloadOutlined, 
  FilePdfOutlined, 
  FileImageOutlined, 
  FileZipOutlined, 
  FileTextOutlined, 
  FileOutlined,
  PaperClipOutlined,
  DeleteOutlined
} from '@ant-design/icons';
import { getAttachments, uploadAttachment, deleteAttachment } from '../services/attachmentService';
import { useAuth } from '../context/AuthContext';

const { Text, Title } = Typography;
const { Dragger } = Upload;

const AttachmentsTab = ({ incidentId, incidentStatus }) => {
  const { darkMode } = useAuth();
  const [attachments, setAttachments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);

  const isIncidentOpen = incidentStatus === 'OPEN';

  const handleDelete = async (attachmentId) => {
    try {
      await deleteAttachment(attachmentId);
      message.success('Attachment deleted successfully.');
      fetchAttachments();
    } catch (err) {
      console.error('Delete failed:', err);
      message.error(err.response?.data?.message || 'Failed to delete attachment.');
    }
  };

  // Fetch attachments list
  const fetchAttachments = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getAttachments(incidentId);
      setAttachments(data || []);
    } catch (err) {
      console.error('Failed to fetch attachments:', err);
      message.error(err.response?.data?.message || 'Failed to load attachments.');
    } finally {
      setLoading(false);
    }
  }, [incidentId]);

  useEffect(() => {
    if (incidentId) {
      fetchAttachments();
    }
  }, [incidentId, fetchAttachments]);

  // Formatter for file size
  const formatFileSize = (bytes) => {
    if (!bytes || isNaN(bytes)) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // Determine file icon based on mime type or file extension
  const getFileIcon = (contentType, fileName) => {
    const type = contentType?.toLowerCase() || '';
    const name = fileName?.toLowerCase() || '';
    
    if (type.includes('pdf') || name.endsWith('.pdf')) {
      return <FilePdfOutlined className="text-red-500 text-lg" />;
    }
    if (type.includes('image') || name.endsWith('.png') || name.endsWith('.jpg') || name.endsWith('.jpeg') || name.endsWith('.gif') || name.endsWith('.webp')) {
      return <FileImageOutlined className="text-green-500 text-lg" />;
    }
    if (type.includes('zip') || type.includes('rar') || type.includes('tar') || type.includes('gzip') || name.endsWith('.zip') || name.endsWith('.rar') || name.endsWith('.7z')) {
      return <FileZipOutlined className="text-orange-500 text-lg" />;
    }
    if (type.includes('text') || type.includes('json') || name.endsWith('.txt') || name.endsWith('.json') || name.endsWith('.log') || name.endsWith('.csv')) {
      return <FileTextOutlined className="text-blue-500 text-lg" />;
    }
    return <FileOutlined className="text-slate-400 text-lg" />;
  };

  // Custom upload handler to use our axios configuration
  const handleCustomUpload = async ({ file, onSuccess, onError }) => {
    setUploading(true);
    try {
      const result = await uploadAttachment(incidentId, file);
      message.success(`File "${file.name}" uploaded successfully.`);
      onSuccess(result);
      // Reload attachments list
      fetchAttachments();
    } catch (err) {
      console.error('File upload failed:', err);
      message.error(err.response?.data?.message || `Failed to upload "${file.name}".`);
      onError(err);
    } finally {
      setUploading(false);
    }
  };

  const uploadProps = {
    name: 'file',
    multiple: false,
    customRequest: handleCustomUpload,
    showUploadList: false,
    beforeUpload: (file) => {
      // 20MB limit as a sensible client-side check
      const isLt20M = file.size / 1024 / 1024 < 20;
      if (!isLt20M) {
        message.error('File must be smaller than 20MB!');
        return Upload.LIST_IGNORE;
      }
      return true;
    }
  };

  const columns = [
    {
      title: 'File Name',
      dataIndex: 'fileName',
      key: 'fileName',
      sorter: (a, b) => a.fileName.localeCompare(b.fileName),
      render: (text, record) => (
        <Space size="middle">
          {getFileIcon(record.contentType, record.fileName)}
          <span className="font-medium text-slate-800 dark:text-zinc-200 break-all">{text}</span>
        </Space>
      ),
    },
    {
      title: 'Size',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: '120px',
      sorter: (a, b) => a.fileSize - b.fileSize,
      render: (size) => formatFileSize(size),
    },
    {
      title: 'Type',
      dataIndex: 'contentType',
      key: 'contentType',
      width: '140px',
      render: (type, record) => {
        const ext = record.fileName.split('.').pop().toUpperCase();
        return <Tag color="blue" className="font-semibold">{ext || 'FILE'}</Tag>;
      }
    },
    {
      title: 'Action',
      key: 'action',
      width: '240px',
      render: (_, record) => (
        <Space size="small">
          <Button 
            type="primary" 
            ghost 
            size="small" 
            icon={<DownloadOutlined />}
            onClick={() => window.open(record.fileUrl, '_blank')}
            className="hover:scale-102 transition-transform"
          >
            View
          </Button>
          {isIncidentOpen ? (
            <Popconfirm
              title="Delete Attachment"
              description="Are you sure you want to delete this attachment?"
              onConfirm={() => handleDelete(record.id)}
              okText="Yes"
              cancelText="No"
              okButtonProps={{ danger: true }}
            >
              <Button 
                type="primary" 
                danger 
                ghost
                size="small" 
                icon={<DeleteOutlined />}
                className="hover:scale-102 transition-transform"
              >
                Delete
              </Button>
            </Popconfirm>
          ) : (
            <Tooltip title="Attachments can only be deleted while the incident is in OPEN status.">
              <span>
                <Button 
                  type="primary" 
                  danger 
                  ghost
                  size="small" 
                  icon={<DeleteOutlined />}
                  disabled
                >
                  Delete
                </Button>
              </span>
            </Tooltip>
          )}
        </Space>
      ),
    }
  ];

  return (
    <div className="pt-4 flex flex-col gap-6">
      {/* Upload zone */}
      <Card 
        size="small"
        className={`rounded-xl border border-dashed ${
          darkMode ? 'bg-zinc-900/40 border-zinc-700' : 'bg-slate-50/50 border-gray-300'
        }`}
      >
        <Spin spinning={uploading} tip="Uploading attachment to secure storage...">
          <Dragger {...uploadProps} className="p-4 rounded-lg bg-transparent border-none">
            <p className="ant-upload-drag-icon flex justify-center mb-2">
              <InboxOutlined className="text-blue-500 text-4xl" />
            </p>
            <p className="ant-upload-text font-semibold text-sm text-slate-700 dark:text-zinc-200">
              Click or drag file to this area to upload
            </p>
            <p className="ant-upload-hint text-xs text-slate-400 dark:text-zinc-500 mt-1">
              Supports single file uploads up to 20MB. Security scanning will be executed automatically.
            </p>
          </Dragger>
        </Spin>
      </Card>

      {/* Attachments list table */}
      <div>
        <Title level={5} className="!mb-3 flex items-center gap-2">
          <PaperClipOutlined className="text-blue-500" />
          <span>Uploaded Attachments ({attachments.length})</span>
        </Title>
        
        {loading ? (
          <div className="flex justify-center items-center py-12">
            <Spin size="large" tip="Loading attachments..." />
          </div>
        ) : attachments.length === 0 ? (
          <Empty 
            description={
              <span className="text-slate-400 dark:text-zinc-500 text-sm">
                No attachments uploaded yet
              </span>
            } 
            className="py-12 bg-white dark:bg-zinc-900/20 rounded-xl border border-gray-100 dark:border-zinc-800" 
          />
        ) : (
          <Table
            columns={columns}
            dataSource={attachments}
            rowKey="id"
            pagination={{ pageSize: 5 }}
            className="border border-gray-100 dark:border-zinc-800 rounded-lg overflow-hidden bg-white dark:bg-zinc-900"
            size="middle"
          />
        )}
      </div>
    </div>
  );
};

export default AttachmentsTab;
