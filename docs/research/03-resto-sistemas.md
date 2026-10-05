# DBC — Resto de sistemas: especificación detallada

Oct 4, 2026 · @Fernando

Este documento completa al de Release, Ki y estadísticas: cubre progresión, razas, transformaciones, skills, combate, muerte, mundo, NPCs, esferas e ítems, con el mismo nivel de detalle y la misma leyenda.

| Marca | Significa |
| --- | --- |
| **\[OF\]** | Guía, wiki, changelog o FAQ oficial de JinGames |
| **\[COM\]** | Wiki Fandom, foros o mediciones de jugadores |
| **\[DMZ\]** | Diseño de DragonMine Z (GPL) |
| **\[PROP\]** | Propuesta para tu remake |

Muchos números de DBC cambiaron de una versión a otra y además son configurables. Cuando dos fuentes chocan, aparecen las dos con su versión.

## Progresión: TP, atributos y entrenamiento

Todo se compra con TP (Training Points): atributos, skills, técnicas y formas. Los TP salen sobre todo de golpear con el Release encendido, de minijuegos y de misiones.

### Cómo se ganan TP

| Fuente | Detalle |
| --- | --- |
| Golpear entidades | Requiere Release > 0% (≥ 5% para la fórmula de MND). Probabilidad, no garantizado: \~20% por golpe en versiones antiguas \[COM\]. Cantidad = 2 + 2·⌊MND/5⌋·Release/100 \[COM\] |
| Ataques de Ki que impactan | Dan TP; desde JRMCore 1.3.39 una explosión ya no da TP varias veces al mismo objetivo \[OF\] |
| Minijuegos (V → Training) | Cuestan 1 TP iniciarlos; “Concentration” y “Air Boxing” dan más cuanto mayor tu nivel \[OF, COM\] |
| Shadow Dummy | Un clon tuyo para pelear; usa tu MND para el cálculo; poco rentable porque pega fuerte y desaparece \[COM\] |
| Habitación del Tiempo y planeta de King Kai | Entrenamiento por “experiencia”: cada TP cuesta más experiencia que el anterior \[COM\] |
| Misiones de saga | Recompensas fijas por misión \[OF\] |
| Comando | `/jrmctp <cantidad> [jugador]`, hasta 1.000.000.000 \[OF\] |

Sistema antiguo: 10 de “experiencia” por golpe exitoso = 1 TP \[COM\].

### Configs de TP \[COM, config\]

- **“TP gain / melee rate”** (1–10.000, por defecto 200): cada tantos puntos de MND, +1 TP por golpe.
- **“TP amount gained”** (1–100, por defecto 1).
- **TP por golpe** por defecto 2 desde JRMCore 1.3.36 (antes 1) \[OF\].

### Coste de atributos (UC)

- UC es el coste en TP de subir un atributo un punto, y crece con cada punto comprado \[OF\].
- Hay botones ×1, ×10, ×100 y ×1000 que suben varios puntos y suman su coste \[COM\].
- Configs: “Attribute Cost Rate” y “Attribute Multiplier per Attribute” (0,75 por defecto desde la build 1.129, antes 1); UC = 0 desactiva la compra \[OF\].
- Máximo por atributo: 10.000 por defecto desde 1.3.36 (antes 500); “Attribute Over Limit” permite superarlo con formas \[OF\].
- Desde JRMCore 1.3.46 hay tres configs más: “Start Minus” (divide el coste; siempre existió con valor 140 y retrasa cuándo empieza a subir), “Minimum Value” y el multiplicador 0,75 \[OF\]. La fórmula completa sigue sin ser pública.

### Nivel

- Cada raza empieza con 60 puntos de atributo repartidos \[COM\].
- El nivel sube cada 5 puntos de atributo por encima de 55, así que todos empiezan en nivel 1 \[COM\].
- El nivel afecta a los TP de los minijuegos y es requisito de algunas formas, como Ultra Instinct \[COM, OF\].

### Pesas y gravedad

- **Pesas** (ropa con peso; se equipan en un inventario extra con Ctrl + E): suben la probabilidad de ganar TP pero bajan tu daño y defensa. Se las pides a maestros con “ask for weight” \[OF, build 1.2.9\].
- **Gravedad** (Gravity Device, planeta de King Kai, Habitación del Tiempo): te frena y reduce STR y DEX mientras dura, pero da TP con más facilidad; multiplica el peso de las pesas (10 de peso en 10G = 100) \[OF, COM\].
- El primer Gravity Device usaba solo 10G \[OF\].
- Según un jugador, la bonificación por peso solo contaba hasta los primeros 50 de peso, en lugar de cada 50 \[COM\].

### Dificultad

Hard e Insane suben la probabilidad de TP y endurecen la penalización al morir \[OF\]. Desde JRMCore 1.3.27 la dificultad aparece junto al BP \[OF\].

## Razas y clases

La raza fija tus atributos iniciales (60 puntos en total), tus modificadores y tu árbol de transformaciones. La clase solo cambia modificadores.

### Atributos iniciales \[COM\]

| Raza | STR | DEX | CON | WIL | MND | SPI |
| --- | --- | --- | --- | --- | --- | --- |
| Humano | 10 | 10 | 10 | 10 | 10 | 10 |
| Saiyan | 15 | 10 | 10 | 15 | 5 | 5 |
| Half-Saiyan | 10 | 10 | 10 | 15 | 10 | 5 |
| Namekiano | 5 | 5 | 7 | 10 | 15 | 18 |
| Majin | 10 | 15 | 8 | 7 | 10 | 10 |
| Arcosiano | STR + WIL = 20 | 5 | 15 | — | 5 | 15 |

Del Arcosiano solo están documentados DEX, CON, MND y SPI; STR y WIL suman 20 para cuadrar el total de 60.

### Modificadores de clase que faltaban en el otro documento (%) \[COM\]

| Raza + clase | Melee | Defense | Body | AT | Ki Power | Max Ki | Run | Fly |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Namekian Martial Artist | 0 | 0 | +10 | 0 | +30 | +20 | 0 | 0 |
| Namekian Spiritualist | −10 | +10 | 0 | −10 | +40 | +30 | +10 | +10 |
| Namekian Warrior | +10 | −10 | +20 | +10 | +20 | +10 | −10 | −10 |
| Majin Martial Artist | +10 | 0 | 0 | +30 | +10 | 0 | +10 | 0 |
| Majin Spiritualist | 0 | +10 | −10 | +20 | +20 | +10 | +20 | +10 |
| Majin Warrior | +20 | −10 | +10 | +40 | 0 | −10 | 0 | −10 |

Arcosiano (todas las clases, base de raza): +20% Defense, +10% stamina, +30% velocidad \[COM\].

### Rasgos de raza

- **Humano:** stamina y Ki altos, ataques algo más débiles; 2 formas que escalan con su Racial Skill \[COM\].
- **Saiyan:** cola (Oozaru); pelo solo negro en base; la rama de formas más larga \[OF\].
- **Half-Saiyan:** cola opcional; mismas formas Super Saiyan \[OF\].
- **Namekiano:** antenas en vez de pelo; el único que se hace gigante a voluntad; mayores bonos de Body, Ki Power y Max Ki \[COM\]. Matarlos empeora tu alineamiento \[OF\].
- **Arcosiano:** formas mínimas y Power Points (reserva extra que gastas al 100%) \[OF\].
- **Majin:** “pelo” del color de la piel, Super Regeneration y Absorption \[OF\].

