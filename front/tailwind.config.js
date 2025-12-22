/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./App.{js,jsx,ts,tsx}",
    "./src/**/*.{js,jsx,ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#3b82f6',
          dark: '#2563eb',
          light: '#60a5fa',
        },
        secondary: {
          DEFAULT: '#8b5cf6',
          dark: '#7c3aed',
          light: '#a78bfa',
        },
        success: '#10b981',
        warning: '#f59e0b',
        error: '#ef4444',
        background: '#f9fafb',
        surface: '#ffffff',
        text: {
          primary: '#111827',
          secondary: '#6b7280',
          light: '#9ca3af',
        },
      },
    },
  },
  plugins: [],
}

