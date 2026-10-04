package net.kimon.kimon.config;

import net.kimon.kimon.power.KiRegenRate;
import net.kimon.kimon.power.PowerParams;
import net.kimon.kimon.stats.TpParams;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Kimon's configuration (NeoForge {@link ModConfigSpec}).
 *
 * <p>The SERVER spec holds the balance of the Release / Energy loop (synced from the server to
 * clients, so the HUD sees the same numbers). The CLIENT spec holds purely visual options. Values
 * are exposed to the pure game logic as an immutable {@link PowerParams} snapshot, rebuilt whenever
 * the config (re)loads, so the tick loop never touches the spec directly.</p>
 *
 * <p>Defaults come from {@code docs/02-release-ki-stats.md} ("Opciones de config"); all are [PROP]
 * values to be tuned by playtesting.</p>
 */
public final class KimonConfig {

    private KimonConfig() {
    }

    // ---------------------------------------------------------------- server
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue BASE_MAX = SERVER_BUILDER
            .comment("Base Release ceiling in % (before FOCUS bonus).")
            .translation("config.kimon.release.baseMax")
            .defineInRange("release.baseMax", PowerParams.DEFAULTS.baseMaxRelease(), 1.0, 200.0);

    private static final ModConfigSpec.BooleanValue ALLOW_OVERCHARGE = SERVER_BUILDER
            .comment("Allow Release above 100% (up to 200%). Raise release.baseMax too if you want players to start there.")
            .translation("config.kimon.release.allowOvercharge")
            .define("release.allowOvercharge", PowerParams.DEFAULTS.allowOvercharge());

    private static final ModConfigSpec.DoubleValue CHARGE_RATE = SERVER_BUILDER
            .comment("Release gained per second while charging (%/s).")
            .translation("config.kimon.release.chargeRate")
            .defineInRange("release.chargeRate", PowerParams.DEFAULTS.chargeRate(), 0.1, 1000.0);

