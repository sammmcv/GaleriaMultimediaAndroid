# Aplicación de Galería Multimedia

## Descripción
Esta aplicación Android permite gestionar archivos multimedia (imágenes y audio) con funcionalidades avanzadas como visualización, reproducción, metadatos y papelera de reciclaje.

## Características
- **Galería de imágenes**: Visualización en cuadrícula con zoom y rotación
- **Reproductor de audio**: Reproducción directa de archivos de audio
- **Metadatos**: Visualización de información detallada de archivos (tamaño, fecha, resolución, etc.)
- **Papelera de reciclaje**: Sistema de recuperación y eliminación permanente
- **Temas personalizables**: Temas IPN y ESCOM incluidos
- **Filtros de cámara**: Aplicación de efectos en tiempo real (escala de grises, sepia, negativo)

## Tecnologías utilizadas
- Kotlin
- Android Jetpack (ViewPager2, RecyclerView, Fragment)
- Glide para carga de imágenes
- ExifInterface para metadatos
- CameraX para funcionalidades de cámara
- Material Design para la interfaz de usuario

## Requisitos
- Android 5.0 (API 21) o superior
- Permisos de cámara, almacenamiento y micrófono

## Instalación
1. Clona este repositorio
2. Abre el proyecto en Android Studio
3. Ejecuta la aplicación en un dispositivo o emulador

## Estructura del proyecto
- `adapters/`: Adaptadores para RecyclerView
- `dialogs/`: Diálogos personalizados
- `fragments/`: Fragmentos para las diferentes secciones
- `models/`: Clases de datos
- `ui/theme/`: Configuración de temas
- `utils/`: Utilidades (gestión de archivos, metadatos, etc.)

## Uso
La aplicación se divide en tres secciones principales:
1. **Imágenes**: Visualiza y gestiona tus imágenes
2. **Audio**: Reproduce y gestiona tus archivos de audio
3. **Papelera**: Recupera o elimina permanentemente archivos

## Contribuidores
- Samuel Alejandro Cortes Velazquez
- Luis Enrique Barros Martinez

## Licencia
Este proyecto está licenciado bajo la [Licencia MIT](https://opensource.org/licenses/MIT).