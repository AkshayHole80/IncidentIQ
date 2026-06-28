import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Card, Space, Input, Select, Modal, Form, Typography, Alert, message, Tooltip } from 'antd';
import {
  FileTextOutlined,
  UserAddOutlined,
  CloseCircleOutlined,
  SearchOutlined,
  FilterOutlined,
  EyeOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { sortIncidents } from '../services/incidentService';

const { Title, Paragraph, Text } = Typography;
const { Option } = Select;

const AllIncidents = () => {
  const [incidents, setIncidents] = useState([]);
  const [engineers, setEngineers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filter and Search States
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedStatus, setSelectedStatus] = useState(null);
  const [selectedPriority, setSelectedPriority] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState(null);

  // Assign Modal States
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [assignLoading, setAssignLoading] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  // Lookup map for engineer names: id -> Name
  const [engineerMap, setEngineerMap] = useState({});

  const fetchEngineers = async () => {
    try {
      const response = await api.get('/v1/users/support-engineers');
      setEngineers(response.data);
      const mapping = {};
      response.data.forEach(eng => {
        mapping[eng.id] = `${eng.firstName} ${eng.lastName}`;
      });
      setEngineerMap(mapping);
    } catch (err) {
      console.error('Failed to load support engineers', err);
      message.error('Failed to load support engineers directory.');
    }
  };

  const fetchIncidents = async () => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (selectedStatus) {
        response = await api.get(`/v1/incidents/status/${selectedStatus}`);
      } else if (selectedPriority) {
        response = await api.get(`/v1/incidents/priority/${selectedPriority}`);
      } else if (selectedCategory) {
        response = await api.get(`/v1/incidents/category/${selectedCategory}`);
      } else {
        response = await api.get(`/v1/incidents/search`, {
          params: { keyword: searchKeyword }
        });
      }
      setIncidents(sortIncidents(response.data));
    } catch (err) {
      console.error('Failed to fetch incidents', err);
      setError('Could not load incidents. Please verify API Gateway connectivity.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEngineers();
    fetchIncidents();
  }, []);

  // Trigger fetch when a filter changes
  useEffect(() => {
    // Exclude initial loading triggers if necessary, or let it load
    fetchIncidents();
  }, [selectedStatus, selectedPriority, selectedCategory]);

  const handleSearch = () => {
    // Clear filters when searching by keyword to prevent conflict
    setSelectedStatus(null);
    setSelectedPriority(null);
    setSelectedCategory(null);
    fetchIncidents();
  };

  const resetFilters = () => {
    setSearchKeyword('');
    setSelectedStatus(null);
    setSelectedPriority(null);
    setSelectedCategory(null);
    // Directly fetch all
    setLoading(true);
    api.get(`/v1/incidents/search`, { params: { keyword: '' } })
      .then(res => setIncidents(sortIncidents(res.data)))
      .catch(() => setError('Failed to reset list.'))
      .finally(() => setLoading(false));
  };

  const openAssignModal = (incident) => {
    setSelectedIncident(incident);
    setAssignModalOpen(true);
    form.resetFields();
  };

  const handleAssignSubmit = async (values) => {
    setAssignLoading(true);
    try {
      await api.put(`/v1/incidents/${selectedIncident.id}/assign`, {
        assignedTo: values.engineerId,
      });
      message.success(`Incident #${selectedIncident.id} assigned to ${engineerMap[values.engineerId] || 'Engineer'}.`);
      setAssignModalOpen(false);
      fetchIncidents();
    } catch (err) {
      console.error('Failed to assign incident', err);
      const errorMsg = err.response?.data?.message || 'Assignment failed. Please check roles and permissions.';
      message.error(errorMsg);
    } finally {
      setAssignLoading(false);
    }
  };

  const handleCloseIncident = async (id) => {
    try {
      await api.patch(`/v1/incidents/${id}/close`);
      message.success(`Incident #${id} has been CLOSED.`);
      fetchIncidents();
    } catch (err) {
      console.error('Failed to close incident', err);
      const errorMsg = err.response?.data?.message || 'Could not close incident.';
      message.error(errorMsg);
    }
  };

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
      width: '90px',
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
      width: '130px',
      render: (category) => <Tag color="geekblue">{category}</Tag>,
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      width: '110px',
      render: (priority) => <Tag color={getPriorityTagColor(priority)}>{priority}</Tag>,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: '120px',
      render: (status) => <Tag color={getStatusTagColor(status)}>{status}</Tag>,
    },
    {
      title: 'Assigned To',
      dataIndex: 'assignedTo',
      key: 'assignedTo',
      width: '180px',
      render: (assignedId) => {
        if (!assignedId) return <Text type="secondary">Unassigned</Text>;
        return <Text strong>{engineerMap[assignedId] || `Engineer #${assignedId}`}</Text>;
      }
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: '160px',
      sorter: (a, b) => new Date(a.createdAt) - new Date(b.createdAt),
      render: (dateStr) => dateStr ? new Date(dateStr).toLocaleString() : 'N/A',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: '200px',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="View Details">
            <Button
              shape="circle"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/incidents/${record.id}`)}
            />
          </Tooltip>

          {record.status === 'OPEN' && (
            <Button
              type="primary"
              size="small"
              icon={<UserAddOutlined />}
              onClick={() => openAssignModal(record)}
            >
              Assign
            </Button>
          )}

          {record.status === 'RESOLVED' && (
            <Button
              type="primary"
              size="small"
              danger
              icon={<CloseCircleOutlined />}
              onClick={() => handleCloseIncident(record.id)}
            >
              Close
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
              <FileTextOutlined className="text-blue-500 text-xl" />
              <Title level={4} className="!m-0">Incident Management (Admin Panel)</Title>
            </Space>
            <Space>
              <Button icon={<SyncOutlined spin={loading} />} onClick={fetchIncidents}>
                Refresh
              </Button>
              <Button onClick={resetFilters}>
                Reset Filters
              </Button>
            </Space>
          </div>
        }
        className="rounded-lg shadow-sm border-zinc-100 dark:border-zinc-800"
      >
        {/* Search and Filters bar */}
        <div className="flex flex-wrap gap-4 mb-6 items-center">
          <div className="flex flex-1 min-w-[250px]">
            <Input
              placeholder="Search by title or description..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onPressEnter={handleSearch}
              prefix={<SearchOutlined className="text-zinc-300" />}
              className="rounded-r-none"
            />
            <Button type="primary" onClick={handleSearch} className="rounded-l-none">
              Search
            </Button>
          </div>

          <Space wrap className="text-zinc-400 dark:text-zinc-500">
            <FilterOutlined />
            <Select
              placeholder="Status"
              value={selectedStatus}
              onChange={(val) => {
                setSelectedStatus(val);
                setSelectedPriority(null);
                setSelectedCategory(null);
              }}
              className="w-[130px]"
              allowClear
            >
              <Option value="OPEN">OPEN</Option>
              <Option value="IN_PROGRESS">IN PROGRESS</Option>
              <Option value="RESOLVED">RESOLVED</Option>
              <Option value="CLOSED">CLOSED</Option>
            </Select>

            <Select
              placeholder="Priority"
              value={selectedPriority}
              onChange={(val) => {
                setSelectedPriority(val);
                setSelectedStatus(null);
                setSelectedCategory(null);
              }}
              className="w-[130px]"
              allowClear
            >
              <Option value="LOW">LOW</Option>
              <Option value="MEDIUM">MEDIUM</Option>
              <Option value="HIGH">HIGH</Option>
              <Option value="CRITICAL">CRITICAL</Option>
            </Select>

            <Select
              placeholder="Category"
              value={selectedCategory}
              onChange={(val) => {
                setSelectedCategory(val);
                setSelectedStatus(null);
                setSelectedPriority(null);
              }}
              className="w-[150px]"
              allowClear
            >
              <Option value="APPLICATION">APPLICATION</Option>
              <Option value="DATABASE">DATABASE</Option>
              <Option value="NETWORK">NETWORK</Option>
              <Option value="SECURITY">SECURITY</Option>
              <Option value="INFRASTRUCTURE">INFRASTRUCTURE</Option>
            </Select>
          </Space>
        </div>

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
          locale={{ emptyText: 'No incidents match your criteria.' }}
        />
      </Card>

      {/* Assign Engineer Dialog */}
      <Modal
        open={assignModalOpen}
        title={<Title level={4} className="!m-0">Assign Support Engineer</Title>}
        onCancel={() => setAssignModalOpen(false)}
        footer={null}
        destroyOnHidden
      >
        <div className="my-4">
          <Text strong>Ticket ID:</Text> #{selectedIncident?.id}<br />
          <Text strong>Title:</Text> {selectedIncident?.title}
        </div>

        <Form
          form={form}
          name="assign_engineer_form"
          onFinish={handleAssignSubmit}
          layout="vertical"
        >
          <Form.Item
            name="engineerId"
            label="Support Engineer"
            rules={[{ required: true, message: 'Please select an engineer to assign!' }]}
          >
            <Select placeholder="Select support engineer" showSearch optionFilterProp="children">
              {engineers.map(eng => (
                <Option key={eng.id} value={eng.id}>
                  {eng.firstName} {eng.lastName} ({eng.email})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item className="!mb-0 text-right">
            <Space>
              <Button onClick={() => setAssignModalOpen(false)}>Cancel</Button>
              <Button type="primary" htmlType="submit" loading={assignLoading}>
                Assign Engineer
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AllIncidents;
