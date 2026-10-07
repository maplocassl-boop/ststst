package com.example.model

import com.example.R
import kotlinx.serialization.Serializable

@Serializable
enum class RiskLevel(
    val label: String,
    val badgeText: String,
    val priorityOrder: Int,
    val actionTimeframe: String
) {
    CRITICO(
        label = "Crítico (Intolerable)",
        badgeText = "RIESGO CRÍTICO",
        priorityOrder = 4,
        actionTimeframe = "Suspensión inmediata de la tarea hasta mitigar el peligro."
    ),
    ALTO(
        label = "Alto (Importante)",
        badgeText = "RIESGO ALTO",
        priorityOrder = 3,
        actionTimeframe = "Corrección urgente requerida antes de continuar el turno."
    ),
    MEDIO(
        label = "Medio (Moderado)",
        badgeText = "RIESGO MEDIO",
        priorityOrder = 2,
        actionTimeframe = "Planificar medidas correctivas con plazo definido (24–72 h)."
    ),
    BAJO(
        label = "Bajo (Tolerable)",
        badgeText = "RIESGO BAJO",
        priorityOrder = 1,
        actionTimeframe = "Mantener monitoreo periódico y mejora continua."
    )
}

@Serializable
enum class ControlHierarchyLevel(
    val orderNumber: Int,
    val title: String,
    val shortTag: String,
    val effectiveness: String,
    val isoReference: String,
    val description: String
) {
    ELIMINACION(
        orderNumber = 1,
        title = "1. Eliminación",
        shortTag = "ELIMINACIÓN",
        effectiveness = "100% · Máxima Eficacia",
        isoReference = "ISO 45001 Cl. 8.1.2.a",
        description = "Suprimir físicamente el peligro del proceso o entorno de trabajo."
    ),
    SUSTITUCION(
        orderNumber = 2,
        title = "2. Sustitución",
        shortTag = "SUSTITUCIÓN",
        effectiveness = "80% · Muy Alta Eficacia",
        isoReference = "ISO 45001 Cl. 8.1.2.b",
        description = "Reemplazar materiales, equipos o procesos peligrosos por otros de menor riesgo."
    ),
    INGENIERIA(
        orderNumber = 3,
        title = "3. Controles de Ingeniería",
        shortTag = "INGENIERÍA",
        effectiveness = "60% · Alta Eficacia",
        isoReference = "ISO 45001 Cl. 8.1.2.c",
        description = "Aislar a las personas del peligro mediante guardas, ventilación localizada o barreras físicas."
    ),
    ADMINISTRATIVO(
        orderNumber = 4,
        title = "4. Controles Administrativos",
        shortTag = "ADMINISTRATIVO",
        effectiveness = "40% · Eficacia Media",
        isoReference = "ISO 45001 Cl. 8.1.2.d",
        description = "Modificar la forma de trabajar: permisos de trabajo (LOTO/Trabajo en Caliente), señalización, rotación y capacitación."
    ),
    EPP(
        orderNumber = 5,
        title = "5. Equipos de Protección Personal (EPP)",
        shortTag = "EPP",
        effectiveness = "20% · Última Barrera",
        isoReference = "ISO 45001 Cl. 8.1.2.e / OSHA 1910.132",
        description = "Proteger al trabajador con equipos certificados (casco, careta, guantes, respirador, arnés) como barrera complementaria."
    )
}

@Serializable
data class ActionControlItem(
    val id: String,
    val hierarchyLevel: ControlHierarchyLevel,
    val description: String,
    val isCompleted: Boolean = false
)

