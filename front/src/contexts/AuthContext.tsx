import React, { createContext, useState, useContext, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { authAPI } from '../services/api';
import { User, AuthContextType, Role } from '../types';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [activeRole, setActiveRole] = useState<Role | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const [storedUser, accessToken, storedActiveRole] = await AsyncStorage.multiGet([
        'user',
        'accessToken',
        'activeRole',
      ]);

      if (storedUser[1] && accessToken[1]) {
        const userData = JSON.parse(storedUser[1]);
        setUser(userData);

        // Set active role: use stored one if valid, otherwise use first role
        if (storedActiveRole[1]) {
          setActiveRole(storedActiveRole[1] as Role);
        } else if (userData.roles && userData.roles.length > 0) {
          setActiveRole(userData.roles[0]);
        }
      }
    } catch (error) {
      console.error('Failed to check auth status:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const loginWithGoogle = async (authData: any): Promise<void> => {
    try {
      const { accessToken, refreshToken, user: userData } = authData;

      // Set default active role to the first role
      const defaultRole = userData.roles && userData.roles.length > 0 ? userData.roles[0] : null;

      await AsyncStorage.multiSet([
        ['accessToken', accessToken],
        ['refreshToken', refreshToken],
        ['user', JSON.stringify(userData)],
        ['activeRole', defaultRole || ''],
      ]);

      setUser(userData);
      setActiveRole(defaultRole);
    } catch (error: any) {
      console.error('Google login failed:', error);
      throw new Error('Google login failed. Please try again.');
    }
  };

  const logout = async (): Promise<void> => {
    try {
      await AsyncStorage.multiRemove(['accessToken', 'refreshToken', 'user', 'activeRole']);
      setUser(null);
      setActiveRole(null);
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  const changeActiveRole = async (role: Role): Promise<void> => {
    try {
      await AsyncStorage.setItem('activeRole', role);
      setActiveRole(role);
    } catch (error) {
      console.error('Failed to change active role:', error);
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
    activeRole,
    isLoading,
    isAuthenticated: !!user,
    loginWithGoogle,
    logout,
    refreshToken,
    setActiveRole: changeActiveRole,
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
