# DBC — Release %, Ki, barras y stats: especificación detallada

Oct 4, 2026 · @Fernando

En DBC el Release % es un multiplicador de casi todo el combate (daño melee, defensa, Ki Power, BP) y a la vez compite con la regeneración de Ki: cuanto más alto el Release, menos regeneras. Esa tensión “subir poder vs. recuperar Ki” es el núcleo de cómo se siente el juego, y es lo primero que conviene replicar.

Los valores por punto de atributo sí están documentados por la comunidad. Las curvas exactas de velocidad de carga y de regeneración no son públicas; para esas, la sección de especificación propone fórmulas propias con valores configurables.

**Leyenda de confianza** (se usa en todo el documento):

| Marca | Significa |
| --- | --- |
| **\[OF\]** | Guía, wiki, changelog o FAQ oficial de JinGames |
| **\[COM\]** | Wiki Fandom, foros o mediciones de jugadores, con configs por defecto |
| **\[DMZ\]** | Diseño de DragonMine Z (código GPL, se puede estudiar) |
| **\[PROP\]** | Propuesta mía para tu remake, a ajustar con playtesting |

Nada aquí sale de código descompilado de DBC: la licencia de JinGames lo prohíbe.

## Release % (subir y bajar poder)

El Release % es cuánto de tu poder estás usando ahora mismo; se ve sobre la barra de Ki, arriba a la izquierda. A 0% eres casi un jugador vanilla; a tu máximo, todas tus estadísticas de combate están a pleno.

### Controles \[OF\]

| Tecla | Efecto |
| --- | --- |
| C (mantener) | Sube el Release (“Ki Charge”). Muestra el aura del color elegido |
| Ctrl + C | Baja el Release (“Ki Discharge”) |
| H | Resetea el Release y, si estás transformado, te destransforma |
| R | Turbo: acelera la subida del Release, da velocidad y salto, y gasta más Ki y hambre |

### Límites

- **Mínimo:** 0%. Por debajo de 0% no se pueden usar poderes; con menos de 5% no se ganan TP ni se aplican varias skills \[OF\].
- **Máximo base:** 50% \[OF, COM\].
- **Potential Unlock:** sube el máximo hasta 100% en nivel 10 \[OF\]. Interpolado lineal serían +5% por nivel; la progresión exacta no está documentada \[PROP\].
- **Sobrecarga:** hay una config que permite empezar en 100% y llegar a 200% con Potential Unlock, según jugadores de servidores \[COM\]. En ataques de Ki, Potential Unlock deja cargar por encima de la carga normal \[OF\].
- **Desde JRMCore 1.3.44** el Release se resetea al resetear el personaje \[OF\].

### Cómo sube

- Sube **por pasos visibles de 5%**, no de forma continua; jugadores pidieron que fuera suave (de 0,1 en 0,1) \[COM\].
- **Se siente más lento a partir del 50%**: en el foro de 2016 varios jugadores se quejan de lo que tarda llegar a 100% incluso con Potential Unlock al máximo \[COM\]. No hay cifra publicada de % por segundo.
- **Turbo (R)** acelera la subida \[COM\].
- **Sin Ki no sube**: al quedarte sin Ki el Release cae a 0% \[COM\]. Desde JRMCore 1.3.39, un ataque que gasta el 100% de tu energía también te deja en 0% \[OF\].

### Qué escala con el Release \[COM\]

| Estadística | ¿Escala con el Release? |
| --- | --- |
| Melee Damage | Sí |
| Defense y Passive | Sí |
| Ki Power (daño, coste y destrucción del Ki) | Sí |
| TP por golpe | Sí (fórmula en la sección de atributos) |
| Battle Power mostrado | Sí |
| Body (vida máxima) | No |
| Max Ki | No |
| Action Time (stamina) | No |

### Interacciones

- **Regeneración de Ki:** regeneras más cuanto más cerca de 0% estás; con el sistema de 2015 se regeneraba en los escalones 0–35% y por encima de \~50% la regeneración automática prácticamente se anula \[COM\]. Detalle en la sección de Ki.
- **Ki Fist / armas de Ki:** se apagan con Release 0% \[OF\].
- **Transformarse:** H resetea el Release y destransforma \[OF\]; si transformarse por sí solo cambia el Release no está documentado.