@Serializable
data class FollowUpExchange(
    val id: String,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SstAuditReport(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val sectorTag: String,
    val imageSourceLabel: String,
    val sampleDrawableRes: Int? = null,
    val customImageUri: String? = null,
    val riskLevel: RiskLevel,
    val severityLabel: String,
    val probabilityLabel: String,
    val hazardCategories: List<String>,
    val section1Diagnostico: String,
    val section2Peligros: String,
    val section3Evaluacion: String,
    val section4Medidas: String,
    val section5Recomendaciones: String,
    val thoughtSummary: String? = null,
    val actionItems: List<ActionControlItem> = emptyList(),
    val followUpHistory: List<FollowUpExchange> = emptyList(),
    val rawFullResponse: String
) {
    val completedActionsCount: Int
        get() = actionItems.count { it.isCompleted }

    val totalActionsCount: Int
        get() = actionItems.size

    fun toShareableFormattedText(): String = buildString {
        appendLine("==================================================")
        appendLine("ACTA DE AUDITORÍA VISUAL Y ANÁLISIS DE RIESGOS SST")
        appendLine("Estándares de Referencia: ISO 45001:2018 / OSHA")
        appendLine("==================================================")
        appendLine("Puesto / Entorno: $title")
        appendLine("Sector Industrial: $sectorTag")
        appendLine("Nivel de Riesgo Global: ${riskLevel.label}")
        appendLine("Severidad: $severityLabel | Probabilidad: $probabilityLabel")
        appendLine("Categorías Detectadas: ${hazardCategories.joinToString(", ")}")
        appendLine("--------------------------------------------------")
        appendLine()
        appendLine("1. DIAGNÓSTICO DEL ENTORNO")
        appendLine(section1Diagnostico.trim())
        appendLine()
        appendLine("2. IDENTIFICACIÓN DE PELIGROS Y ACTOS INSEGUROS")
        appendLine(section2Peligros.trim())
        appendLine()
        appendLine("3. EVALUACIÓN DEL NIVEL DE RIESGO")
        appendLine(section3Evaluacion.trim())
        appendLine()
        appendLine("4. MEDIDAS PREVENTIVAS Y CORRECTIVAS (Jerarquía de Controles)")
        appendLine(section4Medidas.trim())
        if (actionItems.isNotEmpty()) {
            appendLine()
            appendLine("Checklist de Controles (${completedActionsCount}/${totalActionsCount} verificados):")
            actionItems.forEach { item ->
                val status = if (item.isCompleted) "[X]" else "[ ]"
                appendLine("$status [${item.hierarchyLevel.shortTag}] ${item.description}")
            }
        }
        appendLine()
        appendLine("5. RECOMENDACIONES TÉCNICAS Y TIPS DE CULTURA PREVENTIVA")
        appendLine(section5Recomendaciones.trim())
        appendLine("==================================================")
    }
}

data class IndustrialScenarioSample(
    val id: String,
    val title: String,
    val sector: String,
    val subtitle: String,
    val drawableRes: Int,
    val defaultContextNote: String,
    val expectedRiskPreview: RiskLevel,
    val fallbackReport: SstAuditReport
)

object SampleScenariosCatalog {
    val industrialSectors = listOf(
        "Detección Automática por IA",
        "Soldadura y Metalmecánica",
        "Logística y Almacenamiento",
        "Mantenimiento Eléctrico e Industrial",
        "Construcción y Obra Civil",
        "Planta Química / Procesos",
        "Oficina y Ergonomía Laboral"
    )

    val scenarios: List<IndustrialScenarioSample> = listOf(
        IndustrialScenarioSample(
            id = "welding_workshop",
            title = "Taller de Soldadura y Corte",
            sector = "Soldadura y Metalmecánica",
            subtitle = "Trabajo en caliente, cilindros de gas comprimido y cables en piso",
            drawableRes = R.drawable.img_sample_welding,
            defaultContextNote = "Operación de soldadura de arco eléctrico en taller metalmecánico con proyección de partículas incandescentes y presencia de cilindros de gas.",
            expectedRiskPreview = RiskLevel.CRITICO,
            fallbackReport = SstAuditReport(
                id = 101L,
                title = "Auditoría: Puesto de Soldadura y Fabricación Metálica",
                sectorTag = "Soldadura y Metalmecánica",
                imageSourceLabel = "Escenario Industrial: Taller de Soldadura",
                sampleDrawableRes = R.drawable.img_sample_welding,
                riskLevel = RiskLevel.CRITICO,
                severityLabel = "ALTA (Extremadamente Dañino)",
                probabilityLabel = "ALTA",
                hazardCategories = listOf("Físico", "Químico", "Locativo", "Mecánico", "Eléctrico", "EPP"),
                section1Diagnostico = """
                    • Actividad Identificada: Proceso de soldadura al arco eléctrico y conformado de estructuras metálicas en nave industrial cerrada.
                    • Puesto de Trabajo: Operador de soldadura / armador metalmecánico sobre banco de trabajo fijo.
                    • Sector Industrial: Industria manufacturera metalmecánica y calderería pesada (CIIU 2511 / OSHA 29 CFR 1910 Subparte Q).
                    • Condiciones Generales del Entorno: Se observa generación activa de chispas y escoria incandescente con radio de proyección superior a 3 metros, presencia de cilindros de gases comprimidos en la zona adyacente, cableado eléctrico tendido sobre el nivel de tránsito peatonal y ventilación natural limitada para la evacuación de humos metálicos.
                """.trimIndent(),
                section2Peligros = """
                    • Riesgos Físicos (Radiación No Ionizante y Térmico): Emisión intensa de radiación ultravioleta (UV) e infrarroja (IR) del arco eléctrico capaz de producir queratoconjuntivitis actínica ("ojo de arco") y quemaduras dérmicas. Proyección de partículas incandescentes a más de 1.200 °C.
                    • Riesgos Químicos (Humos de Soldadura y Gases): Inhalación de humos metálicos (óxidos de hierro, manganeso, cromo hexavalente según electrodo) y gases de protección en puesto sin campana de extracción localizada (sistema LEV) visible sobre el punto de fusión.
                    • Riesgos Locativos y de Incendio/Explosión: Cilindros de gas comprimido ubicados dentro del radio de proyección de chispas sin biombo ignífugo separador ni cadena de sujeción doble visible. Cables de masa y portaelectrodo tendidos por el suelo generando riesgo de tropiezo y caída al mismo nivel.
                    • Riesgos Eléctricos: Tensión en vacío de la máquina soldadora y exposición de cables en piso húmedo o con escoria metálica caliente que puede degradar el aislamiento dieléctrico.
                    • Riesgos Ergonómicos: Postura estática prolongada de pie con flexión cervical e inclinación anterior del tronco sobre la mesa de trabajo.
                    • Fallas en el Uso de EPP: Se requiere verificar el uso continuo de respirador con filtro P100 para humos metálicos bajo la careta facial, polainas de cuero cromo para calzado, mandil de carnaza completo y mangas ignífugas certificadas.
                """.trimIndent(),
                section3Evaluacion = """
                    • Nivel de Severidad Estimado: EXTREMADAMENTE DAÑINO (Severidad Alta). La coexistencia de trabajo en caliente con cilindros de gas comprimido cercanos y humos metálicos puede derivar en lesiones graves (quemaduras de 2.º y 3.er grado, explosión/incendio, electrocución o patologías respiratorias crónicas).
                    • Probabilidad de Ocurrencia: ALTA. El contacto de escoria incandescente con mangueras, cables o materiales combustibles cercanos es inminente durante la operación continua sin pantallas ignífugas.
                    • Clasificación en Matriz de Riesgo (ISO 45001 / GTC 45): NIVEL DE RIESGO I — CRÍTICO (Intolerable).
                    • Dictamen de Auditoría: Requiere intervención inmediata antes de continuar el ciclo productivo; aplicar protocolo de Permiso de Trabajo en Caliente y segregación física de cilindros.
                """.trimIndent(),
                section4Medidas = """
                    1. ELIMINACIÓN: Retirar inmediatamente del radio de 11 metros (regla de los 35 pies de OSHA 1910.252) cualquier material inflamable, solvente, trapo impregnado de grasa o cilindro de gas que no esté en uso activo.
                    2. SUSTITUCIÓN: Reemplazar consumibles de soldadura de alta emisión de humos por alambres tubulares o electrodos de bajo contenido de manganeso y recubrimientos menos tóxicos.
                    3. CONTROLES DE INGENIERÍA:
                       • Instalar brazo articulado de extracción localizada de humos (LEV) posicionado a 30-45 cm del punto de soldadura.
                       • Colocar biombos o cortinas ignífugas translúcidas (norma ISO 25980) alrededor del banco de soldadura para contener chispas y radiación UV.
                       • Anclar los cilindros verticalmente con doble cadena metálica, capuchón protector de válvula y arrestallamas en reguladores.
                       • Elevar los cables eléctricos mediante soportes aéreos o instalar pasacables industriales de piso.
                    4. CONTROLES ADMINISTRATIVOS:
                       • Implementar Permiso de Trabajo en Caliente firmado antes de cada turno y asignar vigía contra incendios con extintor ABC/CO2 a menos de 3 metros.
                       • Establecer pausas activas de descarga postural cada 50 minutos de trabajo continuo.
                    5. EQUIPOS DE Protección PERSONAL (EPP):
                       • Careta fotosensible certificada ANSI Z87.1 (filtro DIN 11-13), respirador elastomérico de media cara con filtros P100 para humos metálicos, guantes de soldador tipo mosquetero en cuero cromo (EN 12477), delantal, manguitos y polainas ignífugas, y botas de seguridad dieléctricas con puntera compuesta.
                """.trimIndent(),
                section5Recomendaciones = """
                    • Normativas de Referencia Aplicables:
                      - ISO 45001:2018 (Cláusula 8.1.2: Jerarquía de controles y gestión del cambio en procesos críticos).
                      - OSHA 29 CFR 1910.252 (Welding, Cutting, and Brazing — Requisitos de prevención de incendios y ventilación).
                      - ANSI Z49.1 (Safety in Welding, Cutting, and Allied Processes).
                      - NFPA 51B (Estándar para la prevención de incendios durante soldadura, corte y otros trabajos en caliente).
                    • Dinámica de Cultura Preventiva ("Regla del Círculo de 10 Metros"):
                      - Antes de encender el arco, realizar una "Pausa de 60 Segundos" en equipo donde el soldador gira 360° inspeccionando que ningún cable pise escoria caliente, los cilindros estén encadenados y el extintor tenga presión vigente.
                      - Realizar charla pre-operacional de 5 minutos enfocada en el efecto silencioso de los humos metálicos y el ajuste hermético del respirador P100.
                """.trimIndent(),
                actionItems = listOf(
                    ActionControlItem(
                        id = "w_1",
                        hierarchyLevel = ControlHierarchyLevel.ELIMINACION,
                        description = "Despejar materiales combustibles y cilindros inactivos en un radio mínimo de 10 metros (NFPA 51B).",
                        isCompleted = true
                    ),
                    ActionControlItem(
                        id = "w_2",
                        hierarchyLevel = ControlHierarchyLevel.INGENIERIA,
                        description = "Instalar biombos ignífugos perimetrales (ISO 25980) y brazo extractor de humos localizado.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "w_3",
                        hierarchyLevel = ControlHierarchyLevel.INGENIERIA,
                        description = "Canalizar y elevar cables eléctricos de soldadura para liberar el pasillo de tránsito.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "w_4",
                        hierarchyLevel = ControlHierarchyLevel.ADMINISTRATIVO,
                        description = "Emitir Permiso de Trabajo en Caliente y verificar extintor ABC operativo a menos de 3 metros.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "w_5",
                        hierarchyLevel = ControlHierarchyLevel.EPP,
                        description = "Verificar uso de respirador con filtro P100 bajo careta ANSI Z87.1, mandil y polainas de cuero cromo.",
                        isCompleted = false
                    )
                ),
                rawFullResponse = "Informe pre-cargado de referencia SST para Taller de Soldadura."
            )
        ),
        IndustrialScenarioSample(
            id = "logistics_warehouse",
            title = "Almacén Logístico y Montacargas",
            sector = "Logística y Almacenamiento",
            subtitle = "Tránsito de montacargas, levantamiento manual de cargas y estanterías en altura",
            drawableRes = R.drawable.img_sample_warehouse,
            defaultContextNote = "Nave de almacenamiento logístico con operación simultánea de montacargas (forklift), manipulación manual de cajas pesadas y escalera portátil cerca de racks.",
            expectedRiskPreview = RiskLevel.ALTO,
            fallbackReport = SstAuditReport(
                id = 102L,
                title = "Auditoría: Centro de Distribución y Almacenamiento en Racks",
                sectorTag = "Logística y Almacenamiento",
                imageSourceLabel = "Escenario Industrial: Almacén Logístico",
                sampleDrawableRes = R.drawable.img_sample_warehouse,
                riskLevel = RiskLevel.ALTO,
                severityLabel = "ALTA (Dañino a Extremadamente Dañino)",
                probabilityLabel = "MEDIA-ALTA",
                hazardCategories = listOf("Mecánico", "Ergonómico", "Locativo", "Físico", "EPP"),
                section1Diagnostico = """
                    • Actividad Identificada: Operaciones de intralogística, almacenamiento en estanterías selectivas (racks metálicos de gran altura), transporte mecánico con montacargas contrabalanceado y manipulación manual de cajas.
                    • Puesto de Trabajo: Operador de almacén (picking) y conductor de montacargas en pasillo operativo compartido.
                    • Sector Industrial: Logística, cadena de suministro y centros de distribución (OSHA 29 CFR 1910.178 / ISO 45001).
                    • Condiciones Generales del Entorno: Se evidencia interacción simultánea hombre-máquina (peatón y montacargas en el mismo corredor), levantamiento manual de carga desde nivel bajo y presencia de escalera portátil en zona de circulación vehicular.
                """.trimIndent(),
                section2Peligros = """
                    • Riesgos Mecánicos (Atropellamiento y Aplastamiento): Coexistencia de trabajador a pie y montacargas en movimiento dentro del mismo pasillo sin barrera física de segregación peatonal. Riesgo de caída de pallets o cajas desde niveles superiores de la estantería.
                    • Riesgos Ergonómicos (Sobreesfuerzo Biomecánico): Manipulación manual de carga con flexión profunda de tronco y carga alejada del eje corporal, incrementando la compresión discal lumbosacra (L4-L5 / L5-S1) por encima de los límites recomendados por la ecuación NIOSH (23 kg).
                    • Riesgos Locativos (Caída de Altura y al Mismo Nivel): Escalera portátil posicionada en corredor activo de montacargas, expuesta a impacto accidental en su base.
                    • Riesgos Físicos: Nivel de iluminación desigual entre niveles superiores e inferiores de los racks e impacto acústico de alarmas de retroceso.
                    • Fallas en el Uso de EPP: Verificar uso de chaleco reflectivo de alta visibilidad clase 2 (ANSI/ISEA 107), casco de seguridad con barbiquejo para trabajos próximos a cargas en altura y calzado de seguridad con puntera de protección contra impacto (EN ISO 20345).
                """.trimIndent(),
                section3Evaluacion = """
                    • Nivel de Severidad Estimado: ALTA. Un atropellamiento por montacargas (peso > 3.000 kg) o el desplome de carga paletizada desde altura tiene potencial fatal o de incapacidad permanente; el sobreesfuerzo lumbar genera trastornos musculoesqueléticos severos.
                    • Probabilidad de Ocurrencia: MEDIA-ALTA. La simultaneidad de picking manual, escalera en pasillo y tránsito de montacargas eleva considerablemente la probabilidad de colisión o lesión dorsolumbar.
                    • Clasificación en Matriz de Riesgo: NIVEL DE RIESGO II — ALTO (Importante).
                    • Dictamen de Auditoría: Se debe segregar temporalmente el pasillo durante las tareas de picking manual o uso de escaleras antes de reanudar el tráfico de montacargas.
                """.trimIndent(),
                section4Medidas = """
                    1. ELIMINACIÓN: Prohibir el ingreso simultáneo de montacargas en el pasillo mientras haya personal a pie realizando picking manual o utilizando escaleras (bloqueo temporal de pasillo con conos/barrera extensible).
                    2. SUSTITUCIÓN: Sustituir el uso de escaleras manuales en pasillos de racks por plataformas móviles con barandilla tipo "order picker" o bajar el pallet completo a nivel de piso con el montacargas antes de manipular cajas.
                    3. CONTROLES DE INGENIERÍA:
                       • Instalar barreras físicas (bolardos y barandillas de acero) en los cabezales de estantería y rutas peatonales segregadas.
                       • Incorporar proyectores LED de seguridad ("Blue Spot" y líneas perimetrales rojas) en el montacargas para advertir su aproximación en cruces ciegos.
                       • Colocar mallas anticaída y topes traseros en los niveles superiores de las estanterías.
                       • Proporcionar mesas elevadoras de tijera o transpaletas eléctricas para evitar el levantamiento desde el suelo.
                    4. CONTROLES ADMINISTRATIVOS:
                       • Establecer regla de distancia mínima de seguridad de 3 metros (10 pies) entre peatones y equipos móviles en operación.
                       • Etiquetar el peso máximo de las cajas y aplicar técnica de levantamiento seguro (espalda recta, flexión de rodillas, carga pegada al cuerpo).
                    5. EQUIPOS DE PROTECCIÓN PERSONAL (EPP):
                       • Chaleco de alta visibilidad ANSI/ISEA 107 Clase 2, calzado de seguridad con puntera de acero/composite y suela antideslizante, guantes de agarre anticorte nivel A2 y casco industrial.
                """.trimIndent(),
                section5Recomendaciones = """
                    • Normativas de Referencia Aplicables:
                      - OSHA 29 CFR 1910.178 (Powered Industrial Trucks — Operación segura de montacargas).
                      - ISO 11228-1 / Método NIOSH (Ergonomía — Manipulación manual de cargas: levantamiento y transporte).
                      - EN 15635 (Almacenaje en estanterías metálicas: uso y mantenimiento del equipo de almacenamiento).
                      - ISO 45001:2018 (Cláusula 6.1.2: Identificación de peligros y evaluación de riesgos operativos).
                    • Dinámica de Cultura Preventiva ("Contacto Visual 3 Segundos"):
                      - Instaurar el protocolo "Ojos con Ojos": ningún trabajador a pie puede cruzar o ingresar a un pasillo sin antes hacer contacto visual directo con el operador del montacargas y recibir confirmación con la mano.
                      - Taller práctico de 5 minutos al inicio de turno sobre higiene postural y calentamiento articular de columna y hombros.
                """.trimIndent(),
                actionItems = listOf(
                    ActionControlItem(
                        id = "l_1",
                        hierarchyLevel = ControlHierarchyLevel.ELIMINACION,
                        description = "Bloquear el tránsito de montacargas en el pasillo mientras haya personal en escalera o picking manual.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "l_2",
                        hierarchyLevel = ControlHierarchyLevel.SUSTITUCION,
                        description = "Retirar la escalera portátil del corredor y descender el pallet a piso o usar plataforma con barandilla.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "l_3",
                        hierarchyLevel = ControlHierarchyLevel.INGENIERIA,
                        description = "Instalar luces perimetrales LED ('Blue Spot') en montacargas y protectores de columna en racks (EN 15635).",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "l_4",
                        hierarchyLevel = ControlHierarchyLevel.ADMINISTRATIVO,
                        description = "Capacitar en levantamiento seguro según ecuación NIOSH y regla de 3 metros de distancia al montacargas.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "l_5",
                        hierarchyLevel = ControlHierarchyLevel.EPP,
                        description = "Asegurar uso obligatorio de chaleco reflectivo Clase 2, casco y botas con puntera certificada.",
                        isCompleted = false
                    )
                ),
                rawFullResponse = "Informe pre-cargado de referencia SST para Almacén Logístico."
            )
        ),
        IndustrialScenarioSample(
            id = "electrical_maintenance",
            title = "Mantenimiento de Tablero Eléctrico",
            sector = "Mantenimiento Eléctrico e Industrial",
            subtitle = "Tablero industrial abierto, escalera metálica y herramientas en piso",
            drawableRes = R.drawable.img_sample_construction,
            defaultContextNote = "Intervención de mantenimiento en gabinete eléctrico industrial abierto, presencia de escalera portátil de aluminio y extensiones eléctricas en el suelo.",
            expectedRiskPreview = RiskLevel.CRITICO,
            fallbackReport = SstAuditReport(
                id = 103L,
                title = "Auditoría: Intervención en Tablero Eléctrico de Fuerza",
                sectorTag = "Mantenimiento Eléctrico e Industrial",
                imageSourceLabel = "Escenario Industrial: Tablero Eléctrico",
                sampleDrawableRes = R.drawable.img_sample_construction,
                riskLevel = RiskLevel.CRITICO,
                severityLabel = "ALTA (Extremadamente Dañino / Fatal)",
                probabilityLabel = "ALTA",
                hazardCategories = listOf("Eléctrico", "Locativo", "Mecánico", "EPP"),
                section1Diagnostico = """
                    • Actividad Identificada: Inspección y mantenimiento correctivo/preventivo en tablero eléctrico de distribución industrial.
                    • Puesto de Trabajo: Técnico electricista industrial / operario de mantenimiento de planta.
                    • Sector Industrial: Mantenimiento industrial e instalaciones eléctricas de baja/media tensión (NFPA 70E / OSHA 29 CFR 1910 Subparte S).
                    • Condiciones Generales del Entorno: Se observa gabinete eléctrico con puerta abierta y barrajes/conductores energizados o expuestos, uso de escalera portátil metálica conductora en las inmediaciones y herramientas/cables dispuestos directamente sobre el piso de concreto sin delimitación de frontera de arco eléctrico.
                """.trimIndent(),
                section2Peligros = """
                    • Riesgos Eléctricos (Contacto Directo e Indirecto y Arco Eléctrico): Exposición a partes activas en tablero abierto con riesgo de electrocución por paso de corriente a través del cuerpo (fibrilación ventricular) y quemaduras severas por relámpago de arco eléctrico (Arc Flash > 19.000 °C).
                    • Riesgo Crítico por Equipo Inadecuado: Presencia de escalera portátil de aluminio (material altamente conductor) junto a un tablero eléctrico abierto, anulando el aislamiento respecto a tierra.
                    • Riesgos Locativos: Ausencia de acordonamiento perimetral para delimitar la Frontera de Aproximación Limitada y la Frontera de Arco Eléctrico. Cables de extensión y herramientas de mano dispersos en el piso generando riesgo de tropiezo hacia el panel abierto.
                    • Ausencia Visible de Bloqueo y Etiquetado (LOTO): No se visualizan candados dieléctricos ni tarjetas de bloqueo personal en los interruptores principales.
                    • Fallas en el Uso de EPP: Se requiere traje/indumentaria ignífuga con clasificación ATPV (cal/cm²) acorde al estudio de arco eléctrico, guantes dieléctricos clase 0/00 con guante protector de cuero, careta facial contra arco eléctrico y casco dieléctrico Clase E.
                """.trimIndent(),
                section3Evaluacion = """
                    • Nivel de Severidad Estimado: EXTREMADAMENTE DAÑINO (Potencial de fatalidad por electrocución o quemaduras por arco eléctrico).
                    • Probabilidad de Ocurrencia: ALTA. El uso de escalera metálica cerca de partes expuestas, sumado a la falta de bloqueo LOTO visible y herramientas en el suelo, configura una condición insegura crítica.
                    • Clasificación en Matriz de Riesgo: NIVEL DE RIESGO I — CRÍTICO (Intolerable).
                    • Dictamen de Auditoría: Detener inmediatamente la intervención, desenergizar el circuito y aplicar Las 5 Reglas de Oro de la Seguridad Eléctrica antes de reanudar.
                """.trimIndent(),
                section4Medidas = """
                    1. ELIMINACIÓN: Desenergizar completamente el tablero eléctrico aguas arriba antes de realizar cualquier intervención mecánica o de conexionado (Trabajar sin tensión como regla primordial según NFPA 70E Art. 110).
                    2. SUSTITUCIÓN: Retirar de inmediato la escalera portátil de aluminio y sustituirla por una escalera dieléctrica certificada en fibra de vidrio (ANSI A14.5).
                    3. CONTROLES DE INGENIERÍA:
                       • Instalar pantallas aislantes internas de policarbonato (protección IP2X contra contacto directo accidental en barrajes).
                       • Colocar alfombra dieléctrica certificada (ASTM D178) frente al tablero eléctrico para aislar al técnico del piso de concreto.
                       • Aplicar dispositivos mecánicos de bloqueo (Lockout/Tagout - LOTO) con candado dieléctrico personal en el seccionador principal.
                    4. CONTROLES ADMINISTRATIVOS:
                       • Aplicar estrictamente las "5 Reglas de Oro" eléctricas: 1) Desconectar, 2) Bloquear y etiquetar (LOTO), 3) Verificar ausencia de tensión con multímetro CAT III/IV, 4) Poner a tierra y en cortocircuito, 5) Delimitar y señalizar la zona de trabajo.
                       • Delimitar con conos y cinta de peligro la Frontera de Arco Eléctrico.
                    5. EQUIPOS DE PROTECCIÓN PERSONAL (EPP):
                       • Casco dieléctrico Clase E (ANSI Z89.1) con pantalla facial anti-arco, guantes aislantes dieléctricos (ASTM D120 / EN 60903) verificados por inflado previo, ropa ignífuga certificada NFPA 70E (mínimo Categoría 2 / 8 cal/cm²) y calzado dieléctrico sin componentes metálicos.
                """.trimIndent(),
                section5Recomendaciones = """
                    • Normativas de Referencia Aplicables:
                      - NFPA 70E (Standard for Electrical Safety in the Workplace — Fronteras de choque, arco eléctrico y LOTO).
                      - OSHA 29 CFR 1910.147 (The Control of Hazardous Energy — Lockout/Tagout) y 1910.333 (Prácticas de trabajo eléctrico).
                      - ISO 45001:2018 (Cláusula 8.1: Planificación y control operacional de energías peligrosas).
                    • Dinámica de Cultura Preventiva ("Un Candado, Una Vida"):
                      - Implementar el ritual pre-tarea "Prueba-Verifica-Prueba" (Test-Before-Touch): probar el detector de tensión en una fuente conocida, verificar ausencia de tensión en las tres fases del tablero, y volver a probar el instrumento.
                      - Prohibir por política interna el ingreso de escaleras de aluminio a salas eléctricas o subestaciones.
                """.trimIndent(),
                actionItems = listOf(
                    ActionControlItem(
                        id = "e_1",
                        hierarchyLevel = ControlHierarchyLevel.ELIMINACION,
                        description = "Desenergizar el circuito principal y verificar ausencia de tensión (Test-Before-Touch CAT IV).",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "e_2",
                        hierarchyLevel = ControlHierarchyLevel.SUSTITUCION,
                        description = "Reemplazar inmediatamente la escalera de aluminio por una escalera dieléctrica de fibra de vidrio.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "e_3",
                        hierarchyLevel = ControlHierarchyLevel.INGENIERIA,
                        description = "Instalar bloqueo LOTO con candado personal y colocar tapete dieléctrico ASTM D178 frente al panel.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "e_4",
                        hierarchyLevel = ControlHierarchyLevel.ADMINISTRATIVO,
                        description = "Acordonar la Frontera de Arco Eléctrico y retirar herramientas/cables del piso de tránsito.",
                        isCompleted = false
                    ),
                    ActionControlItem(
                        id = "e_5",
                        hierarchyLevel = ControlHierarchyLevel.EPP,
                        description = "Equipar guantes dieléctricos probados, careta Arc-Flash, casco Clase E y ropa ignífuga NFPA 70E.",
                        isCompleted = false
                    )
                ),
                rawFullResponse = "Informe pre-cargado de referencia SST para Mantenimiento Eléctrico."
            )
        )
    )
}