### Racial Skill por niveles \[OF\]

| Raza | Skill | Desbloqueos |
| --- | --- | --- |
| Humano | Human Potential | 1 Buffed · 2 Full Released · 5 God Form |
| Saiyan / Half | Super Form | 0 Oozaru · 1 SSJ + Golden Oozaru · 2 Grade 2 · 3 Grade 3 · 4 Full Power SSJ · 5 SSJ2 · 6 SSJ3 · 7 SSJ4 + formas divinas |
| Namekiano | Power Boost | 1 Giant · 2 Full Released · 5 God Form |
| Arcosiano | Transformations | 0 formas mínimas · 1 Power Points · 3 Super/5th · 6 Ultimate/Golden + God Form |
| Majin | Abilities | 1 Super Regeneration · 2 Evil · 3 Full Power/Super · 4 Absorption · 5 Pure + God Form con God Form 1 |

**Fórmula de las formas humanas y namekianas** \[COM\]: con α = bono base de la forma y β = nivel de la Racial Skill (máximo 5):

```latex
bono = \alpha + \alpha \cdot 0{,}1 \cdot \beta
```

La wiki da como valores efectivos: Buffed 32% al desbloquear y 80% con la skill al máximo; Full Released 38% → 72,5%. Esas cifras no salen exactas de la fórmula, así que tómalas como referencia de diseño.

Desde la build 1.131 existe un “Bonus Attribute Multiplier Per Racial Skill Level” por raza, pensado para compensar a las razas con menos formas \[OF\].

## Transformaciones

Una forma multiplica STR, DEX y WIL, divide el daño recibido y cuesta Ki por segundo. Los multiplicadores viven en la config “Form Damage Multiplier” como porcentajes (150 = ×1,5), así que cada servidor tiene los suyos.

### Multiplicadores por defecto (config moderna) \[COM, config\]

| Raza | Forma → multiplicador (%) |
| --- | --- |
| Saiyan / Half-Saiyan | Base 100 · Oozaru 110 · Golden Oozaru 130 · SSJ 150 · Grade 2 200 · Grade 3 250 · Full Power SSJ 200 · SSJ2 300 · SSJ3 350 · SSJ4 400 · SS God 450 · SS Blue 500 · SS Rosé 500 · Blue Evolution 550 |
| Humano | Base 100 · Full Released 150 · Buffed 200 · God 250 |
| Namekiano | Base 100 · Full Released 150 · Giant 200 · God 250 |
| Arcosiano | Minimal 30 · 1st 40 · 2nd 60 · 3rd 80 · Final (base) 100 · 5th 200 · Golden 250 · God 300 |
| Kaioken | x1 100 · x2 120 · x3 140 · x10 160 · x20 180 · x50 200 · x100 300 |

La lista interna de formas Saiyan también incluye LSS y LSS2, eliminadas hace años aunque sus nombres sigan en la config \[OF\].

**Otras versiones que circulan** (para que no te confundas al comparar):

- Config de 2016: SSJ 120, Grade 2 150, Grade 3 160, Full Power 130, SSJ2 200, SSJ3 250, Oozaru 110, Golden 130 \[COM, foro\].
- Wiki “Transformations” antigua: SSJ ×1,3 (10 Ki/s, 100 TP), Grade 2 ×1,5 (250 TP), Grade 3 ×1,6 y DEX ×1,4 (350 TP), Full Power igual que SSJ pero 1 Ki/s (500 TP), SSJ2 ×2, SSJ3 ×2,5 (20 Ki/s, 2.000 TP), SSJ4 ×2,6 \[COM\].
- Wiki “Saiyan” reciente: SSJ ×1,8 y −9% de daño recibido; Grade 2 ×2,2 y −33%; Grade 3 ×2,4 en STR y WIL con menos DEX \[COM\].

### Reglas de cálculo

- **A qué afecta:** STR, DEX y WIL. CON, SPI y MND no \[COM\]. Cada forma puede repartir distinto: Grade 3 sube STR/WIL más que DEX; las humanas suben DEX más; las namekianas WIL más \[COM\].
- **Vida:** el daño recibido se divide por el multiplicador; las wikis recientes lo muestran como “% de reducción de daño” \[COM\].
- **Bono plano (formas Saiyan):** si atributo + bono > atributo × multiplicador, se usa el bono. Así el SSJ siempre ayuda con atributos bajos \[COM\].
- **Kaioken:** multiplicador aparte que se multiplica con la forma (SSJ2 ×2 con x2 ×1,2 = ×2,4). Drena vida, no Ki \[COM\].
- **Estado Majin** (Babidi): ×1,1 que se multiplica con la forma, pero no con Kaioken \[COM\].
- **Regeneración de Ki por forma:** config aparte. Humano por defecto: Base 100, Full −50, Buffed −25, God −40 (negativo = drena) \[COM, config\].
- **Bonus por nivel de Racial Skill** por raza desde la build 1.131 \[OF\].

### Drenaje

- Por segundo y ponderado por tus atributos: con atributos iguales, STR aporta \~40%, WIL \~35% y DEX \~25% \[COM\]. Más fuerte = más drenaje.
- Valores viejos medidos: SSJ 10 Ki/s, Full Power SSJ 1 Ki/s, SSJ3 20 Ki/s \[COM\].
- Configs de Kaioken desde JRMCore 1.3.42–1.3.43: multiplicador de drenaje de vida por raza y por nivel (por defecto 100%), y temporizadores de “strain” activo y temporal \[OF\].

### Controles

- Mantener G transforma a la forma elegida en el Action Menu (X). Doble G = transformación instantánea, que se desbloquea con Form Mastery \[OF\].
- H mantenido destransforma por completo; doble H baja una forma \[OF\].
- Config “Kaioken Single Form Descend” y otra que permite transformación instantánea a Kaioken, Mystic, Ultra Instinct y God of Destruction \[OF\].

### Formas especiales

