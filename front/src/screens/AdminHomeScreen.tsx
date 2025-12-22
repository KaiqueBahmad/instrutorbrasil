import React from 'react';
import { View, Text, TouchableOpacity, ScrollView, Alert, StyleSheet } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { colors } from '../constants/colors';

export default function AdminHomeScreen() {
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
          Admin Dashboard
        </Text>
        <Text style={styles.headerSubtitle}>
          Welcome back, {user?.name}
        </Text>
      </View>

      {/* Content */}
      <View style={styles.content}>
        {/* Admin Info Card */}
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

        {/* Stats Cards */}
        <View style={styles.statsContainer}>
          <View style={[styles.statCard, styles.statCardMargin]}>
            <Text style={styles.statNumber}>247</Text>
            <Text style={styles.statLabel}>
              Total Users
            </Text>
          </View>
          <View style={styles.statCard}>
            <Text style={[styles.statNumber, styles.statNumberSecondary]}>58</Text>
            <Text style={styles.statLabel}>
              Instructors
            </Text>
          </View>
        </View>

        {/* Admin Tools Section */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>
            Admin Tools
          </Text>
          <View>
            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonError]}
              onPress={() =>
                Alert.alert('Coming Soon', 'User management feature')
              }
            >
              <Text style={styles.actionButtonTitleError}>
                Manage Users
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                View, edit, and delete users
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonSecondary, styles.actionButtonSpacing]}
              onPress={() =>
                Alert.alert('Coming Soon', 'Instructor verification feature')
              }
            >
              <Text style={styles.actionButtonTitleSecondary}>
                Verify Instructors
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                Review and approve instructor applications
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonPrimary, styles.actionButtonSpacing]}
              onPress={() =>
                Alert.alert('Coming Soon', 'Reports feature')
              }
            >
              <Text style={styles.actionButtonTitlePrimary}>
                Reports & Analytics
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                View platform statistics and reports
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonWarning, styles.actionButtonSpacing]}
              onPress={() =>
                Alert.alert('Coming Soon', 'System settings feature')
              }
            >
              <Text style={styles.actionButtonTitleWarning}>
                System Settings
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                Configure platform settings
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.actionButton, styles.actionButtonSuccess, styles.actionButtonSpacing]}
              onPress={() =>
                Alert.alert('Coming Soon', 'Content moderation feature')
              }
            >
              <Text style={styles.actionButtonTitleSuccess}>
                Content Moderation
              </Text>
              <Text style={styles.actionButtonSubtitle}>
                Review flagged content and complaints
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
    backgroundColor: colors.error,
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
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    alignSelf: 'flex-start',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 16,
    marginTop: 4,
  },
  roleBadgeText: {
    fontSize: 14,
    color: colors.error,
    fontWeight: '600',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 24,
  },
  statCard: {
    backgroundColor: colors.surface,
    borderRadius: 8,
    padding: 16,
    flex: 1,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  statCardMargin: {
    marginRight: 8,
  },
  statNumber: {
    fontSize: 24,
    fontWeight: 'bold',
    color: colors.primary,
  },
  statNumberSecondary: {
    color: colors.secondary,
  },
  statLabel: {
    fontSize: 14,
    color: colors.textSecondary,
    marginTop: 4,
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
  actionButtonWarning: {
    backgroundColor: 'rgba(245, 158, 11, 0.1)',
  },
  actionButtonError: {
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
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
  actionButtonTitleWarning: {
    color: colors.warning,
    fontWeight: '600',
    fontSize: 16,
  },
  actionButtonTitleError: {
    color: colors.error,
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
