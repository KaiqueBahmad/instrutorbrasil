export enum Role {
  USER = 'USER',
  INSTRUCTOR = 'INSTRUCTOR',
  ADMIN = 'ADMIN',
}

export enum AuthProvider {
  LOCAL = 'LOCAL',
  GOOGLE = 'GOOGLE',
}

export interface User {
  id: number;
  email: string;
  name: string;
  roles: Role[];
  provider: AuthProvider;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface MessageResponse {
  message: string;
  timestamp: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AuthContextType {
  user: User | null;
  activeRole: Role | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  loginWithGoogle: (authData: AuthResponse) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  setActiveRole: (role: Role) => void;
}
