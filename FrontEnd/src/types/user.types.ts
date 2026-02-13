export enum UserRole {
  USER = 'USER',
  SELLER = 'SELLER',
  ADMIN = 'ADMIN',
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BLOCKED = 'BLOCKED',
}

export interface User {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: UserRole;
  status: UserStatus;
  point: number;
  badges: Badge[];
  createdAt: string;
}

export interface Badge {
  id: number;
  name: string;
  iconUrl: string;
}

export interface UserProfile {
  id: number;
  nickname: string;
  bio: string;
  imageUrl: string;
  reviewCount: number;
  badges: Badge[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone: string;
}

export interface SignupResponse {
  id: number;
  email: string;
  name: string;
  message: string;
}
