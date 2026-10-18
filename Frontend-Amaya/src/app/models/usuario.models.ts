export interface Rol {
  id: number;
  nombre: string;
  descripcion: string | null;
}

export interface Usuario {
  id: number;
  nombreUsuario: string;
  nombreEmpleado: string;
  email: string | null;
  estado: number;
  fechaRegistro: string;
  idRol: number;
  rol: string;
}

export interface UsuarioRequest {
  nombreUsuario: string;
  password: string | null;
  nombreEmpleado: string;
  email: string | null;
  idRol: number;
}
