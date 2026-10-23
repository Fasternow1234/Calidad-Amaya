# TestLink local para Calidad-Amaya

Instancia aislada para documentar y ejecutar los casos de HU08. El puerto web solo se publica en el equipo local (`127.0.0.1:8090`). La base de datos de TestLink es un volumen Docker independiente; no es la base de datos del sistema Amaya.

## Iniciar

Desde esta carpeta, con Docker Desktop abierto:

```powershell
docker compose up -d
docker compose ps
```

Abrir `http://localhost:8090` en el navegador. En el asistente inicial de TestLink, usar `db` como servidor de base de datos, `root` como usuario administrador y `teste` como contraseña. Base de datos: `testlink`. Usuario de aplicación de base de datos: `teste`, contraseña: `teste`.

Estas credenciales son únicamente para esta instancia local desechable. No reutilizarlas en otros sistemas.

## Detener y volver a iniciar

```powershell
docker compose stop
docker compose start
```

Los casos y ejecuciones permanecen guardados en el volumen local `testlink-db` al detener los contenedores.

## Borrar la instancia y sus datos

```powershell
docker compose down -v
```

Este último comando elimina la base de datos y los datos de TestLink de esta instancia.
