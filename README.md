# 🎃 PumpkinAnnouncer v2.2

![Velocity](https://img.shields.io/badge/Velocity-Ready-blue?style=for-the-badge)
![Paper](https://img.shields.io/badge/Paper-Ready-white?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![Version](https://img.shields.io/badge/Version-v2.2-success?style=for-the-badge)

¡El sistema definitivo de anuncios automáticos, ahora nativo para servidores **Paper** y redes **Velocity** en un solo archivo!
PumpkinAnnouncer te permite programar mensajes inmersivos con soporte total para el chat, ActionBars, BossBars y Sonidos.

## ✨ Características

- 🌐 **Multiplataforma (1 Solo Archivo):** Funciona perfectamente si lo pones en la carpeta `plugins` de tu servidor Paper o en tu proxy Velocity. ¡Él sabrá qué hacer!
- 🎨 **Colores Universales:** Puedes mezclar códigos Legacy (`&a&l`), BungeeHex (`&#FF0000`) y MiniMessage (`<gradient:red:blue>`) en la misma línea. ¡El plugin traduce todo mágicamente!
- 🪧 **BossBars y ActionBars:** Llama la atención de tus jugadores mostrando barras de jefe temporales y textos sobre la barra de vida, acompañados de sonidos personalizados.
- 🎯 **Multi-Servidor (Velocity):** Envía anuncios a toda la red (`global`) o solo a servidores específicos (ej. `["lobby", "survival"]`).
- 📏 **Centrado Matemático:** Usa la etiqueta `[center]` al inicio de cualquier línea para centrarla perfectamente en el chat de Minecraft, ignorando inteligentemente los códigos de color.
- 🖱️ **Interactividad:** Soporta etiquetas `<click>` y `<hover>` para enlaces o textos emergentes en el chat.
- ⌨️ **Autocompletado Pro:** Presiona `TAB` en el comando `/pa test` y el plugin autocompletará los IDs de tus anuncios creados.

---

## 📜 Comandos y Permisos

| Comando | Descripción | Permiso |
| :--- | :--- | :--- |
| `/pa reload` | Recarga la configuración en tiempo real. | `pumpkin.admin` |
| `/pa list` | Muestra una lista de todos los anuncios cargados. | `pumpkin.admin` |
| `/pa test <id>` | Prueba un anuncio específico al instante. | `pumpkin.admin` |

*Nota: Al usar `/pa test`, presiona la tecla `TAB` para ver los anuncios disponibles automáticamente.*

---

## 🛠️ Instalación

1. Descarga el archivo `.jar` de PumpkinAnnouncer.
2. Arrástralo a la carpeta `plugins` de tu servidor **Velocity**.
3. Reinicia el proxy.
4. Edita el archivo `plugins/pumpkinannouncer/config.yml` a tu gusto.
5. Usa `/pa reload` dentro del juego o consola para aplicar los cambios.

---

## 📖 Ejemplos de Anuncios (`config.yml`)

Aquí tienes varios ejemplos de cómo puedes configurar tus anuncios. Todo utiliza el formato [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

### 1. Anuncio Global Centrado (Ej. Discord)
Este anuncio se enviará a todos los servidores (`global`), estará totalmente centrado y el enlace será clickeable.

```yaml
  discord:
    servers: ["global"]
    lines:
      - "[center]<color:#A0C4FF><bold>DISCORD</bold>"
      - ""
      - "[center]<white>¡Únete a nuestra comunidad oficial!"
      - "[center]<white>Link: <click:open_url:'https://discord.gg/tuenlace'><hover:show_text:'<gray>¡Haz clic para entrar!'><underlined><color:#A0C4FF>discord.gg/tuenlace</color></underlined></hover></click>"
      - "[center]<gray>» <white>Sorteos, soporte y novedades diarias."
```

### 2. Anuncio para Servidores Específicos
Si solo quieres que un mensaje aparezca en la modalidad de "Survival" y "Skyblock".
```yaml
  votar:
    servers: ["survival", "skyblock"]
    lines:
      - "[center]<gradient:green:yellow><bold>¡VOTA POR EL SERVIDOR!</bold></gradient>"
      - ""
      - "[center]<white>Apóyanos votando todos los días."
      - "[center]<white>Usa el comando <gold>/votar</gold> para recibir recompensas."
      - "[center]<gray>¡Gracias por jugar con nosotros!"
```
### 3. Anuncio Informativo Básico (Sin Centrar)
```yaml
  limpieza:
    servers: ["global"]
    lines:
      - "<red><bold>⚠ AVISO IMPORTANTE</bold>"
      - "<white>El servidor realiza una limpieza de entidades cada 15 minutos."
      - "<white>Asegúrate de guardar tus objetos de valor."
```
### ⚙️ Configuración General
En la parte superior de tu ```config.yml``` puedes modificar el tiempo entre anuncios (en segundos) y los mensajes del plugin:
```yaml
settings:
  cooldown-seconds: 300 # 300 segundos = 5 minutos

messages:
  help: "<gradient:gold:yellow><bold>PumpkinAnnouncer</bold></gradient>\n<gray>» <yellow>/pa reload <dark_gray>- <white>Recarga\n<gray>» <yellow>/pa list <dark_gray>- <white>Ver IDs\n<gray>» <yellow>/pa test <id> <dark_gray>- <white>Probar"
  reload-success: "<green>¡Listo, bro! Todo al cien."
  list-header: "<gold><bold>Anuncios en el sistema:</bold></gold>"
  id-not-found: "<red>Ese anuncio no existe, checa tu config."
```