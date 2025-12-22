import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, Alert, StyleSheet, Dimensions, Modal } from 'react-native';
import ScreenHeader from '../components/ScreenHeader';
import SettingsModal from '../components/SettingsModal';

type VehicleType = 'carro' | 'moto' | 'onibus' | 'caminhao';

const VEHICLE_TYPES: { id: VehicleType; label: string }[] = [
  { id: 'carro', label: 'Carro' },
  { id: 'moto', label: 'Moto' },
  { id: 'onibus', label: 'Ônibus' },
  { id: 'caminhao', label: 'Caminhão' },
];

const STATES = [
  'São Paulo',
  'Rio de Janeiro',
  'Minas Gerais',
  'Paraná',
  'Bahia',
];

const NEIGHBORHOODS = [
  'Centro',
  'Jardins',
  'Vila Nova',
  'Mooca',
  'Pinheiros',
  'Itaim',
];

export default function UserHomeScreen() {
  const [searchQuery, setSearchQuery] = useState('');
  const [showSettings, setShowSettings] = useState(false);
  const [selectedVehicles, setSelectedVehicles] = useState<VehicleType[]>([]);
  const [showVehicleDropdown, setShowVehicleDropdown] = useState(false);
  const [showStateDropdown, setShowStateDropdown] = useState(false);
  const [showNeighborhoodDropdown, setShowNeighborhoodDropdown] = useState(false);
  const [selectedState, setSelectedState] = useState<string>('');
  const [selectedNeighborhood, setSelectedNeighborhood] = useState<string>('');

  const toggleVehicle = (vehicleId: VehicleType) => {
    setSelectedVehicles((prev) =>
      prev.includes(vehicleId)
        ? prev.filter((id) => id !== vehicleId)
        : [...prev, vehicleId]
    );
  };

  const getVehicleLabel = () => {
    if (selectedVehicles.length === 0) return 'Veículo';
    return `${selectedVehicles.length} selecionado${selectedVehicles.length > 1 ? 's' : ''}`;
  };

  const closeAllDropdowns = () => {
    setShowVehicleDropdown(false);
    setShowStateDropdown(false);
    setShowNeighborhoodDropdown(false);
  };

  const handleUseCurrentLocation = () => {
    // TODO: Implementar localização atual
    Alert.alert('Localização', 'Funcionalidade ainda não implementada');
  };

  const handleSearch = () => {
    const filters = {
      query: searchQuery.trim(),
      vehicles: selectedVehicles,
      state: selectedState,
      neighborhood: selectedNeighborhood,
    };
    Alert.alert(
      'Busca',
      `Termo: ${filters.query || 'Todos'}\nVeículos: ${
        filters.vehicles.length > 0 ? filters.vehicles.join(', ') : 'Todos'
      }\nEstado: ${filters.state || 'Todos'}\nBairro: ${filters.neighborhood || 'Todos'}`
    );
  };

  return (
    <View style={styles.container}>
      <ScreenHeader onSettingsPress={() => setShowSettings(true)} />
      <SettingsModal visible={showSettings} onClose={() => setShowSettings(false)} />

      <ScrollView style={styles.content}>
        {/* Título */}
        <Text style={styles.title}>InstrutorBrasil</Text>

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
              onPress={() => {
                if (!showVehicleDropdown) {
                  closeAllDropdowns();
                  setShowVehicleDropdown(true);
                } else {
                  closeAllDropdowns();
                }
              }}
            >
              <Text style={styles.dropdownButtonText}>{getVehicleLabel()}</Text>
            </TouchableOpacity>

            {showVehicleDropdown && (
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

        {/* Filtros de localização */}
        <View style={styles.locationFilters}>
          {/* Dropdown de Estado */}
          <View style={styles.dropdownWrapper}>
            <TouchableOpacity
              style={styles.dropdownButton}
              onPress={() => {
                if (!showStateDropdown) {
                  closeAllDropdowns();
                  setShowStateDropdown(true);
                } else {
                  closeAllDropdowns();
                }
              }}
            >
              <Text style={styles.dropdownButtonText}>
                {selectedState || 'Estado'}
              </Text>
            </TouchableOpacity>

            {showStateDropdown && (
              <View style={styles.dropdownMenu}>
                {STATES.map((state) => (
                  <TouchableOpacity
                    key={state}
                    style={styles.dropdownItem}
                    onPress={() => {
                      setSelectedState(state);
                      setShowStateDropdown(false);
                    }}
                  >
                    <Text style={styles.dropdownItemText}>{state}</Text>
                    {selectedState === state && (
                      <Text style={styles.checkbox}>•</Text>
                    )}
                  </TouchableOpacity>
                ))}
              </View>
            )}
          </View>

          {/* Dropdown de Bairro */}
          <View style={styles.dropdownWrapper}>
            <TouchableOpacity
              style={styles.dropdownButton}
              onPress={() => {
                if (!showNeighborhoodDropdown) {
                  closeAllDropdowns();
                  setShowNeighborhoodDropdown(true);
                } else {
                  closeAllDropdowns();
                }
              }}
            >
              <Text style={styles.dropdownButtonText}>
                {selectedNeighborhood || 'Bairro'}
              </Text>
            </TouchableOpacity>

            {showNeighborhoodDropdown && (
              <View style={styles.dropdownMenu}>
                {NEIGHBORHOODS.map((neighborhood) => (
                  <TouchableOpacity
                    key={neighborhood}
                    style={styles.dropdownItem}
                    onPress={() => {
                      setSelectedNeighborhood(neighborhood);
                      setShowNeighborhoodDropdown(false);
                    }}
                  >
                    <Text style={styles.dropdownItemText}>{neighborhood}</Text>
                    {selectedNeighborhood === neighborhood && (
                      <Text style={styles.checkbox}>•</Text>
                    )}
                  </TouchableOpacity>
                ))}
              </View>
            )}
          </View>

          {/* Botão de localização atual */}
          <TouchableOpacity
            style={styles.locationButton}
            onPress={handleUseCurrentLocation}
          >
            <Text style={styles.locationButtonText}>📍 Usar localização</Text>
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
    marginTop: 16,
    marginBottom: 24,
  },
  searchContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 16,
    position: 'relative',
    zIndex: 1,
  },
  locationFilters: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 32,
  },
  locationButton: {
    flex: 1,
    backgroundColor: '#f9fafb',
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  locationButtonText: {
    fontSize: 14,
    color: '#111827',
    fontWeight: '500',
  },
  searchInput: {
    flex: 3,
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
    flex: 2,
    minWidth: 100,
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
    width: '100%',
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
    flex: 1,
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
    zIndex: -1,
  },
  resultsText: {
    fontSize: 16,
    color: '#9ca3af',
  },
});
