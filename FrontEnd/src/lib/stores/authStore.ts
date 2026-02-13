'use client';

import { create } from 'zustand';
import type { User } from '@/types/user.types';
import { storage } from '../utils/storage';
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '../utils/constants';
import { authApi, userApi } from '../api/endpoints';

interface AuthState {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  fetchUser: () => Promise<void>;
  setUser: (user: User | null) => void;
  initialize: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isLoading: true,
  isAuthenticated: false,

  login: async (email, password) => {
    const { data } = await authApi.login({ email, password });
    const tokens = data.data;
    storage.set(TOKEN_KEY, tokens.accessToken);
    storage.set(REFRESH_TOKEN_KEY, tokens.refreshToken);
    await get().fetchUser();
  },

  logout: async () => {
    try { await authApi.logout(); } catch {}
    storage.remove(TOKEN_KEY);
    storage.remove(REFRESH_TOKEN_KEY);
    set({ user: null, isAuthenticated: false });
  },

  fetchUser: async () => {
    try {
      const { data } = await userApi.getMe();
      set({ user: data.data, isAuthenticated: true, isLoading: false });
    } catch {
      set({ user: null, isAuthenticated: false, isLoading: false });
    }
  },

  setUser: (user) => set({ user, isAuthenticated: !!user }),

  initialize: async () => {
    const token = storage.get(TOKEN_KEY);
    if (token) {
      await get().fetchUser();
    } else {
      set({ isLoading: false });
    }
  },
}));
