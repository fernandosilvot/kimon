package net.kimon.kimon.skill;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kimon.kimon.Kimon;
import net.kimon.kimon.config.KimonConfig;
import net.kimon.kimon.power.KiSpending;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * The Dash skill: a short burst sideways or backwards on the ground, server-authoritative. It needs the
 * skill, an off-cooldown dash and a bit of Ki; the client only says which way the player is steering.
 */
@EventBusSubscriber(modid = Kimon.MODID)
public final class DashHandler {

    public static final int BACK = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private static final double LIFT = 0.12;

    private static final Map<UUID, Integer> NEXT_ALLOWED_TICK = new ConcurrentHashMap<>();

    private DashHandler() {
    }

    /**
     * The horizontal direction of a dash for a player looking at {@code yawDegrees}: returned as a
     * unit vector (x, z), or null for a direction that isn't allowed (a dash never goes forward).
     */
    public static double[] direction(double yawDegrees, int direction) {
        double yaw = Math.toRadians(yawDegrees);
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double leftX = Math.cos(yaw);
        double leftZ = Math.sin(yaw);
        return switch (direction) {
            case BACK -> new double[] {-forwardX, -forwardZ};
            case LEFT -> new double[] {leftX, leftZ};
            case RIGHT -> new double[] {-leftX, -leftZ};
            default -> null;
        };
    }

    public static void dash(ServerPlayer player, int direction) {
        double[] dir = direction(player.getYRot(), direction);
        if (dir == null) {
            return;
        }
        SkillData data = player.getData(ModSkillAttachments.SKILLS.get());
        double strength = SkillEffects.total(data, SkillCatalog.current(), SkillEffect.DASH);
        if (strength <= 0.0) {
            player.sendSystemMessage(Component.translatable("msg.kimon.dash_no_skill"), true);
            return;
        }
        Integer next = NEXT_ALLOWED_TICK.get(player.getUUID());
        if ((next != null && player.tickCount < next) || !player.onGround()) {
            return;
        }
        SkillParams params = KimonConfig.skillParams();
        if (!KiSpending.trySpend(player, KiSpending.maxKi(player) * params.dashKiFraction())) {
            player.sendSystemMessage(Component.translatable("msg.kimon.no_energy"), true);
            return;
        }
        NEXT_ALLOWED_TICK.put(player.getUUID(), player.tickCount + params.dashCooldownTicks());
        player.setDeltaMovement(new Vec3(dir[0] * strength, LIFT, dir[1] * strength));
        player.hurtMarked = true; // make the server send the new velocity to the client
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NEXT_ALLOWED_TICK.remove(event.getEntity().getUUID());
    }
}
