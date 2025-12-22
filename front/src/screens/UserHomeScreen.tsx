import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, Alert, StyleSheet } from 'react-native';
import ScreenHeader from '../components/ScreenHeader';
import SettingsModal from '../components/SettingsModal';

type VehicleType = 'carro' | 'moto' | 'onibus' | 'caminhao';

const VEHICLE_TYPES: { id: VehicleType; label: string }[] = [
  { id: 'carro', label: 'Carro' },
  { id: 'moto', label: 'Moto' },
  { id: 'onibus', label: 'Ônibus' },
  { id: 'caminhao', label: 'Caminhão' },
];

export default function UserHomeScreen() {
  const [searchQuery, setSearchQuery] = useState('');
  const [showSettings, setShowSettings] = useState(false);
  const [selectedVehicles, setSelectedVehicles] = useState<VehicleType[]>([]);
  const [showDropdown, setShowDropdown] = useState(false);

  const toggleVehicle = (vehicleId: VehicleType) => {
    setSelectedVehicles((prev) =>
      prev.includes(vehicleId)
        ? prev.filter((id) => id !== vehicleId)
        : [...prev, vehicleId]
    );
  };

  const getDropdownLabel = () => {
    if (selectedVehicles.length === 0) return 'Filtrar';
    return `${selectedVehicles.length} filtro${selectedVehicles.length > 1 ? 's' : ''}`;
  };

  const handleSearch = () => {
    const filters = {
      query: searchQuery.trim(),
      vehicles: selectedVehicles,
    };
    Alert.alert(
      'Busca',
      `Termo: ${filters.query || 'Todos'}\nVeículos: ${
        filters.vehicles.length > 0
          ? filters.vehicles.join(', ')
          : 'Todos'
      }`
    );
  };

  return (
    <View style={styles.container}>
      <ScreenHeader onSettingsPress={() => setShowSettings(true)} />
      <SettingsModal visible={showSettings} onClose={() => setShowSettings(false)} />

      <ScrollView style={styles.content}>
        {/* Título */}
        <Text style={styles.title}>InstructorBrasil</Text>

        {/* Barra de busca */}
        <View style={styles.searchContainer}>
          <TextInput
            style={styles.searchInput}
            placeholder="Buscar instrutores, categorias..."
            placeholderTextColor="#9ca3af"
            value={searchQuery}
            onChangeText={setSearchQuery}
            onSubmitEditing={handleSearch}
            returnKeyType="search"
          />

          {/* Dropdown de veículos */}
          <View style={styles.dropdownWrapper}>
            <TouchableOpacity
              style={styles.dropdownButton}
              onPress={() => setShowDropdown(!showDropdown)}
            >
              <Text style={styles.dropdownButtonText}>{getDropdownLabel()}</Text>
            </TouchableOpacity>

            {/* Menu dropdown */}
            {showDropdown && (
              <View style={styles.dropdownMenu}>
                {VEHICLE_TYPES.map((vehicle) => {
                  const isSelected = selectedVehicles.includes(vehicle.id);
                  return (
                    <TouchableOpacity
                      key={vehicle.id}
                      style={styles.dropdownItem}
                      onPress={() => toggleVehicle(vehicle.id)}
                    >
                      <Text style={styles.dropdownItemText}>{vehicle.label}</Text>
                      <Text style={styles.checkbox}>{isSelected ? '☑' : '☐'}</Text>
                    </TouchableOpacity>
                  );
                })}
              </View>
            )}
          </View>

          <TouchableOpacity
            style={styles.searchButton}
            onPress={handleSearch}
          >
            <Text style={styles.searchButtonText}>Buscar</Text>
          </TouchableOpacity>
        </View>

        {/* Mensagem de resultados */}
        <View style={styles.resultsContainer}>
          <Text style={styles.resultsText}>
            {searchQuery ? 'Nenhum resultado encontrado' : 'Digite algo para buscar'}
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
  searchContainer: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 32,
    position: 'relative',
    zIndex: 1,
  },
  searchInput: {
    flex: 1,
    backgroundColor: '#f9fafb',
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 16,
    color: '#111827',
  },
  dropdownWrapper: {
    position: 'relative',
    width: 120,
    zIndex: 1001,
  },
  dropdownButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f9fafb',
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 12,
    width: '100%',
  },
  dropdownButtonText: {
    fontSize: 14,
    color: '#111827',
    fontWeight: '500',
  },
  dropdownMenu: {
    position: 'absolute',
    top: 48,
    left: 0,
    width: 200,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
    zIndex: 1002,
  },
  dropdownItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  dropdownItemText: {
    fontSize: 14,
    color: '#111827',
  },
  checkbox: {
    fontSize: 18,
    color: '#111827',
  },
  searchButton: {
    backgroundColor: '#111827',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    justifyContent: 'center',
  },
  searchButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  resultsContainer: {
    alignItems: 'center',
    paddingVertical: 48,
  },
  resultsText: {
    fontSize: 16,
    color: '#9ca3af',
  },
});
