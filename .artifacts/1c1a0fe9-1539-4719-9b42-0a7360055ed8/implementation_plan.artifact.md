# Plan de Implementación: Nuevas Funcionalidades y CRUD Completo (Actualizado)

## Resumen de Cambios

Este plan detalla los pasos para implementar los requerimientos solicitados manteniendo la arquitectura limpia (Clean Architecture) y el diseño visual actual:
1. **Limpieza inicial**: Eliminar la precarga de instantes de ejemplo, dejando únicamente las etiquetas iniciales en la base de datos.
2. **Columna Archivado**: Agregar `archivado: Boolean` a `InstanteEntity`, subir versión de `AppDatabase` a 2 con `fallbackToDestructiveMigration()`, y actualizar DAOs/repositorio para filtrar no archivados / archivados y archivar/desarchivar.
3. **Estado Vacío**: Actualizar la pantalla de Inicio para mostrar un estado vacío centrado con el mensaje *"Agrega esos momentos que te hicieron sentir"* e ícono cuando no haya instantes.
4. **Menú de Tarjetas**: Agregar un menú de tres puntos (`DropdownMenu`) junto al corazón con opciones: **Editar**, **Archivar** / **Desarchivar**, y **Eliminar** (con `AlertDialog` de confirmación y borrado físico del archivo de foto). El estado del menú sobrevive a la rotación (`rememberSaveable`).
5. **Botón X en Foto**: Mostrar un botón "X" cuando hay foto elegida para quitarla. En modo edición, la X solo borra el archivo temporal si es nuevo; el original solo se borra al guardar o si se elimina el instante.
6. **Etiquetas Dinámicas**: Las etiquetas de los chips de filtro y del formulario se leen dinámicamente de la tabla `etiquetas`. Se incluye un botón `"+ Agregar etiqueta"` (diálogo con campo de texto) y se permite eliminar una etiqueta propia mediante pulsación larga (`onLongClick`).
7. **Modo Edición en Nuevo Instante**: Reutilizar la pantalla de creación como modo edición al recibir un `id` de instante, cargando sus datos y haciendo `update` con reemplazo de etiquetas.
8. **Nuevas Pantallas**:
   - Pantalla de **Detalle** (con opciones de editar y eliminar).
   - Pantalla de **Citas** (CRUD completo con agregar cita propia, marcar favorita y eliminar).
   - Chip de filtro "Archivados" en la pantalla de Inicio.

## Propuesta de Cambios por Componente

### 1. Capa de Datos y Dominio (Data & Domain)
- **[MODIFY] InstanteEntity.kt**: Añadir `val archivado: Boolean = false`.
- **[MODIFY] Instante.kt**: Añadir `val archivado: Boolean`.
- **[MODIFY] InstanteMapper.kt**: Mapear `archivado`.
- **[MODIFY] AppDatabase.kt**: Subir versión a 2, añadir `.fallbackToDestructiveMigration()`, eliminar instantes de ejemplo en el callback (mantener solo etiquetas).
- **[MODIFY] InstanteDao.kt**: Consultas para listar no archivados, archivados, archivar/desarchivar, actualizar completo, eliminar por ID.
- **[MODIFY] EtiquetaDao.kt**: Insertar y eliminar etiqueta personalizada.
- **[MODIFY] CitaDao.kt**: CRUD completo para citas (actualizar, eliminar, marcar favorita).
- **[MODIFY] InstantesRepository.kt** y **InstantesRepositoryImpl.kt**: Métodos de dominio y datos actualizados.

### 2. Capa de Presentación (UI & ViewModels)
- **[MODIFY] InicioViewModel.kt** y **inicio.kt**: Estado vacío, chip "Archivados", menú desplegable (Editar, Archivar/Desarchivar, Eliminar con diálogo y borrado físico).
- **[MODIFY] NuevoInstanteViewModel.kt** y **NuevoInstate.kt**: Carga de datos para edición, manejo de archivo temporal al presionar "X", gestión de etiquetas dinámicas y diálogo para agregar nueva etiqueta.
- **[NEW] DetalleInstanteScreen.kt**: Pantalla de detalle con opciones de editar y eliminar.
- **[NEW] CitasScreen.kt**: Pantalla para gestionar citas (agregar, marcar favorita, eliminar).
- **[MODIFY] MainActivity.kt**: Conectar navegación inferior correctamente a Citas, Inicio, Nuevo y Detalle.

## Plan de Verificación

### Pruebas Automatizadas y Compilación
- Ejecutar `gradle_build("app:assembleDebug")` para validar la compilación limpia de la base de datos, migraciones y Compose.

### Verificación Manual
- Comprobar base de datos limpia de instantes y con 4 etiquetas iniciales.
- Probar creación de etiquetas propias y borrado por pulsación larga.
- Probar edición de instantes y manejo de fotos.
- Probar filtro de "Archivados" y la nueva sección de Citas.
