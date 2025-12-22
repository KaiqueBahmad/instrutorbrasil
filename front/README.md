# InstrutorBrasil Mobile App

A React Native mobile application built with Expo, TypeScript, and NativeWind (Tailwind CSS) that integrates with the InstrutorBrasil Spring Boot backend.

## Features

- **Authentication System**
  - User registration and login
  - JWT token-based authentication
  - Automatic token refresh
  - Secure token storage with AsyncStorage

- **Role-Based Access Control**
  - USER role: Browse instructors, manage bookings
  - INSTRUCTOR role: Manage schedule, students, and earnings
  - ADMIN role: User management, system settings, analytics

- **Mobile-First Design**
  - Responsive UI with NativeWind (Tailwind CSS)
  - Clean and modern interface
  - Cross-platform support (iOS & Android)

## Tech Stack

- **React Native** - Mobile framework
- **Expo** - Development platform
- **TypeScript** - Type safety
- **NativeWind** - Tailwind CSS for React Native
- **React Navigation** - Navigation library
- **Axios** - HTTP client
- **AsyncStorage** - Persistent storage

## Prerequisites

- Node.js (v20.18.0 or higher)
- npm or yarn
- Expo CLI
- iOS Simulator (for macOS) or Android Emulator

## Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm start
```

3. Run on your preferred platform:
```bash
# iOS (requires macOS)
npm run ios

# Android
npm run android

# Web
npm run web
```

## Project Structure

```
front/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── Button.tsx       # Custom button component
│   │   ├── Card.tsx         # Card container component
│   │   ├── Input.tsx        # Input field component
│   │   └── index.ts         # Component exports
│   ├── contexts/            # React contexts
│   │   └── AuthContext.tsx  # Authentication state management
│   ├── navigation/          # Navigation setup
│   │   ├── AppNavigator.tsx # Main navigation component
│   │   └── types.ts         # Navigation type definitions
│   ├── screens/             # App screens
│   │   ├── LoginScreen.tsx
│   │   ├── RegisterScreen.tsx
│   │   ├── UserHomeScreen.tsx
│   │   ├── InstrutorHomeScreen.tsx
│   │   └── AdminHomeScreen.tsx
│   ├── services/            # API services
│   │   └── api.ts           # API client and endpoints
│   └── types/               # TypeScript type definitions
│       └── index.ts         # Shared types
├── App.tsx                  # App entry point
├── global.d.ts              # Global type definitions
├── tailwind.config.js       # Tailwind CSS configuration
└── tsconfig.json            # TypeScript configuration
```

## API Configuration

The app connects to the Spring Boot backend by default at:
- Android Emulator: `http://10.0.2.2:8080`
- iOS Simulator: `http://localhost:8080`
- Physical Device: Update `API_BASE_URL` in `src/services/api.ts` to your computer's IP address

To change the API URL, edit `src/services/api.ts`:

```typescript
const API_BASE_URL = 'http://YOUR_IP_ADDRESS:8080';
```

## Authentication Flow

1. **Login/Register**: User enters credentials
2. **Token Storage**: Access and refresh tokens stored in AsyncStorage
3. **Auto-Refresh**: Access token automatically refreshed when expired
4. **Role-Based Routing**: Users redirected to appropriate home screen based on role
5. **Logout**: Tokens cleared from storage

## Available Scripts

- `npm start` - Start Expo development server
- `npm run android` - Run on Android emulator
- `npm run ios` - Run on iOS simulator
- `npm run web` - Run in web browser

## Components

### Button
Reusable button component with variants and loading states:
```tsx
<Button
  title="Login"
  variant="primary"
  size="md"
  isLoading={false}
  onPress={handleLogin}
/>
```

### Card
Container component for consistent card styling:
```tsx
<Card>
  <Text>Card content</Text>
</Card>
```

### Input
Input field component with label and error support:
```tsx
<Input
  label="Email"
  value={email}
  onChangeText={setEmail}
  keyboardType="email-address"
  error={emailError}
/>
```

## Styling with NativeWind

This project uses NativeWind, which allows you to use Tailwind CSS classes in React Native:

```tsx
<View className="flex-1 bg-background px-6 py-4">
  <Text className="text-2xl font-bold text-text-primary">
    Welcome!
  </Text>
</View>
```

### Custom Theme Colors

The app includes custom color palette defined in `tailwind.config.js`:
- `primary` - Blue (#3b82f6)
- `secondary` - Purple (#8b5cf6)
- `success` - Green (#10b981)
- `warning` - Amber (#f59e0b)
- `error` - Red (#ef4444)
- `background` - Light gray (#f9fafb)
- `surface` - White (#ffffff)
- `text-primary` - Dark gray (#111827)
- `text-secondary` - Medium gray (#6b7280)

## Development Tips

### Running on Android Emulator
- The Android emulator uses `10.0.2.2` to access localhost on your machine
- Make sure your backend is running on `http://localhost:8080`

### Running on iOS Simulator
- iOS simulator can access localhost directly
- Make sure Xcode is installed (macOS only)

### Running on Physical Device
- Connect to the same WiFi network as your development machine
- Update `API_BASE_URL` in `src/services/api.ts` to your computer's IP address
- Make sure your backend allows connections from your device's IP

### Debugging
- Press `j` in terminal to open debugger
- Use React DevTools for component inspection
- Check console logs for API errors

## Backend Integration

This app integrates with the InstrutorBrasil Spring Boot backend. Make sure the backend is running before using the app.

Backend endpoints used:
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/refresh-token` - Token refresh
- `GET /auth/me` - Get current user
- `POST /auth/forgot-password` - Password recovery (coming soon)
- `POST /auth/reset-password` - Password reset (coming soon)

## Troubleshooting

### Metro bundler issues
```bash
npm start -- --reset-cache
```

### TypeScript errors
```bash
npm run tsc
```

### NativeWind not working
Make sure `babel.config.js` includes the nativewind plugin:
```javascript
plugins: ['nativewind/babel']
```

## License

This project is part of the InstrutorBrasil platform.