## Barra de Ki (Energy)

El Ki es el combustible de todo lo especial (ataques, vuelo, skills, formas) y su máximo sale casi solo de SPI. Al vaciarse, el Release cae a 0%.

### Ki máximo \[COM\]

- **SPI:** 40 Max Ki por punto, multiplicado por el modificador de raza+clase (tabla en la sección de atributos).
- SPI **no** se multiplica por formas ni por Form Mastery.
- Max Ki **no** escala con el Release.
- La guía oficial antigua atribuía el Ki máximo a Mind; en versiones modernas es Spirit \[OF antiguo vs COM actual\].

### Regeneración

- **Base:** cada punto de SPI da 1 de “RegenRateEnergy”, una estadística oculta con efecto exacto desconocido \[COM\].
- **Depende del Release:** cuanto más bajo el Release, más regeneras; a 0% regeneras el máximo posible. Con JRMCore de 2015 había que estar por debajo de 50% para regenerar \[COM\].
- **Se corta en combate:** desde un fix de JRMCore, tras recibir daño de un ser vivo no hay regeneración durante 30 segundos \[OF\].
- **Velocidad global:** config “Energy Regen Rate” con valores normal, slow, fast o faster (por defecto normal) \[COM, config\].
- **Meditation (Kami, Piccolo):** config “Skill Meditation – category” \[COM, config\]:
  - *passive* (por defecto): sube la regeneración de Body, Ki y stamina.
  - *active*: regenera Ki solo mientras mantienes C, como el antiguo “Ki charge”; gasta stamina.
- **Otras fuentes:** Senzu (rellena), comida en versiones antiguas, Medical Pod.

### Costes de Ki por acción

Medidos por un jugador en versiones antiguas (wiki Fandom, página “DFG”) \[COM, baja precisión\]:

| Acción | Coste aproximado |
| --- | --- |
| Caminar | 0 |
| Correr | \~1 Ki cada \~5 bloques |
| Saltar | \~3 Ki por salto; \~6 con skill Jump y sobrecarga |
| Volar | \~2 Ki por segundo |
| Golpe | Gasta Ki; más STR = más Ki por golpe |
| Ataque de Ki sobrecargado a 200% | El doble de Ki |
| Ataque de Ki con carga incompleta | Menos Ki |
| Swoop (dash en vuelo) | Bloqueado con menos de 10% − (nivel Fly × 0,5%) de Ki \[OF\] |

Otros costes \[OF, COM\]:

- **Formas:** drenan Ki por segundo según forma y atributos. Con atributos iguales, STR aporta \~40% del drenaje, WIL \~35% y DEX \~25% \[COM\].
- **Kaioken:** drena vida, no Ki \[COM\].
- **Ki Fist / Ki Blade:** gastan Ki al golpear; la guadaña cuesta 3 veces más y pega 3 veces más \[OF\].
- **Config:** existe un multiplicador de velocidad, coste de Ki y coste de stamina del vuelo \[OF\].

### Al vaciarse

El Release baja a 0%, no puedes usar poderes y quedas a merced de la regeneración o de una Senzu \[COM, OF\].

## Vida (Body) y Stamina (Action Time)

Ambas salen de CON y ninguna escala con el Release. Las formas no suben la vida máxima: dividen el daño que recibes.

### Body (vida) \[COM\]

- **CON:** 20 Body por punto × modificador de raza+clase.
- **Regeneración:** 1 “RegenRateBody” por punto de CON (efecto exacto desconocido); Meditation pasiva la sube; se corta 30 s tras recibir daño \[OF\].
- **Formas:** el daño recibido se divide por el multiplicador de la forma, lo que equivale a tener más vida.
- **Al llegar a 0:** mueres con un daño equivalente a caer al vacío. Con “Friendly Fist” en PvP quedas KO en vez de morir.
- **Curación:** Senzu, Medical Pod, Super Regeneration del Majin (gasta Ki y stamina).
- **Kaioken** y algunas formas drenan vida en vez de Ki.

### Action Time (stamina) \[COM\]

