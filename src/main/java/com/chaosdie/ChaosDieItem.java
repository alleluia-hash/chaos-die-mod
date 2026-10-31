package com.chaosdie;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The Chaos Die: right-click to roll a d20 and get a random effect.
 * Low rolls hurt, high rolls help, a natural 20 is a jackpot.
 * Sneak while rolling to roll with advantage (best of two).
 */
public class ChaosDieItem extends Item {
    private static final int COOLDOWN_TICKS = 60;

    public ChaosDieItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            return TypedActionResult.success(stack, true);
        }

        ServerWorld serverWorld = (ServerWorld) world;
        ServerPlayerEntity player = (ServerPlayerEntity) user;
        Random random = world.getRandom();

        int roll = random.nextInt(20) + 1;
        boolean advantage = player.isSneaking();
        if (advantage) {
            roll = Math.max(roll, random.nextInt(20) + 1);
        }

        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS, 1.0f, 0.6f + random.nextFloat() * 0.8f);

        String name = applyRoll(roll, serverWorld, player, random);

        Formatting color;
        if (roll == 20) {
            color = Formatting.GOLD;
        } else if (roll >= 15) {
            color = Formatting.GREEN;
        } else if (roll >= 7) {
            color = Formatting.YELLOW;
        } else {
            color = Formatting.RED;
        }

        player.sendMessage(Text.literal("[d20] ").formatted(Formatting.DARK_PURPLE)
                .append(Text.literal("Rolled " + roll + (advantage ? " (advantage)" : "") + ": ")
                        .formatted(Formatting.GRAY))
                .append(Text.literal(name).formatted(color, Formatting.BOLD)), false);

        if (roll <= 6) {
            serverWorld.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getBodyY(0.5), player.getZ(),
                    25, 0.5, 0.6, 0.5, 0.02);
        } else if (roll >= 15) {
            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getBodyY(0.5),
                    player.getZ(), 25, 0.5, 0.6, 0.5, 0.02);
        } else {
            serverWorld.spawnParticles(ParticleTypes.ENCHANT, player.getX(), player.getBodyY(0.8),
                    player.getZ(), 30, 0.5, 0.6, 0.5, 0.5);
        }

        user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(stack, false);
    }

    /** Applies the outcome of a roll and returns its display name. */
    private static String applyRoll(int roll, ServerWorld w, ServerPlayerEntity p, Random r) {
        switch (roll) {
            case 1 -> {
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
                if (bolt != null) {
                    bolt.refreshPositionAfterTeleport(p.getX(), p.getY(), p.getZ());
                    bolt.setCosmetic(true);
                    w.spawnEntity(bolt);
                }
                fx(p, StatusEffects.SLOWNESS, 8, 1);
                fx(p, StatusEffects.BLINDNESS, 3, 0);
                fx(p, StatusEffects.WEAKNESS, 15, 0);
                return "Critical Fail: Smitten";
            }
            case 2 -> {
                fx(p, StatusEffects.NAUSEA, 12, 0);
                fx(p, StatusEffects.SLOWNESS, 12, 0);
                return "Wobbly Knees";
            }
            case 3 -> {
                fx(p, StatusEffects.HUNGER, 15, 2);
                return "Empty Stomach";
            }
            case 4 -> {
                fx(p, StatusEffects.DARKNESS, 12, 0);
                return "Lights Out";
            }
            case 5 -> {
                fx(p, StatusEffects.LEVITATION, 4, 0);
                return "Hold My Drink";
            }
            case 6 -> {
                for (int i = 0; i < 6; i++) {
                    ChickenEntity chicken = EntityType.CHICKEN.create(w);
                    if (chicken == null) {
                        continue;
                    }
                    chicken.refreshPositionAndAngles(
                            p.getX() + (r.nextDouble() - 0.5) * 3.0,
                            p.getY() + 2 + r.nextInt(3),
                            p.getZ() + (r.nextDouble() - 0.5) * 3.0,
                            r.nextFloat() * 360.0f, 0.0f);
                    w.spawnEntity(chicken);
                }
                return "Chicken Rain";
            }
            case 7 -> {
                fx(p, StatusEffects.UNLUCK, 90, 0);
                return "Black Cat";
            }
            case 8 -> {
                fx(p, StatusEffects.WEAKNESS, 25, 0);
                return "Wet Noodle Arms";
            }
            case 9 -> {
                return "Clatter... Nothing Happens";
            }
            case 10 -> {
                give(p, new ItemStack(Items.BREAD, 1));
                return "Pocket Snack";
            }
            case 11 -> {
                fx(p, StatusEffects.SPEED, 30, 1);
                return "Zoomies";
            }
            case 12 -> {
                fx(p, StatusEffects.JUMP_BOOST, 25, 2);
                return "Springs In Your Shoes";
            }
            case 13 -> {
                fx(p, StatusEffects.NIGHT_VISION, 90, 0);
                return "Cat Eyes";
            }
            case 14 -> {
                fx(p, StatusEffects.REGENERATION, 10, 1);
                fx(p, StatusEffects.ABSORPTION, 20, 1);
                return "Second Wind";
            }
            case 15 -> {
                ItemStack[] pool = {
                        new ItemStack(Items.DIAMOND, 2),
                        new ItemStack(Items.EMERALD, 4),
                        new ItemStack(Items.GOLDEN_APPLE, 1),
                        new ItemStack(Items.COOKED_BEEF, 8),
                        new ItemStack(Items.ENDER_PEARL, 2),
                        new ItemStack(Items.IRON_INGOT, 8),
                        new ItemStack(Items.GOLD_INGOT, 6),
                        new ItemStack(Items.EXPERIENCE_BOTTLE, 5)
                };
                give(p, pool[r.nextInt(pool.length)].copy());
                return "Treasure Roll";
            }
            case 16 -> {
                for (int i = 0; i < 8; i++) {
                    w.spawnEntity(new ExperienceOrbEntity(w,
                            p.getX() + (r.nextDouble() - 0.5), p.getY() + 1.5, p.getZ() + (r.nextDouble() - 0.5), 6));
                }
                return "XP Shower";
            }
            case 17 -> {
                fx(p, StatusEffects.FIRE_RESISTANCE, 180, 0);
                fx(p, StatusEffects.WATER_BREATHING, 180, 0);
                return "Diver's Blessing";
            }
            case 18 -> {
                fx(p, StatusEffects.STRENGTH, 20, 1);
                fx(p, StatusEffects.RESISTANCE, 20, 1);
                return "Battle Fury";
            }
            case 19 -> {
                fx(p, StatusEffects.LUCK, 300, 1);
                fx(p, StatusEffects.HASTE, 120, 1);
                return "Fortune's Favor";
            }
            default -> {
                give(p, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                give(p, new ItemStack(Items.DIAMOND, 3));
                fx(p, StatusEffects.REGENERATION, 8, 2);
                fx(p, StatusEffects.ABSORPTION, 30, 3);
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);
                w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getBodyY(0.5), p.getZ(),
                        60, 0.6, 0.8, 0.6, 0.4);
                return "NATURAL 20!";
            }
        }
    }

    private static void fx(ServerPlayerEntity p, StatusEffect effect, int seconds, int amplifier) {
        p.addStatusEffect(new StatusEffectInstance(effect, seconds * 20, amplifier));
    }

    private static void give(ServerPlayerEntity p, ItemStack stack) {
        if (!p.getInventory().insertStack(stack)) {
            p.dropItem(stack, false);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.chaosdie.chaos_die.tooltip").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.chaosdie.chaos_die.tooltip2").formatted(Formatting.DARK_PURPLE));
    }
                                                         }
            
