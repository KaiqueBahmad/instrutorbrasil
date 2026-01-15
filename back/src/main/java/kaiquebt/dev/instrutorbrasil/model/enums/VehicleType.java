package kaiquebt.dev.instrutorbrasil.model.enums;

public enum VehicleType {
	CARRO("Carro"),
	MOTO("Moto"),
	ONIBUS("Ônibus"),
	CAMINHAO("Caminhão");

	private final String displayName;

	VehicleType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