- **CON:** 3,5 Action Time por punto × modificador.
- **Regeneración:** 1 “RegenRateStamina” por punto de CON.
- **Qué la gasta:** golpes, bloqueos, dash, vuelo (multiplicador configurable), Meditation activa, Super Regeneration.
- **Al vaciarse:** no puedes bloquear y tus golpes bajan al daño de Minecraft vanilla; la guía oficial lo resume como hacer 1 de daño \[OF\].
- **Formas:** no cambian la stamina ni su coste, salvo el de bloquear (probablemente un descuido) \[COM\].
- **STR** aumenta el gasto de stamina \[COM\].

## Atributos → estadísticas

Cada atributo se convierte en una estadística con un ratio fijo por punto, que luego ajusta el modificador de raza+clase. Solo STR, DEX y WIL se multiplican por formas y Form Mastery.

### Ratios por punto (configs por defecto) \[COM\]

| Atributo | Estadística | Por punto | ¿Escala con Release? | ¿Lo multiplican las formas? |
| --- | --- | --- | --- | --- |
| STR | Melee Damage | 2,5 | Sí | Sí |
| DEX | Defense | 4 | Sí | Sí |
| DEX | Velocidad de carrera y vuelo | +100% al llegar al máximo de atributo | — | Sí |
| CON | Body | 20 | No | No (las formas dividen el daño recibido) |
| CON | Action Time | 3,5 | No | No |
| WIL | Ki Power | 5,2 | Sí | Sí |
| SPI | Max Ki | 40 | No | No |
| MND | TP ganados y número de skills | ver fórmula | — | No |

**Passive** (reducción de daño sin bloquear) = 20% de Defense \[COM\].

**TP por golpe** \[COM\], con P = Release % y M = MND:

```latex
TP = 2 + 2 \cdot \left\lfloor \frac{M}{5} \right\rfloor \cdot \frac{P}{100}
```

Requiere Release ≥ 5% y Body ≥ 1. Contra jugadores y Shadow Dummies se usa el MND del objetivo.

### Cómo se aplica el modificador de raza+clase \[COM\]

El modificador es un porcentaje que se suma a 100% y multiplica el ratio base. Ejemplo: Saiyan Warrior con 100 STR → 100 × 2,5 × 1,40 = 350 Melee Damage al 100% de Release.

Reconstrucción general (coherente con todo lo documentado, no confirmada en código):

```latex
Stat = Atributo \cdot Ratio \cdot \left(1 + \frac{Mod}{100}\right) \cdot Forma \cdot \frac{Release}{100}
```

`Forma` solo aplica a STR, DEX y WIL; el factor `Release` solo a las estadísticas que escalan.

### Modificadores conocidos (%) \[COM\]

| Raza + clase | Melee | Defense | Body | AT | Ki Power | Max Ki | Run | Fly |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Human Martial Artist | 0 | 0 | 0 | +30 | +10 | +10 | +10 | 0 |
| Human Spiritualist | −10 | +10 | −10 | +20 | +20 | +20 | +20 | +10 |
| Human Warrior | +10 | −10 | +10 | +40 | 0 | 0 | 0 | −10 |
| Saiyan Martial Artist | +30 | 0 | 0 | 0 | +20 | 0 | 0 | +10 |
| Saiyan Spiritualist | +20 | +10 | −10 | −10 | +30 | +10 | +10 | +20 |
| Saiyan Warrior | +40 | −10 | +10 | +10 | +10 | −10 | −10 | 0 |
| Half-Saiyan Martial Artist | +15 | 0 | 0 | +15 | +15 | +5 | +5 | +5 |
| Half-Saiyan Spiritualist | +5 | +10 | −10 | +5 | +25 | +15 | +15 | +15 |

El resto de combinaciones (Namekiano, Arcosiano, Majin, Half-Saiyan Warrior) está en la misma página de la wiki Fandom; no pude abrirla completa (devolvió error 402). Los Arcosianos tienen +20% Defense, +10% stamina y +30% velocidad, los más altos del juego \[COM\].

**Límites de atributo:** máximo por defecto 10.000 desde JRMCore 1.3.36 (antes 500) \[OF\].

## Ataques de Ki

El daño y el coste de un ataque de Ki salen de Ki Power (WIL × 5,2, escalado por Release y formas) y de cuánto lo cargas. Cargarlo al 200% duplica daño y coste.

### Parámetros de una técnica \[OF, COM\]

