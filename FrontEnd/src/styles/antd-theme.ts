import type { ThemeConfig } from 'antd';

export const theme: ThemeConfig = {
  token: {
    colorPrimary: '#1677ff',
    borderRadius: 6,
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
  },
  components: {
    Layout: {
      headerBg: '#fff',
      headerHeight: 64,
      siderBg: '#fff',
    },
    Menu: {
      itemBorderRadius: 6,
    },
    Card: {
      borderRadiusLG: 8,
    },
  },
};
