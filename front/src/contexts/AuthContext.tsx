import React, { createContext, useState, useContext, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { authAPI } from '../services/api';
import { User, AuthContextType } from '../types';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const [storedUser, accessToken] = await AsyncStorage.multiGet([
        'user',
        'accessToken',
      ]);

      if (storedUser[1] && accessToken[1]) {
        setUser(JSON.parse(storedUser[1]));
      }
    } catch (error) {
      console.error('Failed to check auth status:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (email: string, password: string): Promise<void> => {
    try {
      const response = await authAPI.login({ email, password });
      const { accessToken, refreshToken, user: userData } = response;

      await AsyncStorage.multiSet([
        ['accessToken', accessToken],
        ['refreshToken', refreshToken],
        ['user', JSON.stringify(userData)],
      ]);

      setUser(userData);
    } catch (error: any) {
      console.error('Login failed:', error);
      throw new Error(
        error.response?.data?.message || 'Login failed. Please try again.'
      );
    }
  };

  const register = async (
    name: string,
    email: string,
    password: string
  ): Promise<void> => {
    try {
      const response = await authAPI.register({ name, email, password });
      const { accessToken, refreshToken, user: userData } = response;

      await AsyncStorage.multiSet([
        ['accessToken', accessToken],
        ['refreshToken', refreshToken],
        ['user', JSON.stringify(userData)],
      ]);

      setUser(userData);
    } catch (error: any) {
      console.error('Registration failed:', error);
      throw new Error(
        error.response?.data?.message ||
          'Registration failed. Please try again.'
      );
    }
  };

  const logout = async (): Promise<void> => {
    try {
      await AsyncStorage.multiRemove(['accessToken', 'refreshToken', 'user']);
      setUser(null);
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  const refreshToken = async (): Promise<void> => {
    try {
      const storedRefreshToken = await AsyncStorage.getItem('refreshToken');
      if (!storedRefreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await authAPI.refreshToken(storedRefreshToken);
      const { accessToken, refreshToken: newRefreshToken } = response;

      await AsyncStorage.multiSet([
        ['accessToken', accessToken],
        ['refreshToken', newRefreshToken],
      ]);
    } catch (error) {
      console.error('Token refresh failed:', error);
      await logout();
    }
  };

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: !!user,
    login,
    register,
    logout,
    refreshToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