El sistema ha tenido 4 versiones; la actual (v4) permite subir de nivel las técnicas y repartir puntos entre sus parámetros \[COM\].

| Parámetro | Qué hace |
| --- | --- |
| Tipo | Wave, Blast, Disk, Laser, Spiral, Large Blast, Barrage, Shield, Explosion |
| Power / Damage | Daño base (en v3 se medía en medios corazones) |
| Speed | Velocidad del proyectil |
| Cooldown | Tiempo hasta poder volver a usarla; Barrage tiene uno muy corto |
| Charge time | Tiempo de carga |
| Density | Tamaño: más densidad = más pequeño; decide los choques (clash) |
| Effect | Explosión, empuje (Shield), explosión final (Explosion) |
| Color y sonido | Estéticos |

El comando de admin `/dbcspawnki (Type) (Speed) (Damage) (Effect) (Color) (Density) (Sound) (Charge %) [X Y Z]` confirma esos mismos parámetros \[OF\].

### Carga y sobrecarga \[COM, OF\]

- Se mantiene el botón de disparo para cargar; soltar dispara.
- **Carga incompleta** reduce mucho el daño y algo el coste.
- **Sobrecarga hasta 200%** (requiere Potential Unlock) duplica el daño y duplica el coste de Ki.
- Si el ataque consume el 100% de tu Ki, tu Release baja a 0% \[OF\].

### Coste de Ki según la velocidad

Medición de un jugador en versión antigua \[COM, baja precisión\]:

| Speed | Coste extra de Ki |
| --- | --- |
| 0,2 – 1,2 | 1 |
| 1,4 – 1,8 | 2 |
| 2,0 – 2,6 | 3 |
| 2,8 – 3,2 | 4 |
| 3,4 – 3,8 | 5 |
| 4,0 | 6 |

Mismo jugador: el tiempo de casteo no cuesta nada a 20 y suma 2 de Ki por cada punto que lo bajas.

### Choques (clash)

Los ataques pueden chocar; la Density interviene en quién gana. Con God of Destruction, los ataques personalizados destruyen los más débiles que ellos \[OF\]. La regla exacta del choque no es pública.

### Lo que no se sabe

La fórmula exacta de daño (cómo se combinan Damage, Ki Power, carga y la defensa del objetivo) no está publicada. La especificación de abajo propone una.

## Battle Power y scouter

El BP es un número visual que resume tu poder actual y sube y baja con el Release; no afecta al combate en sí.

- **Es solo visual:** desde DBC 1.4.40 el jugador elige en Client Settings la escala “Normal” o “High” (números enormes); el propio changelog dice que es solo un cambio visual \[OF\].
- **Escala con el Release** \[COM\]: otro jugador puede ocultar o cambiar su BP rápido bajando su Release; el de los mobs no cambia \[OF\].
- **Desde JRMCore 1.3.27** el texto de BP incluye la dificultad del jugador y la reserva de Power Points arcosianos \[OF\].
- **Scouter (F4):** modos de localización (mobs/NPCs cercanos, y una variante “MP” para jugadores) y de lectura de BP \[OF\].
- **Rotura por tier** \[OF, wiki antigua\]:

| Tier del scouter | Se rompe por encima de |
| --- | --- |
| 1 | 100.000 BP |
| 2 | 1.000.000 BP |
| 3 | Nunca (solo desgaste normal) |

La fórmula exacta del BP no es pública.

## Cómo lo resuelve DragonMine Z

DragonMine Z (Forge 1.20.1, GPL) resuelve los mismos sistemas con fórmulas lineales simples y todo configurable, y es la mejor referencia legal para ver código moderno \[DMZ\].

