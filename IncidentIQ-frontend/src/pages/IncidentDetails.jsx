import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Tag, Button, Steps, Space, Typography, Skeleton, Alert, Modal, Form, Select, Input, message, Grid, Row, Col, Badge, Popconfirm, Tabs, Timeline, Spin, Empty, Upload } from 'antd';
import { ArrowLeftOutlined, InfoCircleOutlined, UserAddOutlined, CheckSquareOutlined, CloseCircleOutlined, RobotOutlined, UserOutlined, CalendarOutlined, CheckCircleOutlined, EditOutlined, DeleteOutlined, UploadOutlined } from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { updateIncident, deleteIncident } from '../services/incidentService';
import { getAuditLogs } from '../services/auditService';
import AttachmentsTab from './AttachmentsTab';
import { uploadAttachment } from '../services/attachmentService';

const { Title, Paragraph, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;
const { useBreakpoint } = Grid;

const IncidentDetails = () => {
  const { id } = useParams();
  const { user, darkMode } = useAuth();
  const navigate = useNavigate();
  const screens = useBreakpoint();

  const [incident, setIncident] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [creator, setCreator] = useState(null);

  // Assignment Modal States
  const [engineers, setEngineers] = useState([]);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [assignLoading, setAssignLoading] = useState(false);
  const [engineerMap, setEngineerMap] = useState({});

  // Resolution Modal States
  const [resolveModalOpen, setResolveModalOpen] = useState(false);
  const [resolveLoading, setResolveLoading] = useState(false);
  const [resolveFileList, setResolveFileList] = useState([]);

  const resolveUploadProps = {
    onRemove: (file) => {
      const index = resolveFileList.indexOf(file);
      const newFileList = resolveFileList.slice();
      newFileList.splice(index, 1);
      setResolveFileList(newFileList);
    },
    beforeUpload: (file) => {
      const isLt20M = file.size / 1024 / 1024 < 20;
      if (!isLt20M) {
        message.error('File must be smaller than 20MB!');
        return Upload.LIST_IGNORE;
      }
      setResolveFileList([...resolveFileList, file]);
      return false;
    },
    fileList: resolveFileList,
  };

  // Edit Incident States
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editLoading, setEditLoading] = useState(false);

  // Audit Log States
  const [auditLogs, setAuditLogs] = useState([]);
  const [auditLoading, setAuditLoading] = useState(false);

  const [form] = Form.useForm();
  const [resolveForm] = Form.useForm();
  const [editForm] = Form.useForm();

  const fetchAuditHistory = async () => {
    setAuditLoading(true);
    try {
      const logs = await getAuditLogs(id);
      setAuditLogs(logs);
    } catch (err) {
      console.error('Failed to load audit history', err);
      message.error(err.response?.data?.message || 'Failed to load audit history logs.');
    } finally {
      setAuditLoading(false);
    }
  };

  const handleTabChange = (key) => {
    if (key === 'history') {
      fetchAuditHistory();
    }
  };

  const getActionColor = (action) => {
    switch (action) {
      case 'CREATED': return 'green';
      case 'ASSIGNED': return 'blue';
      case 'UPDATED': return 'gray';
      case 'RESOLVED': return 'orange';
      case 'CLOSED':
      case 'DELETED': return 'red';
      default: return 'blue';
    }
  };

  const getActionIcon = (action) => {
    switch (action) {
      case 'CREATED': return '🟢';
      case 'ASSIGNED': return '👤';
      case 'UPDATED': return '⚙️';
      case 'RESOLVED': return '✅';
      case 'CLOSED': return '🔒';
      case 'DELETED': return '❌';
      default: return 'ℹ️';
    }
  };

  const handleEditSubmit = async (values) => {
    setEditLoading(true);
    try {
      await updateIncident(id, {
        title: values.title,
        description: values.description,
      });
      message.success('Incident updated successfully!');
      setEditModalOpen(false);
      fetchIncidentDetails();
    } catch (err) {
      console.error('Failed to update incident', err);
      message.error(err.response?.data?.message || 'Failed to update incident.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDeleteConfirm = async () => {
    try {
      await deleteIncident(id);
      message.success('Incident deleted successfully!');
      navigate('/my-incidents');
    } catch (err) {
      console.error('Failed to delete incident', err);
      message.error(err.response?.data?.message || 'Failed to delete incident.');
    }
  };

  const fetchIncidentDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/api/v1/incidents/${id}`);
      const incidentData = response.data;
      setIncident(incidentData);

      // Fetch creator details using the new GET /api/v1/users/{id} endpoint
      try {
        const creatorResponse = await api.get(`/api/v1/users/${incidentData.createdBy}`);
        setCreator(creatorResponse.data);
      } catch (creatorErr) {
        console.warn(`Failed to load creator profile for user #${incidentData.createdBy}`, creatorErr);
        setCreator(null);
      }
    } catch (err) {
      console.error('Failed to load incident details', err);
      if (err.response && err.response.status === 403) {
        setError("Access Denied: You are not authorized to view this ticket. Only the creator, assignee, or admins can access it.");
      } else {
        setError("Could not load incident details. Please check the ticket number.");
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchEngineers = async () => {
    try {
      const response = await api.get('/api/v1/users/support-engineers');
      setEngineers(response.data);
      const mapping = {};
      response.data.forEach(eng => {
        mapping[eng.id] = `${eng.firstName} ${eng.lastName} (${eng.email})`;
      });
      setEngineerMap(mapping);
    } catch (err) {
      console.error('Failed to load engineers list', err);
    }
  };

  useEffect(() => {
    fetchIncidentDetails();
    fetchEngineers(); // Fetch engineers for lookup regardless, so we map names correctly
  }, [id, user]);

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

  // Convert Status to Steps Index
  const getStepIndex = (status) => {
    switch (status) {
      case 'OPEN': return 0;
      case 'IN_PROGRESS': return 1;
      case 'RESOLVED': return 2;
      case 'CLOSED': return 3;
      default: return 0;
    }
  };

  const handleAssign = async (values) => {
    setAssignLoading(true);
    try {
      await api.put(`/api/v1/incidents/${id}/assign`, {
        assignedTo: values.engineerId,
      });
      message.success('Engineer successfully assigned. Ticket set to IN PROGRESS.');
      setAssignModalOpen(false);
      fetchIncidentDetails();
    } catch (err) {
      message.error(err.response?.data?.message || 'Assignment failed.');
    } finally {
      setAssignLoading(false);
    }
  };

  const handleResolve = async (values) => {
    setResolveLoading(true);
    try {
      await api.patch(`/api/v1/incidents/${id}/resolve`, {
        resolutionNotes: values.resolutionNotes,
      });

      if (resolveFileList.length > 0) {
        try {
          await Promise.all(resolveFileList.map(file => uploadAttachment(id, file)));
        } catch (uploadError) {
          console.error('Failed to upload resolution attachments', uploadError);
          message.error('Failed to upload some resolution attachments. You can re-upload them in the Attachments tab.');
        }
      }

      message.success('Incident resolved successfully.');
      setResolveFileList([]);
      resolveForm.resetFields();
      setResolveModalOpen(false);
      fetchIncidentDetails();
    } catch (err) {
      message.error(err.response?.data?.message || 'Resolution failed.');
    } finally {
      setResolveLoading(false);
    }
  };

  const handleClose = async () => {
    try {
      await api.patch(`/api/v1/incidents/${id}/close`);
      message.success('Incident archived and CLOSED.');
      fetchIncidentDetails();
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to close ticket.');
    }
  };

  // Step renderer custom styles
  const stepsItems = [
    { title: 'OPEN', content: 'Filed & Queueing' },
    { title: 'IN PROGRESS', content: 'Assigned & Active' },
    { title: 'RESOLVED', content: 'Fixed & Verified' },
    { title: 'CLOSED', content: 'Archived by Admin' },
  ];

  return (
    <div className="max-w-[1000px] mx-auto p-4">
      <Button
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate(-1)}
        className="mb-5"
      >
        Back to Dashboard
      </Button>

      {error && (
        <Alert
          message="Authorization Blocked"
          description={error}
          type="error"
          showIcon
          className="mb-5"
        />
      )}

      {loading ? (
        <Card className="rounded-xl">
          <Skeleton active paragraph={{ rows: 10 }} />
        </Card>
      ) : incident && (
        <Card
          title={
            <div className="flex justify-between items-center flex-wrap gap-2 py-1">
              <Space size="middle">
                <InfoCircleOutlined className="text-blue-500 text-xl" />
                <span className="text-lg font-semibold">Incident Information: Ticket #{incident.id}</span>
              </Space>
              <Tag color={getStatusTagColor(incident.status)} className="text-xs px-2.5 py-1 font-bold border-none">
                {incident.status}
              </Tag>
            </div>
          }
          className="rounded-xl shadow-sm border-zinc-100 dark:border-zinc-800"
        >
          <Tabs
            defaultActiveKey="details"
            onChange={handleTabChange}
            items={[
              {
                key: 'details',
                label: 'Details',
                children: (
                  <div className="pt-4">
                    {/* SaaS Visual Steps Timeline */}
                    <div className="bg-zinc-50 dark:bg-zinc-800/50 p-6 px-4 rounded-xl mb-6 border border-zinc-100 dark:border-zinc-700">
                      <Steps
                        orientation={screens.xs ? 'vertical' : 'horizontal'}
                        current={getStepIndex(incident.status)}
                        items={stepsItems}
                        className="px-3"
                      />
                    </div>

                    <Title level={3} className="!mb-5">{incident.title}</Title>

                    <Row gutter={[24, 24]} className="mb-6">
                      <Col xs={24} md={16}>
                        <Descriptions bordered column={1} size="middle" className="bg-white dark:bg-zinc-900 border-zinc-100 dark:border-zinc-800">
                          <Descriptions.Item label={<Space><CalendarOutlined /> Filed Date</Space>}>
                            {new Date(incident.createdAt).toLocaleString()}
                          </Descriptions.Item>
                          <Descriptions.Item label={<Space><UserOutlined /> Filed By</Space>}>
                            {creator ? (
                              <Text strong>{creator.firstName} {creator.lastName} ({creator.email})</Text>
                            ) : incident.createdBy === user?.id ? (
                              <Text strong>{user.firstName} {user.lastName} (You)</Text>
                            ) : engineerMap[incident.createdBy] ? (
                              <Text strong>{engineerMap[incident.createdBy]}</Text>
                            ) : (
                              <Text>User Account #{incident.createdBy}</Text>
                            )}
                          </Descriptions.Item>
                          <Descriptions.Item label={<Space><UserOutlined /> Assigned To</Space>}>
                            {incident.assignedTo ? (
                              <Text strong>{engineerMap[incident.assignedTo] || `Engineer #${incident.assignedTo}`}</Text>
                            ) : (
                              <Text type="secondary" italic>Unassigned</Text>
                            )}
                          </Descriptions.Item>
                        </Descriptions>
                      </Col>

                      <Col xs={24} md={8}>
                        {/* Gemini AI Classification Card */}
                        <Card
                          title={
                            <Space>
                              <RobotOutlined className="text-blue-500 text-lg" />
                              <span className="text-sm font-bold">Gemini AI Classification</span>
                            </Space>
                          }
                          size="small"
                          className="bg-sky-50/20 dark:bg-zinc-800 border-sky-200 dark:border-zinc-700 rounded-lg"
                        >
                          <div className="flex flex-col gap-3">
                            <div>
                              <div className="text-[11px] font-semibold text-zinc-400 dark:text-zinc-500 tracking-wider mb-1">AI-ASSESSED CATEGORY</div>
                              <Tag color="geekblue" className="text-xs px-2 py-0.5 border-none">{incident.category}</Tag>
                            </div>
                            <div>
                              <div className="text-[11px] font-semibold text-zinc-400 dark:text-zinc-500 tracking-wider mb-1">AI-ASSESSED PRIORITY</div>
                              <Tag color={getPriorityTagColor(incident.priority)} className="text-xs px-2 py-0.5 font-bold border-none">{incident.priority}</Tag>
                            </div>
                          </div>
                        </Card>
                      </Col>
                    </Row>

                    <div className="mb-6">
                      <Text strong className="text-sm block mb-2">Detailed Incident Description</Text>
                      <div className="bg-zinc-50 dark:bg-zinc-900 p-4 px-5 rounded-lg border border-zinc-100 dark:border-zinc-800 leading-relaxed">
                        <Paragraph className="whitespace-pre-wrap !m-0 text-sm">
                          {incident.description}
                        </Paragraph>
                      </div>
                    </div>

                    {/* Resolution notes summary */}
                    {incident.resolutionNotes && (
                      <div className="mb-6">
                        <Text strong className="text-sm block mb-2 text-green-500">
                          <CheckCircleOutlined /> Resolution Summary
                        </Text>
                        <div className="bg-green-50/30 dark:bg-emerald-950/20 p-4 px-5 rounded-lg border border-green-200 dark:border-emerald-900 leading-relaxed">
                          <Paragraph className="whitespace-pre-wrap !m-0 text-sm">
                            {incident.resolutionNotes}
                          </Paragraph>
                        </div>
                      </div>
                    )}

                    {/* Dynamic Contextual actions bottom toolbar */}
                    <div className="flex justify-end border-t border-zinc-100 dark:border-zinc-800 pt-5 mt-5">
                      <Space>
                        {/* Creator Edit/Delete Actions */}
                        {incident.createdBy === user?.id && incident.status === 'OPEN' && (
                          <>
                            <Button 
                              icon={<EditOutlined />} 
                              onClick={() => {
                                editForm.setFieldsValue({
                                  title: incident.title,
                                  description: incident.description,
                                });
                                setEditModalOpen(true);
                              }}
                            >
                              Edit Incident
                            </Button>
                            <Popconfirm
                              title="Are you sure you want to delete this incident?"
                              onConfirm={handleDeleteConfirm}
                              okText="Yes"
                              cancelText="No"
                              okButtonProps={{ danger: true }}
                            >
                              <Button type="primary" danger icon={<DeleteOutlined />}>
                                Delete Incident
                              </Button>
                            </Popconfirm>
                          </>
                        )}

                        {/* Admin Actions */}
                        {user?.role === 'ADMIN' && incident.status === 'OPEN' && (
                          <Button type="primary" icon={<UserAddOutlined />} onClick={() => setAssignModalOpen(true)}>
                            Assign Support Engineer
                          </Button>
                        )}
                        {user?.role === 'ADMIN' && incident.status === 'RESOLVED' && (
                          <Button type="primary" danger icon={<CloseCircleOutlined />} onClick={handleClose}>
                            Archive & Close Ticket
                          </Button>
                        )}

                        {/* Support Engineer Actions */}
                        {user?.role === 'SUPPORT_ENGINEER' && incident.status === 'IN_PROGRESS' && incident.assignedTo === user.id && (
                          <Button
                            type="primary"
                            icon={<CheckSquareOutlined />}
                            onClick={() => setResolveModalOpen(true)}
                            className="!bg-green-500 !border-green-500 hover:!bg-green-600 hover:!border-green-600 text-white"
                          >
                            Resolve Incident
                          </Button>
                        )}
                      </Space>
                    </div>
                  </div>
                )
              },
              {
                key: 'history',
                label: 'History',
                children: (
                  <div className="pt-6 pb-2">
                    {auditLoading ? (
                      <div className="flex justify-center items-center py-16">
                        <Spin size="large" tip="Loading audit logs..." />
                      </div>
                    ) : auditLogs.length === 0 ? (
                      <Empty description="No history recorded for this incident." className="py-12" />
                    ) : (
                      <div className="max-w-[650px] mx-auto">
                        <Timeline
                          mode="left"
                          items={auditLogs.map((log) => ({
                            color: getActionColor(log.action),
                            children: (
                              <div className="mb-4">
                                <div className="flex items-center gap-2">
                                  <span className="font-bold text-sm text-slate-800 dark:text-zinc-100">
                                    {getActionIcon(log.action)} {log.action}
                                  </span>
                                </div>
                                <div className="text-xs font-semibold text-zinc-400 dark:text-zinc-500 mt-1">
                                  By: {log.userName || 'System'}
                                </div>
                                <div className="text-sm text-slate-600 dark:text-zinc-300 mt-1.5 bg-zinc-50 dark:bg-zinc-900/40 p-3 rounded-lg border border-zinc-100 dark:border-zinc-800/80 leading-relaxed">
                                  {log.details}
                                </div>
                                <div className="text-[10px] text-zinc-400 dark:text-zinc-500 mt-1.5">
                                  {log.createdAt ? new Date(log.createdAt).toLocaleString() : 'N/A'}
                                </div>
                              </div>
                            )
                          }))}
                        />
                      </div>
                    )}
                  </div>
                )
              },
              {
                key: 'attachments',
                label: 'Attachments',
                children: <AttachmentsTab incidentId={id} incidentStatus={incident.status} />
              }
            ]}
          />
        </Card>
      )}

      {/* Assign Engineer Modal Dialog */}
      <Modal
        open={assignModalOpen}
        title={<Title level={4} className="!m-0">Assign Support Engineer</Title>}
        onCancel={() => setAssignModalOpen(false)}
        footer={null}
        destroyOnHidden
      >
        <div className="my-4">
          <Text strong>Incident Summary:</Text>
          <Paragraph type="secondary" className="mt-1">
            {incident?.title}
          </Paragraph>
        </div>

        <Form form={form} name="details_assign_engineer" onFinish={handleAssign} layout="vertical">
          <Form.Item
            name="engineerId"
            label="Choose Engineer"
            rules={[{ required: true, message: 'Please select an engineer!' }]}
          >
            <Select placeholder="Select support engineer" showSearch optionFilterProp="children">
              {engineers.map(eng => (
                <Option key={eng.id} value={eng.id}>
                  {eng.firstName} {eng.lastName} ({eng.email})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item className="text-right !mb-0">
            <Space>
              <Button onClick={() => setAssignModalOpen(false)}>Cancel</Button>
              <Button type="primary" htmlType="submit" loading={assignLoading}>
                Assign Engineer
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Resolve Modal Dialog */}
      <Modal
        open={resolveModalOpen}
        title={<Title level={4} className="!m-0">Document Ticket Resolution</Title>}
        onCancel={() => {
          setResolveFileList([]);
          setResolveModalOpen(false);
        }}
        footer={null}
        destroyOnHidden
      >
        <Form form={resolveForm} name="details_resolve_ticket" onFinish={handleResolve} layout="vertical">
          <Form.Item
            name="resolutionNotes"
            label="Resolution Summary Notes"
            rules={[
              { required: true, message: 'Please explain how you fixed this incident!' },
              { min: 10, message: 'Explanation must be at least 10 characters long!' }
            ]}
          >
            <TextArea rows={5} placeholder="Describe root cause and solution details..." />
          </Form.Item>

          <Form.Item label="Resolution Proof/Attachments (Optional)">
            <Upload {...resolveUploadProps} listType="picture" multiple>
              <Button icon={<UploadOutlined />}>Select Attachment</Button>
            </Upload>
          </Form.Item>

          <Form.Item className="text-right !mb-0">
            <Space>
              <Button onClick={() => {
                setResolveFileList([]);
                setResolveModalOpen(false);
              }}>Cancel</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={resolveLoading}
                className="!bg-green-500 !border-green-500 hover:!bg-green-600 hover:!border-green-600 text-white"
              >
                Resolve Ticket
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Edit Modal Dialog */}
      <Modal
        open={editModalOpen}
        title={<Title level={4} className="!m-0">Edit Incident Details</Title>}
        onCancel={() => setEditModalOpen(false)}
        footer={null}
        destroyOnHidden
      >
        <Form
          form={editForm}
          name="details_edit_incident"
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
              <Button onClick={() => setEditModalOpen(false)}>Cancel</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={editLoading}
              >
                Save Changes
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default IncidentDetails;
