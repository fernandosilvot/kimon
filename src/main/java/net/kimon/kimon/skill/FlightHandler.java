package net.kimon.kimon.skill;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.power.KiSpending;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Flight from the Fly skill, server-authoritative. Toggling on needs the skill and some Ki; while the
 * player is actually airborne in flight it drains Ki every tick, and running out (or losing the skill)
 * ends it. Creative and spectator players are left alone.
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class FlightHandler {

    private static final float VANILLA_FLYING_SPEED = 0.05f;
    private static final double DT = 0.05;

    private static final Set<UUID> FLYING = ConcurrentHashMap.newKeySet();

    private FlightHandler() {
    }

    private static boolean exempt(ServerPlayer player) {
        return player.getAbilities().instabuild || player.isSpectator();
    }

    /** The fly key: starts flight if it can, stops it if it is on. */
    public static void toggle(ServerPlayer player) {
        if (exempt(player)) {
            return;
        }
        if (FLYING.contains(player.getUUID())) {
            stop(player);
            player.sendSystemMessage(Component.translatable("msg.kimon.flight_off"), true);
            return;
        }
        SkillData data = player.getData(ModSkillAttachments.SKILLS.get());
        SkillCatalog catalog = SkillCatalog.current();
        if (!SkillEffects.has(data, catalog, SkillEffect.FLIGHT)) {
            player.sendSystemMessage(Component.translatable("msg.kimon.flight_no_skill"), true);
            return;
        }
        double perSecond = KimonConfig.skillParams().flightKiPerSecond();
        if (!KiSpending.has(player, perSecond)) {
            player.sendSystemMessage(Component.translatable("msg.kimon.no_energy"), true);
            return;
        }
        FLYING.add(player.getUUID());
        Abilities abilities = player.getAbilities();
        abilities.mayfly = true;
        abilities.flying = true;
        abilities.setFlyingSpeed(flyingSpeed(data, catalog));
        player.onUpdateAbilities();
        player.sendSystemMessage(Component.translatable("msg.kimon.flight_on"), true);
    }

    /** Flying speed: vanilla's, plus the Fly skill's percent bonus. */
    static float flyingSpeed(SkillData data, SkillCatalog catalog) {
        double bonus = SkillEffects.total(data, catalog, SkillEffect.FLIGHT);
        return (float) (VANILLA_FLYING_SPEED * (1.0 + bonus / 100.0));
    }

    private static void stop(ServerPlayer player) {
        FLYING.remove(player.getUUID());
        if (exempt(player)) {
            return;
        }
        Abilities abilities = player.getAbilities();
        abilities.mayfly = false;
        abilities.flying = false;
        abilities.setFlyingSpeed(VANILLA_FLYING_SPEED);
        player.onUpdateAbilities();
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !FLYING.contains(player.getUUID())) {
            return;
        }
        SkillData data = player.getData(ModSkillAttachments.SKILLS.get());
        if (!SkillEffects.has(data, SkillCatalog.current(), SkillEffect.FLIGHT)) {
            stop(player);
            return;
        }
        // Only the time actually spent flying costs Ki; standing on the ground between flights is free.
        if (player.getAbilities().flying) {
            double cost = KimonConfig.skillParams().flightKiPerSecond() * DT;
            if (!KiSpending.trySpend(player, cost)) {
                stop(player);
                player.sendSystemMessage(Component.translatable("msg.kimon.flight_no_ki"), true);
            }
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FLYING.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        FLYING.remove(event.getEntity().getUUID());
    }
}
