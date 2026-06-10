<template>
  <n-config-provider :theme="darkTheme" :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <n-global-style />
          <AppContent />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { zhCN, dateZhCN, darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import AppContent from './AppContent.vue'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#6366f1',
    primaryColorHover: '#818cf8',
    primaryColorPressed: '#4338ca',
    primaryColorSuppl: '#6366f1',
    infoColor: '#00d4ff',
    successColor: '#10b981',
    warningColor: '#f59e0b',
    errorColor: '#ef4444',
    textColorBase: '#f1f5f9',
    textColor1: '#f1f5f9',
    textColor2: '#94a3b8',
    textColor3: '#64748b',
    placeholderColor: '#64748b',
    dividerColor: 'rgba(99, 102, 241, 0.2)',
    borderColor: 'rgba(99, 102, 241, 0.2)',
    cardColor: 'rgba(17, 24, 39, 0.8)',
    modalColor: 'rgba(17, 24, 39, 0.95)',
    popoverColor: 'rgba(17, 24, 39, 0.95)',
    tableColor: 'rgba(17, 24, 39, 0.8)',
    inputColor: 'rgba(30, 41, 59, 0.8)',
    actionColor: 'rgba(30, 41, 59, 0.8)',
    bodyColor: '#0a0e1a',
    hoverColor: 'rgba(99, 102, 241, 0.1)',
  },
  Button: {
    textColorPrimary: '#fff',
    textColorHoverPrimary: '#fff',
    textColorPressedPrimary: '#fff',
  },
  Card: {
    borderColor: 'rgba(99, 102, 241, 0.2)',
    color: 'rgba(17, 24, 39, 0.8)',
  },
  Input: {
    borderHover: '#6366f1',
    borderFocus: '#6366f1',
    boxShadowFocus: '0 0 20px rgba(99, 102, 241, 0.2)',
  },
  Tag: {
    borderPrimary: 'rgba(99, 102, 241, 0.3)',
    colorPrimary: 'rgba(99, 102, 241, 0.1)',
  },
}
</script>
