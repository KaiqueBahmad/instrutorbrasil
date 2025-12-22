import React, { useRef, useEffect, useState } from 'react';
import { View, StyleSheet, ActivityIndicator, Alert, Platform, Text } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { AuthStackParamList } from '../navigation/types';

type GoogleLoginScreenNavigationProp = NativeStackNavigationProp<
  AuthStackParamList,
  'GoogleLogin'
>;

interface Props {
  navigation: GoogleLoginScreenNavigationProp;
  onSuccess: (tokens: any) => void;
}

// Get API URL based on platform
const getApiUrl = () => {
  if (Platform.OS === 'web') {
    return 'http://localhost:8080';
  }
  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080';
  }
  if (Platform.OS === 'ios') {
    return 'http://localhost:8080';
  }
  return 'http://localhost:8080';
};

const API_BASE_URL = getApiUrl();

// Web implementation
function WebGoogleLogin({ navigation, onSuccess }: Props) {
  const popupRef = useRef<Window | null>(null);
  const popupCheckIntervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    // Listen for postMessage from the popup
    const handleMessage = async (event: MessageEvent) => {
      // Verify the message is from our backend
      if (event.origin !== API_BASE_URL) {
        return;
      }

      if (event.data.type === 'GOOGLE_LOGIN_SUCCESS' && event.data.data) {
        const authData = event.data.data;
        cleanup();

        try {
          await AsyncStorage.multiSet([
            ['accessToken', authData.accessToken],
            ['refreshToken', authData.refreshToken],
            ['user', JSON.stringify(authData.user)],
          ]);
          onSuccess(authData);
        } catch (error) {
          console.error('Error storing tokens:', error);
          Alert.alert('Error', 'Failed to save login data');
          navigation.goBack();
        }
      }
    };

    window.addEventListener('message', handleMessage);

    // Open OAuth URL in a popup
    const width = 500;
    const height = 600;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

    popupRef.current = window.open(
      `${API_BASE_URL}/oauth2/authorization/google`,
      'Google Login',
      `width=${width},height=${height},left=${left},top=${top}`
    );

    if (!popupRef.current) {
      Alert.alert('Error', 'Please allow popups for this site');
      navigation.goBack();
      window.removeEventListener('message', handleMessage);
      return;
    }

    // Check if popup was closed manually
    popupCheckIntervalRef.current = setInterval(() => {
      if (popupRef.current && popupRef.current.closed) {
        cleanup();
        Alert.alert('Login Cancelled', 'Google login was cancelled');
        navigation.goBack();
      }
    }, 1000);

    return () => {
      window.removeEventListener('message', handleMessage);
      cleanup();
    };
  }, []);

  const cleanup = () => {
    if (popupCheckIntervalRef.current) {
      clearInterval(popupCheckIntervalRef.current);
      popupCheckIntervalRef.current = null;
    }
    if (popupRef.current && !popupRef.current.closed) {
      popupRef.current.close();
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.loading}>
        <ActivityIndicator size="large" color="#3b82f6" />
        <Text style={styles.loadingText}>
          Please complete the login in the popup window
        </Text>
      </View>
    </View>
  );
}

// Native implementation using WebView
function NativeGoogleLogin({ navigation, onSuccess }: Props) {
  const WebView = require('react-native-webview').WebView;
  const webViewRef = useRef<any>(null);

  const handleNavigationStateChange = (navState: any) => {
    console.log('Navigation URL:', navState.url);
  };

  const handleMessage = async (event: any) => {
    try {
      const data = JSON.parse(event.nativeEvent.data);

      if (data.accessToken && data.refreshToken && data.user) {
        await AsyncStorage.multiSet([
          ['accessToken', data.accessToken],
          ['refreshToken', data.refreshToken],
          ['user', JSON.stringify(data.user)],
        ]);
        onSuccess(data);
      } else {
        Alert.alert('Login Failed', 'Could not retrieve authentication data');
        navigation.goBack();
      }
    } catch (error) {
      console.error('Error processing login response:', error);
      Alert.alert('Login Failed', 'An error occurred during authentication');
      navigation.goBack();
    }
  };

  const injectedJavaScript = `
    (function() {
      const checkForJsonResponse = () => {
        const bodyText = document.body.innerText || document.body.textContent;
        try {
          const json = JSON.parse(bodyText);
          if (json.accessToken && json.refreshToken && json.user) {
            window.ReactNativeWebView.postMessage(JSON.stringify(json));
          }
        } catch (e) {
          // Not JSON, continue
        }
      };

      window.addEventListener('load', () => {
        setTimeout(checkForJsonResponse, 500);
        setTimeout(checkForJsonResponse, 1000);
        setTimeout(checkForJsonResponse, 2000);
      });

      setTimeout(checkForJsonResponse, 500);
    })();
    true;
  `;

  return (
    <View style={styles.container}>
      <WebView
        ref={webViewRef}
        source={{ uri: `${API_BASE_URL}/oauth2/authorization/google` }}
        onNavigationStateChange={handleNavigationStateChange}
        onMessage={handleMessage}
        injectedJavaScript={injectedJavaScript}
        javaScriptEnabled={true}
        domStorageEnabled={true}
        startInLoadingState={true}
        renderLoading={() => (
          <View style={styles.loading}>
            <ActivityIndicator size="large" color="#3b82f6" />
          </View>
        )}
        onError={(syntheticEvent: any) => {
          const { nativeEvent } = syntheticEvent;
          console.error('WebView error:', nativeEvent);
          Alert.alert('Error', 'Failed to load Google login page');
          navigation.goBack();
        }}
      />
    </View>
  );
}

// Main component that chooses implementation based on platform
export default function GoogleLoginScreen(props: Props) {
  if (Platform.OS === 'web') {
    return <WebGoogleLogin {...props} />;
  }
  return <NativeGoogleLogin {...props} />;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  loading: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#ffffff',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
    paddingHorizontal: 32,
  },
});