| Forma | Requisitos | Efecto | Coste del skill |
| --- | --- | --- | --- |
| Oozaru | Cola + luna llena o Fake Moon | ×1,1; incontrolable salvo con Super Form | Racial Skill 0 |
| Golden Oozaru → SSJ4 | Super Form al máximo; transformarse otra vez desde Golden Oozaru; después queda accesible sin pasar por Oozaru mientras conserves la cola | SSJ4 ×4 (config moderna) | Racial Skill 7 \[COM\] |
| God Form | Super Form 5+ en Saiyans; sin coste de Ki | Forma divina; con Ki Sense detectas a otros en forma divina | 20.000 TP, 10 Mind; Saiyans hasta nivel 3, resto nivel 1 \[COM\] |
| SS Blue / Rosé / Blue Evolution | Combinación de Super Form y God Form; Rosé con estado Divine | 500 / 500 / 550 | — |
| Mystic (Old Kai’s Ritual) | Alineamiento 100% para comprarlo a King Kai | Sin coste de Ki; multiplicador por raza; pierde niveles con el tiempo y hay que volver a King Kai | 1.500 TP, 10 Mind \[COM\] |
| Ultra Instinct | God Form (nivel 2 Saiyans), Super Form 5, vida bajo el umbral (20% en 2018; 30% según la wiki actual; configurable), Release > 0, sin Pain, solo desde forma base | 3 niveles: Sign ×3, casi perfecto ×3,5, Mastered ×4, con esquiva creciente (80% en la versión inicial) | 50.000 TP, 10 Mind \[COM\] |
| God of Destruction | Nivel y alineamiento; Whis | Destroyer Ki y Destroyer Aura (cuesta Ki). No se combina con otras formas: si se junta con Mystic o UI te daña mucho y quita todas | 50.000 TP, 10 Mind \[COM, OF\] |

Detalles de Ultra Instinct \[OF\]:

- No se puede cargar la transformación en movimiento.
- Barra de “Heat”; al usarlo demasiado recibes **Pain**: ralentiza y quita 0,1% de la vida máxima cada 5 s.
- La duración de Pain depende del nivel de UI alcanzado. Una config puede exigir haber tenido Pain para llegar al nivel de pelo blanco.
- UI contra UI: la esquiva se divide por la diferencia de niveles.
- Config propia en `config/jingames/dbc/forms/ultra_instinct.cfg`.

### Formas arcosianas y Power Points \[COM, OF\]

- Los Power Points se generan en una forma “reducida” (por debajo de la final) con el Release bajo 50%, al mismo ritmo que la regeneración de Ki.
- Se gastan al subir de forma y mientras usas formas no raciales: Mystic 70, God 90 y UI/God of Destruction 100 por tick (según la wiki).
- Configs: máximo, crecimiento, coste y multiplicador de daño por Power Points.
- El texto de BP muestra la reserva de Power Points \[OF\].

### Estados que modifican las formas \[COM, OF\]

- **Legendary:** cada 20–30 min, 10% de probabilidad (configurable). Potencia las formas SSJ no divinas y el aura pasa a verde. Desde la build 1.128 también afecta a las formas Majin.
- **Divine:** convierte las formas divinas en Rosé (Saiyan), naranja (Namekiano) o negro (Arcosiano).

## Form Mastery

Cada forma tiene su propio nivel de maestría, que sube usándola y hace la forma más fuerte y más barata. Llegó en la build 1.128 (diciembre de 2022) y todo es configurable por forma y por raza \[OF\].

### Cómo se gana \[OF\]

Estando en la forma, sube la maestría de la(s) forma(s) activa(s) por cuatro vías, cada una con un valor plano más un extra según MND (flat, Mind flat, Mind por nivel, Mind máximo):

| Vía | Cuándo |
| --- | --- |
| On Update | Cada \~5 s mientras sigues en la forma |
| On Attack | Al hacer daño |
| On Damage Taken | Al recibir daño |
| On Fire Ki | Al disparar ataques de Ki |

No se gana con Release 0% \[OF, build 1.129\]. Fusionados: los niveles se combinan, pero no se ganan.

**Rendimientos decrecientes** (“Gain Multiplier Divider Plus”, D). El ejemplo oficial con D = 100, nivel L = 200 y ganancia 3 da 1, así que la fórmula efectiva es:

```latex
ganancia_{real} = ganancia \cdot \left(1 - \frac{L}{L + D}\right) = ganancia \cdot \frac{D}{L + D}
```

### Qué mejora \[OF\]

- **Poder de la forma:** multiplicador de daño/atributo con valor plano, por nivel y máximo.
- **Costes y temporizadores** (cada uno con plano, por nivel y mínimo o máximo según el signo): coste de Ki, coste de Ki de los Power Points arcosianos, coste de vida de Kaioken, “strain” activo y temporal de Kaioken, Heat y Pain de Ultra Instinct, requisito de vida de UI, pérdida de nivel de Mystic, coste de la Destroyer Aura, timers del ritual Saiyan God.
- **Transformación instantánea** (doble G) al llegar a “Instant Transformation Unlock Level”; un valor negativo la desactiva para esa forma.
- **Nivel máximo** por forma (50 por defecto desde la build 1.129; antes 100) \[OF\].

### Reglas encadenadas \[OF\]

- **Auto Learn on Level:** aprende skills o niveles de Racial Skill al llegar a cierta maestría. Formato `Racial,1,10;Racial,2,20;KK,1,10` (Racial Skill 1 con maestría 10, Racial 2 con 20, Kaioken 1 con 10).
- **Required Masteries:** exige maestría en otras formas para poder transformarse. Ejemplo `Base,5.0;SS2,12`.
- **Add Gains to Other Masteries:** al ganar maestría en una forma, otras ganan también con un factor. Con ganancia 15 y `Base,1.0;SS,2;SS2,0.5`: Base +15, SSJ +30, SSJ2 +7,5.

### IDs que usa DBC \[OF\]

- Skills: FZ Fusion, JP Jump, DS Dash, FL Fly, EN Endurance, OC Potential Unlock, KS Ki Sense, MD Meditation, KK Kaioken, GF God Form, OK Old Kai Unlock, KP Ki Protection, KF Ki Fist, KB Ki Boost, DF Defense Penetration, KI Ki Infuse, UI Ultra Instinct, IT Instant Transmission, GD God of Destruction.
- Formas: Humano Base, Full, Buffed, God · Saiyan Base, SS, SSG2, SSG3, SSFullPow, SS2, SS3, Oozaru, Golden, SSGod, SSB, SSGodR, LSS, LSS2, SS4, SSBE · Namekiano Base, Full, Giant, God · Arcosiano Form0, Form1, Form2, Form3, Base, Form5, Ultimate, God · Majin Base, Evil, Full, Pure, God.

### Comandos y archivos \[OF\]

- `/jrmcformmastery [jugador] (add|set) (forma o id) (cantidad)`, por ejemplo `/jrmcformmastery @p set ss2 55`; `/jrmcformmasterycheck @p`.
- `config/jingames/dbc/forms/form_mastery_main.cfg` (activado o no) y `config/jingames/dbc/races/<raza>/form_mastery.cfg` (una sección por forma).
- `/jrmcrei` permite resetear el personaje conservando o no las maestrías.

## Skills

Las skills se aprenden de maestros pagando TP y Mind, y casi todas suben hasta nivel 10. Desde la build 1.128 el coste base de casi todas es configurable y el Mind mínimo puede ser 0 \[OF\].

### Lista \[COM salvo indicación\]

