package com.shieldstatuses;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Keeps track of which shields are disabled.
 *
 *  - YOUR shield:   read from the shield cooldown (the server always sends it).
 *  - OTHER players: marked disabled for 5 seconds when
 *        (a) the game plays the shield-break sound at them, or
 *        (b) YOU hit them with an axe while they are blocking.
 *  - Some servers (1.21 via ViaVersion, plugins) send only the cooldown and never the
 *    shield-break sound. In that case we play the vanilla sound ourselves.
 *
 * The public static methods findHolder / disabledAmount are used by ShieldRendererHook.
 */
public class ShieldTracker {

    public static final int DISABLE_TICKS = 100;
    private static final int SOUND_WAIT_TICKS = 3;

    /** other player -> client tick when their shield is ready again */
    private static final Map<Entity, Long> DISABLED = new WeakHashMap<>();
    /** player -> client tick at which a disable happened and we still have to check the sound */
    private static final Map<Entity, Long> SOUND_CHECK = new WeakHashMap<>();

    private static long ticks = 0L;
    private static long lastBreakSoundTick = -1000L;
    private static float lastOwnCooldown = 0.0F;
    private static PlayerEntity rendering = null;

    // ------------------------------------------------------------------ events

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ticks++;
        DISABLED.values().removeIf(expiry -> expiry <= ticks);

        PlayerEntity me = Minecraft.getInstance().player;
        if (me == null) {
            lastOwnCooldown = 0.0F;
            SOUND_CHECK.clear();
            return;
        }

        // my own shield just got disabled (cooldown jumped from 0 to full)
        float cooldown = me.getCooldowns().getCooldownPercent(Items.SHIELD, 0.0F);
        if (lastOwnCooldown <= 0.01F && cooldown > 0.5F) {
            SOUND_CHECK.put(me, ticks);
        }
        lastOwnCooldown = cooldown;

        // play the break sound ourselves if the server did not send it
        if (!SOUND_CHECK.isEmpty()) {
            Iterator<Map.Entry<Entity, Long>> it = SOUND_CHECK.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Entity, Long> entry = it.next();
                long start = entry.getValue();
                if (ticks - start < SOUND_WAIT_TICKS) {
                    continue;
                }
                it.remove();
                if (lastBreakSoundTick < start - 2L) {
                    entry.getKey().handleEntityEvent((byte) 30); // vanilla "shield break" sound
                }
            }
        }
    }

    /** Remember that the game really played a shield-break sound (so we never double it). */
    @SubscribeEvent
    public static void onSoundPlayed(PlaySoundEvent event) {
        String name = event.getName();
        if (name != null && name.contains("shield.break")) {
            lastBreakSoundTick = ticks;
        }
    }

    /** The game plays shield-break at another player -> his shield is disabled. */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onSoundAtEntity(PlaySoundAtEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof AbstractClientPlayerEntity) || entity == Minecraft.getInstance().player) {
            return;
        }
        if (isShieldBreak(event.getSound())) {
            DISABLED.put(entity, ticks + DISABLE_TICKS);
        }
    }

    /** I hit another player with an axe while he is blocking -> his shield gets disabled. */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        PlayerEntity me = Minecraft.getInstance().player;
        if (me == null || event.getPlayer() != me) {
            return;
        }
        Entity target = event.getTarget();
        if (!(target instanceof AbstractClientPlayerEntity) || target == me) {
            return;
        }
        if (!(me.getMainHandItem().getItem() instanceof AxeItem)) {
            return;
        }
        if (!((LivingEntity) target).isBlocking() || DISABLED.containsKey(target)) {
            return;
        }
        DISABLED.put(target, ticks + DISABLE_TICKS);
        SOUND_CHECK.put(target, ticks);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        DISABLED.clear();
        SOUND_CHECK.clear();
        lastOwnCooldown = 0.0F;
        rendering = null;
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        rendering = event.getPlayer();
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        rendering = null;
    }

    // ------------------------------------------------------------------ used by the renderer

    /** Which player does this shield stack belong to? (null = do not tint) */
    public static PlayerEntity findHolder(ItemStack stack, boolean gui) {
        if (rendering != null) {
            return rendering;
        }
        PlayerEntity me = Minecraft.getInstance().player;
        if (me == null) {
            return null;
        }
        if (stack == me.getMainHandItem() || stack == me.getOffhandItem()) {
            if (gui && !ShieldConfig.SHOW_IN_GUI.get()) {
                return null;
            }
            return me;
        }
        return null;
    }

    /**
     * -1 = do not tint, 0 = shield is ready (green), 1 = just disabled (red),
     * values in between = recovering.
     */
    public static float disabledAmount(PlayerEntity player, float partialTicks) {
        if (player == null) {
            return -1.0F;
        }
        if (player == Minecraft.getInstance().player) {
            return player.getCooldowns().getCooldownPercent(Items.SHIELD, partialTicks);
        }
        if (!ShieldConfig.SHOW_OTHER_PLAYERS.get()) {
            return -1.0F;
        }
        Long expiry = DISABLED.get(player);
        if (expiry == null) {
            return 0.0F;
        }
        float remaining = (float) (expiry - ticks) - partialTicks;
        if (remaining <= 0.0F) {
            return 0.0F;
        }
        return Math.min(1.0F, remaining / (float) DISABLE_TICKS);
    }

    // ------------------------------------------------------------------ helpers

    private static boolean isShieldBreak(SoundEvent sound) {
        return sound != null
                && sound.getRegistryName() != null
                && "item.shield.break".equals(sound.getRegistryName().getPath());
    }
}
