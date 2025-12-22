import React from 'react';
import { View, Text, TouchableOpacity, ScrollView, Alert, StyleSheet } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { colors } from '../constants/colors';

export default function UserHomeScreen() {
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    Alert.alert('Logout', 'Are you sure you want to logout?', [
      {
        text: 'Cancel',
        style: 'cancel',
      },
      {
        text: 'Logout',
        style: 'destructive',
        onPress: async () => {
          await logout();
        },
      },
    ]);
  };

  return (
    <ScrollView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>
          Welcome, {user?.name}!
        </Text>
        <Text style={styles.headerSubtitle}>User Dashboard</Text>
      </View>

      {/* Content */}
      <View style={styles.content}>
        {/* User Info Card */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>
            Your Profile
          </Text>
          <View>
            <View style={styles.infoItem}>
              <Text style={styles.infoLabel}>Name</Text>
              <Text style={styles.infoValue}>
                {user?.name}
              </Text>
            </View>
            <View style={[styles.infoItem, styles.infoItemSpacing]}>
              <Text style={styles.infoLabel}>Email</Text>
              <Text style={styles.infoValue}>
                {user?.email}
              </Text>
            </View>
            <View style={[styles.infoItem, styles.infoItemSpacing]}>
              <Text style={styles.infoLabel}>Role</Text>
              <View style={styles.roleBadge}>
                <Text style={styles.roleBadgeText}>
                  {user?.role.replace('ROLE_', '')}
                </Text>
              </View>
            </View>
          </View>
        </View>

        {/* Features Section */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>
            Quick Actions
          </Text>
          <View>
            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonPrimary]}
              onPress={() =>
                Alert.alert('Coming Soon', 'Browse instructors feature')
              }
            >
              <Text style={styles.actionButtonTitlePrimary}>
                Browse Instructors
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                Find and connect with instructors
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonSecondary, styles.actionButtonSpacing]}
              onPress={() =>
                Alert.alert('Coming Soon', 'My bookings feature')
              }
            >
              <Text style={styles.actionButtonTitleSecondary}>
                My Bookings
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                View your scheduled sessions
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonSuccess, styles.actionButtonSpacing]}
              onPress={() =>
                Alert.alert('Coming Soon', 'Messages feature')
              }
            >
              <Text style={styles.actionButtonTitleSuccess}>
                Messages
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                Chat with your instructors
              </Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Logout Button */}
        <TouchableOpacity
          style={styles.logoutButton}
          onPress={handleLogout}
        >
          <Text style={styles.logoutButtonText}>
            Logout
          </Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    backgroundColor: colors.primary,
    paddingHorizontal: 24,
    paddingTop: 48,
    paddingBottom: 32,
  },
  headerTitle: {
    color: '#ffffff',
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  headerSubtitle: {
    color: 'rgba(255, 255, 255, 0.8)',
    fontSize: 14,
  },
  content: {
    paddingHorizontal: 24,
    paddingVertical: 32,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: 8,
    padding: 24,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.textPrimary,
    marginBottom: 16,
  },
  infoItem: {
    marginBottom: 0,
  },
  infoItemSpacing: {
    marginTop: 12,
  },
  infoLabel: {
    fontSize: 14,
    color: colors.textSecondary,
    marginBottom: 4,
  },
  infoValue: {
    fontSize: 16,
    color: colors.textPrimary,
    fontWeight: '500',
  },
  roleBadge: {
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
    alignSelf: 'flex-start',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 16,
    marginTop: 4,
  },
  roleBadgeText: {
    fontSize: 14,
    color: colors.primary,
    fontWeight: '600',
  },
  actionButton: {
    borderRadius: 8,
    padding: 16,
  },
  actionButtonSpacing: {
    marginTop: 12,
  },
  actionButtonPrimary: {
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
  },
  actionButtonSecondary: {
    backgroundColor: 'rgba(139, 92, 246, 0.1)',
  },
  actionButtonSuccess: {
    backgroundColor: 'rgba(16, 185, 129, 0.1)',
  },
  actionButtonTitlePrimary: {
    color: colors.primary,
    fontWeight: '600',
    fontSize: 16,
  },
  actionButtonTitleSecondary: {
    color: colors.secondary,
    fontWeight: '600',
    fontSize: 16,
  },
  actionButtonTitleSuccess: {
    color: colors.success,
    fontWeight: '600',
    fontSize: 16,
  },
  actionButtonSubtitle: {
    color: colors.textSecondary,
    fontSize: 14,
    marginTop: 4,
  },
  logoutButton: {
    backgroundColor: colors.error,
    borderRadius: 8,
    paddingVertical: 16,
    alignItems: 'center',
  },
  logoutButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
});