| Skill (ID) | TP | Mind | Efecto | Maestros \[OF\] |
| --- | --- | --- | --- | --- |
| Jump (JP) | 40 | 5 | Más altura de salto y menos daño por caída por nivel | Babidi, Frieza, Kami, Korin, Roshi |
| Dash (DS) | — | — | Dash en el suelo (Ctrl + A/S/D, no hacia delante) | Babidi, Frieza, Roshi, Vegeta |
| Fly (FL) | 60 | 10 | Volar con F (gasta Ki); “swoop” con Ctrl + dirección; más nivel = más rápido | Babidi, Frieza, Gohan, King Kai, Kami, Korin, Piccolo, Roshi, Trunks |
| Endurance (EN) | 150 | 10 | −3% de daño recibido por nivel, multiplicativo con las formas (la wiki antigua decía máximo 30%) | Babidi, Cell, Frieza, Kami, Vegeta |
| Potential Unlock (OC) | 400 | 10 | Sube el Release máximo (50% → 100% en nivel 10) y permite sobrecargar Ki | Babidi, Cell, Frieza, Goku, Guru, Kami, Piccolo, Vegeta |
| Ki Sense (KS) | 300 | 10 | Ver vida y Ki de entidades; lock-on con Z; modos con F4 | Cell, Gohan, Goku, Kami, Piccolo |
| Meditation (MD) | — | — | Pasiva: más regeneración; activa: recarga Ki con C | Kami, Piccolo |
| Ki Protection (KP) | 700 | 10 | Menos daño a cambio de Ki (el coste escala con el daño); la defensa escala con SPI | Cell, Gohan, Piccolo |
| Ki Fist (KF) | 1.000 | 10 | Más daño melee a cambio de Ki | Goku, Trunks, Gohan, Piccolo, Cell |
| Ki Infuse (KI) | 200 | 10 | Más daño de proyectiles normales (no Ki) a cambio de Ki | Goku, Trunks |
| Ki Boost (KB) | 800 | 10 | +1% de Ki máximo por nivel | King Kai |
| Defense Penetration (DF) | 600 | 10 | Ignora 1% de la defensa enemiga por nivel; si aun así tu daño no supera su defensa, haces hasta 10% de esa defensa como daño | Goku, Trunks |
| Fusion (FZ) | 300 | 10 | Fusión con otro jugador de la misma raza; +10% de éxito por nivel (se promedia entre ambos) | Goku |
| Kaioken (KK) | — | — | Forma apilable que drena vida | King Kai |
| God Form (GF) | 20.000 | 10 | Formas divinas; Saiyans hasta nivel 3 | Jin, Whis |
| Old Kai’s Ritual / Mystic (OK) | 1.500 | 10 | Mystic; requiere alineamiento 100%; pierde niveles con el tiempo | King Kai |
| Ultra Instinct (UI) | 50.000 | 10 | Ver Transformaciones | Jin, Whis |
| God of Destruction (GD) | 50.000 | 10 | Ver Transformaciones | Whis |
| Instant Transmission (IT) | — | — | Teletransporte corto y largo | Goku, Cell |

Otras fuentes dan costes distintos para Ultra Instinct (500.000 TP; mejoras de 200.000 y 300.000 TP con 15 y 20 Mind). Toma la tabla como orden de magnitud: los servidores los cambian.

### Coste por nivel \[COM, config\]

El coste configurado se suma en cada nivel: con Fly = 5 cuesta 5, 10, 15… Valores por defecto antiguos: Dash 1, Endurance 15, Fly 1, Fusion 20, God Form 1, Jump 1, Kaioken 1, Ki Sense 1, Meditation 1, Potential Unlock 1. La Racial Skill tiene una lista de costes por nivel aparte.

### Detalles por skill

- **Mind como límite:** cada skill cuesta Mind y el Mind sale del atributo MND, así que MND decide cuántas skills puedes tener \[OF, COM\].
- **Kaioken:** nivel 10 da 30% de probabilidad de no recibir daño al entrar o subir de nivel; con Endurance, 50% de probabilidad de reducir el daño de Kaioken, hasta −50% con Endurance 10 \[OF, changelog 2017\]. Deja el estado “Strain” (hay comando para quitarlo) \[COM\].
- **Ki Fist + Ki Infuse:** juntos desbloquean Ki Blade y Ki Scythe. Su daño sube con el nivel de ambas y su coste baja un poco; la guadaña pega y cuesta 3 veces más; con Release 0% se apagan; el color sigue al aura \[OF\].
- **Configs de skills de Ki:** daño y coste por nivel de Ki Fist, Ki Infuse, Ki Defense y Ki Blade \[OF\].
- **Swoop:** bloqueado con menos de 10% − (nivel Fly × 0,5%) de Ki \[OF\].

### Instant Transmission \[OF, build 1.128\]

- **Corta distancia:** mirar a una entidad + Alt + clic derecho. Apareces sobre, detrás o delante del objetivo (los dos últimos pueden meterte en paredes; se recomienda desactivarlos en servidores).
- **Larga distancia:** mantener Alt unos segundos y elegir a un miembro del grupo, incluso en otra dimensión, salvo el Otro Mundo. No se puede fusionado.
- **Acompañantes:** solo, con el grupo o con todos los jugadores cercanos, con límite configurable.
- Sujetar un objeto o golpear cancela el modo. Tiene sonido propio (el de Goku Black con Divine) y animación propia.
- **Configs** (`config/jingames/dbc/skills/instant_transmission.cfg`): qué modos están activos, nivel de skill que desbloquea cada modo, alcance y si atraviesa bloques, cooldowns, Ki Sense requerido, coste de Ki (plano y porcentual) y por acompañante, lista negra de dimensiones, aviso al servidor.

### Habilidades raciales Majin \[OF, build 1.128\]

- **Super Regeneration:** al cargar Ki recuperas vida, a cambio de Ki y stamina. Configs: vida ganada, coste de Ki y stamina, y cuánto suben por nivel de Racial Skill.
- **Absorption:** mantén G con la habilidad activada. Lanzas un proyectil que busca un objetivo y, si eres más fuerte (comparando ataque y vida, sin contar formas), lo mata y vuelve.
  - Sube tu nivel de absorción según el poder del objetivo y da atributos extra a todas tus formas.
  - Cuesta vida, tiene cooldown y se pierde al morir.
  - Absorber un Namekiano o un Arcosiano cambia un poco tu aspecto y muestra la ropa del absorbido.
  - Configs: multiplicador por nivel, nivel máximo, ganancia mínima, sumar o reemplazar niveles, lista negra de entidades, velocidad del proyectil.
- **Formas Majin:** no gastan Ki por defecto. Full Power es la forma no divina más fuerte; Pure tiene la mejor regeneración y vuelve rosa el cuerpo y el aura.

## Combate cuerpo a cuerpo

El golpe de DBC reemplaza el daño vanilla por Melee Damage (STR × 2,5 × modificadores × forma × Release) y gasta Ki y stamina. La defensa se resta en capas: Defense o Passive, Endurance, Ki Protection y la forma.

### Orden de reducción de daño (reconstrucción) \[COM, PROP\]

1. Daño entrante del atacante (melee o Ki).
2. Menos Defense si bloqueas, o Passive (20% de Defense) si no. Defense Penetration del atacante ignora 1% por nivel.
3. × (1 − 3% × nivel de Endurance).
4. Ki Protection activa: absorbe parte a cambio de Ki.
5. ÷ multiplicador de la forma del defensor.
6. Ultra Instinct: probabilidad de esquivar del todo. God of Destruction en Turbo: ignora ataques por debajo del 80% de tu poder.

