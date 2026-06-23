import React, { useState } from 'react';
import { Layout, Menu, Button, Dropdown, Space, Avatar, Typography, Breadcrumb, Badge, Popover, List, Switch } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  PlusCircleOutlined,
  FileTextOutlined,
  UserOutlined,
  LogoutOutlined,
  BellOutlined,
  BulbOutlined,
  BulbFilled,
  SafetyCertificateOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

const MainLayout = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout, darkMode, toggleDarkMode } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Mock Notifications
  const notifications = [
    { id: 1, title: 'New Critical Incident #102 created', time: '10m ago', unread: true },
    { id: 2, title: 'AI automatically classified Category: SECURITY', time: '10m ago', unread: true },
    { id: 3, title: 'Incident #98 was resolved by engineer', time: '1h ago', unread: false },
    { id: 4, title: 'Welcome to IncidentIQ Control Panel', time: '1d ago', unread: false },
  ];

  const notificationContent = (
    <div className="w-72">
      <div className="flex justify-between p-2.5 border-b border-gray-100 dark:border-zinc-700">
        <Text strong>Notifications</Text>
        <Text type="secondary" className="text-xs cursor-pointer">Mark all read</Text>
      </div>
      <List
        size="small"
        dataSource={notifications}
        renderItem={item => (
          <List.Item
            className={`cursor-pointer px-3 py-2 ${item.unread ? 'bg-blue-500/5' : 'bg-transparent'}`}
          >
            <List.Item.Meta
              avatar={<InfoCircleOutlined className={`mt-1 ${item.unread ? 'text-blue-500' : 'text-zinc-300 dark:text-zinc-600'}`} />}
              title={<span className={`text-xs ${item.unread ? 'font-bold' : 'font-normal'}`}>{item.title}</span>}
              description={<span className="text-[10px]">{item.time}</span>}
            />
          </List.Item>
        )}
      />
    </div>
  );

  const getMenuItems = () => {
    const role = user?.role;
    const items = [
      {
        key: '/',
        icon: <DashboardOutlined />,
        label: <Link to="/">Dashboard</Link>,
      },
    ];

    if (role === 'USER') {
      items.push(
        {
          key: '/incidents/create',
          icon: <PlusCircleOutlined />,
          label: <Link to="/incidents/create">Create Incident</Link>,
        },
        {
          key: '/incidents/my',
          icon: <FileTextOutlined />,
          label: <Link to="/incidents/my">My Incidents</Link>,
        }
      );
    } else if (role === 'SUPPORT_ENGINEER') {
      items.push({
        key: '/incidents/assigned',
        icon: <FileTextOutlined />,
        label: <Link to="/incidents/assigned">Assigned Incidents</Link>,
      });
    } else if (role === 'ADMIN') {
      items.push(
        {
          key: '/incidents/all',
          icon: <FileTextOutlined />,
          label: <Link to="/incidents/all">All Incidents</Link>,
        },
        {
          key: '/incidents/create',
          icon: <PlusCircleOutlined />,
          label: <Link to="/incidents/create">Create Incident</Link>,
        },
        {
          key: '/incidents/my',
          icon: <FileTextOutlined />,
          label: <Link to="/incidents/my">My Incidents</Link>,
        }
      );
    }

    return items;
  };

  const userMenuItems = [
    {
      key: 'profile',
      label: (
        <div className="px-3 py-1">
          <div><strong>{user?.firstName} {user?.lastName}</strong></div>
          <div className="text-xs text-gray-500">{user?.email}</div>
          <div className="text-[10px] mt-1">
            <span
              className={`inline-block border font-bold rounded px-1.5 py-0.5 text-[10px] ${user?.role === 'ADMIN'
                  ? 'text-red-500 bg-red-500/10 border-red-500/20'
                  : user?.role === 'SUPPORT_ENGINEER'
                    ? 'text-blue-500 bg-blue-500/10 border-blue-500/20'
                    : 'text-green-500 bg-green-500/10 border-green-500/20'
                }`}
            >
              {user?.role}
            </span>
          </div>
        </div>
      ),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: handleLogout,
      danger: true,
    },
  ];

  // Dynamic Breadcrumb Generator
  const generateBreadcrumbs = () => {
    const pathnames = location.pathname.split('/').filter(x => x);
    const breadcrumbItems = [
      {
        title: <Link to="/"><DashboardOutlined /> Dashboard</Link>
      }
    ];

    pathnames.forEach((name, index) => {
      const isId = !isNaN(name);
      let title = name.charAt(0).toUpperCase() + name.slice(1);
      if (isId) {
        title = `Detail #${name}`;
      }

      if (index === pathnames.length - 1) {
        breadcrumbItems.push({ title });
      } else {
        const url = `/${pathnames.slice(0, index + 1).join('/')}`;
        breadcrumbItems.push({
          title: <Link to={url}>{title}</Link>
        });
      }
    });

    return breadcrumbItems;
  };

  return (
    <Layout className={`min-h-screen ${darkMode ? 'bg-zinc-950' : 'bg-slate-50'}`}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={256}
        theme={darkMode ? 'dark' : 'light'}
        className={`shadow-sm border-r ${darkMode ? 'border-zinc-800' : 'border-gray-200'}`}
      >
        <div
          className={`h-16 flex items-center transition-all duration-200 border-b ${collapsed ? 'justify-center pl-0' : 'justify-start pl-6'
            } ${darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-200'
            }`}
        >
          <SafetyCertificateOutlined className={`text-2xl text-blue-500 ${collapsed ? 'mr-0' : 'mr-3'}`} />
          {!collapsed && (
            <span className={`text-lg font-bold tracking-wide ${darkMode ? 'text-zinc-100' : 'text-slate-800'}`}>
              IncidentIQ
            </span>
          )}
        </div>
        <Menu
          theme={darkMode ? 'dark' : 'light'}
          mode="inline"
          selectedKeys={[location.pathname]}
          items={getMenuItems()}
          className="py-4 border-none"
        />
      </Sider>
      <Layout>
        <Header
          className={`px-6 flex justify-between items-center shadow-sm z-10 border-b h-16 ${darkMode ? 'bg-zinc-900 border-zinc-800' : 'bg-white border-gray-100'
            }`}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            className="text-base w-12 h-12 flex items-center justify-center"
          />

          <Space size="large" className="items-center">
            {/* Dark Mode Switcher */}
            <Space size="small" className="flex items-center">
              {darkMode ? <BulbFilled className="text-yellow-400" /> : <BulbOutlined className="text-gray-400" />}
              <Switch checked={darkMode} onChange={toggleDarkMode} checkedChildren="Dark" unCheckedChildren="Light" />
            </Space>

            {/* Notification Popover */}
            <Popover content={notificationContent} title={null} trigger="click" placement="bottomRight" arrow>
              <Badge count={2} size="small" className="cursor-pointer">
                <Button type="text" shape="circle" icon={<BellOutlined className="text-lg" />} />
              </Badge>
            </Popover>

            {/* Profile Dropdown */}
            <Dropdown menu={{ items: userMenuItems }} trigger={['click']} placement="bottomRight">
              <Space className="cursor-pointer px-2 py-1 rounded hover:bg-black/5 dark:hover:bg-white/5 transition-all">
                <Avatar className="!bg-blue-500" icon={<UserOutlined />} />
                <span className={`font-medium ${darkMode ? 'text-zinc-200' : 'text-gray-700'}`}>
                  {user?.firstName || 'User'}
                </span>
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content className="m-6 min-h-[280px] flex flex-col gap-4">
          {/* Global Breadcrumb */}
          <Breadcrumb items={generateBreadcrumbs()} className="mb-2" />

          <div className="flex-grow">
            {children}
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
