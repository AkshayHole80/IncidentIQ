import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Typography, Button, Space, Skeleton, Alert, Table, Tag, Timeline, Badge, Input, Tooltip, message } from 'antd';
import {
  FileTextOutlined,
  ClockCircleOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  AlertOutlined,
  PlusOutlined,
  ArrowRightOutlined,
  SearchOutlined,
  SyncOutlined,
  EyeOutlined,
  LineChartOutlined,
  PieChartOutlined,
  BarChartOutlined,
  BellOutlined,
} from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip as ChartTooltip, LineChart, Line, CartesianGrid, Legend } from 'recharts';
import { useAuth } from '../context/AuthContext';
import { useNotification, formatRelativeTime } from '../context/NotificationContext';
import api from '../api/axiosConfig';
import { sortIncidents } from '../services/incidentService';

const { Title, Paragraph, Text } = Typography;

const Dashboard = () => {
  const { user, darkMode } = useAuth();
  const navigate = useNavigate();
  const getStatLabel = (key) => {
    const role = user?.role;
    if (role === 'USER') {
      const labels = {
        total: 'My Incidents',
        open: 'My Open',
        inProgress: 'My In Progress',
        resolved: 'My Resolved',
        closed: 'My Closed',
        critical: 'My Critical'
      };
      return labels[key];
    } else if (role === 'SUPPORT_ENGINEER') {
      const labels = {
        total: 'Assigned Incidents',
        open: 'Open Assigned',
        inProgress: 'In Progress Assigned',
        resolved: 'Resolved Assigned',
        closed: 'Closed Assigned',
        critical: 'Critical Assigned'
      };
      return labels[key];
    } else {
      const labels = {
        total: 'Total Incidents',
        open: 'Open',
        inProgress: 'In Progress',
        resolved: 'Resolved',
        closed: 'Closed',
        critical: 'Critical'
      };
      return labels[key];
    }
  };
  const { 
    notifications, 
    loading: notificationsLoading, 
    setDrawerOpen, 
    markAsRead: markNotificationRead 
  } = useNotification();
  
  // Loading & Error States
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Dynamic Incident Lists & Stats
  const [allIncidents, setAllIncidents] = useState([]);
  const [filteredIncidents, setFilteredIncidents] = useState([]);
  const [stats, setStats] = useState({
    totalIncidents: 0,
    openIncidents: 0,
    inProgressIncidents: 0,
    resolvedIncidents: 0,
    closedIncidents: 0,
    criticalIncidents: 0
  });

  // Table & Filter States
  const [selectedFilter, setSelectedFilter] = useState({ type: 'ALL', value: null });
  const [searchQuery, setSearchQuery] = useState('');

  // Chart Data States
  const [statusChartData, setStatusChartData] = useState([]);
  const [priorityChartData, setPriorityChartData] = useState([]);
  const [trendChartData, setTrendChartData] = useState([]);

  // Fetch Dashboard Data
  const fetchDashboardData = async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    try {
      const statsRes = await api.get('/v1/incidents/stats');
      setStats(statsRes.data);

      let incidentsRes;
      if (user.role === 'ADMIN') {
        incidentsRes = await api.get('/v1/incidents/search', { params: { keyword: '' } });
      } else if (user.role === 'SUPPORT_ENGINEER') {
        incidentsRes = await api.get('/v1/incidents/assigned');
      } else {
        incidentsRes = await api.get('/v1/incidents');
      }

      const data = sortIncidents(incidentsRes.data);
      setAllIncidents(data);
      setFilteredIncidents(data);

      computeChartData(data);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
      setError('Could not establish secure link to API Gateway. Verify microservices are operational.');
      message.error('Gateway fetch failure.');
    } finally {
      setLoading(false);
    }
  };

  const computeChartData = (data) => {
    // A. Status Pie Chart
    const statusCounts = {};
    data.forEach(inc => {
      statusCounts[inc.status] = (statusCounts[inc.status] || 0) + 1;
    });
    const statusChart = Object.keys(statusCounts).map(status => ({
      name: status,
      value: statusCounts[status]
    }));
    setStatusChartData(statusChart);

    // B. Priority Bar Chart
    const priorityCounts = { LOW: 0, MEDIUM: 0, HIGH: 0, CRITICAL: 0 };
    data.forEach(inc => {
      if (priorityCounts[inc.priority] !== undefined) {
        priorityCounts[inc.priority]++;
      }
    });
    const priorityChart = Object.keys(priorityCounts).map(prio => ({
      priority: prio,
      count: priorityCounts[prio]
    }));
    setPriorityChartData(priorityChart);

    // C. Trend Line Chart
    const trendMap = {};
    data.forEach(inc => {
      if (inc.createdAt) {
        const date = inc.createdAt.split('T')[0];
        trendMap[date] = (trendMap[date] || 0) + 1;
      }
    });
    const sortedDates = Object.keys(trendMap).sort((a, b) => new Date(a) - new Date(b));
    const trendChart = sortedDates.map(date => ({
      date,
      incidents: trendMap[date]
    }));
    setTrendChartData(trendChart.slice(-7));
  };

  useEffect(() => {
    if (user) {
      fetchDashboardData();
    }
  }, [user]);

  const handleMetricCardClick = (type, value) => {
    setSelectedFilter({ type, value });
    filterIncidentList(type, value, searchQuery);
  };

  const handleSearch = (e) => {
    const val = e.target.value;
    setSearchQuery(val);
    filterIncidentList(selectedFilter.type, selectedFilter.value, val);
  };

  const filterIncidentList = (filterType, filterValue, query) => {
    let list = [...allIncidents];

    if (filterType === 'STATUS') {
      list = list.filter(inc => inc.status === filterValue);
    } else if (filterType === 'PRIORITY') {
      list = list.filter(inc => inc.priority === filterValue);
    }

    if (query) {
      const q = query.toLowerCase();
      list = list.filter(inc => 
        inc.title.toLowerCase().includes(q) || 
        inc.description.toLowerCase().includes(q) ||
        String(inc.id).includes(q)
      );
    }

    setFilteredIncidents(list);
  };

  const resetFilters = () => {
    setSelectedFilter({ type: 'ALL', value: null });
    setSearchQuery('');
    setFilteredIncidents(allIncidents);
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

  const COLORS = {
    OPEN: '#ff4d4f',
    IN_PROGRESS: '#1890ff',
    RESOLVED: '#52c41a',
    CLOSED: '#8c8c8c',
    CRITICAL: '#ff4d4f',
    HIGH: '#ffa940',
    MEDIUM: '#1890ff',
    LOW: '#73d13d'
  };

  // KPI card style helper using Tailwind and fallback bg color
  const getCardBg = (active, color) => {
    if (active) return { background: `${color}25`, borderColor: color };
    return {};
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
      width: '120px',
      render: (status) => <Tag color={getStatusTagColor(status)}>{status}</Tag>,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: '160px',
      render: (dateStr) => dateStr ? new Date(dateStr).toLocaleDateString() : 'N/A',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: '80px',
      render: (_, record) => (
        <Tooltip title="View Details">
          <Button
            type="text"
            shape="circle"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/incidents/${record.id}`)}
          />
        </Tooltip>
      ),
    },
  ];

  const getQuickActionCards = () => {
    const role = user?.role;
    if (role === 'USER') {
      return (
        <Card title="Quick Actions" size="small" className="rounded-xl shadow-sm border border-gray-100 dark:border-zinc-800">
          <Space orientation="vertical" className="w-full">
            <Button type="primary" icon={<PlusOutlined />} block onClick={() => navigate('/incidents/create')} className="rounded-lg h-9">
              Report Incident
            </Button>
            <Button icon={<FileTextOutlined />} block onClick={() => navigate('/incidents/my')} className="rounded-lg h-9">
              My Tickets
            </Button>
          </Space>
        </Card>
      );
    } else if (role === 'SUPPORT_ENGINEER') {
      return (
        <Card title="Quick Actions" size="small" className="rounded-xl shadow-sm border border-gray-100 dark:border-zinc-800">
          <Space orientation="vertical" className="w-full">
            <Button type="primary" icon={<FileTextOutlined />} block onClick={() => navigate('/incidents/assigned')} className="rounded-lg h-9">
              Assigned Tickets
            </Button>
          </Space>
        </Card>
      );
    } else if (role === 'ADMIN') {
      return (
        <Card title="Quick Actions" size="small" className="rounded-xl shadow-sm border border-gray-100 dark:border-zinc-800">
          <Space orientation="vertical" className="w-full">
            <Button type="primary" icon={<PlusOutlined />} block onClick={() => navigate('/incidents/all')} className="rounded-lg h-9">
              Assign Incidents
            </Button>
            <Button block icon={<LineChartOutlined />} onClick={resetFilters} className="rounded-lg h-9">
              Refresh Metrics
            </Button>
          </Space>
        </Card>
      );
    }
    return null;
  };

  return (
    <div>
      {/* SaaS Welcome Banner */}
      <div className={`p-6 md:p-8 rounded-2xl text-white mb-6 border shadow-md flex flex-wrap justify-between items-center gap-4 ${
        darkMode ? 'bg-gradient-to-r from-zinc-800 to-zinc-950 border-zinc-700' : 'bg-gradient-to-r from-blue-500 to-blue-600 border-none'
      }`}>
        <div>
          <Space align="center" size="middle" className="flex-wrap">
            <Title level={2} className="!m-0 !text-white">
              Welcome back, {user?.firstName || 'User'}
            </Title>
            <Tag color={user?.role === 'ADMIN' ? 'red' : user?.role === 'SUPPORT_ENGINEER' ? 'blue' : 'green'} className="font-bold border-none px-2.5 py-0.5">
              {user?.role}
            </Tag>
          </Space>
          <Paragraph className={`mt-2 text-sm md:text-base mb-0 ${darkMode ? 'text-zinc-400' : 'text-blue-100'}`}>
            Real-time incident management dashboard. Systems status is optimal.
          </Paragraph>
        </div>
        <div>
          <Button icon={<SyncOutlined spin={loading} />} onClick={fetchDashboardData} className="rounded-lg">
            Sync Dashboard
          </Button>
        </div>
      </div>

      {error && (
        <Alert
          message="System Warning"
          description={error}
          type="warning"
          showIcon
          className="mb-6"
        />
      )}

      {/* KPI Cards Section */}
      <Row gutter={[16, 16]} className="mb-6">
        <Col xs={24} sm={12} md={8} lg={4}>
          <div
            className={`p-5 rounded-2xl cursor-pointer border transition-all duration-300 shadow-sm hover:shadow-md hover:-translate-y-1 ${
              darkMode ? 'bg-zinc-800 border-zinc-700' : 'bg-white border-gray-100'
            }`}
            style={getCardBg(selectedFilter.type === 'ALL', '#1890ff')}
            onClick={resetFilters}
          >
            <Statistic
              title={<Text type="secondary" strong className="text-xs">{getStatLabel('total')}</Text>}
              value={stats.totalIncidents}
              prefix={<FileTextOutlined className="text-blue-500 mr-1" />}
              styles={{ content: { fontSize: '24px', fontWeight: 'bold' } }}
            />
          </div>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <div
            className={`p-5 rounded-2xl cursor-pointer border transition-all duration-300 shadow-sm hover:shadow-md hover:-translate-y-1 ${
              darkMode ? 'bg-zinc-800 border-zinc-700' : 'bg-white border-gray-100'
            }`}
            style={getCardBg(selectedFilter.type === 'STATUS' && selectedFilter.value === 'OPEN', '#ff4d4f')}
            onClick={() => handleMetricCardClick('STATUS', 'OPEN')}
          >
            <Statistic
              title={<Text type="secondary" strong className="text-xs">{getStatLabel('open')}</Text>}
              value={stats.openIncidents}
              prefix={<ClockCircleOutlined className="text-red-500 mr-1" />}
              styles={{ content: { fontSize: '24px', fontWeight: 'bold' } }}
            />
          </div>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <div
            className={`p-5 rounded-2xl cursor-pointer border transition-all duration-300 shadow-sm hover:shadow-md hover:-translate-y-1 ${
              darkMode ? 'bg-zinc-800 border-zinc-700' : 'bg-white border-gray-100'
            }`}
            style={getCardBg(selectedFilter.type === 'STATUS' && selectedFilter.value === 'IN_PROGRESS', '#1890ff')}
            onClick={() => handleMetricCardClick('STATUS', 'IN_PROGRESS')}
          >
            <Statistic
              title={<Text type="secondary" strong className="text-xs">{getStatLabel('inProgress')}</Text>}
              value={stats.inProgressIncidents}
              prefix={<PlayCircleOutlined className="text-blue-500 mr-1" />}
              styles={{ content: { fontSize: '24px', fontWeight: 'bold' } }}
            />
          </div>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <div
            className={`p-5 rounded-2xl cursor-pointer border transition-all duration-300 shadow-sm hover:shadow-md hover:-translate-y-1 ${
              darkMode ? 'bg-zinc-800 border-zinc-700' : 'bg-white border-gray-100'
            }`}
            style={getCardBg(selectedFilter.type === 'STATUS' && selectedFilter.value === 'RESOLVED', '#52c41a')}
            onClick={() => handleMetricCardClick('STATUS', 'RESOLVED')}
          >
            <Statistic
              title={<Text type="secondary" strong className="text-xs">{getStatLabel('resolved')}</Text>}
              value={stats.resolvedIncidents}
              prefix={<CheckCircleOutlined className="text-green-500 mr-1" />}
              styles={{ content: { fontSize: '24px', fontWeight: 'bold' } }}
            />
          </div>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <div
            className={`p-5 rounded-2xl cursor-pointer border transition-all duration-300 shadow-sm hover:shadow-md hover:-translate-y-1 ${
              darkMode ? 'bg-zinc-800 border-zinc-700' : 'bg-white border-gray-100'
            }`}
            style={getCardBg(selectedFilter.type === 'STATUS' && selectedFilter.value === 'CLOSED', '#8c8c8c')}
            onClick={() => handleMetricCardClick('STATUS', 'CLOSED')}
          >
            <Statistic
              title={<Text type="secondary" strong className="text-xs">{getStatLabel('closed')}</Text>}
              value={stats.closedIncidents}
              prefix={<CloseCircleOutlined className="text-gray-500 mr-1" />}
              styles={{ content: { fontSize: '24px', fontWeight: 'bold' } }}
            />
          </div>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <div
            className={`p-5 rounded-2xl cursor-pointer border transition-all duration-300 shadow-sm hover:shadow-md hover:-translate-y-1 ${
              darkMode ? 'bg-zinc-800 border-zinc-700' : 'bg-white border-gray-100'
            }`}
            style={getCardBg(selectedFilter.type === 'PRIORITY' && selectedFilter.value === 'CRITICAL', '#ff4d4f')}
            onClick={() => handleMetricCardClick('PRIORITY', 'CRITICAL')}
          >
            <Statistic
              title={<Text type="secondary" strong className="text-xs">{getStatLabel('critical')}</Text>}
              value={stats.criticalIncidents}
              prefix={<AlertOutlined className="text-red-500 mr-1" />}
              styles={{ content: { fontSize: '24px', fontWeight: 'bold' } }}
            />
          </div>
        </Col>
      </Row>

      {/* Main Panel Grid */}
      <Row gutter={[24, 24]} className="mb-6">
        <Col xs={24} lg={16}>
          <Card
            title={
              <Space className="flex items-center">
                <LineChartOutlined className="text-blue-500 text-lg" />
                <span>Interactive Performance Analytics</span>
              </Space>
            }
            className="rounded-xl shadow-sm border border-gray-100 dark:border-zinc-800 min-h-[400px]"
          >
            {loading ? (
              <Skeleton active paragraph={{ rows: 8 }} />
            ) : (
              <Row gutter={[16, 24]}>
                <Col xs={24} md={12}>
                  <div className="text-center mb-2">
                    <Text strong><PieChartOutlined /> Incidents By Status</Text>
                  </div>
                  <div className="h-56">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={statusChartData}
                          innerRadius={60}
                          outerRadius={80}
                          paddingAngle={5}
                          dataKey="value"
                        >
                          {statusChartData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[entry.name] || '#1890ff'} />
                          ))}
                        </Pie>
                        <ChartTooltip />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </Col>

                <Col xs={24} md={12}>
                  <div className="text-center mb-2">
                    <Text strong><BarChartOutlined /> Priority Severity Breakdown</Text>
                  </div>
                  <div className="h-56">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={priorityChartData}>
                        <XAxis dataKey="priority" />
                        <YAxis allowDecimals={false} />
                        <ChartTooltip />
                        <Bar dataKey="count" fill="#1890ff" radius={[4, 4, 0, 0]}>
                          {priorityChartData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[entry.priority] || '#1890ff'} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </Col>

                <Col xs={24}>
                  <div className="text-center mb-2 mt-4">
                    <Text strong><LineChartOutlined /> Incident Log Trend (Last 7 Active Dates)</Text>
                  </div>
                  <div className="h-44">
                    {trendChartData.length === 0 ? (
                      <div className="flex justify-center items-center h-full">
                        <Text type="secondary">Inconsistent date logging data. Trend chart offline.</Text>
                      </div>
                    ) : (
                      <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={trendChartData}>
                          <CartesianGrid strokeDasharray="3 3" vertical={false} />
                          <XAxis dataKey="date" />
                          <YAxis allowDecimals={false} />
                          <ChartTooltip />
                          <Line type="monotone" dataKey="incidents" stroke="#1890ff" strokeWidth={3} dot={{ r: 4 }} />
                        </LineChart>
                      </ResponsiveContainer>
                    )}
                  </div>
                </Col>
              </Row>
            )}
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Space orientation="vertical" size="large" className="w-full">
            {getQuickActionCards()}

            {/* Recent Activity Panel */}
            <Card
              title={
                <Space className="flex items-center">
                  <SyncOutlined spin={loading} className="text-green-500" />
                  <span>Recent Activity Log</span>
                </Space>
              }
              className="rounded-xl shadow-sm border border-gray-100 dark:border-zinc-800"
            >
              {loading ? (
                <Skeleton active paragraph={{ rows: 6 }} />
              ) : allIncidents.length === 0 ? (
                <Text type="secondary">No system logs registered.</Text>
              ) : (
                <div className="max-h-[340px] overflow-y-auto pr-1">
                  <Timeline
                    size="small"
                    items={allIncidents.slice(0, 5).map(inc => {
                      let timelineColor = 'blue';
                      if (inc.status === 'RESOLVED') timelineColor = 'green';
                      if (inc.status === 'OPEN') timelineColor = 'red';
                      
                      return {
                        color: timelineColor,
                        content: (
                          <div className="mb-2">
                            <div className="flex justify-between items-start">
                              <Text strong className="text-xs hover:text-blue-500 transition-all truncate block max-w-[200px]">
                                <Link to={`/incidents/${inc.id}`}>#{inc.id} {inc.title}</Link>
                              </Text>
                            </div>
                            <div className="mt-1 flex items-center">
                              <Tag color={getPriorityTagColor(inc.priority)} className="text-[9px] font-bold px-1.5 py-0">
                                {inc.priority}
                              </Tag>
                              <span className="text-[10px] text-gray-400 ml-2">
                                {inc.createdAt ? new Date(inc.createdAt).toLocaleTimeString() : ''}
                              </span>
                            </div>
                          </div>
                        )
                      };
                    })}
                  />
                </div>
              )}
            </Card>

            {/* Recent Notifications Widget Card */}
            <Card
              title={
                <Space className="flex items-center">
                  <BellOutlined className="text-blue-500 text-lg" />
                  <span>Recent Notifications</span>
                </Space>
              }
              extra={
                <Button 
                  type="link" 
                  size="small" 
                  onClick={() => setDrawerOpen(true)}
                  className="p-0 text-xs font-semibold"
                >
                  View All
                </Button>
              }
              className="rounded-xl shadow-sm border border-gray-100 dark:border-zinc-800"
            >
              {notificationsLoading ? (
                <Skeleton active paragraph={{ rows: 4 }} />
              ) : notifications.length === 0 ? (
                <Text type="secondary" className="block text-center py-4">No recent notifications.</Text>
              ) : (
                <div className="flex flex-col gap-3">
                  {notifications.slice(0, 5).map(item => {
                    const ticketMatch = item.message?.match(/#(\d+)/);
                    const ticketId = ticketMatch ? ticketMatch[1] : null;

                    const handleItemClick = async () => {
                      if (!item.read) {
                        await markNotificationRead(item.id);
                      }
                      if (ticketId) {
                        navigate(`/incidents/${ticketId}`);
                      }
                    };

                    return (
                      <div 
                        key={item.id}
                        onClick={handleItemClick}
                        className={`group flex items-start gap-2.5 p-2 rounded-lg cursor-pointer transition-all hover:bg-slate-50 dark:hover:bg-zinc-800/40 ${
                          !item.read ? 'bg-blue-500/5' : ''
                        }`}
                      >
                        <Badge 
                          status={item.read ? 'default' : 'processing'} 
                          className="mt-1.5 flex-shrink-0" 
                        />
                        <div className="flex-grow min-w-0">
                          <div className="flex justify-between items-baseline gap-2">
                            <Text 
                              strong={!item.read} 
                              className={`text-xs block truncate ${
                                !item.read ? 'text-slate-800 dark:text-zinc-100' : 'text-slate-600 dark:text-zinc-400'
                              }`}
                            >
                              {item.title}
                            </Text>
                            <span className="text-[9px] text-zinc-400 dark:text-zinc-500 flex-shrink-0">
                              {formatRelativeTime(item.createdAt)}
                            </span>
                          </div>
                          <Paragraph 
                            ellipsis={{ rows: 2 }} 
                            className={`!m-0 text-[11px] leading-snug mt-0.5 ${
                              !item.read ? 'text-slate-700 dark:text-zinc-200' : 'text-slate-400 dark:text-zinc-500'
                            }`}
                          >
                            {item.message}
                          </Paragraph>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </Card>
          </Space>
        </Col>
      </Row>

      {/* Interactive Table List */}
      <Card
        title={
          <div className="flex justify-between items-center flex-wrap gap-4">
            <Space align="center" className="flex-wrap">
              <Title level={4} className="!m-0">Incident Queue</Title>
              {selectedFilter.type !== 'ALL' && (
                <Tag closable onClose={resetFilters} color="blue" className="font-semibold">
                  Filtered: {selectedFilter.type} = {selectedFilter.value}
                </Tag>
              )}
            </Space>
            
            <Input
              placeholder="Search ID, title, description..."
              value={searchQuery}
              onChange={handleSearch}
              prefix={<SearchOutlined className="text-gray-400" />}
              className="w-72 rounded-lg"
              allowClear
            />
          </div>
        }
        className="rounded-xl shadow-sm border border-gray-100 dark:border-zinc-800"
      >
        <Table
          columns={columns}
          dataSource={filteredIncidents}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 5 }}
          locale={{ emptyText: 'No incidents match your filter or search query.' }}
        />
      </Card>
    </div>
  );
};

export default Dashboard;