| Sistema | DragonMine Z | Equivalente DBC |
| --- | --- | --- |
| Atributos | STR, SKP (golpes con Ki), RES, VIT, PWR (Ki Power), ENE (energía); mínimo 5, máximo configurable | STR, DEX, CON, WIL, MND, SPI |
| Ki máximo | 20 + ENE × escalado de clase | SPI × 40 × mod |
| Stamina máxima | 20 + RES × escalado | CON × 3,5 × mod |
| Poise (resistir aturdimiento) | 25 + defensa | No existe |
| Regeneración por segundo | (base por 5 s + atributo × escalado) ÷ 5; Ki desde ENE, vida y stamina desde VIT | Atributo → “RegenRate” oculta |
| Regeneración de Ki al cargar | Mucho mayor mientras mantienes C; desde v2.1 escala con la saturación de comida | Meditation activa o pasiva |
| Release | 0–100%; multiplica melee, defensa y BP; mantenerlo alto cuesta energía | Igual, sin coste de mantenimiento documentado |
| Drenaje de formas | Valor por forma (p. ej. SSJ 0,08, Grade 2 0,12, Grade 3 0,48) escalado por el Release actual | Por segundo, ponderado por STR/WIL/DEX |
| Transformarse | Mantener G llena una barra “Form Release” hasta 100% | Mantener G |
| BP | (STR + SKP + RES + PWR) con escalados, pasado por una curva y × Release | Fórmula no pública |
| Potential Unlock | Sube el máximo de 50% a 100% | Igual |

Dos ideas de DMZ que vale la pena copiar en tu diseño:

1. **Coste de mantenimiento del Release** (la energía que gasta estar cargado) y **drenaje de formas multiplicado por el Release**: bajar el poder pasa a ser una decisión táctica.
2. **Stats como atributos de Forge**: funcionan también en NPCs y mobs y son compatibles con otros mods (v2.1).

Como DMZ es GPL-3.0, si copias su código tu mod debe publicarse también bajo GPL. Inspirarte en el diseño (lo que hay en esta tabla) no te obliga.

## Especificación propuesta para tu remake

Todo lo de esta sección es **\[PROP\]**: fórmulas mías que reproducen el comportamiento documentado de DBC, con cada número como opción de config para ajustarlo jugando. El servidor calcula todo; el cliente solo envía teclas y dibuja.

### Datos por jugador (Data Attachment)

| Campo | Tipo | Se guarda | Se sincroniza |
| --- | --- | --- | --- |
| release | float 0–maxRelease | Sí | Sí |
| ki, body, stamina | float | Sí | Sí |
| chargeState | enum (ver diagrama) | No | Sí (para el aura) |
| turbo | bool | No | Sí |
| lastHurtTick | long | No | No |
| atributos, raza, clase, skills, forma | varios | Sí | Al cambiar |

### Release

```latex
maxRelease = \min(100,\ 50 + 5 \cdot nivelPU) \quad (\text{o } 200 \text{ si allowOvercharge})
```

```latex
\frac{d\,release}{dt} = chargeRate \cdot (turbo\ ?\ 1{,}5 : 1) \cdot (release < 50\ ?\ 1 : 0{,}5)
```

- `chargeRate` = 10 %/s por defecto: 0→50% en 5 s y 50→100% en 10 s, el “se siente lento pasado el 50%” de DBC.
- `dischargeRate` = 25 %/s (Ctrl+C). H pone release = 0 al instante.
- Internamente float; el HUD muestra pasos de `displayStep` = 5% (como DBC) o 1% (suave).
- Mantener el Release cuesta Ki, como en DMZ: `upkeep/s = maxKi × 0,002 × (release/100)²`.
- Si ki llega a 0: release = 0 y estado EXHAUSTED hasta tener al menos 5% de Ki.

&#91;embedded content: Máquina de estados del Release · 5 estados\]

El servidor solo sube el Release en “Cargando”; desde cualquier estado, quedarse sin Ki lleva a “Agotado” hasta recuperar el 5%.

### Ki

```latex
maxKi = SPI \cdot 40 \cdot (1 + modMaxKi)
```

```latex
regenKi/s = maxKi \cdot 0{,}02 \cdot regenMult \cdot \max\!\left(0,\ 1 - \frac{release}{50}\right) \cdot (1 + 0{,}1 \cdot nivelMeditation)
```

- `regenMult` = 0,5 / 1 / 1,5 / 2 para slow / normal / fast / faster.
- Regeneración máxima a 0% de Release y nula desde 50%: el mismo dilema de DBC.
- Bloqueo en combate: sin regeneración durante 600 ticks (30 s) tras recibir daño de un ser vivo.
- **Meditation activa** (opción de config): mientras mantienes C ya al máximo de Release, gana `maxKi × 0,01 × nivel` por segundo y gasta `maxStamina × 0,05` por segundo.
- Costes iniciales: vuelo 2 Ki/s × multiplicador de config; salto 3 Ki; correr 0,2 Ki por bloque; golpe `1 + STR/200` Ki.

