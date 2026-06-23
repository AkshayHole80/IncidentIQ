import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Card, Space, Typography, Tooltip, Alert } from 'antd';
import { FileTextOutlined, EyeOutlined, SyncOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const { Title, Paragraph } = Typography;

const MyIncidents = () => {
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const fetchMyIncidents = async () => {
    setLoading(true);
    setError(null);
    try {
      // In the backend, GET /api/v1/incidents retrieves incidents created by the current user
      const response = await api.get('/api/v1/incidents');
      setIncidents(response.data);
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
      width: '100px',
      render: (_, record) => (
        <Tooltip title="View Details">
          <Button
            type="primary"
            shape="circle"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/incidents/${record.id}`)}
          />
        </Tooltip>
      ),
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
    </div>
  );
};

export default MyIncidents;
