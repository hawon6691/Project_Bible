'use client';

import { create } from 'zustand';
import type { CartItem } from '@/types/order.types';
import { cartApi } from '../api/endpoints';

let fetchCartPromise: Promise<void> | null = null;
const quantitySyncTimers = new Map<number, ReturnType<typeof setTimeout>>();
const quantitySyncDelayMs = 250;

interface CartState {
  items: CartItem[];
  isLoading: boolean;
  totalCount: number;
  fetchCart: () => Promise<void>;
  addItem: (data: { productId: number; quantity: number; sellerId: number; selectedOptions?: string }) => Promise<void>;
  updateQuantity: (id: number, quantity: number) => Promise<void>;
  removeItem: (id: number) => Promise<void>;
  clearCart: () => Promise<void>;
}

export const useCartStore = create<CartState>((set, get) => ({
  items: [],
  isLoading: false,
  totalCount: 0,

  fetchCart: async () => {
    if (fetchCartPromise) {
      return fetchCartPromise;
    }

    set({ isLoading: true });
    fetchCartPromise = (async () => {
      try {
        const { data } = await cartApi.getItems();
        const items = data.data;
        set({ items, totalCount: items.length, isLoading: false });
      } catch {
        set({ isLoading: false });
      } finally {
        fetchCartPromise = null;
      }
    })();

    return fetchCartPromise;
  },

  addItem: async (itemData) => {
    await cartApi.addItem(itemData);
    // UX 지연을 줄이기 위해 재조회는 백그라운드로 돌리고 호출자 대기를 최소화한다.
    void get().fetchCart();
  },

  updateQuantity: async (id, quantity) => {
    const nextItems = get().items.map((item) =>
      item.id === id ? { ...item, quantity } : item,
    );
    set({ items: nextItems });

    const existingTimer = quantitySyncTimers.get(id);
    if (existingTimer) {
      clearTimeout(existingTimer);
    }

    quantitySyncTimers.set(
      id,
      setTimeout(async () => {
        try {
          await cartApi.updateItem(id, { quantity });
        } catch {
          // 동기화 실패 시 서버 기준으로 재조회해 상태를 맞춘다.
          await get().fetchCart();
        } finally {
          quantitySyncTimers.delete(id);
        }
      }, quantitySyncDelayMs),
    );
  },

  removeItem: async (id) => {
    const pendingTimer = quantitySyncTimers.get(id);
    if (pendingTimer) {
      clearTimeout(pendingTimer);
      quantitySyncTimers.delete(id);
    }

    const prevItems = get().items;
    const nextItems = prevItems.filter((item) => item.id !== id);
    set({ items: nextItems, totalCount: nextItems.length });

    try {
      await cartApi.removeItem(id);
    } catch (error) {
      set({ items: prevItems, totalCount: prevItems.length });
      throw error;
    }
  },

  clearCart: async () => {
    quantitySyncTimers.forEach((timer) => clearTimeout(timer));
    quantitySyncTimers.clear();
    await cartApi.clear();
    set({ items: [], totalCount: 0 });
  },
}));
