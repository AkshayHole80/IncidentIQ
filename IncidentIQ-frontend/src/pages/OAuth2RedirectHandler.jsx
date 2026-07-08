import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Spin } from 'antd';

const OAuth2RedirectHandler = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { loginWithToken } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');

    if (token) {
      loginWithToken(token).then(() => {
        navigate('/');
      }).catch((err) => {
        console.error("Authentication integration error", err);
        navigate('/login');
      });
    } else {
      navigate('/login');
    }
  }, [location, navigate, loginWithToken]);

  return (
    <div className="flex justify-center items-center h-screen bg-slate-50 dark:bg-zinc-950">
      <Spin size="large" tip="Processing Google Sign-In..." />
    </div>
  );
};

export default OAuth2RedirectHandler;
