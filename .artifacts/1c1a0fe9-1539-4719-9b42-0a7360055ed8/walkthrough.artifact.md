# Walkthrough - CRUD Completo y Nuevas Funcionalidades

Se han implementado exitosamente todas las mejoras y nuevas funcionalidades solicitadas, manteniendo intacto el diseño visual, colores y tipografías.

## Resumen de Cambios

### 1. Capa de Datos y Dominio (Clean Architecture)
- **[MODIFY] [InstanteEntity.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/data/entity/InstanteEntity.kt)**: Se añadió la columna `archivado: Boolean = false`.
- **[MODIFY] [Instante.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/domain/model/Instante.kt)**: Se añadió el campo `archivado: Boolean`.
- **[MODIFY] [InstanteMapper.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/data/mapper/InstanteMapper.kt)**: Se mapeó correctamente el campo `archivado`.
- **[MODIFY] [AppDatabase.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/data/local/AppDatabase.kt)**: Se actualizó a la versión `2` con `fallbackToDestructiveMigration()`, limpiando la precarga de instantes de ejemplo y conservando únicamente las etiquetas iniciales (*Naturaleza, Arte, Atardecer, Ciudad*).
- **[MODIFY] [InstanteDao.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/data/dao/InstanteDao.kt)**, **[EtiquetaDao.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/data/dao/EtiquetaDao.kt)** y **[CitaDao.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/data/dao/CitaDao.kt)**: Se añadieron consultas para listar no archivados/archivados, archivar/desarchivar, actualizar completo, y CRUD completo para citas y etiquetas.
- **[MODIFY] [InstantesRepository.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/domain/repository/InstantesRepository.kt)** e **[InstantesRepositoryImpl.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/data/repository/InstantesRepositoryImpl.kt)**: Repositorio actualizado con soporte para edición, archivado y gestión de etiquetas/citas.

### 2. Capa de Presentación (UI & ViewModels)
- **[MODIFY] [InicioViewModel.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/ui/viewmodel/InicioViewModel.kt)** y **[inicio.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/inicio.kt)**:
  - Estado vacío centrado con mensaje *"Agrega esos momentos que te hicieron sentir"*.
  - Chip de filtro *"Archivados"*.
  - Menú de tres puntos (`DropdownMenu`) con opciones **Editar**, **Archivar/Desarchivar** y **Eliminar** (con diálogo de confirmación `AlertDialog` y borrado físico del archivo de imagen), sobreviviendo a la rotación (`rememberSaveable`).
  - Carga dinámica de etiquetas desde la base de datos para los chips de filtro.
- **[MODIFY] [NuevoInstanteViewModel.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/ui/viewmodel/NuevoInstanteViewModel.kt)** y **[NuevoInstate.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/NuevoInstate.kt)**:
  - Modo edición al recibir un `instanteId`, cargando datos y actualizando en Room.
  - Botón "X" para quitar foto con manejo de archivos temporales.
  - Creación de etiquetas propias (`"+ Agregar etiqueta"`) mediante diálogo y eliminación por pulsación larga (`onLongClick`).
- **[NEW] [CitasScreen.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/ui/screen/CitasScreen.kt)**: Pantalla de Citas con CRUD completo (agregar, marcar favorita, eliminar).
- **[MODIFY] [MainActivity.kt](file:///C:/Users/ximem/AndroidStudioProjects/AppInstantesqueSeSienten/app/src/main/java/com/instantes/app/MainActivity.kt)**: Navegación conectada entre Inicio, Nuevo (Crear/Editar), Citas y Bienvenida.

## Validación y Pruebas
- Compilación exitosa (`gradle_build("app:assembleDebug")` completado con éxito sin errores).
