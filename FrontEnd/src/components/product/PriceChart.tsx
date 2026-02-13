'use client';

import { useEffect, useState } from 'react';
import { Card, Spin, Select, Typography } from 'antd';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { priceApi } from '@/lib/api/endpoints';
import { formatPrice } from '@/lib/utils/format';

const { Title } = Typography;

interface Props { productId: number; }
interface PricePoint { date: string; price: number; }

export default function PriceChart({ productId }: Props) {
  const [data, setData] = useState<PricePoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [days, setDays] = useState(30);

  useEffect(() => {
    setLoading(true);
    priceApi.getHistory(productId, { days }).then((res) => setData(res.data.data || [])).catch(() => {}).finally(() => setLoading(false));
  }, [productId, days]);

  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={5} style={{ margin: 0 }}>가격 변동 추이</Title>
        <Select value={days} onChange={setDays} options={[{ label: '1주일', value: 7 }, { label: '1개월', value: 30 }, { label: '3개월', value: 90 }, { label: '6개월', value: 180 }]} style={{ width: 100 }} />
      </div>
      {loading ? <div style={{ textAlign: 'center', padding: 40 }}><Spin /></div>
       : data.length === 0 ? <div style={{ textAlign: 'center', padding: 40, color: '#8c8c8c' }}>가격 데이터가 없습니다</div>
       : (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
            <YAxis tickFormatter={(v) => `${(v / 10000).toFixed(0)}만`} tick={{ fontSize: 12 }} />
            <Tooltip formatter={(value: number) => [formatPrice(value), '가격']} />
            <Line type="monotone" dataKey="price" stroke="#1677ff" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