### Vida y stamina

```latex
maxBody = CON \cdot 20 \cdot (1 + modBody) \qquad maxStamina = CON \cdot 3{,}5 \cdot (1 + modAT)
```

- Regeneración: vida 1% del máximo por segundo; stamina 5% por segundo. Ambas con el mismo bloqueo de 30 s en combate (la stamina sin bloqueo si prefieres combate más rápido).
- Stamina en 0: no se puede bloquear y el golpe usa solo el daño vanilla.

### Daño

```latex
melee = STR \cdot 2{,}5 \cdot (1 + modMelee) \cdot forma \cdot \frac{release}{100}
```

```latex
defense = DEX \cdot 4 \cdot (1 + modDef) \cdot forma \cdot \frac{release}{100} \qquad passive = 0{,}2 \cdot defense
```

```latex
dañoRecibido = \frac{\max(1,\ entrante - (bloqueando\ ?\ defense : passive))}{formaObjetivo}
```

```latex
kiPower = WIL \cdot 5{,}2 \cdot (1 + modKiPow) \cdot forma \cdot \frac{release}{100}
```

```latex
dañoKi = dañoTecnica \cdot \left(1 + \frac{kiPower}{100}\right) \cdot carga \qquad carga \in [0{,}2;\ 2]
```

- Coste del ataque: `costeBase × carga + coste por Speed` (tabla de la sección de Ki como punto de partida).
- Carga > 1 solo con Potential Unlock. Si el coste vacía el Ki: release = 0.

### Battle Power

```latex
BP = \left\lfloor k \cdot (melee_{100} + defense_{100} + kiPower_{100})^{1{,}2} \right\rfloor \cdot \frac{release}{100}
```

`_100` = calculado al 100% de Release. `k` = 1 en escala “Normal” y 1000 en “High” (opción de cliente, solo visual, como DBC).

### Ticks y sincronización