El orden exacto entre 2 y 5 no está documentado; la wiki confirma que Endurance y la reducción de las formas se multiplican entre sí.

### Acciones

| Acción | Detalle |
| --- | --- |
| Golpe | Gasta Ki (más con más STR) y stamina; da TP con probabilidad; sin stamina = daño vanilla \[COM\] |
| Bloqueo | Usa Defense completa; gasta stamina, y es el único coste que sí cambian las formas \[COM\] |
| Dash | Ctrl + A/S/D en el suelo, con la skill Dash \[OF\] |
| Swoop | Dash en vuelo con Ctrl + dirección; requiere un mínimo de Ki \[OF\] |
| Lock-on | Z, requiere Ki Sense \[OF\] |
| Turbo | R: más velocidad y salto, sube el Release más rápido, gasta más Ki y hambre \[COM\] |

### PvP y estados

- **Friendly Fist:** en lugar de matar deja KO; el KO fuerza la vista en tercera persona hasta recuperarse \[OF\].
- **Safe Zones:** zonas sin combate, con lista negra y blanca de entidades (la blanca incluye la entidad de Instant Transmission) \[OF\].
- **Comando** `jrmcpvpcheck` para revisar el PvP \[OF\].
- **Sin regeneración 30 s** tras recibir daño de un ser vivo \[OF\].
- **Contra mobs vanilla**, en versiones antiguas, ningún stat de DBC aumentaba la defensa salvo Endurance contra mobs de DBC \[COM\].
- **Knockback:** DBC no lo escala con el poder; el add-on de la comunidad “ninjinkb” existe precisamente para eso \[COM\].

## Muerte, Otro Mundo y alineamiento

Al morir en Survival no reapareces: vas al Otro Mundo y vuelves hablando con Enma, por un deseo o por un comando. El alineamiento (0–100) decide dónde apareces, el color de tu aura y qué maestros y formas tienes disponibles.

### Sistema de muerte \[OF, COM\]

1. Mueres en Survival → apareces en el Otro Mundo, en el Check-In Station de Enma (X 0, Y 0), en un punto distinto según tu alineamiento (configurable; el punto se fija al entrar aunque luego cambie tu alineamiento) \[OF\].
2. Enma revive (vuelves al Overworld) o reencarna (empiezas de nuevo). El botón de revivir tiene cooldown \[COM\].
3. Al revivir apareces en una posición por alineamiento; por defecto Good (75, 220, 55) y Evil (96, 230, 7) \[COM, config\].
4. Penalización: depende de la dificultad (Normal, Hard, Insane) \[OF\]. “Reincarnation Penalty” decide qué porcentaje de lo desarrollado conservas al reencarnar (100 = todo) \[COM, config\].

Otras configs \[COM, OF\]:

- **Death System Off:** desactiva todo el sistema (reapareces normal).
- **Switch Inventory:** un inventario para el Otro Mundo y otro para el mundo de los vivos; ninguno se pierde al morir.
- **Revive Dimension id y rotaciones** por alineamiento (DBC 1.4.71).
- **Comandos:** `/dbcrevive <jugador>` pone a 0 el cooldown de revivir; un comando de supervivencia acepta peticiones de revivir y de teletransporte.
- **Client setting** para mostrar u ocultar el escritorio de Enma.

No se puede usar Instant Transmission de larga distancia hacia el Otro Mundo \[OF\].

### Alineamiento (karma)

Es un valor de 0 a 100: 100 bueno, 50 neutral, 0 malvado. Se cambia con `/jrmca set alignment <valor>` \[COM\].

| Acción | Efecto |
| --- | --- |
| Final de casi cada misión: “¿por qué luchas?” | Por los demás = sube; por ti = neutral; por el mal = baja \[COM\] |
| Matar Namekianos guerreros | Baja \[OF, COM\] |
| Atacar jugadores | Baja \[COM\] |
| Matar a un jugador bueno | Te vuelve más malvado \[OF\] |
| Matar a un jugador malvado | Te vuelve más bueno, salvo que ya seas malvado \[OF\] |
| Aceptar el control de Babidi | Alineamiento a 0 \[COM\] |

`/jrmckills <jugador>` muestra contadores de muertes y alineamiento \[OF\].

**Qué cambia según el alineamiento:**

- **Color del aura y del Ki:** azul si bueno, violeta si neutral, rojo si malvado (si no eliges uno propio) \[COM\].
- **Formas:** Mystic exige 100%; el estado Majin exige ser malvado; God of Destruction tiene un máximo de alineamiento; Divine/Rosé se asocia al mal \[COM, OF\].
- **Lugar de aparición** en el Otro Mundo y al revivir \[OF\].
- **Revivir por deseo:** “revivir a todos los neutrales” usa el alineamiento \[COM\].

## Dimensiones y estructuras

DBC añade 5 dimensiones y varias estructuras en el Overworld; las más importantes están en coordenadas fijas para que todos los jugadores las encuentren \[OF\].

### Dimensiones \[OF\]

| Dimensión | Contenido | Acceso |
| --- | --- | --- |
| Overworld | Kami’s Lookout siempre en X 0, Z 0 (Kami, Mr. Popo, Korin, Piccolo, Vegeta, Trunks, Whis y la puerta de la Habitación del Tiempo). Kame House en el océano; casa de Goku, Cell Arena y nave de Babidi en llanuras | — |
| Planeta Namek | Biomas y agua verdes, árboles Ajisa, casas namekianas, casa de Guru, nave de Frieza, dinosaurios y ranas namekianas | Spacepod o Instant Transmission |
| Planeta Vegeta | Páramo con agua roja y guerreros Saiyan (dos tipos, con tasas de aparición configurables) | Spacepod o Instant Transmission |
| Otro Mundo | Palacio de Enma en X 0, Y 0; Camino de la Serpiente hasta el planeta de King Kai (X 100, Z −3700) con gravedad; Infierno con ogros que aparecen al azar | Morir |
| Habitación del Tiempo | Gravedad alta y entrenamiento por experiencia; Mr. Popo | Puerta del Lookout |
| Null Realm | Vacío con 2 arenas | Whis o Instant Transmission |

### Estructuras y utilidades \[OF, COM\]

- `/dbc loc` (o `/dbclocations`) dice dónde están las estructuras generadas.
- **Gravedad** por zona: planeta de King Kai, Habitación del Tiempo y Gravity Device; multiplica las pesas.
- **Healing Water y Medical Pod:** curan cada cierto número de ticks (configurable desde DBC 1.4.71).
- **Bloques de mundo:** tierra y piedra de bioma rocoso (tasa configurable), losas de madera Maple, trono de Guru en el que te puedes sentar.
- **Transporte:** Spacepod (clic central, número de destino y “start”) y Nube Voladora; ambas con multiplicador de velocidad configurable.
- **Mapas de saga:** el Torneo del Poder y estructuras como la casa de Guru llegaron en 2020–2021 con NPCs del Universo 6 y 11 \[OF\].

