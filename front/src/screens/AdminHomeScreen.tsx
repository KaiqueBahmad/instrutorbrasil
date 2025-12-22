import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet } from 'react-native';
import ScreenHeader from '../components/ScreenHeader';
import SettingsModal from '../components/SettingsModal';

export default function AdminHomeScreen() {
  const [showSettings, setShowSettings] = useState(false);

  return (
    <View style={styles.container}>
      <ScreenHeader onSettingsPress={() => setShowSettings(true)} />
      <SettingsModal visible={showSettings} onClose={() => setShowSettings(false)} />

      <ScrollView style={styles.content}>
        {/* Título */}
        <Text style={styles.title}>Painel Admin</Text>

        {/* Cards de Informação Placeholder */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Gerenciar Usuários</Text>
          <Text style={styles.cardPlaceholder}>
            Visualize, edite e gerencie todos os usuários da plataforma
          </Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Gerenciar Instrutores</Text>
          <Text style={styles.cardPlaceholder}>
            Aprovar, remover ou editar perfis de instrutores
          </Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Relatórios</Text>
          <Text style={styles.cardPlaceholder}>
            Visualize estatísticas e relatórios da plataforma
          </Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Configurações do Sistema</Text>
          <Text style={styles.cardPlaceholder}>
            Ajuste configurações globais da plataforma
          </Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#111827',
    marginTop: 32,
    marginBottom: 24,
  },
  card: {
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    padding: 20,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#e5e7eb',
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
    marginBottom: 8,
  },
  cardPlaceholder: {
    fontSize: 14,
    color: '#6b7280',
    lineHeight: 20,
  },
});