1. **Cliente → servidor:** un payload `ChargeInput(charge, discharge, turbo)` solo cuando cambia una tecla. Nunca valores de poder.
2. **Servidor:** actualiza todo en `PlayerTickEvent.Post`, cada tick (20/s), con dt = 0,05 s.
3. **Servidor → dueño:** `PowerSync(release, ki, body, stamina, state)` cada 2 ticks si algo cambió; sincronización completa al entrar, al reaparecer y al cambiar de dimensión (el bug #2510 de NeoForge no reenvía attachments al cambiar de dimensión).
4. **Servidor → jugadores que te ven:** solo `AuraState(charging, turbo, tramo de release de 10%)` al cambiar; con eso dibujan el aura sin recibir tus números.
5. **Cliente:** interpola la barra entre paquetes para que suba suave.

### HUD

- Arriba a la izquierda: barra de Ki con el Release % escrito encima, barra de vida y barra de stamina debajo.
- Color del Release: blanco hasta 50%, amarillo hasta el máximo, rojo en sobrecarga.
- BP opcional junto a la barra; aura visible mientras chargeState es CHARGING.

### Opciones de config

| Clave | Por defecto | Lado |
| --- | --- | --- |
| release.baseMax | 50 | Servidor |
| release.perPotentialLevel | 5 | Servidor |
| release.allowOvercharge | false | Servidor |
| release.chargeRate (%/s) | 10 | Servidor |
| release.slowdownAbove50 | 0,5 | Servidor |
| release.turboMult | 1,5 | Servidor |
| release.dischargeRate (%/s) | 25 | Servidor |
| release.upkeepFactor | 0,002 | Servidor |
| ki.perSPI | 40 | Servidor |
| ki.regenPct | 0,02 | Servidor |
| ki.regenRate | normal | Servidor |
| ki.regenCutoffRelease | 50 | Servidor |
| combat.regenLockTicks | 600 | Servidor |
| meditation.mode | passive | Servidor |
| hud.displayStep | 5 | Cliente |
| hud.bpScale | normal | Cliente |

## Lo que sigue sin confirmar y cómo medirlo

Cinco valores definen la sensación del combate y ninguno es público; se pueden medir jugando DBC 1.7.10 con un cronómetro, sin tocar su código.

| Dato | Cómo medirlo en DBC |
| --- | --- |
| Velocidad de carga del Release | Grabar pantalla manteniendo C de 0% a tu máximo, con y sin Turbo, y con distintos MND/SPI |
| Regeneración de Ki según Release | Vaciar el Ki y cronometrar la recarga a 0%, 25% y 50% |
| Coste de mantener el Release | Quedarse quieto al 100% sin cargar y ver si baja el Ki |
| Fórmula de daño de Ki | Atacar a un Shadow Dummy con una técnica fija variando WIL, carga y Release |
| Fórmula de BP | Anotar el BP con distintos atributos y Release y ajustar una curva |

También puedes subir tus propios `jinryuujrmcore.cfg` y las configs de `config/jingames/dbc/`: traen los valores por defecto de costes, regeneración y multiplicadores, y leer tus archivos de configuración es legítimo.

## Fuentes

- [Attributes and Statistics — wiki Fandom de DBC](https://dragonblockc.fandom.com/wiki/Attributes_and_Statistics) (ratios por punto y modificadores)
- [DFG — mediciones de un jugador, wiki Fandom](https://dragonblockc.fandom.com/wiki/DFG)
- [Ki attacks — wiki Fandom](https://dragonblockc.fandom.com/wiki/Ki_attacks)
- [JRMC Config — wiki Fandom](https://dragonblockc.fandom.com/wiki/JRMC_Config)
- [Turbo mode — wiki Fandom](https://dragonblockc.fandom.com/wiki/Turbo_mode)
- [Getting Started — jindbc.fandom.com](https://jindbc.fandom.com/wiki/Getting_Started)
- [How to play Dragon Block C Guide — JinGames](https://main.jingames.net/how-to-play-dragon-block-c-guide/)
- [Controls — wiki oficial](https://main.jingames.net/wiki/dragon-block-c/controls/)
- [Stat Sheet — wiki oficial](https://main.jingames.net/wiki/dragon-block-c/player/stat-sheet/)
- [Other Features (scouter) — wiki oficial](https://main.jingames.net/wiki/dragon-block-c/other-features/)
- [Changelogs de builds 1.104–1.106 — JinGames](https://main.jingames.net/category/minecraft/dragon-block-c/)
- [Changelogs JRMCore 1.3.39–1.3.43 — JinGames](https://main.jingames.net/page/2/)
- [Update 22w26 (JRMCore 1.3.44) — JinGames](https://main.jingames.net/mod-update-quality-of-life-and-bug-fix-release-22w26/)
- [Fix Update 17w31 (escala de BP) — JinGames](https://main.jingames.net/fix-update-17w31/)
- [Status Info week 47 (sin regeneración 30 s) — JinGames](https://main.jingames.net/status-info-week-47/)
- [Foro: Faster Ki Charge Past 50%](https://main.jingames.net/forums/topic/faster-ki-charge-past-50/)
- [Foro: No Ki, 50%, no Ki Attacks](https://main.jingames.net/forums/topic/no-ki-50-no-ki-attacks/)
- [Foro: Ki Regeneration](https://main.jingames.net/forums/topic/ki-regeneration/)
- [Foro: Problem with ki not recharging when releasing](https://main.jingames.net/forums/topic/problem-with-ki-not-recharging-when-releasing/)
- [Foro: Energy Release Question](https://main.jingames.net/forums/topic/energy-release-question/)
- [Foro: % Lock + Power Release/KI Charge Ideas](https://main.jingames.net/forums/topic/lock-power-releaseki-charge-ideas/)
- [DragonMine Z — Stats and Attributes (wiki GitHub)](https://github.com/DragonMineZ/dragonminez/wiki/Stats-and-Attributes)
- [DragonMine Z — Transformations and Mastery](https://github.com/DragonMineZ/dragonminez/wiki/Transformations-and-Mastery)
- [DragonMine Z — Patch notes v2.1](https://github.com/DragonMineZ/dragonminez/blob/main/PATCH_NOTES/PATCH_NOTES_V2.1_final.md)
- [NeoForge issue #2510](https://github.com/neoforged/NeoForge/issues/2510)
