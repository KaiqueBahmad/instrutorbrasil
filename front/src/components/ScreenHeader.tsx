import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useAuth } from '../contexts/AuthContext';

interface ScreenHeaderProps {
  onSettingsPress: () => void;
}

export default function ScreenHeader({ onSettingsPress }: ScreenHeaderProps) {
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
  };

  return (
    <View style={styles.header}>
      <Text style={styles.userName}>{user?.name}</Text>
      <View style={styles.headerActions}>
        <TouchableOpacity
          onPress={onSettingsPress}
          style={styles.settingsButton}
        >
          <Text style={styles.settingsIcon}>⋮</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={handleLogout}>
          <Text style={styles.logoutText}>Sair</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 10,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  userName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
  },
  settingsButton: {
    padding: 4,
  },
  settingsIcon: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#6b7280',
  },
  logoutText: {
    fontSize: 14,
    color: '#6b7280',
  },
});
