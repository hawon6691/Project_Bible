/** @type {import('next').NextConfig} */
const strictMode = process.env.NEXT_PUBLIC_STRICT_MODE === 'true';

const nextConfig = {
  reactStrictMode: strictMode,
  experimental: {
    missingSuspenseWithCSRBailout: false,
  },
  transpilePackages: ['antd', '@ant-design/icons'],
  images: {
    domains: ['localhost'],
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:3000'}/api/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;
