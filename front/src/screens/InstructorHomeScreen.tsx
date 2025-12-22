import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet } from 'react-native';
import ScreenHeader from '../components/ScreenHeader';
import SettingsModal from '../components/SettingsModal';

export default function InstrutorHomeScreen() {
  const [showSettings, setShowSettings] = useState(false);

  return (
    <View style={styles.container}>
      <ScreenHeader onSettingsPress={() => setShowSettings(true)} />
      <SettingsModal visible={showSettings} onClose={() => setShowSettings(false)} />

      <ScrollView style={styles.content}>
        {/* Título */}
        <Text style={styles.title}>Painel do Instrutor</Text>

        {/* Cards de Informação Placeholder */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Próximas Aulas</Text>
          <Text style={styles.cardPlaceholder}>
            Aqui aparecerão suas próximas aulas agendadas
          </Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Alunos Ativos</Text>
          <Text style={styles.cardPlaceholder}>
            Lista dos seus alunos atualmente matriculados
          </Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Estatísticas</Text>
          <Text style={styles.cardPlaceholder}>
            Visualize seu desempenho e progresso dos alunos
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
