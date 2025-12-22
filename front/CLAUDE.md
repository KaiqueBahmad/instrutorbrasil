# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a React Native mobile application built with Expo for the InstrutorBrasil platform. The app connects to a Spring Boot backend and provides role-based interfaces for users, instructors, and administrators to manage driving instruction services.

## Development Commands

### Starting the Application
- `npm start` - Start Expo development server
- `npm run android` - Run on Android emulator/device
- `npm run ios` - Run on iOS simulator/device (macOS only)
- `npm run web` - Run in web browser

### Restarting with Cache Clear
When making configuration changes (Babel, Tailwind, etc.), restart with:
```bash
npm start -- --reset-cache
```

## Architecture

### Navigation Structure
The app uses a dual-navigator pattern (`src/navigation/AppNavigator.tsx`):

1. **AuthNavigator**: Handles unauthenticated flows (Login, Register)
2. **AppNavigator**: Handles authenticated flows with role-based home screens
   - Routes users to different home screens based on their role (USER, INSTRUCTOR, ADMIN)
   - Role determination happens in `getHomeScreen()` which switches on `user.role`

### Authentication Flow
Authentication is managed through `AuthContext` (`src/contexts/AuthContext.tsx`):

1. **Token Management**:
   - Access tokens and refresh tokens stored in AsyncStorage
   - Automatic token refresh via Axios interceptor in `src/services/api.ts`
   - When a 401 error occurs, the interceptor attempts to refresh the token automatically

2. **Auth State**:
   - `user` object contains: id, email, name, role, provider, emailVerified
   - `isAuthenticated` derived from user presence
   - `isLoading` tracks initial auth check

3. **API Configuration** (`src/services/api.ts`):
   - Platform-specific API URLs:
     - Android emulator: `http://10.0.2.2:8080` (maps to host's localhost)
     - iOS simulator: `http://localhost:8080`
     - Web: `http://localhost:8080`
   - For physical devices, update `getApiUrl()` to use your machine's IP address

### Role System
Three distinct roles defined in `src/types/index.ts`:
- `ROLE_USER`: Regular users who book instructors
- `ROLE_INSTRUCTOR`: Instrutors who manage schedules and students
- `ROLE_ADMIN`: Administrators with full system access

Each role has a dedicated home screen that serves as the entry point after authentication.

### Styling with StyleSheet

This project uses React Native's built-in StyleSheet API for styling:

1. **Color Constants** (`src/constants/colors.ts`):
   ```typescript
   export const colors = {
     primary: '#3b82f6',
     secondary: '#8b5cf6',
     success: '#10b981',
     warning: '#f59e0b',
     error: '#ef4444',
     background: '#f9fafb',
     surface: '#ffffff',
     textPrimary: '#111827',
     textSecondary: '#6b7280',
     textLight: '#9ca3af',
     border: '#d1d5db',
   };
   ```

2. **Usage Pattern**:
   ```tsx
   import { StyleSheet } from 'react-native';
   import { colors } from '../constants/colors';

   <View style={styles.container}>
     <Text style={styles.title}>Title</Text>
   </View>

   const styles = StyleSheet.create({
     container: {
       flex: 1,
       backgroundColor: colors.background,
       paddingHorizontal: 24,
       paddingVertical: 16,
     },
     title: {
       fontSize: 24,
       fontWeight: 'bold',
       color: colors.textPrimary,
     },
   });
   ```

3. **Styling Best Practices**:
   - Always use `StyleSheet.create()` for better performance
   - Import colors from `src/constants/colors.ts` for consistency
   - Use array syntax for combining styles: `style={[styles.base, styles.variant]}`
   - For conditional styles: `style={[styles.base, condition && styles.conditional]}`

### Component Structure

**Reusable Components** (`src/components/`):
- `Button.tsx`: Supports variants (primary/secondary/outline), sizes, and loading states
- `Input.tsx`: Text input with label and error message support
- `Card.tsx`: Container component for consistent card styling
- All exported through `src/components/index.ts`

### TypeScript Configuration

Path aliases configured in `tsconfig.json`:
- `@/*` maps to `src/*`
- Strict mode enabled
- React Native JSX transform

## Backend Integration

**API Endpoints** (all prefixed with backend base URL):
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/refresh-token` - Token refresh
- `GET /auth/me` - Get current user profile
- `POST /auth/forgot-password` - Password recovery
- `POST /auth/reset-password` - Password reset

**Error Handling**:
- API errors are caught and rethrown with user-friendly messages
- Token refresh failures trigger automatic logout
- Failed auth state checks are logged to console

## Common Development Patterns

### Adding New Screens
1. Create screen component in `src/screens/`
2. Add navigation types to `src/navigation/types.ts`
3. Register in appropriate navigator (Auth or App stack)
4. For role-specific screens, update `getHomeScreen()` in AppNavigator

### Adding New API Endpoints
1. Define request/response types in `src/types/index.ts`
2. Create endpoint function in `src/services/api.ts`
3. Export through appropriate API object (e.g., `authAPI`)

### State Management Pattern
Currently uses Context API for auth state. For feature-specific state:
- Simple state: Use local component state with `useState`
- Shared state: Create new context following `AuthContext` pattern
- Async operations: Follow the try/catch pattern in AuthContext methods

## Important Notes

- When modifying `babel.config.js`, restart with cache reset: `npm start -- --reset-cache`
- The app expects the Spring Boot backend to be running on the configured URL
- User data and tokens persist in AsyncStorage across app restarts
- All screens should handle loading states during async operations
- Role checks should use the `Role` enum from `src/types/index.ts`, not string literals
- For consistent styling, always import colors from `src/constants/colors.ts`
