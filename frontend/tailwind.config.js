export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],
  theme: {
    extend: {
      colors: {
        'glass-bg': 'rgba(255, 255, 255, 0.06)',
        'glass-border': 'rgba(255, 255, 255, 0.12)',
        'deep-blue': {
          900: '#0a0e27',
          800: '#0d1b3e',
          700: '#112855'
        },
        'accent-blue': '#64b4ff',
        'accent-purple': '#c896ff',
        'accent-green': '#64c896',
        'accent-gold': '#ffb464'
      },
      borderRadius: {
        '3xl': '24px',
        '2xl': '16px',
        'xl': '12px',
        'lg': '8px',
        'md': '6px'
      },
      backdropBlur: {
        'glass': '20px'
      },
      boxShadow: {
        'glass': '0 8px 32px rgba(0, 0, 0, 0.3)'
      }
    }
  },
  plugins: []
};
