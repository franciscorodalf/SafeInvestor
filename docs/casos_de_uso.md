**Descripción**: La aplicación de finanzas personales permite a los usuarios gestionar sus metas de ahorro, registrar ingresos y gastos, visualizar gráficas financieras y personalizar su experiencia con un modo oscuro. Además, el sistema ofrece autenticación segura mediante registro, inicio y cierre de sesión, y recuperación de contraseña. Se incluyen relaciones de **inclusión** (`<<include>>`) y **extensión** (`<<extend>>`) en ciertos casos de uso.


## Elementos del Diagrama de Casos de Uso

| Elemento                             | Descripción                                                                 |
|--------------------------------------|-----------------------------------------------------------------------------|
| **Actor: Usuario**                   | Persona que usa la aplicación para gestionar sus finanzas.                  |
| **Caso de Uso: Registrar Meta**       | Permite al Usuario establecer metas de ahorro con un monto y fecha límite.  |
| **Caso de Uso: Registrar Gasto**      | Permite al Usuario ingresar y categorizar sus gastos.                       |
| **Caso de Uso: Registrar Ingreso**    | Permite al Usuario añadir un ingreso y actualizar su saldo.                 |
| **Caso de Uso: Visualizar Gráfica**  | Permite al Usuario ver un resumen gráfico de sus gastos.                    |
| **Caso de Uso: Editar Meta**          | Permite al Usuario modificar una meta de ahorro existente.                   |
| **Caso de Uso: Modo Oscuro**          | Permite al Usuario cambiar entre modo claro y oscuro en la aplicación.       |
| **Caso de Uso: Registro de Usuario**  | Permite a un nuevo Usuario crear una cuenta en la aplicación.                |
| **Caso de Uso: Inicio de Sesión**     | Permite al Usuario autenticarse en la aplicación.                            |
| **Caso de Uso: Cierre de Sesión**     | Permite al Usuario cerrar sesión de manera segura.                           |
| **Caso de Uso: Recuperar Contraseña** | Permite al Usuario restablecer su contraseña en caso de olvido.              |


## Relaciones entre los Elementos

| Relación                                         | Descripción                                                                                     |
|--------------------------------------------------|-------------------------------------------------------------------------------------------------|
| **Usuario → Registrar Meta**                     | Asociación: El Usuario crea metas de ahorro con un objetivo y fecha límite.                     |
| **Usuario → Registrar Gasto**                    | Asociación: El Usuario registra sus gastos y los categoriza.                                    |
| **Usuario → Registrar Ingreso**                  | Asociación: El Usuario introduce sus ingresos para actualizar su saldo.                         |
| **Usuario → Visualizar Gráfica**                | Asociación: El Usuario consulta gráficos y estadísticas de sus finanzas.                        |
| **Registrar Meta → Editar Meta**                 | **Extensión** (`<<extend>>`): El Usuario puede modificar una meta previamente creada.           |
| **Registrar Gasto → Visualizar Gráfica**        | **Inclusión** (`<<include>>`): Cada gasto registrado impacta en los reportes financieros.       |
| **Registrar Ingreso → Visualizar Gráfica**      | **Inclusión** (`<<include>>`): Cada ingreso registrado se refleja en los reportes.              |
| **Registro de Usuario → Inicio de Sesión**       | **Inclusión** (`<<include>>`): Un Usuario registrado debe iniciar sesión para acceder a la app. |
| **Inicio de Sesión → Cierre de Sesión**          | **Extensión** (`<<extend>>`): Un usuario autenticado puede cerrar sesión en cualquier momento.  |
| **Inicio de Sesión → Recuperar Contraseña**      | **Extensión** (`<<extend>>`): Si el Usuario olvida su contraseña, puede recuperarla.           |


## Explicación de las Relaciones de Inclusión (`<<include>>`) y Extensión (`<<extend>>`)

- **Incluir Visualizar Gráfica**: Cada vez que un Usuario registra un gasto o un ingreso, este se refleja automáticamente en los reportes financieros (`<<include>>`).
  
- **Extender Editar Meta**: La opción de modificar una meta de ahorro (`<<extend>>`) solo se activa si el Usuario desea cambiarla después de haberla creada.
  
- **Extender Cierre de Sesión**: Cerrar sesión (`<<extend>>`) es una acción opcional después de iniciar sesión, pero no obligatoria.
  
- **Extender Recuperar Contraseña**: Si el Usuario olvida su clave, puede activar la función de recuperación (`<<extend>>`).


## Diagrama Completo

**Descripción**: A continuación, se representa el diagrama de casos de uso con los actores, casos de uso y las relaciones de asociación, inclusión y extensión.

<img src="../images/CasosdeUso.png">

---

Este modelo de diagrama de casos de uso permite visualizar cómo interactúa el Usuario con la aplicación, destacando las funciones clave y las relaciones opcionales que amplían la funcionalidad principal.