    private static final ModConfigSpec.DoubleValue SLOWDOWN_ABOVE_50 = SERVER_BUILDER
            .comment("Charge-rate multiplier once Release is at or above 50%.")
            .translation("config.kimon.release.slowdownAbove50")
            .defineInRange("release.slowdownAbove50", PowerParams.DEFAULTS.slowdownAbove50(), 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue TURBO_MULT = SERVER_BUILDER
            .comment("Charge-rate multiplier while Turbo is held.")
            .translation("config.kimon.release.turboMult")
            .defineInRange("release.turboMult", PowerParams.DEFAULTS.turboMult(), 1.0, 10.0);

    private static final ModConfigSpec.DoubleValue TURBO_KI_DRAIN = SERVER_BUILDER
            .comment("Fraction of max Energy drained per second while charging with Turbo.")
            .translation("config.kimon.release.turboKiDrain")
            .defineInRange("release.turboKiDrain", PowerParams.DEFAULTS.turboKiDrain(), 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue DISCHARGE_RATE = SERVER_BUILDER
            .comment("Release lost per second while discharging (%/s).")
            .translation("config.kimon.release.dischargeRate")
            .defineInRange("release.dischargeRate", PowerParams.DEFAULTS.dischargeRate(), 0.1, 1000.0);

    private static final ModConfigSpec.DoubleValue UPKEEP_FACTOR = SERVER_BUILDER
            .comment("Energy upkeep for holding Release: maxEnergy * factor * (release/100)^2 per second.")
            .translation("config.kimon.release.upkeepFactor")
            .defineInRange("release.upkeepFactor", PowerParams.DEFAULTS.upkeepFactor(), 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue KI_PER_SPIRIT = SERVER_BUILDER
            .comment("Max Energy per SPIRIT point.")
            .translation("config.kimon.ki.perSPI")
            .defineInRange("ki.perSPI", PowerParams.DEFAULTS.kiPerSpirit(), 0.1, 10000.0);

    private static final ModConfigSpec.DoubleValue KI_REGEN_PCT = SERVER_BUILDER
            .comment("Fraction of max Energy regenerated per second at 0% Release.")
            .translation("config.kimon.ki.regenPct")
            .defineInRange("ki.regenPct", PowerParams.DEFAULTS.kiRegenPct(), 0.0, 1.0);

    private static final ModConfigSpec.EnumValue<KiRegenRate> KI_REGEN_RATE = SERVER_BUILDER
            .comment("Global Energy regeneration speed: SLOW, NORMAL, FAST or FASTER.")
            .translation("config.kimon.ki.regenRate")
            .defineEnum("ki.regenRate", PowerParams.DEFAULTS.kiRegenRate());

    private static final ModConfigSpec.DoubleValue KI_REGEN_CUTOFF = SERVER_BUILDER
            .comment("Release % at/above which Energy no longer regenerates.")
            .translation("config.kimon.ki.regenCutoffRelease")
            .defineInRange("ki.regenCutoffRelease", PowerParams.DEFAULTS.kiRegenCutoff(), 1.0, 200.0);

    private static final ModConfigSpec.DoubleValue EXHAUST_RECOVER = SERVER_BUILDER
            .comment("Fraction of max Energy needed to leave the EXHAUSTED state.")
            .translation("config.kimon.ki.exhaustRecoverPct")
            .defineInRange("ki.exhaustRecoverPct", PowerParams.DEFAULTS.exhaustRecoverPct(), 0.0, 1.0);

    private static final ModConfigSpec.IntValue TP_BASE = SERVER_BUILDER
            .comment("Flat Training Points per successful hit (TP = base + perFocusStep * floor(FOCUS/focusDivisor) * release/100).")
            .translation("config.kimon.tp.baseAmount")
            .defineInRange("tp.baseAmount", TpParams.DEFAULTS.baseAmount(), 0, 1000);

    private static final ModConfigSpec.IntValue TP_PER_FOCUS_STEP = SERVER_BUILDER
            .comment("Extra TP per FOCUS step at 100% Release.")
            .translation("config.kimon.tp.perFocusStep")
            .defineInRange("tp.perFocusStep", TpParams.DEFAULTS.perFocusStep(), 0, 1000);

    private static final ModConfigSpec.IntValue TP_FOCUS_DIVISOR = SERVER_BUILDER
            .comment("FOCUS points per step in the TP formula.")
            .translation("config.kimon.tp.focusDivisor")
            .defineInRange("tp.focusDivisor", TpParams.DEFAULTS.focusDivisor(), 1, 10000);

    private static final ModConfigSpec.DoubleValue TP_HIT_CHANCE = SERVER_BUILDER
            .comment("Probability (0-1) that a hit with Release >= 5% grants TP.")
            .translation("config.kimon.tp.hitChance")
            .defineInRange("tp.hitChance", TpParams.DEFAULTS.hitChance(), 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue ALTAR_STAMINA_COST = SERVER_BUILDER
            .comment("Fraction of max Stamina one Training Altar use costs (it counts as a hit on a dummy).")
            .translation("config.kimon.tp.altarStaminaCost")
            .defineInRange("tp.altarStaminaCost", TpParams.DEFAULTS.altarStaminaCost(), 0.0, 1.0);

    private static final ModConfigSpec.IntValue POWER_PER_POINT = SERVER_BUILDER
            .comment("Power gained for every attribute point bought with TP (Power drives tier bonuses and form gating).")
            .translation("config.kimon.progression.powerPerPoint")
            .defineInRange("progression.powerPerPoint", TpParams.DEFAULTS.powerPerPoint(), 0, 1000);

    private static final ModConfigSpec.IntValue REGEN_LOCK_TICKS = SERVER_BUILDER
            .comment("Ticks without Energy regeneration after being hurt by a living entity (600 = 30 s).")
            .translation("config.kimon.combat.regenLockTicks")
            .defineInRange("combat.regenLockTicks", PowerParams.DEFAULTS.regenLockTicks(), 0, 72000);

    private static final ModConfigSpec.BooleanValue STAMINA_REGEN_LOCKED = SERVER_BUILDER
            .comment("Whether the post-damage lock also stops Stamina regeneration (false = faster combat).")
            .translation("config.kimon.combat.staminaRegenLocked")
            .define("combat.staminaRegenLocked", PowerParams.DEFAULTS.staminaRegenLocked());

    private static final ModConfigSpec.DoubleValue HIT_STAMINA_COST = SERVER_BUILDER
            .comment("Fraction of max Stamina an empowered melee hit costs. With too little Stamina (or Energy) the hit does vanilla damage.")
            .translation("config.kimon.combat.hitStaminaCost")
            .defineInRange("combat.hitStaminaCost", PowerParams.DEFAULTS.hitStaminaCost(), 0.0, 1.0);

    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    // ---------------------------------------------------------------- client
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue DISPLAY_STEP = CLIENT_BUILDER
            .comment("Release % shown on the HUD moves in steps of this size (5 like the original feel, 1 for smooth).")
            .translation("config.kimon.hud.displayStep")
            .defineInRange("hud.displayStep", 5, 1, 10);

    public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    // --------------------------------------------------------------- snapshot
    private static volatile PowerParams cached = PowerParams.DEFAULTS;
    private static volatile TpParams cachedTp = TpParams.DEFAULTS;

    /** The current balance values (defaults until the config has loaded). */
    public static PowerParams params() {
        return cached;
    }

    /** The current Training Point rules (defaults until the config has loaded). */
    public static TpParams tpParams() {
        return cachedTp;
    }

    /** HUD Release step in %, from the client config (5 until it has loaded). */
    public static int displayStep() {
        try {
            return DISPLAY_STEP.getAsInt();
        } catch (IllegalStateException notLoaded) {
            return 5;
        }
    }

    private static PowerParams build() {
        return new PowerParams(
                BASE_MAX.getAsDouble(),
                ALLOW_OVERCHARGE.getAsBoolean(),
                CHARGE_RATE.getAsDouble(),
                SLOWDOWN_ABOVE_50.getAsDouble(),
                TURBO_MULT.getAsDouble(),
                TURBO_KI_DRAIN.getAsDouble(),
                DISCHARGE_RATE.getAsDouble(),
                UPKEEP_FACTOR.getAsDouble(),
                KI_PER_SPIRIT.getAsDouble(),
                KI_REGEN_PCT.getAsDouble(),
                KI_REGEN_RATE.get(),
                KI_REGEN_CUTOFF.getAsDouble(),
                EXHAUST_RECOVER.getAsDouble(),
                REGEN_LOCK_TICKS.getAsInt(),
                STAMINA_REGEN_LOCKED.getAsBoolean(),
                HIT_STAMINA_COST.getAsDouble());
    }

    /** Rebuilds the snapshot when the SERVER config loads or changes. */
    public static void onLoad(ModConfigEvent.Loading event) {
        refresh(event.getConfig().getSpec());
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        refresh(event.getConfig().getSpec());
    }

    private static void refresh(Object spec) {
        if (spec == SERVER_SPEC) {
            cached = build();
            cachedTp = new TpParams(TP_BASE.getAsInt(), TP_PER_FOCUS_STEP.getAsInt(), TP_FOCUS_DIVISOR.getAsInt(),
                    TP_HIT_CHANCE.getAsDouble(), ALTAR_STAMINA_COST.getAsDouble(), POWER_PER_POINT.getAsInt());
        }
    }
}
