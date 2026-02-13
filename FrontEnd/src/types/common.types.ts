export interface ApiResponse<T> {
  success: boolean;
  data: T;
  meta?: PaginationMeta;
  error?: ApiError;
}

export interface PaginationMeta {
  page: number;
  limit: number;
  totalCount: number;
  totalPages: number;
}

export interface ApiError {
  code: string;
  message: string;
}

export interface PaginationParams {
  page?: number;
  limit?: number;
}

export interface MessageResponse {
  message: string;
}
