import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Card, Space, Typography, Tooltip, Alert, Modal, Form, Input, Popconfirm, message } from 'antd';
import { FileTextOutlined, EyeOutlined, SyncOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { updateIncident, deleteIncident, sortIncidents } from '../services/incidentService';

const { Title, Paragraph } = Typography;

const MyIncidents = () => {
  const { user } = useAuth();
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // Edit / Delete states
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingIncident, setEditingIncident] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [form] = Form.useForm();

  const handleEditSubmit = async (values) => {
    if (!editingIncident) return;
    setEditLoading(true);
    try {
      await updateIncident(editingIncident.id, {
        title: values.title,
        description: values.description,
      });
      message.success('Incident updated successfully!');
      setEditModalOpen(false);
      setEditingIncident(null);
      fetchMyIncidents();
    } catch (err) {
      console.error('Failed to update incident', err);
      message.error(err.response?.data?.message || 'Failed to update incident.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDeleteConfirm = async (id) => {
    try {
      await deleteIncident(id);
      message.success('Incident deleted successfully!');
      setIncidents(prev => prev.filter(inc => inc.id !== id));
    } catch (err) {
      console.error('Failed to delete incident', err);
      message.error(err.response?.data?.message || 'Failed to delete incident.');
    }
  };

  const fetchMyIncidents = async () => {
    setLoading(true);
    setError(null);
    try {
      // In the backend, GET /api/v1/incidents retrieves incidents created by the current user
      const response = await api.get('/v1/incidents');
      setIncidents(sortIncidents(response.data));
    } catch (err) {
      console.error('Failed to load my incidents', err);
      setError('Could not fetch your incidents. Please check your network connection.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyIncidents();
  }, []);

  const getPriorityTagColor = (priority) => {
    switch (priority) {
      case 'CRITICAL': return 'red';
      case 'HIGH': return 'orange';
      case 'MEDIUM': return 'blue';
      case 'LOW': return 'green';
      default: return 'default';
    }
  };

  const getStatusTagColor = (status) => {
    switch (status) {
      case 'OPEN': return 'red';
      case 'IN_PROGRESS': return 'blue';
      case 'RESOLVED': return 'green';
      case 'CLOSED': return 'gray';
      default: return 'default';
    }
  };

  const columns = [
    {
      title: 'Ticket ID',
      dataIndex: 'id',
      key: 'id',
      width: '100px',
      sorter: (a, b) => a.id - b.id,
      render: (id) => <strong className="text-blue-500 hover:text-blue-600 cursor-pointer font-bold" onClick={() => navigate(`/incidents/${id}`)}>#{id}</strong>,
    },
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      render: (text, record) => (
        <span className="cursor-pointer font-medium hover:text-blue-500" onClick={() => navigate(`/incidents/${record.id}`)}>
          {text}
        </span>
      )
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
      width: '140px',
      render: (category) => <Tag color="geekblue">{category}</Tag>,
    },
    {
      title: 'Priority (AI Assessed)',
      dataIndex: 'priority',
      key: 'priority',
      width: '180px',
      render: (priority) => <Tag color={getPriorityTagColor(priority)}>{priority}</Tag>,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: '140px',
      render: (status) => <Tag color={getStatusTagColor(status)}>{status}</Tag>,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: '180px',
      sorter: (a, b) => new Date(a.createdAt) - new Date(b.createdAt),
      render: (dateStr) => dateStr ? new Date(dateStr).toLocaleString() : 'N/A',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: '160px',
      render: (_, record) => {
        const isCreator = record.createdBy === user?.id;
        const isOpen = record.status === 'OPEN';
        const canEditOrDelete = isCreator && isOpen;

        return (
          <Space size="middle">
            <Tooltip title="View Details">
              <Button
                type="primary"
                shape="circle"
                icon={<EyeOutlined />}
                onClick={() => navigate(`/incidents/${record.id}`)}
              />
            </Tooltip>
            {canEditOrDelete && (
              <>
                <Tooltip title="Edit Incident">
                  <Button
                    shape="circle"
                    icon={<EditOutlined />}
                    onClick={() => {
                      setEditingIncident(record);
                      form.setFieldsValue({
                        title: record.title,
                        description: record.description,
                      });
                      setEditModalOpen(true);
                    }}
                  />
                </Tooltip>
                <Tooltip title="Delete Incident">
                  <Popconfirm
                    title="Are you sure you want to delete this incident?"
                    onConfirm={() => handleDeleteConfirm(record.id)}
                    okText="Yes"
                    cancelText="No"
                    okButtonProps={{ danger: true }}
                  >
                    <Button
                      type="primary"
                      danger
                      shape="circle"
                      icon={<DeleteOutlined />}
                    />
                  </Popconfirm>
                </Tooltip>
              </>
            )}
          </Space>
        );
      },
    },
  ];

  return (
    <div>
      <Card
        title={
          <div className="flex justify-between items-center flex-wrap gap-3">
            <Space>
              <FileTextOutlined className="text-blue-500 text-xl" />
              <Title level={4} className="!m-0">My Reported Incidents</Title>
            </Space>
            <Button icon={<SyncOutlined spin={loading} />} onClick={fetchMyIncidents}>
              Refresh List
            </Button>
          </div>
        }
        className="rounded-lg shadow-sm border-zinc-100 dark:border-zinc-800"
      >
        <Paragraph type="secondary" className="mb-5">
          This table displays all IT issues you have registered in the system. Click on any ticket to view its current status, assignment details, and resolution notes.
        </Paragraph>

        {error && (
          <Alert
            message="Error"
            description={error}
            type="error"
            showIcon
            className="mb-5"
          />
        )}

        <Table
          columns={columns}
          dataSource={incidents}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 8 }}
          locale={{ emptyText: 'You have not reported any incidents yet.' }}
        />
      </Card>

      {/* Edit Incident Modal */}
      <Modal
        open={editModalOpen}
        title={<Title level={4} className="!m-0">Edit Incident</Title>}
        onCancel={() => {
          setEditModalOpen(false);
          setEditingIncident(null);
        }}
        footer={null}
        destroyOnClose
      >
        <Form
          form={form}
          name="edit_incident_form"
          onFinish={handleEditSubmit}
          layout="vertical"
          className="mt-4"
        >
          <Form.Item
            name="title"
            label="Incident Title"
            rules={[
              { required: true, message: 'Please enter a title!' },
              { min: 5, message: 'Title must be at least 5 characters long!' }
            ]}
          >
            <Input placeholder="Enter brief title of the issue" />
          </Form.Item>

          <Form.Item
            name="description"
            label="Detailed Description"
            rules={[
              { required: true, message: 'Please enter a description!' },
              { min: 10, message: 'Description must be at least 10 characters long!' }
            ]}
          >
            <Input.TextArea rows={6} placeholder="Describe the steps to reproduce or issue details..." />
          </Form.Item>

          <Form.Item className="text-right !mb-0">
            <Space>
              <Button onClick={() => {
                setEditModalOpen(false);
                setEditingIncident(null);
              }}>
                Cancel
              </Button>
              <Button type="primary" htmlType="submit" loading={editLoading}>
                Save Changes
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MyIncidents;
