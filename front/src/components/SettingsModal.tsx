import React from 'react';
import { View, Text, TouchableOpacity, Modal, StyleSheet } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { Role } from '../types';

interface SettingsModalProps {
  visible: boolean;
  onClose: () => void;
}

const getRoleDisplayName = (role: Role) => {
  switch (role) {
    case Role.ADMIN:
      return 'Admin';
    case Role.INSTRUCTOR:
      return 'Instrutor';
    case Role.USER:
    default:
      return 'Usuário';
  }
};

export default function SettingsModal({ visible, onClose }: SettingsModalProps) {
  const { user, activeRole, setActiveRole } = useAuth();

  const hasMultipleRoles = user && user.roles && user.roles.length > 1;

  const handleRoleChange = (role: Role) => {
    setActiveRole(role);
    onClose();
  };

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="fade"
      onRequestClose={onClose}
    >
      <TouchableOpacity
        style={styles.modalOverlay}
        activeOpacity={1}
        onPress={onClose}
      >
        <View style={styles.modalContent}>
          <Text style={styles.modalTitle}>Configurações</Text>

          {hasMultipleRoles && (
            <View style={styles.modalSection}>
              <Text style={styles.modalSectionTitle}>Trocar Perfil</Text>
              {user?.roles.map((role) => (
                <TouchableOpacity
                  key={role}
                  style={styles.modalOption}
                  onPress={() => handleRoleChange(role)}
                >
                  <Text style={styles.modalOptionText}>
                    {getRoleDisplayName(role)}
                  </Text>
                  {activeRole === role && (
                    <Text style={styles.checkmark}>•</Text>
                  )}
                </TouchableOpacity>
              ))}
            </View>
          )}

          <TouchableOpacity
            style={styles.modalCloseButton}
            onPress={onClose}
          >
            <Text style={styles.modalCloseText}>Fechar</Text>
          </TouchableOpacity>
        </View>
      </TouchableOpacity>
    </Modal>
  );
}

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalContent: {
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 24,
    width: '100%',
    maxWidth: 400,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 20,
  },
  modalSection: {
    marginBottom: 20,
  },
  modalSectionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#6b7280',
    marginBottom: 12,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  modalOption: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
    backgroundColor: '#f9fafb',
    borderRadius: 8,
    marginBottom: 8,
  },
  modalOptionText: {
    fontSize: 16,
    color: '#111827',
  },
  checkmark: {
    fontSize: 18,
    color: '#10b981',
    fontWeight: 'bold',
  },
  modalCloseButton: {
    backgroundColor: '#111827',
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  modalCloseText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
});
