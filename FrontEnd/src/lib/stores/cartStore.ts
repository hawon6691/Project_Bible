'use client';

import { create } from 'zustand';
import type { CartItem } from '@/types/order.types';
import { cartApi } from '../api/endpoints';

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
    set({ isLoading: true });
    try {
      const { data } = await cartApi.getItems();
      const items = data.data;
      set({ items, totalCount: items.length, isLoading: false });
    } catch {
      set({ isLoading: false });
    }
  },

  addItem: async (itemData) => {
    await cartApi.addItem(itemData);
    await get().fetchCart();
  },

  updateQuantity: async (id, quantity) => {
    await cartApi.updateItem(id, { quantity });
    await get().fetchCart();
  },

  removeItem: async (id) => {
    await cartApi.removeItem(id);
    await get().fetchCart();
  },

  clearCart: async () => {
    await cartApi.clear();
    set({ items: [], totalCount: 0 });
  },
}));
