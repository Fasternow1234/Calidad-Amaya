export interface LoginRequest {
  nombreUsuario: string;
  password: string;
}

export interface OpcionInfo {
  id: number;
  codigo: string;
  nombre: string;
  orden: number;
}

export interface UsuarioInfo {
  id: number;
  nombreUsuario: string;
  nombreEmpleado: string;
  email: string | null;
  estado: number;
  rol: string;
  opciones: OpcionInfo[];
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  usuario: UsuarioInfo;
}