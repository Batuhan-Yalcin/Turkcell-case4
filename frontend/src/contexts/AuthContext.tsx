import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../types';
import apiService from '../services/api';

interface AuthContextType {
  user: any | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<any | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        try {
            // Token'ı doğrula ve msisdn'e göre kullanıcıyı çek
          const storedMsisdn = localStorage.getItem('msisdn');
          if (storedMsisdn) {
            const resp = await apiService.getUserByMsisdn(storedMsisdn);
            setUser(resp.data);
          } else {
            const userInfo = await apiService.getUsers();
            if (userInfo.data.users && userInfo.data.users.length > 0) {
              setUser(userInfo.data.users[0]);
            }
          }
        } catch (error) {
          console.error('Token validation failed:', error);
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (request: LoginRequest) => {
    try {
      setIsLoading(true);
      const response = await apiService.login(request);
      const { accessToken, refreshToken, msisdn, role } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('msisdn', msisdn);
      
      try {
        const resp = await apiService.getUserByMsisdn(msisdn);
        setUser(resp.data);
      } catch {
        setUser({ msisdn, role });
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (request: RegisterRequest) => {
    try {
      setIsLoading(true);
      const response = await apiService.register(request);
      const { accessToken, refreshToken, msisdn, role } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('msisdn', msisdn);
      
      try {
        const resp = await apiService.getUserByMsisdn(msisdn);
        setUser(resp.data);
      } catch {
        setUser({ msisdn, role });
      }
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  };

  const refreshToken = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        const response = await apiService.refreshToken(refreshToken);
        const { accessToken, refreshToken: newRefreshToken } = response.data;
        
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
      }
    } catch (error) {
      console.error('Token refresh failed:', error);
      logout();
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    register,
    logout,
    refreshToken,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