Cómo genera DBC el terreno de cada dimensión (alturas, ruido, biomas) no está documentado; la especificación propone hacerlo con datapacks.

## NPCs, maestros, enemigos y sagas

Los maestros enseñan skills y técnicas a cambio de TP y Mind, y las sagas son archivos JSON por mundo que cualquiera puede editar. Ese motor de misiones es lo más fácil de replicar tal cual.

### Maestros \[OF, COM\]

| Maestro | Dónde | Enseña / hace |
| --- | --- | --- |
| Kami | Lookout | Jump, Fly, Potential Unlock, Endurance, Meditation, Ki Sense; corta y regenera colas; resetea el personaje; pesas |
| Mr. Popo | Lookout / Habitación del Tiempo | Acceso a la Habitación del Tiempo |
| Korin | Torre de Korin | Jump, Fly; Nube Voladora y Senzus |
| Piccolo | Lookout | Ki Blast, Destructo Disk, Makankosappo, Masenko; Fly, Potential Unlock, Meditation, Ki Sense, Ki Protection, Ki Fist; pesas |
| Vegeta | Lookout | Ki Blast, Big Bang, Final Flash, Galick Gun, Power Ball; Dash, Potential Unlock, Endurance |
| Trunks | Lookout | Fly, Ki Fist, Ki Infuse, Defense Penetration |
| Whis | Lookout | God Form, Ultra Instinct, God of Destruction; lleva al Null Realm; pesas |
| Roshi | Kame House | Jump, Dash, Fly; Kamehameha; pesas |
| Goku | Su casa | Fusion, Potential Unlock, Ki Sense, Ki Fist, Ki Infuse, Defense Penetration, Instant Transmission |
| Gohan | — | Fly, Ki Sense, Ki Protection, Ki Fist |
| Cell | Cell Arena | Endurance, Potential Unlock, Ki Sense, Ki Protection, Ki Fist, Instant Transmission |
| Frieza | Nave en Namek | Jump, Dash, Fly, Endurance, Potential Unlock |
| Babidi | Su nave | Jump, Dash, Fly, Endurance, Potential Unlock; estado Majin |
| Guru | Namek | Potential Unlock |
| King Kai | Otro Mundo | Kaioken, Ki Boost, Fly, Old Kai’s Ritual (Mystic); pesas |
| Enma | Otro Mundo | Revivir y reencarnar |
| Jin | Otro Mundo | God Form, Ultra Instinct |

Cada maestro tiene un botón “pedir pesas” en los que las dan \[OF\]. Whis y Guru tardaron en aparecer de forma natural; al principio se invocaban con `/summon jinryuudragonblockc.whismaster` \[OF\].

### Enemigos \[OF, COM\]

- **Aleatorios:** Saibaimen, guerreros Saiyan en Planeta Vegeta, Namekianos guerreros, ogros del Infierno, dinosaurios y ranas de Namek.
- **De saga:** Raditz, Nappa, Vegeta, Fuerza Ginyu, Frieza, androides, Cell, Buu, villanos de Super y, desde 2020, luchadores del Torneo del Poder (Jiren, Toppo, Dyspo, Caulifla, Kale, Kefla, el Trío del Peligro…).
- **Agresividad:** desde DBC 1.4.81 son agresivos por defecto, salvo la Fuerza Ginyu \[OF\].
- **Shadow Dummy:** un clon tuyo para entrenar.
- No hay tablas públicas fiables de vida y ataque: se definen en el JSON de cada misión (parámetros H y A, abajo).

### Motor de misiones (sagas) \[OF\]

Cada historia es un archivo JSON en `saves/<mundo>/data/missions/` (por ejemplo `mainDBC.json`); se escanean al arrancar el mundo y un error de sintaxis puede tumbar el juego. Se abren con L; `/jrmcm main 0` reinicia la principal.

**Cabecera de la historia:** `Name`, `Description`, `Authors`, `Version` (si cambia, los jugadores empiezan de cero), `Mods` (DBC o NC), `Settings` (repetición en minutos o −1 = no repetible; `unlock` = otras historias requeridas, como `mainDBC` o `mainDBC:5`), `Missions`.

**Cada misión:** `id` (empieza en 0), `translated`, `props` (variantes por raza o clase), `align`, `title`, `subtitle`, `description`, `objectives`, `rewards`. Con `props`, cada campo es una lista con una entrada por variante; la raza tiene prioridad sobre la clase.

**Objetivos** (parámetros separados por `;`, la primera letra indica el tipo):

| Tipo | Qué pide |
| --- | --- |
| kill | Matar a un NPC |
| killsame | Matar a varios NPC iguales generados a la vez |
| item | Conseguir N de un ítem (N = id, M = cantidad, `::meta`) |
| talk | Hablar con un NPC (N entidad, G texto, B botón) |
| state | Estar en una forma (p. ej. `state;NSS`) |
| lvl | Llegar a un nivel |
| dim / biome | Ir una vez a una dimensión o bioma |
| dim2 / biome2 | Estar en ella para que aparezcan los enemigos |

Parámetros de enemigos: N nombre de entidad, M cantidad, H vida, A ataque, P protección (`Pno` vale cualquier entidad del tipo; `Pspwn` no genera ninguna), S y D mensajes de aparición y muerte, O y U sonidos, T transformaciones del NPC (tick y multiplicador de vida y ataque). El primer elemento del objetivo es `start`, `next`, `skip` o `restart`.

**Recompensas** (`recompensas;texto del botón;id siguiente`; varias con `||`; parámetros con `!`):

- `tp!fix!100` (fijo), `tp!align!N` (según coincidan tu alineamiento y el de la misión: nada, mitad o todo), `tp!lvl!N` (nivel × N), `tp!lvlalign!N`.
- `align!-10` (cambia el alineamiento en puntos porcentuales).
- `item!jinryuudragonblockc:SpacePod01Item,1`.
- `command!...` (requiere command blocks activados).
- `nothing`.
- Con `props` = `randrew`, la recompensa (y la misión siguiente) se elige al azar.

Ejemplo oficial: `tp!fix!100||align!-10;jinryuujrmcore.missionSys.Evil;11` da 100 TP, baja 10 de alineamiento y lleva a la misión 11.

En la build 1.128 se subieron las recompensas de TP de las misiones por defecto; para usar las nuevas hay que borrar los archivos viejos \[OF\].

## Esferas del Dragón y deseos

Las esferas (“Dragon Blocks”) aparecen cerca de cada jugador una vez por día de juego; con las 7 colocadas en forma de H invocas a Shenron (Tierra, 1 deseo) o a Porunga (Namek, 3 deseos).

### Ciclo \[OF, COM\]

1. **Aparición:** probabilidad diaria (configurable) de que aparezcan a menos de 64 bloques de un jugador, en la Tierra y en Namek. Brillan un poco.
2. **Búsqueda:** el Dragon Radar las detecta.
3. **Invocación:** colocar las 7 del mismo tipo en forma de H en el suelo y hacer clic derecho en la del centro.
4. **Deseo:** clic derecho en el dragón y elegir. Shenron concede 1 deseo y Porunga 3 (en versiones antiguas Porunga daba el triple del mismo deseo).
5. **Piedra:** después quedan blancas, sin brillo e inutilizables durante un año de juego.

