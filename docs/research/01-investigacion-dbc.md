# Dragon Block C (DBC): informe técnico completo para recrearlo en Minecraft moderno

Dragon Block C no se abandonó solo porque su creador murió. Tamás "JinRyuu" Nagy falleció el 9 de octubre de 2018, a los 30 años.\[1\] Su hermano menor, Benjámin "JinGames_Ben" Nagy, siguió publicando actualizaciones hasta mayo de 2023 (Build 1.133).\[2\]\[3\] Después anunció oficialmente que dejaba el modding: no abrirá el código ni cederá el desarrollo a nadie.\[4\] Para recrearlo en versiones modernas, el único camino legalmente defendible es una reimplementación "clean-room" (sin código ni assets de JinGames), o bien contribuir a un sucesor de código abierto ya existente, como DragonMine Z (GPL-3.0).

## TL;DR

- **Estado real:** el creador, JinRyuu, murió en 2018. Su hermano Ben mantuvo el mod hasta la Build 1.133 (DBC 1.4.85 / JRMCore 1.3.51, 5 de mayo de 2023) y luego escribió en el FAQ oficial "I quit Modding". El mod solo existe para Forge 1.7.10, y la licencia prohíbe modificarlo, redistribuirlo, leer su código fuente o descompilarlo sin permiso escrito.
- **Qué hay que replicar:** un RPG sobre Minecraft con:
  - 6 razas, 3 clases y 6 atributos (STR, DEX, CON, WIL, MND, SPI) que se compran con TP.
  - Un "Release %" de poder y Ki con ataques prefijados y personalizados (9 tipos).
  - Formas por raza desbloqueadas con niveles de "Racial Skill" y "God Form", multiplicadores configurables y Form Mastery.
  - Maestros, sagas en JSON, 5 dimensiones, Esferas del Dragón y un renderizado de jugador propio (JBRA, pelo animado y auras).
- **Recomendación:** no portar ni descompilar DBC. Hay dos opciones:
  - (a) Hacer fork o contribuir a DragonMine Z (Forge 1.20.1, GPL-3.0-or-later, con un fork comunitario para 1.21.1).
  - (b) Escribir un mod nuevo en NeoForge 1.21.1 con Data Attachments sincronizados, payloads con StreamCodec, GeckoLib y una librería de animación del jugador, usando nombres y assets propios para reducir el riesgo frente a Toei/Shueisha.

---

## 1. Contexto general

### 1.1 Autor, historia y la verdad sobre la descontinuación

| Hecho | Detalle | Fuente / confianza |
|---|---|---|
| Creador | Tamás "JinRyuu" Nagy (Hungría; dominios jingames.net y ryugujo.hu) | Licencia oficial y PlanetMinecraft: **confirmado**\[2\]\[5\] |
| Inicio | Empezó a trabajar en sus mods "alrededor de octubre de 2012" | Post oficial "A Tragic end": **confirmado**\[1\] |
| Muerte | "In 2018 October 9, Tamás Nagy, known as JinRyuu has passed away at the age of 30" | Post oficial del 11/10/2018: **confirmado** (no se publicó la causa)\[1\] |
| Continuación | Ben, "JinRyuu's youngest brother", anunció que los mods "aren't dead" | Post oficial "A New Start!": **confirmado**\[3\] |
| Último gran contenido | Build 1.128 (15/12/2022): raza Majin, God of Destruction, Instant Transmission y Form Mastery. Ben la llamó "the last big content update I planned to make for Dragon Block C"\[6\] | Changelog oficial: **confirmado**\[6\] |
| Última build | Build 1.133 (05/05/2023): DBC 1.4.85 y JRMCore 1.3.51\[6\] | Changelog oficial: **confirmado**\[2\] |
| Estado actual | En el FAQ: "I quit Modding… Do not expect new updates"; "To honor Jin_Ryuu's… wishes, I will not release the code or hand off development of the mods to someone else" | FAQ oficial: **confirmado**\[4\] |

**Conclusión:** la premisa del usuario es correcta solo en parte. La muerte de JinRyuu frenó el desarrollo, pero el abandono definitivo fue una decisión de Ben (2022–2023). En el FAQ da estos motivos:
- El código está "poorly written" y habría que reescribirlo desde cero.\[4\]
- Una actualización llevaría "even more than a Year".\[4\]
- No poseen la propiedad intelectual de Dragon Ball.\[4\]
- Los mods dan pocos ingresos.\[4\]
- Prefiere trabajar en proyectos originales.\[4\]

Sobre la versión de destino, Ben dijo que, si algún día actualizaba, sería "the latest Forge version at that time, not 1.12.2".\[4\] **No existe ninguna versión oficial de DBC para 1.12.2.** Todo lo que circula como "DBC 1.12.2" es no oficial; por ejemplo, un proyecto cancelado de MCreator.\[7\]

### 1.2 Historial de versiones (resumen)

| Época | Hitos |
|---|---|
| Mar–may 2013 (MC 1.4.7/1.5) | 1.0f: SSJ2 temprano y bioma nuevo. 1.0h: dos pelos Saiyan. 1.0i (27/04/2013): primer sistema RPG, menú de habilidades (J), barra de vida propia y alineamiento. Mayo de 2013: la vida pasa a fusionarse con la barra de Ki y el BP llega a 1.000 millones\[8\] |
| 2013–2014 | Pruebas públicas del sistema de skills (enero de 2014), maestro Roshi, ataques aprendibles, Potential Unlock (antes "overcharge")\[9\]\[10\] |
| 2015–2016 | Vuelve Kaioken; JRMCore 1.2.6–1.2.7 añade costes de TP configurables y multiplicadores de forma;\[11\]\[12\] pelos "G2" que cambian de forma al transformarse\[13\] |
| 2017–2018 | God forms, SSJ4 configurable, Oozaru, formas de Namekianos, Humanos y Arcosianos, deseos y revive; sistema de permisos para modpacks\[13\]\[14\] |
| 2018–2023 (Ben) | Hasta la Build 1.128: Ultra Instinct, Mystic, God of Destruction, Majin, Form Mastery e Instant Transmission. Builds 1.129–1.133: correcciones y configs\[6\] |

### 1.3 Comunidad

- **Oficial:** sitio main.jingames.net con foros, descargas en dl.jingames.net, el Discord oficial (discord.gg/54mDQkf), un grupo de Facebook y el servidor oficial "Dragon Block Zero S" (dbcserver1710.jingames.net).\[4\]\[5\]\[15\]
- **Herramientas oficiales:** Hair Salon (editor y biblioteca de peinados), HD Skin Upload y Ben's Custom HUD Creator.\[15\]
- **Wikis de fans:** dragonblockc.fandom.com (la más detallada en números), jindbc.fandom.com y la "Unofficial Dragon Block C Wiki" en Miraheze.\[16\]\[17\]\[18\]
- **Wiki antigua oficial:** las páginas main.jingames.net/wiki/… están desactualizadas; hablan, por ejemplo, de un límite de atributos de 120.\[19\]

---

## 2. Dependencias y arquitectura técnica

### 2.1 Familia de mods

