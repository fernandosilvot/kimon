package net.kimon.kimon.skill;

/**
 * Tunable numbers around skills, built from the server config. Pure data.
 *
 * @param mindPerPoint      Mind budget per point of the MIND attribute ({@code skills.mindPerPoint})
 * @param flightKiPerSecond Ki drained per second while flying ({@code skills.flightKiPerSecond}, 2 in the research)
 * @param dashKiFraction    fraction of max Ki a dash costs ({@code skills.dashKiFraction})
 * @param dashCooldownTicks ticks between dashes ({@code skills.dashCooldownTicks})
 */
public record SkillParams(double mindPerPoint, double flightKiPerSecond, double dashKiFraction,
                          int dashCooldownTicks) {

    public static final SkillParams DEFAULTS = new SkillParams(1.0, 2.0, 0.02, 20);
}
