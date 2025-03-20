<div align="justify">


## Introducción
Este documento describe los casos de uso de la aplicación, explicando cómo los usuarios interactúan con el sistema y qué funciones ofrece. Se incluyen diagramas y especificaciones detalladas de cada caso de uso, con el objetivo de proporcionar una guía clara para el desarrollo y validación del sistema
## Casos de Uso

### **1. Iniciar Sesión**

|  Caso de Uso CU | Iniciar Sesión |
|---|---|
| Fuentes  | Documentación de requisitos |
| Actor  | Usuario |
| Descripción | Permite al usuario autenticarse en la aplicación. |
| Flujo básico | 1. El usuario ingresa su correo y contraseña. <br> 2. El sistema verifica las credenciales. <br> 3. Se concede acceso al usuario. |
| Pre-condiciones | El usuario debe estar registrado. |
| Post-condiciones | El usuario accede a su cuenta. |
| Requerimientos | Cuenta de usuario activa. |
| Notas | Si la contraseña es incorrecta, el usuario puede recuperarla. |
| Autor | franciscorodalf |
| Fecha | Fecha de la especificación |

### **2. Recuperar Contraseña**

|  Caso de Uso CU | Recuperar Contraseña |
|---|---|
| Fuentes  | Documentación de requisitos |
| Actor  | Usuario |
| Descripción | Permite al usuario recuperar su contraseña en caso de olvido. |
| Flujo básico | 1. El usuario solicita recuperar su contraseña. <br> 2. El sistema envía un correo de recuperación. <br> 3. El usuario sigue las instrucciones y establece una nueva contraseña. |
| Pre-condiciones | El usuario debe haber proporcionado un correo válido. |
| Post-condiciones | El usuario puede acceder con la nueva contraseña. |
| Requerimientos | Correo electrónico registrado. |
| Notas | Incluye el caso de uso "Enviar Correo". |
| Autor | franciscorodalf |
| Fecha | Fecha de la especificación |

### **3. Enviar Correo**

|  Caso de Uso CU | Enviar Correo |
|---|---|
| Fuentes  | Documentación de requisitos |
| Actor  | Sistema |
| Descripción | Envía un correo electrónico a los usuarios cuando es necesario. |
| Flujo básico | 1. Se envia un correo al usuario. <br> 2. El sistema genera el mensaje y lo envía al destinatario. |
| Pre-condiciones | El correo debe de tener un nombre de usuario asociado |
| Post-condiciones | El usuario recibe el correo en su bandeja de entrada. |
| Requerimientos | Tener iniciada la sesión. |
| Notas | Incluido en "Recuperar Contraseña". |
| Autor | franciscorodalf |
| Fecha | Fecha de la especificación |

### **4. Registrar Ingreso**

|  Caso de Uso CU | Registrar Ingreso |
|---|---|
| Fuentes  | Documentación de requisitos |
| Actor  | Usuario |
| Descripción | Permite al usuario registrar un ingreso en su cuenta. |
| Flujo básico | 1. El usuario accede a la sección de ingresos. <br> 2. Introduce el monto y la fuente del ingreso. <br> 3. Guarda el ingreso. |
| Pre-condiciones | El usuario debe haber iniciado sesión. |
| Post-condiciones | El saldo del usuario se actualiza. |
| Requerimientos | Cuenta de usuario activa. |
| Notas | Incluye el caso de uso "Visualizar Gráfica". |
| Autor | franciscorodalf |
| Fecha | Fecha de la especificación |

### **5. Registrar Gasto**

|  Caso de Uso CU | Registrar Gasto |
|---|---|
| Fuentes  | Documentación de requisitos |
| Actor  | Usuario |
| Descripción | Permite al usuario registrar un gasto en su cuenta. |
| Flujo básico | 1. El usuario accede a la sección de gastos. <br> 2. Introduce el monto, categoría y descripción. <br> 3. Guarda el gasto. |
| Pre-condiciones | El usuario debe haber iniciado sesión. |
| Post-condiciones | El saldo del usuario se actualiza. |
| Requerimientos | Cuenta de usuario activa. |
| Notas | Incluye el caso de uso "Visualizar Gráfica". |
| Autor | franciscorodalf |
| Fecha | Fecha de la especificación |

### **6. Visualizar Gráfica**

|  Caso de Uso CU | Visualizar Gráfica |
|---|---|
| Fuentes  | Documentación de requisitos |
| Actor  | Usuario |
| Descripción | Permite al usuario ver un resumen gráfico de sus ingresos y gastos. |
| Flujo básico | 1. El usuario accede a la sección de reportes. <br> 2. El sistema genera gráficos con el desglose de ingresos y gastos. |
| Pre-condiciones | Debe haber ingresos o gastos registrados. |
| Post-condiciones | El usuario visualiza su información financiera. |
| Requerimientos | Cuenta de usuario activa. |
| Notas | Se actualiza en tiempo real. |
| Autor | franciscorodalf |
| Fecha | Fecha de la especificación |

## Diagrama de Casos de Uso

**Descripción Visual**: A continuación, se representa el diagrama de casos de uso con los actores, casos de uso y las relaciones de asociación, inclusión y extensión.
<img src="../images/CasosdeUso_V1.0.png">

</div>