| Mod | Rol |
|---|---|
| **JRMCore** (JinRyuu's Mod Core) | Núcleo común: datos del jugador, atributos, formas, configs (`jinryuujrmcore.cfg`), sistema de misiones, HUD y comandos `/jrmc*` |
| **JBRA Client** | Renderizado del cuerpo del jugador: razas, pelo, animaciones y skins HD |
| **Dragon Block C** | Contenido Dragon Ball: entidades, dimensiones, ítems, ataques de Ki y maestros\[15\] |
| Naruto C, Sword Art Online C | Otros "power types" sobre JRMCore. Naruto usa chakra; en el código descompilado, `jrmcPwrtyp == 3` corresponde a SAO\[20\] |
| Family C, Years C, Hair C, HD Skins | Familias e hijos NPC, edad, pelo personalizado. Family C se portó a Forge 1.20.2 en dic. de 2023 (con YourDailyModder)\[6\]\[21\] |

Instalación mínima según la guía oficial: **JRMCore + JBRA C + Dragon Block C**,\[22\] todos de la **misma build**.\[15\] Si las builds no coinciden, el juego falla con `NoSuchFieldError` o `ClassNotFoundException`.\[4\] La plataforma es Forge 1.7.10; en un log de 2016 aparece Forge 10.13.4.1558.\[11\] Las builds antiguas de Forge necesitaban Java 7.\[23\]

### 2.2 Lo que se sabe del código (sin acceso a la fuente)

- **Paquetes y clases** (descompilación pública no autorizada de JRMCore 1.3.3, compilada con Java 6, en Pastebin): `JinRyuu.JRMCore`, `JRMCoreH` (helper central), `JRMCoreConfig` y `ComJrmctp` (comando).\[20\]
- **Datos del jugador:** se guardan como NBT dentro del compound `PlayerPersisted` de `getEntityData()`.\[24\]\[25\] JRMCore accede a ellos con `JRMCoreH.nbt(player, "pres")`.\[20\]\[26\] Es decir, DBC no usa un sistema de capabilities moderno, sino NBT "persisted" del jugador.

**Claves NBT confirmadas:**

| Clave | Tipo | Significado | Fuente |
|---|---|---|---|
| `jrmcTpint` | int | Training Points (0–1.000.000.000) | Código descompilado de `/jrmctp`\[20\] |
| `jrmcPwrtyp` | byte | Tipo de poder; 3 = SAO | Código descompilado\[20\] |
| `jrmcSSltX` | string | Nivel de Super Form con formato "TR<n>" (p. ej. "TR4")\[24\] | Foro oficial (scripting con CustomNPCs) |

Otras claves que la comunidad cita (`jrmcStrI`, `jrmcDexI`, `jrmcCnsI`, `jrmcWilI`, `jrmcIntI`, `jrmcCncI`, `jrmcRace`, etc.) **no se pudieron verificar** con fuentes públicas.

- **Red:** el changelog 1.3.49 dice que los datos de Form Mastery "could reach the max packet String limit" y que tuvieron que enviarse por segmentos.\[6\] Esto indica que la sincronización serializaba datos en cadenas de texto, un diseño frágil que **no** conviene copiar.
- **Configs conocidas:**
  - `config/jinryuujrmcore.cfg` (la central, con muchas opciones "Server Sided").\[6\]
  - `config/jingames/dbc/races/majin/main.cfg`.\[6\]
  - `config/jingames/dbc/skills/instant_transmission.cfg`.
  - `config/jingames/dbc/forms/form_mastery_main.cfg`.\[6\]\[27\]
  - Misiones en `saves/<MUNDO>/data/missions` (`mainDBC.json`).\[6\]\[28\]
- **Ofuscación:** las clases están compiladas sin ofuscación especial más allá de los nombres SRG de Forge (`func_74775_l`, etc.). Aun así, la licencia prohíbe expresamente descompilar y leer el código.\[5\]

### 2.3 Mods de terceros que exponen la API "por fuera"

- **CustomNPC+ DBC Addon** (KAMKEEL y somehussar, 1.7.10, 1.2.1 de marzo de 2026). Se engancha a DBC mediante mixins (UniMixins). Añade 21 habilidades basadas en Ki, 11 tipos de condición, un enum de las 19 skills de DBC con `tpCost()` y `mindCost()`, formas y auras personalizadas, y definiciones TypeScript.\[29\]\[30\]\[31\] Su Javadoc y su código son la mejor documentación indirecta del modelo de datos de DBC.
- **PewDizinho/CustomNpcScriptingWithDbcMod:** helpers en JavaScript (Nashorn) como `getDbcHealth` y `getDbcAttributes(player).str/.con`.\[32\]
- **Hedaox:** ninjinentities (cientos de NPC y sagas, GPL-3) y ninjinkb (knockback proporcional a las stats).\[33\]\[34\]

---

## 3. Sistemas de juego

Las convenciones de confianza en esta sección son:
- **[OF]**: guía, changelog o wiki oficial de JinGames.
- **[COM]**: wiki Fandom o foros. Son valores medidos o deducidos por jugadores, con configs por defecto de alguna versión.
- **[CFG]**: valor configurable. Los servidores suelen cambiarlo.

### 3.1 Creación de personaje [OF]

Se abre con la tecla V.\[22\]
- **Página 1:** raza y apariencia. Se recomienda "Custom Hair", que permite usar peinados del Hair Salon y está animado.\[22\]
- **Página 2:** Power Type = Ki, clase y color de aura.\[18\]\[22\]

**Clases** (solo cambian los atributos iniciales):
- Warrior: más poder físico y menos Ki.\[22\]
- Martial Artist: equilibrado.\[22\]
- Spiritualist: más Ki y menos poder físico.\[18\]\[22\]

**Razas y desbloqueos por nivel de Racial Skill:**

| Raza | Racial Skill | Desbloqueos por nivel |
|---|---|---|
| Humano | Human Potential | 1 Buffed · 2 Full Released (ambas escalan con el nivel) · 5 God Form (con skill God Form)\[22\] |
| Saiyan / Half-Saiyan | Super Form | 0 Oozaru · 1 SSJ + Golden Oozaru · 2 SSJ Grade 2 · 3 SSJ Grade 3 · 4 Full Power SSJ (sustituye al SSJ normal) · 5 SSJ2 · 6 SSJ3 · 7 SSJ4 + God Forms\[22\] |
| Namekiano | Power Boost | 1 Giant · 2 Full Released · 5 God Form\[22\] |
| Arcosiano ("Frieza Race") | Transformations | 0 Minimal Forms · 1 Power Points · 3 Super/5th Form · 6 Ultimate/Golden + God Form\[22\] |
| Majin | Abilities | 1 Super Regeneration · 2 Evil/Gray · 3 Full Power/Super Buu · 4 Absorption · 5 Pure Form (+ God Form con God Form 1)\[6\]\[22\] |

Notas:
- El nombre "Arcosian" lo eligió Jin porque "Frieza's race" le parecía un mal nombre [OF, FAQ].\[4\]
- El Majin empieza con más DEX y menos CON y WIL, y su "pelo" siempre tiene el color de la piel [OF].\[6\]
- Saiyan Blue, Rose y Blue Evolution dependen de combinar los niveles de Super Form y God Form; por ejemplo, Blue requeriría Transformation 6 y Godform 2 [COM].\[35\]

### 3.2 Atributos, estadísticas y TP

**Atributos [OF]:** Strength, Dexterity, Constitution, Willpower, Mind y Spirit. Spirit se llamaba antes Concentration.\[19\]\[22\]

| Atributo | Efecto | Datos numéricos |
|---|---|---|
| STR | Daño cuerpo a cuerpo; aumenta el drenaje de formas y de stamina | 2,5 de Melee Damage por punto; "con atributos iguales, STR es responsable del 40% del drenaje" [COM]\[17\]\[18\] |
| DEX | Velocidad y defensa | — |
| CON | Vida ("Body") y stamina ("Action Time") | Regeneración base de 1 por punto [COM]\[17\]\[18\] |
| WIL | Poder de Ki (daño de los ataques de Ki) | —\[18\] |
| MND | Ki máximo, coste de skills (Mind cost) y probabilidad de ganar TP | —\[18\] |
| SPI | Ki y regeneración espiritual | — |

**Estadísticas derivadas [COM]:**
- Melee Damage, que escala con el Release %.\[17\]
- Defense, que reduce el daño al bloquear.\[17\]
- Passive, que reduce el daño sin bloquear y equivale al 20% de Defense.\[17\]
- Vida: las formas no multiplican la vida máxima. En su lugar, **dividen el daño recibido entre el multiplicador**.\[17\]
- Sin stamina solo se hace 1 de daño [OF].\[22\]

**Release %:** con C se carga\[22\] y con Ctrl+C se reduce. Hace falta estar por encima de 0% para usar poderes\[22\] y al menos en 5% para ganar TP. Potential Unlock sube el máximo; en la wiki antigua, de 50% a 100% en el nivel 10 [OF].\[19\]\[22\]\[36\]

**TP (Training Points):** se ganan golpeando, con ataques de Ki, con misiones y con minijuegos. Las pesas y las dificultades Hard e Insane aumentan la probabilidad de ganar TP, pero endurecen la penalización por muerte [OF].\[22\]
- Fórmula de combate [COM]: **TP = 2 + 2 · ⌊MND/5⌋ · (Release/100)**. Requiere Release ≥ 5% y Body ≥ 1. Contra jugadores y shadow dummies se usa el MND del objetivo.\[37\]
- En la versión antigua, 10 "JRMCexp" = 1 TP [OF, wiki antigua].\[19\]
- Comando: `/jrmctp <cantidad> [jugador]`, con un máximo de 1.000.000.000 y nivel de permiso 2 [código].\[20\]

**UC (Upgrade Cost) de los atributos [OF + CFG]:**
- El coste crece con cada nivel según "Attribute Cost Rate" y un "Attribute Multiplier per Attribute", que vale 0,75 por defecto desde la Build 1.129 (antes 1). Si UC = 0, la mejora queda desactivada.\[6\]\[38\]
- Un bug corregido en la 1.129 hacía que un UC entre 0 y 1 diera 2.000 millones.\[6\]
- No se encontró la fórmula exacta publicada; la comunidad la reconstruye con calculadoras en ComputerCraft.\[39\]

**Valores por defecto modernos (JRMCore 1.3.36) [OF]:**
- Attribute Maximum: 10.000 (antes 500).\[40\]
- Attribute Over Limit: true.\[40\]
- TP ganado por golpe: 2 (antes 1).\[40\]
- Mind cost de cada skill y forma: 20.\[40\]
- Multiplicador de velocidad de vuelo: 1,5.\[40\]

**Coste de las skills [COM/CFG]:** es incremental. El valor configurado se suma en cada nivel; por ejemplo, con 5 → 5, 10, 15… TP.\[41\]

### 3.3 Sistema de Ki y técnicas [OF]

- Hay una barra de Ki, una barra de vida y una barra de stamina, y el HUD es personalizable.
- Los ataques se lanzan con Ctrl (2nd Fn) + clic derecho\[22\] y se cambian con Ctrl + rueda, Ctrl + 1–8 o Ctrl + clic central.\[36\]\[42\]
- **Prefijados** (4 huecos, enseñados por maestros):\[10\]
  - Kamehameha, Ki Blast, Spirit Bomb, Destructo Disk, Makankosappo, Masenko.\[22\]
  - Big Bang, Final Flash, Galick Gun, Burning Attack, Supernova.\[22\]
  - Fake Moon (permite convertirse en Oozaru).\[22\]
- **Personalizados** (4 huecos; se pueden enseñar a otros jugadores y crearlos cuesta TP).\[10\]\[22\]
  - Tipos: Wave, Blast, Disk, Laser, Spiral, Large Blast, Barrage, Shield y Explosion.\[22\]
  - Parámetros: velocidad, daño, efecto (explosión, empuje para Shield, explosión final para Explosion), coste de Ki, tiempo de carga (depende del tipo y del daño), color y sonido.\[22\]
- Los ataques pueden chocar entre sí ("clash").\[43\]
- Con God of Destruction, los ataques personalizados se convierten en "Destroyer Ki": son algo más débiles, pero destruyen los ataques más débiles que ellos.\[22\]
- **Fórmula de daño de Ki:** no hay una pública verificada. Se sabe que WIL → Ki Power escala el daño y que Potential Unlock permite sobrecargar por encima de la carga normal.\[19\]\[44\]

### 3.4 Transformaciones

**Mecánica [OF]:**
- Se eligen en el Action Menu (mantener X) y se activan manteniendo G.\[22\]\[45\]
- Doble pulsación de G: instant transform,\[22\] que se desbloquea con Form Mastery.
- H: mantener para destransformarse del todo; doble pulsación para bajar una forma.\[22\]

**Multiplicadores (configs por defecto antiguas; los de la comunidad no coinciden entre sí):**

| Forma | Multiplicador | Otros datos | Fuente |
|---|---|---|---|
| SSJ | ×1,3 a STR/DEX/WIL | 10 Ki/s; 100 TP | [COM] Fandom; el foro de 2016 coincide\[35\]\[46\] |
| SSJ Grade 2 | ×1,5 | 250 TP | [COM]\[35\] |
| SSJ Grade 3 | ×1,6 STR/WIL, ×1,44 DEX | — | [COM] foro\[46\] |
| SSJ2 | ×2,0 | — | [COM]\[35\] |
| SSJ3 | ×2,5 | 20 Ki/s; 2.000 TP | [COM]\[35\]\[46\] |
| Arcosian Super (5th) | ×2,0 | — | [COM]\[35\]\[46\] |
| Humano Full Release (máx.) | ×1,71 STR/WIL, ×1,94 DEX | — | [COM]\[46\] |
| Namek Full Release (máx.) | ×1,71 STR/DEX, ×1,94 WIL | — | [COM]\[46\] |
| Majin Evil (ejemplo oficial) | ×2,2 | — | [OF] changelog 1.129 |
| Kaioken x2/x3/x10/x20/x50/x100 | ×1,2 / 1,4 / 1,6 / 1,8 / 2,0 / 3,0 | Drena vida, no Ki\[35\] | [COM] |
| Estado Majin (Babidi, alineamiento malo) | ×1,1 | No se multiplica con Kaioken\[35\] | [COM]\[35\] |

**Reglas de combinación:**
- Kaioken es un multiplicador **independiente** que se multiplica con la forma activa: SSJ2 ×2 con Kaioken x2 da ×2,4, no ×2,2 [COM].\[35\]
- Una config "Server Sided" decide si Kaioken es sostenible en cualquier forma [foro].\[45\]
- Existe un "bonus plano" de las formas Saiyan. Si `atributo + bonus > atributo × multiplicador`, se aplica el bonus en lugar del multiplicador. Así el SSJ siempre aporta algo en niveles bajos [COM, foro 2016].\[46\]
- Desde la 1.131 existe "Bonus Attribute Multiplier Per Racial Skill Level" por raza. Se pensó para compensar que las razas no Saiyan tienen menos formas [OF].\[6\]\[27\]

**Fórmula oficial de atributo con Mastery y Absorción (Majin, changelog 1.129):**
- Activado: `STR × FormMulti × (Mastery × (1 + Absorción))` → 1000 × 2,2 × (2 × 1,5) = **6.600**.\[6\]
- Desactivado (por defecto): `STR × FormMulti × (Mastery + Absorción)` → 1000 × 2,2 × 2,5 = **5.500**.\[6\]

**Drenaje de Kaioken [COM]:**
- Por nivel de skill, de la wiki Fandom:
  - Nivel 1: x2 a −151 HP por intervalo.\[47\]
  - Nivel 10: x2 a −20 y x3–x20 a −118.\[47\]
- Fórmula de un jugador del foro:
  - Kaioken: daño ≈ (STR+WIL) por etapa, reducido un 10% por nivel de skill.\[48\]
  - Super Kaioken: suma 5·(STR+WIL)·índice de la forma SSJ. Por eso Kaioken con FPSSJ (índice 4) dolía 4 veces más que con SSJ.\[48\]
- Desde la 1.3.42 hay multiplicadores configurables por raza y por nivel [OF].\[40\]

**Form Mastery (Build 1.128+) [OF]:**
- Hay maestría por forma y por raza; el nivel máximo por defecto es 50 (antes 100).\[6\]
- No se gana maestría con Release al 0%.\[6\]
- Desbloquea el instant transform y reduce costes.
- Se configura en `form_mastery_main.cfg`, con opciones como `Gain_Multi_Div_Plus`, `Add_Gains_To_Other_Masteries` e `Instant_Transform_Unlock_Level`.\[6\]

**Formas no raciales [OF]:**
- Kaioken: cuesta vida; se aprende de King Kai.\[22\]
- God Form: sin coste de Ki; se aprende de Jin y de Whis.\[22\]
- Mystic / Old Kai Unlock: sin coste de Ki y con límite de tiempo; al agotarlo se pierde un nivel de la skill. Se aprende de King Kai.\[22\]
- Ultra Instinct: requiere God Form y la Racial Skill al máximo, además de nivel y vida mínimos. Da esquiva automática según una barra "Heat". Se aprende de Jin y de Whis.\[22\]
- God of Destruction: requiere nivel y alineamiento, y da Destroyer Ki y Destroyer Aura. Se aprende de Whis.\[22\]

**Estados especiales [COM/OF]:**
- Legendary: cada 20–30 minutos hay un 10% de probabilidad configurable. Potencia las formas SSJ que no son de la línea divina y vuelve el aura verde.\[35\]
- Divine: con Divine, las formas God se vuelven Rose para Saiyans, naranja para Namekianos y negro para Arcosianos.\[6\]\[35\]
- Arcosianos: además usan Power Points, configurables en Max, Growth, Cost y damage multiplier.\[40\]

### 3.5 Skills [OF]

Cuestan TP y Mind.

| Skill | Efecto | Maestros |
|---|---|---|
| Fusion | Fusión con un jugador cercano de la misma raza; el anfitrión mantiene G y el otro carga C | Goku\[22\] |
| Jump / Dash | Saltar más / dash lateral (Ctrl + A/S/D) | Roshi, Kami, Korin, Babidi, Frieza, Vegeta\[22\] |
| Fly | Vuelo con F; cuesta Ki | Roshi, Kami, Piccolo, Gohan, King Kai, etc.\[22\]\[36\] |
| Endurance | Reduce el daño recibido (máx. 30% en nivel 10 según la wiki antigua)\[19\] | Kami, Cell, Vegeta, etc. |
| Potential Unlock | Sube el Release máximo | Guru, Goku, Kami, Piccolo, etc.\[22\] |
| Ki Sense | Ver la vida y el Ki de entidades; lock-on con Z | Goku, Gohan, Piccolo, Kami, Cell\[22\] |
| Meditation | Regenera Ki al cargar | Kami, Piccolo\[22\] |
| Ki Protection | Defensa extra a cambio de Ki | Cell, Gohan, Piccolo\[22\] |
| Ki Fist + Ki Infuse | Armas de Ki | Goku, Trunks, Gohan, Piccolo, Cell\[22\] |
| Ki Boost | Más Ki máximo | King Kai\[22\] |
| Defense Penetration | Ignora parte de la defensa | Goku, Trunks |
| Instant Transmission | Corto alcance (Alt + clic derecho; sobre, detrás o delante del objetivo). Largo alcance (mantener Alt 3 s; a un miembro del grupo, incluso en otra dimensión salvo el Otro Mundo) | Goku, Cell\[6\]\[22\] |

Habilidades raciales del Majin:
- Super Regeneration: cura cargando Ki, a costa de Ki y stamina.\[6\]
- Absorption: lanza un proyectil que absorbe al objetivo si el usuario es más fuerte.\[6\] Cuesta vida, se pierde al morir y cambia el aspecto del Majin [OF].\[6\]

### 3.6 Alineamiento, muerte y Otro Mundo

- **Alineamiento:** existe desde 2013. Matar Namekianos te vuelve más malvado y el color del aura depende del alineamiento [OF].\[8\]
- **Lo que depende del alineamiento:** el estado Majin (Babidi, alineamiento malo), God of Destruction y Rose (Divine/malo).
- **Muerte:** al morir en Survival se va al Otro Mundo. Enma revive y reencarna a los jugadores,\[22\] y hay una "Death Penalty" configurable.\[49\]\[50\]
- **Resurrección:** también por deseo, con "Revive a player", "Revive and bring here" o "Revive all Neutral players (same karma)" [COM].\[51\]

### 3.7 Dimensiones y estructuras [OF]

| Dimensión | Contenido | Acceso |
|---|---|---|
| Overworld | Kami's Lookout siempre en X0 Z0 (Kami, Korin, Piccolo, Vegeta, Trunks, Whis y la puerta de la Habitación del Tiempo). Kame House (océano); casa de Goku, Cell Arena y nave de Babidi (llanuras)\[22\] | — |
| Planeta Namek | Biomas y agua verdes, árboles Ajisa, casas namekianas, Guru, nave de Frieza, dinos y ranas | Spacepod o Instant Transmission\[22\]\[50\] |
| Planeta Vegeta | Páramo con agua roja y guerreros Saiyan | Spacepod o IT\[22\]\[50\] |
| Otro Mundo | Palacio de Enma en X0 Z0; Camino de la Serpiente hasta el planeta de King Kai (X100, Z−3700; con gravedad); Infierno con ogros | Morir\[22\]\[50\] |
| Habitación del Tiempo | Gravedad y entrenamiento de TP | Puerta en el Lookout\[22\] |
| Null Realm | Vacía, con 2 arenas | Whis o IT\[22\] |

Más detalles:
- Las estructuras generadas se localizan con `/dbc loc`.\[22\]
- Transporte: Spacepod (clic central → número de destino → start) y Nube Voladora.\[42\]\[52\]

### 3.8 NPCs, maestros, sagas y enemigos

**Maestros [OF]:**
- Kami: corta y regenera colas, resetea jugadores y da pesas.\[22\]
- Korin: da la Nube y Senzus.\[22\]
- Roshi, Piccolo, King Kai y Whis: dan pesas. Whis también teletransporta al Null Realm.\[22\]
- Babidi: da el estado Majin.\[22\]
- Enma: revive y reencarna.\[22\]
- Resto: Goku, Gohan, Vegeta, Trunks, Cell, Frieza, Guru y Jin.\[22\]

**Story Mode:**
- Hay misiones principales y secundarias (tecla L), con sagas DB, DBZ y Super.\[22\]
- Desde la 1.4.75 incluye misiones anteriores a DBZ y enemigos más fuertes.\[8\]
- El sistema de misiones está en JRMCore y los datos van en JSON por mundo, de modo que los servidores crean sagas propias.\[28\] Ejemplos: ninjinentities y el hilo "DBC Story Mission Creator".\[33\]
- `/jrmcm main 0` reinicia la saga principal.\[42\]

**Enemigos y otros NPC:**
- Saibaimen, Saiyans de Planeta Vegeta, Namekianos, ogros, la Fuerza Ginyu y jefes de saga.
- Desde la 1.4.81 son agresivos por defecto, salvo los Ginyu.\[6\]
- Shadow Dummies sirven para entrenar.
- **No se encontraron tablas públicas fiables de vida y ataque por enemigo.** Sus valores viven en el JSON de misiones y en las configs, y conviene extraerlos de una instalación propia (leer tus propios archivos de configuración no es descompilar).

### 3.9 Ítems y bloques

**Dragon Blocks (Esferas del Dragón):**
- Pueden aparecer cerca de cada jugador una vez por día de juego, en la Tierra y en Namek, y se detectan con el Dragon Radar [OF].\[51\]\[52\]
- Para invocar al dragón se colocan las 7 en forma de "H" y se hace clic derecho en la del centro [OF].\[52\]
- Shenron concede 1 deseo y Porunga 3 [COM].\[51\] La versión antigua tenía 5 deseos y Namek "triplicaba el efecto" [OF].\[43\]
- Tras usarlas quedan en forma de piedra.\[51\]

**Deseos documentados por la comunidad [COM]:**
- 5 Warenai Crystals, 3 Senzu, 1 Katchin Shard, 5 Janemba Essence, 3 Diamonds, 3 Tier 1 Tech Chips.\[51\]
- Revivir a un jugador, revivir y traerlo, revivir a todos los neutrales.\[51\]
- Cambiar el color del aura o el color de la forma Golden Arcosiana.\[51\]
- Small Club; Power Pole (lanza maligna si es Porunga).\[51\]
- Con Years C: joven, viejo o niño.\[51\]

**Otros:**
- Senzu (de Korin), scouters (4 modos con F4, incluido el de multijugador), pesas (multiplicadas por la gravedad).\[52\]\[53\]
- Dispositivo de gravedad (reduce STR y DEX mientras está activo).\[53\]
- Medical Pod con puertas que se mueven en grupo (hasta 11 en la 1.4.81).\[6\]
- Gis y outfits, armas como la Power Pole, Katchin, Warenai, Spacepod y Nube.
- Las recetas están en la página oficial "Recipes".
- **No hay en DBC un sistema de "cápsulas"** documentado; la comunidad usa mods externos de cápsulas.\[54\]

### 3.10 Interfaz y keybinds [OF]

| Tecla | Función |
|---|---|
| V | Creador de personaje / Data Sheet (News, Character, Skills, Ki Techniques, Training, Saga, Group, Settings)\[22\] |
| X (mantener) | Action Menu: formas, Kaioken, skills y ajustes del servidor\[22\] |
| C / Ctrl+C | Cargar / bajar Release\[36\] |
| G / H | Transformarse / destransformarse\[22\]\[36\] |
| F | Vuelo\[36\] |
| R | Turbo\[22\]\[36\] |
| Z | Lock-on (requiere Ki Sense)\[22\] |
| L | Sagas\[22\] |
| K | Noticias\[22\] |
| F4 | Modos del scouter y de Ki Sense\[22\]\[42\] |
| Ctrl | 2nd Fn\[36\] |
| Alt | 3rd Fn (Instant Transmission)\[6\]\[22\] |

Otros elementos de interfaz:
- La pestaña Training tiene minijuegos ("Concentration") y "Air Boxing".\[15\]
- Hay un sistema de grupos (Group Management).\[15\]\[22\]

### 3.11 Multijugador y administración

- **PvP:** existe la opción "Friendly Fist", que deja KO en lugar de matar.\[6\]
- **Zonas:** hay Safe Zones con blacklist.\[6\]
- **Configs del servidor:** multitud de opciones "Server Sided".\[45\]
- **Comandos documentados:**
  - `/jrmctp`, `/jrmcheal (energy, body, stamina, food) [jugador]`.\[11\]
  - `/jrmcm main 0`, `/dbc loc`.\[22\]
  - Un comando para comprobar Form Mastery.\[6\]
- **Sincronización:** el changelog admite problemas de sincronización de posición al teletransportarse entre dimensiones y un "lag" de los datos de Family C en servidores grandes.\[6\]
- **Licencia para servidores:** usar los mods en un servidor está permitido sin pedir permiso. Los modpacks solo se permiten en plataformas oficiales (CurseForge) [OF, FAQ].\[4\]

---

## 4. Assets y renderizado

- **Jugador:** JBRA sustituye el modelo del jugador por cuerpos de cada raza, con colas, cuernos y antenas, y gestiona tamaños. Por ejemplo, SSJ4 pone la altura de adulto [OF].\[6\]
- **Pelo:**
  - Es "Custom Hair" paramétrico. Los peinados se diseñan en la web Hair Salon y se comparten como código.\[22\]
  - Los pelos "G2" permiten cambiar de forma al transformarse (SSJ, SSJ2).\[13\]
  - Las animaciones son procedurales.
- **Auras:**
  - Tienen un color por alineamiento o elegido; los estados Legendary y Divine cambian el color.
  - Hay efectos especiales, como relámpagos en SSJ3 y vapor de "tetera" en los Majin.\[6\]
  - Kaioken tiene un aura roja propia.\[6\]
- **Animaciones especiales:** la 1.6.51 añadió una animación única para Instant Transmission.\[6\]
- **Sonidos:** scouter, transformaciones, teletransporte (con un sonido distinto para Goku Black si el jugador tiene Divine) y ataques.\[6\]
- **Texturas:** de JinRyuu. Packs HD de la comunidad: Kasai Dragon Block C (v4.x, 2018).\[55\]
- **Conflictos conocidos:** Galacticraft rompe la skin y la personalización.\[15\] Al tocar el render del jugador directamente se pierde compatibilidad.
- **Cómo renderiza internamente:** no hay documentación pública. Es razonable suponer, sin confirmarlo, que usa hooks de `RenderPlayer` y `RenderPlayerEvent` de 1.7.10.

---

## 5. Licencia y aspectos legales

**Licencia de JinGames** (Terms of Use, actualizados el 19/09/2019):
- Copyright © 2012–2018 Tamás Nagy y © 2018–2019 Benjámin Nagy. "All rights reserved".\[5\]
- Solo se permite usar los binarios sin modificar.\[5\]
- Redistribuir, modificar, leer el código fuente, copiarlo o **descompilarlo** exige permiso escrito.\[5\]
- El aviso legal añade: "you may not take any content/assets (textures, codes, sounds etc)".\[5\]
- El FAQ cierra la puerta a forks y re-releases: "Can I change your mods and/or re-release them? No."\[4\]

**Consecuencias prácticas:**
1. **Prohibido:** portar DBC descompilándolo, reutilizar sus texturas, modelos, sonidos o configs, y usar su nombre o marca.
2. **Generalmente permitido:** reimplementar mecánicas de juego. Las ideas y reglas no están protegidas por copyright, aunque no esto no es asesoramiento legal. Lo correcto es documentar el diseño a partir de la guía y la wiki, como hace este informe, y escribir código nuevo.
3. **Observación sobre fuentes:** el Pastebin descompilado y el "DBC 1.12.2" de MCreator violan la licencia. Aquí solo se citan como evidencia de nombres de clases y claves, y no deben usarse como base de código.

**Propiedad intelectual de Dragon Ball:**
- Los derechos son de Bird Studio/Shueisha y Toei Animation. JinGames lo reconoció en su FAQ ("We do not own for example Dragon Ball").\[4\]
- Toei hace cumplir sus derechos con agresividad. Animation Magazine lo resumió así en diciembre de 2021: "Toei Flags 150+ of Fan's YouTube Videos in Copyright Sweep". El afectado, el youtuber Mark Fitzpatrick ("Totally Not Mark"), contó en su vídeo de respuesta, citado por ComicBook.com, que los bloqueos fueron en aumento: "fifteen of my videos had been copyright claimed and blocked by Toei Animation. One hour later, that number rose to twenty-eight. And when I woke up this morning, it had reached a total of 150 videos". Toei se amparó en que el derecho japonés no tiene un "fair use" amplio, y según Fitzpatrick recurrir cada reclamación habría llevado "a cumulative 37 years of effort". Al final YouTube bloqueó los vídeos solo en Japón.
- Los mods de Dragon Ball en CurseForge y Modrinth sobreviven en un área gris tolerada. Siguen esa costumbre:
  - No monetizarlos.
  - No usar audio ni arte oficial.
  - Usar nombres descriptivos.

---

## 6. Ports, forks y sucesores

| Proyecto | Plataforma | Licencia | Estado (oct. 2026) | Relación con DBC |
|---|---|---|---|---|
| **DragonMine Z** (DragonMineZ/dragonminez) | Forge 1.20.1 | GPL-3.0-or-later | WIP activo; 620.530 descargas según su página de CurseForge (autor ezShokkoh); última versión v2.1.3, del 20/07/2026 | Inspirado en DBC, código propio. 6 razas (Human, Saiyan, Namekian, Frost Demon, Bio-Android, Majin); stats STR, SKP, RES, VIT, PWR, ENE; dimensiones HTC, Namek y Sacred Kai; estructuras (Lookout, Korin, Guru, Goku, Roshi, Red Ribbon); radar y Shenron. Wiki en GitHub |
| Fork AgentMelinda/dragonminez-1.21.1 | Port comunitario a 1.21.1 | GPL | Fork; su estado no se pudo verificar | Base para 1.21.x\[56\] |
| Add-ons de DMZ | Forge 1.20.1 | Varias | Activos | DMZ Super (formas de Super), Organic Progression (stats por práctica), DMZ Plus (espacio, HTC, worldgen)\[57\]\[58\] |
| **Dragon Block Rebirth** (Medrado26) | Forge 1.20.1 (requiere GeckoLib)\[59\] | All Rights Reserved | Alpha 0.3 (03/09/2026); 78.633 descargas según su página de CurseForge | "Reimaginación/remake" declarado de DBC; código cerrado |
| Dragon Block C Ultimate (PlanetMinecraft) | Dice ser 1.21.11 | ? | Dice estar al "95%" | **Sin verificar**; usa el nombre de DBC, así que cuidado\[60\] |
| DBC Refresh (modpack) | 1.7.10 / 1.20.1 | — | Activo | Reequilibrado del DBC original\[61\] |
| Add-ons de DBC 1.7.10 | Forge 1.7.10 | Varias | Activos | CNPC+ DBC Addon, DBC Additions (0.1.8.07, abr. 2026), ninjinentities (GPL-3), Jwan's DBC Essentials, HavenCore Custom Transformations\[33\]\[62\]\[63\] |
| Otros "Dragon Ball" en 1.21 | NeoForge/Forge | Varias | Pequeños o abandonados | P. ej. "Anime - Dragon Ball Craft" (NeoForge 1.21.1, decenas de descargas)\[64\] |

**Valoración:**
- Ningún proyecto es un port 1:1 de DBC, ni puede serlo legalmente.
- DragonMine Z es el sucesor abierto más maduro y el único con licencia que permite forks.
- Dragon Block Rebirth es el más fiel en intención, pero es cerrado.\[65\]

---

## 7. Guía para replicarlo en versiones modernas

### 7.1 Plataforma recomendada

**NeoForge 1.21.1.** La retrospectiva oficial de NeoForged ("2025: Big Changes are Coming") dice que "18 months after 1.21's release, it still remains the most popular NeoForge version, having accumulated over 16000 mods". También tiene GeckoLib y librerías de animación disponibles.\[66\]

Alternativa: hacer fork de DragonMine Z (Forge 1.20.1) y portarlo. Ahorra meses, pero obliga a publicar bajo GPL.

### 7.2 Qué cambió (1.7.10 → 1.21.x) por sistema

| Sistema DBC (1.7.10) | Equivalente moderno (NeoForge 1.21.1) | Notas |
|---|---|---|
| NBT en `PlayerPersisted` / IExtendedEntityProperties | **Data Attachments** (`AttachmentType` con `Codec`, `.copyOnDeath()`, `.sync(StreamCodec)`)\[67\] | La sincronización de attachments se añadió en 1.21.4 y se retroportó a 1.21.1.\[68\] Bug conocido (#2510, 1.21.8): los attachments sincronizados no se reenvían al cambiar de dimensión,\[69\] así que hay que reenviarlos a mano en `PlayerChangedDimensionEvent` |
| Paquetes FML `SimpleNetworkWrapper` con cadenas | `CustomPacketPayload` + `StreamCodec`, registrados en `RegisterPayloadHandlersEvent` / `PayloadRegistrar`; envío con `PacketDistributor`\[70\]\[71\] | Los handlers corren en el hilo de red; hay que pasar al hilo principal con `enqueueWork`.\[72\] Nada de serializar a String |
| Config Forge `.cfg` | `ModConfigSpec` (TOML) para el cliente y el servidor, más **datapacks/registros dinámicos** en JSON para razas, formas, ataques y misiones | Así los servidores redefinen el balance sin recompilar, igual que con DBC |
| IDs numéricos de bloques y dimensiones | `DeferredRegister`; dimensiones por datapack (`dimension_type` + `level_stem`) | Namek, Vegeta, Otro Mundo, HTC y Null Realm como JSON más chunk generators o biomas propios |
| Estructuras codificadas a mano | Plantillas NBT + Jigsaw / `Structure` + `StructureSet` | El Lookout fijo en 0,0 requiere colocarlo en `ServerStartedEvent` o con una estructura forzada |
| NBT de ItemStack | **Data Components** (1.20.5+) | Esferas (estrella, piedra o activa), scouter (modo), ropa (color) |
| Render del jugador con hooks de `RenderPlayer` | `RenderPlayerEvent`, `RenderLayer` en `EntityRenderersEvent.AddLayers`, modelos con GeckoLib o con un `HumanoidModel` propio; `RenderType` y shaders para las auras | El pipeline de render cambió mucho en 1.21.2+ (render states); conviene fijar la versión |
| Animaciones a mano | **GeckoLib** (entidades, armaduras y ropa) + **playerAnimator** de KosmX (MIT) o su sucesor recomendado, Player Animation Library (PAL) | KosmX ya no desarrolla playerAnimator de forma activa y recomienda PAL; su última build es para 1.21.7\[73\]\[74\] |
| Atributos propios | Atributos vanilla (`Attribute` registrado) + `AttributeModifier` para formas | Permite compatibilidad con otros mods (Apothic Attributes) |
| Keybinds FML | `RegisterKeyMappingsEvent` + `KeyMapping` con `KeyConflictContext` | Hay que evitar los conflictos con G, H y X que sufría DBC |

### 7.3 Arquitectura modular propuesta

```
core/      (equivalente a JRMCore, sin contenido DB)
  data/    PlayerPowerData (attachment): raza, clase, atributos[6], TP, release,
           ki, stamina, vida extra, formas desbloqueadas, mastery{forma:nivel},
           skills{id:nivel}, alineamiento, estados, slots de técnicas
  stats/   StatCalculator puro (sin dependencias de MC → tests unitarios)
  forms/   Registro dinámico FormDefinition (JSON): requisitos, multiplicadores
           por atributo, bonus plano, drenaje, mastery, stacking (kaioken-like)
  skills/  SkillDefinition (JSON): coste TP incremental, coste Mind, efectos
  ki/      KiTechnique (tipo, daño, velocidad, coste, carga, color, sonido)
  net/     Payloads tipados: SyncFull, SyncDelta, Action(transform, charge…)
  mission/ Motor de misiones data-driven (JSON por mundo/datapack)
  hud/     Overlays configurables
content/   (contenido "anime")
  races/ forms/ techniques/ (datapacks)  entities/ dimensions/ structures/
  items/ (esferas, radar, senzu, scouter, ropa GeckoLib)  wishes/
client/    render de raza, pelo paramétrico, auras, animaciones (PAL)
compat/    (opcional) API pública + eventos para add-ons
```

**Principios:**
- Servidor autoritativo para todo cálculo.
- El cliente solo envía intenciones (por ejemplo, "cargar", "transformar" o "lanzar el slot 2").
- Deltas pequeños de sincronización.
- Toda la matemática en una clase pura y testeable.
- El balance por defecto, en datapacks.

### 7.4 Fórmulas base sugeridas (reproducen el comportamiento documentado)

- `AtributoEfectivo = max(Base × MultForma × MultKaioken × (Mastery + Absorción), Base + BonusPlano)`. El modo multiplicativo de la absorción debe ser opcional, como en la 1.129.\[6\]
- `MeleeDamage = 2,5 × STR_ef × (Release/100)`.
- `Passive = 0,2 × Defense`.
- `DañoRecibido = Daño / MultForma`.
- `TP_por_golpe = 2 + 2 × ⌊MND/5⌋ × (Release/100)`, si Release ≥ 5%.
- `CosteSkill(n) = CosteBase × n`.\[41\]
- `UC(nivel) = f(Attribute Cost Rate, 0,75)`. Es configurable; ajústalo con playtesting, porque la fórmula exacta no es pública.

### 7.5 Orden de implementación por fases

1. **Fase 0, infraestructura:** proyecto NeoForge 1.21.1 (MDK), attachment `PlayerPowerData` con Codec, sincronización completa y por deltas, configs y comandos de depuración (`/power set`, `/power tp`). Tests unitarios de `StatCalculator`.
2. **Fase 1, personaje y stats:** GUI de creación (raza, clase, colores), Data Sheet, compra de atributos con TP, Release % (C), HUD de vida, Ki y stamina, y ganancia de TP en combate.
3. **Fase 2, combate y Ki:** golpe con stamina y daño escalado, bloqueo y defensa, vuelo (F), dash y salto, y entidad de proyectil de Ki genérica con los 9 tipos. Creador de técnicas personalizadas y técnicas prefijadas por datapack.
4. **Fase 3, formas:** registro de formas, Action Menu (X), transformar y destransformar (G/H) con doble pulsación, drenaje, Kaioken apilable, Form Mastery y estados (Legendary, Divine, Majin).
5. **Fase 4, render:** modelos de raza (GeckoLib o capas), pelo paramétrico con cambio por forma, auras (RenderType con blending aditivo y partículas) y animaciones de carga y transformación (PAL).
6. **Fase 5, mundo:** dimensiones (Namek, Vegeta, Otro Mundo con Snake Way, HTC, Null Realm), estructuras y maestros (NPC con diálogo que enseñan skills y técnicas), Spacepod y Nube.
7. **Fase 6, sistemas meta:** esferas, radar, invocación en H y deseos data-driven; muerte → Otro Mundo → revivir; alineamiento; Instant Transmission; fusión y absorción; grupos.
8. **Fase 7, sagas:** motor de misiones JSON, enemigos con stats escalables y Shadow Dummy.
9. **Fase 8, multijugador y pulido:** PvP y Friendly Fist, Safe Zones, permisos, rendimiento (tick budget, sincronización solo a quien sigue la entidad) y una API pública para add-ons.

---

## 8. Caveats

- La mayoría de los números de formas, Kaioken y TP vienen de la wiki Fandom y de foros (2016–2020). Corresponden a configs por defecto de versiones concretas, y Ben cambió varias por defecto en la 1.128 para hacerlas "far less grindy".\[6\] Hay que tratarlos como referencia de diseño, no como verdad del código.
- Hay conflictos entre fuentes. Por ejemplo, un hilo del foro da Oozaru ×1,299 y SSJ ×1,119, frente al ×1,3 de otras fuentes, y la lista de deseos varía entre versiones. Este informe prioriza las fuentes oficiales y las comunitarias más recientes.
- Las claves NBT, aparte de `jrmcTpint`, `jrmcPwrtyp` y `jrmcSSltX`, y los detalles de render **no** se pudieron verificar.
- No se encontraron tablas de stats de jefes ni fórmulas exactas de daño de Ki y de UC.
- Las cifras de descargas y versiones de los proyectos sucesores son de CurseForge y Modrinth en 2026 y cambian a menudo.
- Lo dicho sobre la ley no es asesoramiento legal.

## Fuentes

1. [A Tragic end](https://main.jingames.net/a-tragic-end/)
2. [\[1.7.10\] Dragon Block C (Dragon Ball Z mod) Minecraft Mod](https://www.planetminecraft.com/mod/dragon-block-c-a-dragon-ball-z-mod-with-working-scouters/)
3. [A New Start!](https://main.jingames.net/a-new-start/)
4. [FAQ – JinGames](https://main.jingames.net/faq/)
5. [Terms of Use – JinGames](https://main.jingames.net/license/)
6. [JinGames](https://main.jingames.net/)
7. [Dragon block C (my old project)](https://mcreator.net/modification/62122/dragon-block-c-my-old-project)
8. [Search Results for “dragon block c”](https://main.jingames.net/page/2/?s=dragon+block+c)
9. [JinRyuu (Passed away)](https://main.jingames.net/author/jinryuu/page/20/)
10. [Dragon Block C](https://main.jingames.net/dragon-block-c-master-roshi-arrived/)
11. [Status Info](https://main.jingames.net/status-info-week-17-and-new-updates-customize-skill-tp-costs-and-some-damage-multipliers/)
12. [Update released, Kaioken is back!](https://main.jingames.net/update-released-kaioken-is-back/)
13. [JRMCore G2 custom hair update and Naruto C clone jutsu fix](https://main.jingames.net/jrmcore-g2-custom-hair-update-and-naruto-c-clone-jutsu-fix/)
14. [Happy New Year!](https://main.jingames.net/happy-new-year-18w02/)
15. [Dragon Block C – JinGames](https://main.jingames.net/minecraft-mods/dragon-block-c/)
16. [Dragon Block C](https://unofficialdragonblockc.miraheze.org/wiki/Main_Page)
17. [Attributes and Statistics](https://dragonblockc.fandom.com/wiki/Attributes_and_Statistics)
18. [Getting Started](https://jindbc.fandom.com/wiki/Getting_Started)
19. [Stat Sheet – JinGames](https://main.jingames.net/wiki/dragon-block-c/player/stat-sheet/)
20. [/\* \*/ package JinRyuu.JRMCore;/\* \*/ /\* \*/ import java.util.List; - Pastebin.com](https://pastebin.com/TLU13mg3)
21. [MOD UPDATE: Family C for Forge 1.20.2](https://main.jingames.net/mod-update-family-c-for-forge-1-20-2/)
22. [How to play Dragon Block C Guide – JinGames](https://main.jingames.net/how-to-play-dragon-block-c-guide/)
23. [How to install java 7](https://main.jingames.net/how-to-install-java-1-7/)
24. [How do I make a script for lvlup super form](https://main.jingames.net/forums/topic/how-do-i-make-a-script-for-lvlup-super-form/)
25. [Problem w/ Persistant NBT Data - Modification Development - Minecraft Mods - Mapping and Modding: Java Edition - Minecraft Forum - Minecraft Forum](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/1437210-problem-w-persistant-nbt-data)
26. [How to save custom data with a Player? - Modification Development - Minecraft Mods - Mapping and Modding: Java Edition - Minecraft Forum - Minecraft Forum](https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/1434857-how-to-save-custom-data-with-a-player)
27. [JinGames\_Admin](https://main.jingames.net/author/jingames_ben/)
28. [Custom saga](https://main.jingames.net/forums/topic/custom-saga/)
29. [GitHub - KAMKEEL/CustomNPC-DBC-Addon: DBC Support for CNPC+ · GitHub](https://github.com/KAMKEEL/CustomNPC-DBC-Addon)
30. [CNPC+ DBC Addon - Minecraft Mod - Modpack Index](https://www.modpackindex.com/mod/66533/cnpc-dbc-addon)
31. [CNPC+ DBC Addon - Files - Minecraft Mods - CurseForge](https://www.curseforge.com/minecraft/mc-mods/cnpc-dbc-addon/files/all)
32. [GitHub - PewDizinho/CustomNpcScriptingWithDbcMod: Algumas funções para ajudar com o scripting de Custom Npc 1.7.10 interagindo com o mod Dragon Block C / Some functions to help with Custom Npc 1.7.10 scripting interacting with the Dragon Block C mod · GitHub](https://github.com/PewDizinho/CustomNpcScriptingWithDbcMod)
33. [\[MOD\] ninjinentities, adding hundreds new NPCs](https://main.jingames.net/forums/topic/mod-ninjinentities-adding-hundreds-new-npcs/)
34. [\[Mod\] ninjinkb, adding proportional knockback when attacking](https://main.jingames.net/forums/topic/mod-ninjinkb-adding-proportional-knockback-when-attacking/)
35. [Transformations](https://dragonblockc.fandom.com/wiki/Transformations)
36. [Controls](https://main.jingames.net/wiki/dragon-block-c/controls/)
37. [Training Points](https://dragonblockc.fandom.com/wiki/Training_Points)
38. [The Default Config Files Are Bad, So I Rebalanced Them](https://main.jingames.net/forums/topic/the-default-config-files-are-bad-so-i-rebalanced-them/)
39. [Dragon Block C calculators \[1.7.10\]](https://forums.computercraft.cc/index.php?topic=327.0)
40. [Page 2](https://main.jingames.net/page/2/?page=details&server=81)
41. [JRMC Config](https://dragonblockc.fandom.com/wiki/JRMC_Config)
42. [Usage](https://main.jingames.net/minecraft-mods/dragon-block-c/usage/)
43. [Features](https://main.jingames.net/minecraft-mods/dragon-block-c/features/)
44. [Ki attacks](https://dragonblockc.fandom.com/wiki/Ki_attacks)
45. [Transformations Dragon Block C](https://main.jingames.net/forums/topic/transformations-dragon-block-c/)
46. [The Multiplier of Forms](https://main.jingames.net/forums/topic/the-multiplier-of-forms/)
47. [Kaioken](https://dragonblockc.fandom.com/wiki/Kaioken)
48. [Over-the-top health drain from Kaioken with FPSSJ](https://main.jingames.net/forums/topic/over-the-top-health-drain-from-kaioken-with-fpssj/)
49. [Page 22](https://main.jingames.net/page/22/?pa)
50. [Features Now](https://main.jingames.net/minecraft-mods/dragon-block-c/features-now/)
51. [Dragon Blocks](https://dragonblockc.fandom.com/wiki/Dragon_Blocks)
52. [Dragon Block C -\> Other Features - JinGames](https://main.jingames.net/wiki/dragon-block-c/other-features/)
53. [How do I do…? (Dragon Block C)](https://main.jingames.net/forums/topic/how-do-i-do-dragon-block-c/)
54. [Dragon Block Rebirth Basic - Minecraft Modpack - Modpack Index](https://www.modpackindex.com/modpack/118978/dragon-block-rebirth-basic)
55. [Kasai Dragon Block C](https://main.jingames.net/forums/topic/kasai-dragon-block-c/)
56. [GitHub - AgentMelinda/dragonminez-1.21.1: A Minecraft Mod based in the Dragon Ball Series by Akira Toriyama. Made for Forge 1.20.1 · GitHub](https://github.com/AgentMelinda/dragonminez-1.21.1)
57. [dragon ball z - Minecraft Mods - CurseForge](https://www.curseforge.com/minecraft/search?class=mc-mods&page=1&pageSize=20&sortBy=relevancy&search=dragon+ball+z)
58. [Dragon Mine Z (By Gus) - Minecraft Modpack - Modpack Index](https://www.modpackindex.com/modpack/119451/dragon-mine-z-by-gus)
59. [Dragon Block Rebirth Mod Reviews & Ratings](https://moddex.gg/mod/dragon-block-rebirth)
60. [Dragon Block C Ultimate ( Dragon Ball ) Minecraft Mod](https://www.planetminecraft.com/mod/dragon-block-c-ultimate-dragon-ball/)
61. [Dragon Block C Refresh - Minecraft Modpacks - CurseForge](https://www.curseforge.com/minecraft/modpacks/dragon-block-c-refresh)
62. [Dragon Block C Additions - Minecraft Mods - CurseForge](https://www.curseforge.com/minecraft/mc-mods/dragon-block-c-additions)
63. [Icy's Handy Dragon Block C - Minecraft Modpack - Modpack Index](https://www.modpackindex.com/modpack/101422/icys-handy-dragon-block-c)
64. [dragon ball - Minecraft Mods - CurseForge](https://www.curseforge.com/minecraft/search?class=mc-mods&page=1&pageSize=20&sortBy=relevancy&search=dragon+ball)
65. [Dragon Block Rebirth - Dragon Block Rebirth - Alpha 0.1.1 - Minecraft Mods - CurseForge](https://www.curseforge.com/minecraft/mc-mods/dragon-block-rebirth/files/7776013)
66. [playerAnimator - player-animation-lib-forge-2.0.4+1.21.1.jar - Minecraft Mods - CurseForge](https://www.curseforge.com/minecraft/mc-mods/playeranimator/files/7389814)
67. [Data Attachments](https://docs.neoforged.net/docs/datastorage/attachments/)
68. [2025: Big Changes are Coming - The NeoForged project](https://neoforged.net/news/2025-retrospection/)
69. [Synced data attachments on players aren't re-sent when they change dimension · Issue #2510 · neoforged/NeoForge](https://github.com/neoforged/NeoForge/issues/2510)
70. [Registering Payloads](https://docs.neoforged.net/docs/1.21.3/networking/payload/)
71. [Uses of Interface net.minecraft.network.protocol.common.custom.CustomPacketPayload (neoforge 1.21.0-21.0.30-beta)](https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/network/protocol/common/custom/class-use/CustomPacketPayload.html)
72. [The Networking Refactor - The NeoForged project](https://neoforged.net/news/20.4networking-rework/)
73. [playerAnimator - Fabric and NeoForge Animation Library](https://minecrafthub.io/mods/playeranimator)
74. [playerAnimator — Mod for Minecraft by KosmX](https://minecraftbible.com/mods/playeranimator)
