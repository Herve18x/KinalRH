# Cuentas de Prueba y Demostración (T1.16)

Este documento detalla las cuentas ficticias configuradas en el script `seed_dev.sql` para validar el ingreso y la autorización en una base de datos descartable de pruebas.

## 1. Cuenta: Encargado de Recursos Humanos
* **Nombre de usuario:** `encargado_demo`
* **Rol asignado:** `encargado`
* **Propósito:** Validar las funciones de gestión de recursos humanos y comprobar que cuenta con los permisos necesarios para la consulta completa de colaboradores (`EMPLEADO_VER`).
* **Colaborador asociado:** Juan Ficticio (`EMP-TEST-01`, Activo).

## 2. Cuenta: Visor Institucional
* **Nombre de usuario:** `visor_demo`
* **Rol asignado:** `visor`
* **Propósito:** Evaluar las restricciones de acceso y verificar que **no** posea el permiso de consulta completa detallada de expedientes restringidos, limitándose únicamente a vistas institucionales autorizadas.
* **Colaborador asociado:** Ninguno (cuenta de acceso externo/institucional genérico).

> **Aviso de seguridad:** Las contraseñas se encuentran almacenadas mediante `password_hash` seguro y no se revelan en texto plano en este repositorio.