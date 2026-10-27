package com.animania.client;

import com.animania.modern.DefensiveBeeSwarm;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Tick-based visual flight in world space, independent of rendering frequency. */
final class SwarmFlightAnimation {
    static final class BeeFlight {
        Vec3 previous, position, velocity = Vec3.ZERO, oldVelocity = Vec3.ZERO, offset;
        int retarget;
        final float size, wingPhase, wingSpeed;
        BeeFlight(Vec3 center, RandomSource random) {
            offset = randomOffset(random, .6, .4);
            previous = position = center.add(offset);
            size = .19F + random.nextFloat() * .05F;
            wingPhase = random.nextFloat() * 6.28F;
            wingSpeed = 1.7F + random.nextFloat() * .9F;
            retarget = random.nextInt(18);
        }
    }
    final BeeFlight[] bees;
    private final RandomSource random;
    private int lastTick, lastState;
    private Vec3 lastCenter;

    SwarmFlightAnimation(DefensiveBeeSwarm swarm, int count) {
        random = RandomSource.create(swarm.getUUID().getLeastSignificantBits());
        lastCenter = swarm.position().add(0, .3, 0);
        lastTick = swarm.tickCount; lastState = swarm.defenseState();
        bees = new BeeFlight[count];
        for (int i = 0; i < count; i++) bees[i] = new BeeFlight(lastCenter, random);
    }
    private static Vec3 randomOffset(RandomSource random, double horizontal, double vertical) {
        // Independent destinations throughout a volume, not points on circular shells.
        return new Vec3((random.nextDouble() - random.nextDouble()) * horizontal,
                (random.nextDouble() - random.nextDouble()) * vertical,
                (random.nextDouble() - random.nextDouble()) * horizontal);
    }
    void update(DefensiveBeeSwarm swarm) {
        int passed = swarm.tickCount - lastTick;
        if (passed <= 0) return;
        Vec3 center = swarm.position().add(0, .3, 0);
        if (passed > 8 || center.distanceToSqr(lastCenter) > 64) {
            for (BeeFlight bee : bees) {
                bee.previous = bee.position = center.add(bee.offset);
                bee.velocity = bee.oldVelocity = Vec3.ZERO;
            }
            lastCenter = center; lastTick = swarm.tickCount;
            return;
        }
        Vec3 motion = center.subtract(lastCenter).scale(1.0 / passed);
        for (int tick = 1; tick <= passed; tick++) step(swarm, lastCenter.add(motion.scale(tick)), motion);
        lastCenter = center; lastTick = swarm.tickCount; lastState = swarm.defenseState();
    }
    private void step(DefensiveBeeSwarm swarm, Vec3 center, Vec3 motion) {
        int state = swarm.defenseState();
        boolean chasing = state == DefensiveBeeSwarm.PURSUIT;
        boolean returning = state == DefensiveBeeSwarm.RETURNING;
        Entity offender = swarm.visualOffender();
        for (BeeFlight bee : bees) {
            bee.previous = bee.position; bee.oldVelocity = bee.velocity;
        }
        for (BeeFlight bee : bees) {
            if (--bee.retarget <= 0 || state != lastState) {
                bee.offset = randomOffset(random, chasing ? 1.3 : returning ? .45 : .85, chasing ? .65 : .4);
                bee.retarget = 9 + random.nextInt(chasing ? 18 : 30);
            }
            Vec3 goal = center.add(bee.offset);
            if (chasing && offender != null && offender.distanceToSqr(swarm) < 9) {
                // Different bees approach different parts of the intruder and then peel away.
                goal = offender.position().add(bee.offset.x, offender.getBbHeight() * .65 + bee.offset.y, bee.offset.z);
            } else if (motion.lengthSqr() > .001) {
                goal = goal.subtract(motion.scale(2 + bee.size * 8));
            }
            Vec3 separation = Vec3.ZERO;
            for (BeeFlight other : bees) {
                if (bee == other) continue;
                Vec3 apart = bee.previous.subtract(other.previous);
                double d = apart.lengthSqr();
                if (d > .00001 && d < .09) separation = separation.add(apart.scale((.09 - d) / (.09 * Math.sqrt(d))));
            }
            Vec3 desired = goal.subtract(bee.position).scale(chasing ? .16 : .11)
                    .add(motion.scale(.7)).add(separation.scale(.035));
            // Acceleration and turning are limited separately; each bee retains its own momentum.
            Vec3 change = desired.subtract(bee.velocity);
            if (change.lengthSqr() > .0036) change = change.normalize().scale(.06);
            bee.velocity = bee.velocity.add(change).scale(.96);
            double speed = chasing ? .3 : returning ? .25 : .16;
            if (bee.velocity.lengthSqr() > speed * speed) bee.velocity = bee.velocity.normalize().scale(speed);
            Vec3 next = bee.position.add(bee.velocity);
            var hit = swarm.level().clip(new ClipContext(bee.position, next.add(bee.velocity.normalize().scale(.12)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, swarm));
            if (hit.getType() != HitResult.Type.MISS) {
                Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
                bee.velocity = bee.velocity.subtract(normal.scale(bee.velocity.dot(normal))).scale(.5).add(normal.scale(.025));
                next = bee.position; bee.retarget = 0;
            }
            bee.position = next;
        }
        lastState = state;
    }
}