### Deseos disponibles \[COM\]

| # | Deseo |
| --- | --- |
| 1 | 5 Warenai Crystals |
| 2 | 3 Senzu |
| 3 | 1 fragmento de Katchin |
| 4 | Revivir a un jugador |
| 5 | Revivir a un jugador y traerlo |
| 6 | Revivir a todos los jugadores neutrales (mismo karma) |
| 7 | Cambiar el color del aura |
| 8 | 5 Janemba Essence |
| 9 | Small Club |
| 10 | Power Pole (lanza maligna si es Porunga) |
| 11–12 | Con Years C: volverse joven o viejo |

Otras listas mencionan también 3 diamantes, 3 Tech Chips de tier 1 y cambiar el color de la forma Golden arcosiana; la lista cambió entre versiones. La versión más antigua tenía solo 5 deseos \[OF\].

Ninguna fuente muestra un archivo de deseos personalizable en DBC; en DragonMine Z sí lo es (“Custom Wishes”) \[DMZ\].

## Ítems y bloques

Los ítems de DBC giran en torno a curarse, entrenar, leer el poder ajeno y moverse entre planetas; casi ninguno da poder directo. Las recetas están en la página oficial “Recipes” \[OF\].

| Ítem | Función | Fuente |
| --- | --- | --- |
| Senzu | Restaura vida (y Ki) | Korin; deseo \[OF, COM\] |
| Scouter | F4 cambia de modo: localización (mobs y NPCs; variante “MP” para jugadores) y lectura de BP. Tier 1 se rompe por encima de 100.000 BP, tier 2 por encima de 1.000.000, tier 3 nunca. Muestra grietas al dañarse | \[OF\] |
| Pesas (camiseta, brazos, piernas) | En un inventario extra (Ctrl + E). Suben la probabilidad de TP y bajan daño y defensa; el peso se multiplica por la gravedad | Maestros con “pedir pesas” \[OF\] |
| Gravity Device | Crea gravedad (10G al principio): reduce STR y DEX y facilita los TP | \[OF\] |
| Medical Pod | Cura con el tiempo; las puertas se mueven en grupo (hasta 11 desde 1.4.81) | \[OF\] |
| Healing Water | Cura cada N ticks (configurable) | \[OF\] |
| Ropa y vanity items | Gis y outfits en ranuras extra; se tiñen solo en esas ranuras | \[OF\] |
| Spacepod | Viajar entre Tierra, Namek y Vegeta: clic central, número de destino, “start” | \[OF\] |
| Nube Voladora | Montura voladora; velocidad configurable | Korin \[OF\] |
| Power Pole, Small Club, espadas | Armas; la Power Pole es un deseo | \[COM\] |
| Katchin, Warenai Crystals, Janemba Essence | Materiales de crafteo (y deseos) | \[COM\] |
| Dragon Radar | Detecta las esferas | \[OF\] |
| Bloques de mundo | Árboles Ajisa, losas Maple, tierra y piedra rocosa, trono de Guru, bloques de las estructuras | \[OF\] |

No hay “cápsulas” en DBC: la comunidad usa mods aparte para eso.

## Especificación propuesta para tu remake

La recomendación es copiar la idea más fuerte de DBC, que todo el balance viva en archivos editables, pero con datapacks en vez de `.cfg` sueltos. Así cada servidor ajusta razas, formas, skills, sagas y deseos sin recompilar \[PROP\].

### Registros de datos (datapack)

| Registro | Ruta | Sustituye a |
| --- | --- | --- |
| Razas | `data/<mod>/races/*.json` | Configs de raza y atributos iniciales |
| Clases | `data/<mod>/classes/*.json` | Tabla de modificadores |
| Formas | `data/<mod>/forms/*.json` | “Form Damage Multiplier”, drenaje, regeneración y form\_mastery.cfg |
| Skills | `data/<mod>/skills/*.json` | Costes TP/Mind y efectos por nivel |
| Técnicas | `data/<mod>/techniques/*.json` | Ataques prefijados |
| Maestros | `data/<mod>/masters/*.json` | Qué enseña cada NPC |
| Deseos | `data/<mod>/wishes/*.json` | Lista fija de deseos |
| Sagas | `data/<mod>/sagas/*.json` (o la carpeta del mundo, como DBC) | `mainDBC.json` |

### Ejemplos de esquema

Forma (equivalente a la config de SSJ, con maestría incluida):

```json
{
  "id": "mymod:super_form_1",
  "race": "mymod:saiyan",
  "requires": { "racial_skill": 1, "masteries": { "mymod:base": 5 } },
  "multipliers": { "str": 1.5, "dex": 1.5, "wil": 1.5 },
  "flat_bonus": 15,
  "damage_taken_divisor": 1.5,
  "drain": { "resource": "ki", "per_second": 10, "weights": { "str": 0.40, "wil": 0.35, "dex": 0.25 } },
  "ki_regen_multiplier": -0.05,
  "stacks_with": ["mymod:kaioken"],
  "mastery": {
    "max": 50, "instant_unlock": 10, "divider_plus": 100,
    "gain": { "update": 0.1, "attack": 0.05, "hurt": 0.05, "fire_ki": 0.05 },
    "power": { "flat": 0, "per_level": 0.01, "max": 0.5 },
    "cost": { "flat": 1.0, "per_level": -0.01, "min": 0.5 }
  },
  "visual": { "hair": "spiky_gold", "aura": "#FFD54A", "sound": "mymod:transform_1" }
}
```

Skill:

```json
{
  "id": "mymod:endurance",
  "max_level": 10,
  "cost": { "tp_base": 150, "tp_per_level": 15, "mind": 10 },
  "effects": [{ "type": "damage_reduction", "per_level": 0.03 }]
}
```

Misión (mismos conceptos que DBC, pero tipado en vez de cadenas con `;` y `!`):

```json
{
  "id": 3,
  "variants": [{ "when": { "race": "saiyan" }, "objectives": [{ "type": "state", "form": "mymod:super_form_1" }] }],
  "objectives": [{ "type": "kill_same", "entity": "mymod:saibaman", "count": 3, "health": 200, "attack": 15 }],
  "rewards": [{ "button": "Por los demás", "give": [{ "tp": { "mode": "fixed", "amount": 100 } }, { "alignment": 10 }], "next": 4 }]
}
```

### Pipeline de cálculo de un atributo efectivo

```latex
Atr_{ef} = \max\big(Atr \cdot F_{forma} \cdot F_{kaioken} \cdot F_{estado} \cdot (M_{maestría} + A_{absorción}),\ Atr + bono\big) \cdot (1 + R_{racial})
```

`F_kaioken` no se multiplica con `F_estado` (Majin), como en DBC. Después se aplica el ratio por punto, el modificador de clase y el Release (documento de Release y Ki).

### Orden de implementación sugerido

