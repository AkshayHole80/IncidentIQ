import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Card, Space, Typography, Modal, Form, Input, Alert, message, Tooltip } from 'antd';
import { ToolOutlined, CheckSquareOutlined, EyeOutlined, SyncOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

const AssignedIncidents = () => {
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [resolveModalOpen, setResolveModalOpen] = useState(false);
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const fetchAssignedIncidents = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/api/v1/incidents/assigned');
      setIncidents(response.data);
    } catch (err) {
      console.error('Failed to load assigned incidents', err);
      setError('Could not fetch assigned incidents. Please verify your connection.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAssignedIncidents();
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

  const openResolveModal = (incident) => {
    setSelectedIncident(incident);
    setResolveModalOpen(true);
    form.resetFields();
  };

  const handleResolveSubmit = async (values) => {
    setSubmitLoading(true);
    try {
      await api.patch(`/api/v1/incidents/${selectedIncident.id}/resolve`, {
        resolutionNotes: values.resolutionNotes,
      });
      message.success(`Incident #${selectedIncident.id} has been marked as RESOLVED.`);
      setResolveModalOpen(false);
      fetchAssignedIncidents();
    } catch (err) {
      console.error('Failed to resolve incident', err);
      const errorMsg = err.response?.data?.message || 'Failed to submit resolution. Please try again.';
      message.error(errorMsg);
    } finally {
      setSubmitLoading(false);
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
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      width: '120px',
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
      width: '180px',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="View Details">
            <Button
              shape="circle"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/incidents/${record.id}`)}
            />
          </Tooltip>

          {record.status === 'IN_PROGRESS' && (
            <Button
              type="primary"
              size="small"
              icon={<CheckSquareOutlined />}
              onClick={() => openResolveModal(record)}
              className="!bg-green-500 !border-green-500 hover:!bg-green-600 hover:!border-green-600 text-white"
            >
              Resolve
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card
        title={
          <div className="flex justify-between items-center flex-wrap gap-3">
            <Space>
              <ToolOutlined className="text-orange-500 text-xl" />
              <Title level={4} className="!m-0">My Assigned IT Tickets</Title>
            </Space>
            <Button icon={<SyncOutlined spin={loading} />} onClick={fetchAssignedIncidents}>
              Refresh List
            </Button>
          </div>
        }
        className="rounded-lg shadow-sm border-zinc-100 dark:border-zinc-800"
      >
        <Paragraph type="secondary" className="mb-5">
          Below are the IT tickets assigned to you by administrators. Read descriptions and resolve tickets once fixed. 
          Note that incidents are automatically put into <Text code>IN_PROGRESS</Text> status upon assignment.
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
          locale={{ emptyText: 'No tickets are currently assigned to you.' }}
        />
      </Card>

      {/* Resolve Incident Dialog */}
      <Modal
        open={resolveModalOpen}
        title={<Title level={4} className="!m-0">Resolve Incident #{selectedIncident?.id}</Title>}
        onCancel={() => setResolveModalOpen(false)}
        footer={null}
        destroyOnHidden
      >
        <div className="my-4">
          <Text strong>Ticket Title:</Text>
          <Paragraph type="secondary" className="mt-1">
            {selectedIncident?.title}
          </Paragraph>
        </div>

        <Form
          form={form}
          name="resolve_incident_form"
          onFinish={handleResolveSubmit}
          layout="vertical"
        >
          <Form.Item
            name="resolutionNotes"
            label="Resolution Summary Notes"
            rules={[
              { required: true, message: 'Please document how this ticket was resolved!' },
              { min: 10, message: 'Please write a clearer explanation of the fix (at least 10 characters).' },
              { max: 2000, message: 'Notes cannot exceed 2000 characters!' }
            ]}
          >
            <TextArea
              rows={5}
              placeholder="Explain the root cause of the incident and how it was successfully mitigated..."
            />
          </Form.Item>

          <Form.Item className="!mb-0 text-right">
            <Space>
              <Button onClick={() => setResolveModalOpen(false)}>Cancel</Button>
              <Button type="primary" htmlType="submit" loading={submitLoading} className="!bg-green-500 !border-green-500 hover:!bg-green-600 hover:!border-green-600 text-white">
                Resolve Ticket
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AssignedIncidents;
