import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { View, ActivityIndicator, StyleSheet } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { Role } from '../types';

import LoginScreen from '../screens/LoginScreen';
import GoogleLoginScreen from '../screens/GoogleLoginScreen';
import UserHomeScreen from '../screens/UserHomeScreen';
import InstructorHomeScreen from '../screens/InstructorHomeScreen';
import AdminHomeScreen from '../screens/AdminHomeScreen';

import { AuthStackParamList, AppStackParamList } from './types';

const AuthStack = createNativeStackNavigator<AuthStackParamList>();
const AppStack = createNativeStackNavigator<AppStackParamList>();

function AuthNavigator() {
  const { loginWithGoogle } = useAuth();

  return (
    <AuthStack.Navigator
      screenOptions={{
        headerShown: false,
        animation: 'slide_from_right',
      }}
    >
      <AuthStack.Screen name="Login" component={LoginScreen} />
      <AuthStack.Screen name="GoogleLogin">
        {(props) => (
          <GoogleLoginScreen {...props} onSuccess={loginWithGoogle} />
        )}
      </AuthStack.Screen>
    </AuthStack.Navigator>
  );
}

function AppNavigator() {
  const { activeRole } = useAuth();

  const getHomeScreen = () => {
    switch (activeRole) {
      case Role.ADMIN:
        return AdminHomeScreen;
      case Role.INSTRUCTOR:
        return InstructorHomeScreen;
      case Role.USER:
      default:
        return UserHomeScreen;
    }
  };

  return (
    <AppStack.Navigator
      screenOptions={{
        headerShown: false,
        animation: 'fade',
      }}
    >
      <AppStack.Screen name="Home" component={getHomeScreen()} />
    </AppStack.Navigator>
  );
}

export default function RootNavigator() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator size="large" color="#3b82f6" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      {isAuthenticated ? <AppNavigator /> : <AuthNavigator />}
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  loading: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f9fafb',
  },
});