1. **Base:** datos del jugador, sincronización, atributos, TP, Release, Ki, vida y stamina (documento anterior).
2. **Progresión:** compra de atributos con coste creciente, nivel, minijuegos simples, pesas y gravedad.
3. **Razas y clases** desde JSON, con pantalla de creación.
4. **Skills** desde JSON: Fly, Dash, Jump, Endurance, Potential Unlock, Ki Sense con lock-on.
5. **Formas** desde JSON con el pipeline de arriba, Kaioken y la máquina de transformación (G/H/doble G).
6. **Form Mastery** sobre las formas.
7. **Ataques de Ki** prefijados y personalizados.
8. **Mundo:** Lookout, dimensiones por datapack, Spacepod, maestros como NPC con diálogo.
9. **Muerte y Otro Mundo**, alineamiento, Enma.
10. **Sagas** con el motor de misiones y enemigos con vida y ataque por misión.
11. **Esferas y deseos**, Senzu, scouter, Medical Pod.
12. **Formas especiales:** God, UI con Heat y Pain, Mystic, God of Destruction, Power Points, Majin con absorción, fusión, Instant Transmission.

Cada paso deja algo jugable; los pasos 1, 5 y 7 son los que definen si “se siente” como DBC.

## Lo que sigue sin confirmar

Estos datos no aparecen en ninguna fuente pública; en tu remake ponlos como config y ajústalos jugando.

| Dato | Estado |
| --- | --- |
| Fórmula completa del coste de atributos (UC) | Solo se conocen sus parámetros (Start Minus 140, mínimo, ×0,75) |
| Multiplicadores “reales” de cada forma | Tres juegos distintos según versión; usa la config moderna como base |
| Drenaje exacto de cada forma por segundo | Solo fórmula cualitativa (pesos 40/35/25) y valores viejos |
| Costes de Dash, Meditation, Kaioken, Instant Transmission | No documentados |
| Atributos iniciales de STR y WIL del Arcosiano | Suman 20 |
| Probabilidad diaria de las esferas y tiempo exacto en piedra | “Configurable” y “un año” |
| Vida y ataque de cada enemigo de saga | Viven en `mainDBC.json`; puedes leer los de tu propia instalación |
| Generación de terreno de cada dimensión | No documentada |
| Penalización exacta por muerte según dificultad | No documentada |

La forma más rápida de cerrar la mitad de esta tabla es abrir los archivos de config y de misiones de tu propia instalación de DBC (`config/jinryuujrmcore.cfg`, `config/jingames/dbc/`, `saves/<mundo>/data/missions/`): leerlos es legítimo y traen los valores por defecto. Si me los pasas, completo estas tablas.

## Fuentes

- [Build 1.128: Majin, God of Destruction, Instant Transmission, Form Mastery — JinGames](https://main.jingames.net/mod-update-majin-race-god-of-destruction-instant-transmission-form-mastery-22w50/)
- [Mission System — JinGames](https://main.jingames.net/minecraft-mods/jinryuus-mods-core-mod/mission-system/)
- [How to play Dragon Block C Guide — JinGames](https://main.jingames.net/how-to-play-dragon-block-c-guide/)
- [Other Features (esferas, scouter) — wiki oficial](https://main.jingames.net/wiki/dragon-block-c/other-features/)
- [Stat Sheet — wiki oficial](https://main.jingames.net/wiki/dragon-block-c/player/stat-sheet/)
- [Races — wiki oficial](https://main.jingames.net/wiki/dragon-block-c/player/races/)
- [Ultra Instinct Update 18w32](https://main.jingames.net/ultra-instinct-update-18w32a/) y [fix 18w32b](https://main.jingames.net/ultra-instinct-fix-18w32b/)
- [Tournament of Power update 20w52](https://main.jingames.net/update-dbc-tournament-of-power-energy-attack-changes-20w52/)
- [Weight system update (2016)](https://main.jingames.net/dbc-weight-system-update-and-status-info-week-26/)
- [Update 22w26](https://main.jingames.net/mod-update-quality-of-life-and-bug-fix-release-22w26/)
- [Kaioken is back (alineamiento por muertes PvP)](https://main.jingames.net/update-released-kaioken-is-back/)
- [Changelogs varios — página principal de JinGames](https://main.jingames.net/)
- [Wiki Fandom: Attributes and Statistics](https://dragonblockc.fandom.com/wiki/Attributes_and_Statistics)
- [Wiki Fandom: Transformations](https://dragonblockc.fandom.com/wiki/Transformations)
- [Wiki Fandom: Saiyan](https://dragonblockc.fandom.com/wiki/Saiyan)
- [Wiki Fandom: Human (Race)](<https://dragonblockc.fandom.com/wiki/Human_(Race)>)
- [Wiki Fandom: Namekian (Race)](<https://dragonblockc.fandom.com/wiki/Namekian_(Race)>)
- [Wiki Fandom: Arcosian (Race)](<https://dragonblockc.fandom.com/wiki/Arcosian_(Race)>)
- [Wiki Fandom: Ultra Instinct](https://dragonblockc.fandom.com/wiki/Ultra_Instinct)
- [Wiki Fandom: Skills](https://dragonblockc.fandom.com/wiki/Skills)
- [Wiki Fandom: Kaioken](https://dragonblockc.fandom.com/wiki/Kaioken)
- [Wiki Fandom: JRMC Config](https://dragonblockc.fandom.com/wiki/JRMC_Config)
- [Wiki Fandom: config del sistema de muerte](https://dragonblockc.fandom.com/wiki/Dragon_Block_C_Congif)
- [Wiki Fandom: Karma](https://dragonblockc.fandom.com/wiki/Karma)
- [Wiki Fandom: Dragon Blocks](https://dragonblockc.fandom.com/wiki/Dragon_Blocks)
- [Wiki Fandom: Kami’s lookout](https://dragonblockc.fandom.com/wiki/Kami%27s_lookout)
- [Wiki Fandom: Commands](https://dragonblockc.fandom.com/wiki/Commands)
- [Wiki Fandom: Experience](https://dragonblockc.fandom.com/wiki/Experience)
- [jindbc.fandom.com: Getting Started](https://jindbc.fandom.com/wiki/Getting_Started)
- [Foro: How change ssj multiplier](https://main.jingames.net/forums/topic/how-change-ssj-multipiler/)
- [Foro: The Multiplier of Forms](https://main.jingames.net/forums/topic/the-multiplier-of-forms/)
- [Foro: How do I go Ultra Instinct using commands](https://main.jingames.net/forums/topic/how-do-i-go-ultra-instinct-using-commands/)
- [Foro: God of Destruction form in config file](https://main.jingames.net/forums/topic/god-of-destruction-form-in-config-file/)
- [Foro: How do I do…? (pesas y gravedad)](https://main.jingames.net/forums/topic/how-do-i-do-dragon-block-c/)
- [Foro: Good vs Evil](https://main.jingames.net/forums/topic/good-vs-evil-5/)
- [Dragon Block C Lite — changelog (Kaioken y Endurance)](https://www.technicpack.net/modpack/dragon-block-c-lite.941977/changelog)
- [DragonMine Z — Transformations and Mastery](https://github.com/DragonMineZ/dragonminez/wiki/Transformations-and-Mastery)
