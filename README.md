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
  cooldown-seconds: 60

messages:
  help: "<gradient:gold:yellow><bold>PumpkinAnnouncer</bold></gradient>\n<gray>» <yellow>/pa reload <dark_gray>- <white>Recarga\n<gray>» <yellow>/pa list <dark_gray>- <white>Ver IDs\n<gray>» <yellow>/pa test <id> <dark_gray>- <white>Probar"
  reload-success: "<green>¡Listo, bro! Todo al cien."
  list-header: "<gold><bold>Anuncios en el sistema:</bold></gold>"
  id-not-found: "<red>Ese anuncio no existe, checa tu config."

anuncios:
  discord:
    servers: ["global"]
    lines:
      - "            <color:#A0C4FF><bold>DISCORD</bold>"
      - "<white>"
      - " <white>¡Únete a nuestra comunidad oficial!"
      - " <white>Link: <click:open_url:'https://discord.gg/VVCnuympkD'><hover:show_text:'<gray>¡Haz clic para entrar, bro!'><underlined><color:#A0C4FF>discord.gg/VVCnuympkD</color></underlined></hover></click>"
      - " <gray>» <white>Sorteos, soporte y novedades diarias."
 tienda:
    servers: ["global"]
    lines:
      - "            <color:#FDFD96><bold>TIENDA OFICIAL</bold>"
      - "<white>"
      - " <white>¡Aprovecha nuestras ofertas de temporada!"
      - " <white>Obtén hasta un <color:#FFB7B2><bold>30% de descuento</bold></color> en rangos."
      - " <white>Link: <click:open_url:'https://tienda.tuservidor.com'><hover:show_text:'<gray>¡Haz clic para ver la tienda, bro!'><underlined><color:#FDFD96>tienda.tuservidor.com</color></underlined></hover></click>"
      - " <gray>» <white>Apoya al servidor y obtén beneficios únicos."
survival_info:
    servers: ["survival"]
    lines:
      - "            <color:#C1E1C1><bold>SURVIVAL</bold>"
      - "<white>"
      - " <white>¡Bienvenido a la aventura, bro! Recuerda que"
      - " <white>puedes proteger tu zona usando el <color:#C1E1C1><bold>/claim</bold></color>."
      - " <gray>» <white>Consulta las guías en nuestro <color:#A0C4FF>Discord</color>."

