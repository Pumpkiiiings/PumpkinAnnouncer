# PumpkinAnnouncer

![Version](https://img.shields.io/badge/Version-1.8-orange?style=flat-square)
![Platform](https://img.shields.io/badge/Platform-Velocity-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

**PumpkinAnnouncer** es una solución profesional de mensajería programada para proxies Velocity. Diseñado para simplificar la comunicación en red, permite la creación de anuncios dinámicos, multilínea y totalmente personalizables sin la necesidad de caracteres de escape complejos.

---

## 🛠 Características Principales

* **Sistema Multilínea Nativo:** Gestión de mensajes mediante listas YAML, permitiendo una estructura visual clara en el chat.
* **Identificadores Alfanuméricos:** Asignación de nombres únicos (IDs) a cada anuncio para una gestión administrativa eficiente.
* **Internacionalización Completa:** Todos los mensajes del sistema son editables desde el archivo de configuración.
* **Soporte MiniMessage:** Integración completa con el formato de Adventure para colores, gradientes y eventos de clic.
* **Carga Dinámica:** Sistema de recarga en caliente que actualiza la configuración y el temporizador sin reiniciar el proxy.

---

## 💻 Comandos y Permisos

| Comando | Acción | Permiso |
| :--- | :--- | :--- |
| `/pa reload` | Recarga la configuración y reinicia el scheduler. | `pumpkin.admin` |
| `/pa list` | Despliega los IDs de los anuncios cargados en memoria. | `pumpkin.admin` |
| `/pa test <id>` | Ejecuta un broadcast inmediato del anuncio especificado. | `pumpkin.admin` |

---

## 📋 Configuración (`config.yml`)

El archivo de configuración es lo mas simple.

```yaml
settings:
  cooldown-seconds: 60  # Intervalo de rotación entre anuncios

messages:
  help: "<gold><bold>PumpkinAnnouncer</bold></gold> \n <gray>Usa /pa reload, list o test"
  reload-success: "<green>Configuración recargada exitosamente."
  id-not-found: "<red>Error: El ID especificado no existe."

anuncios:
  tienda:
    - "<green>==================================</green>"
    - "  <yellow>Visita nuestra tienda oficial</yellow>"
    - "  <white>tienda.pumpkingz.net</white>"
    - "<green>==================================</green>"
