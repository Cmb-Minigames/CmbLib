package xyz.devcmb.cmbLib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class CmbLib extends JavaPlugin {
    private static CmbLib instance;
    public static CmbLib getPlugin() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    // Basic utility methods

    /**
     * A countdown title sequence to display to a player
     * @param player The player to display the countdown title to
     * @param totalSeconds The total seconds to countdown
     */
    public static void Countdown(Player player, int totalSeconds){
        new BukkitRunnable(){
            int seconds = totalSeconds;
            @Override
            public void run() {
                if(seconds == 0){
                    this.cancel();

                    Title goTitle = Title.title(
                            Component.text("GO!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                            Component.empty(),
                            Title.Times.times(ticksToMilliseconds(0), ticksToMilliseconds(40), ticksToMilliseconds(10))
                    );

                    player.showTitle(goTitle);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10, 2.5f);
                    return;
                }

                TextColor color = NamedTextColor.WHITE;

                switch(seconds){
                    case 3:
                        color = NamedTextColor.GREEN;
                        break;
                    case 2:
                        color = NamedTextColor.YELLOW;
                        break;
                    case 1:
                        color = NamedTextColor.RED;
                        break;
                    default:
                        break;
                }

                Title countdownTitle = Title.title(
                        Component.text("> " + seconds + " <").color(color).decorate(TextDecoration.BOLD),
                        Component.text("The game will begin shortly"),
                        Title.Times.times(ticksToMilliseconds(0), ticksToMilliseconds(20), ticksToMilliseconds(0))
                );

                player.showTitle(countdownTitle);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10, 1);
                seconds--;
            }
        }.runTaskTimer(getPlugin(), 0, 20);
    }

    /**
     * Fill a range of blocks with a specific block
     * @param fromLocation The starting location
     * @param toLocation The ending location
     * @param fillBlock The block to fill with
     */
    public static void fillBlocks(Location fromLocation, Location toLocation, Material fillBlock){
        World world = fromLocation.getWorld();
        if (world == null || !world.equals(toLocation.getWorld())) {
            throw new IllegalArgumentException("Both locations must be in the same world");
        }

        int minX = Math.min(fromLocation.getBlockX(), toLocation.getBlockX());
        int maxX = Math.max(fromLocation.getBlockX(), toLocation.getBlockX());
        int minY = Math.min(fromLocation.getBlockY(), toLocation.getBlockY());
        int maxY = Math.max(fromLocation.getBlockY(), toLocation.getBlockY());
        int minZ = Math.min(fromLocation.getBlockZ(), toLocation.getBlockZ());
        int maxZ = Math.max(fromLocation.getBlockZ(), toLocation.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(fillBlock);
                }
            }
        }
    }

    /**
     * Get a random element from a list
     * @param list The list to get a random element from
     * @param <T> The type of the list
     * @return A random element from the list
     */
    public static <T> T getRandom(List<T> list){
        Random random = new Random();
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }

    /**
     * Find a valid location to spawn a player at
     * @param spawnLocation The location to check validation for
     * @return A valid location to spawn a player at
     */
    public static Location findValidLocation(Location spawnLocation) {
        Location newLocation = spawnLocation.clone();

        if (!Objects.requireNonNull(newLocation.getWorld()).getNearbyEntities(newLocation, 1, 1, 1).isEmpty()) {
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    if (xOffset == 0 && zOffset == 0) continue;

                    Location checkLocation = newLocation.clone().add(xOffset, 0, zOffset);

                    if (Objects.requireNonNull(checkLocation.getWorld()).getNearbyEntities(checkLocation, 1, 1, 1).isEmpty()) {
                        return checkLocation;
                    }
                }
            }
        }

        return newLocation;
    }

    public static Chest fillChestRandomly(Chest chestData, List<ItemStack> items, Integer min, Integer max) {
        Inventory chestInventory = chestData.getBlockInventory();
        chestInventory.clear();

        Random random = new Random();
        int amount = random.nextInt(max - min + 1) + min;

        List<Integer> slots = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            ItemStack item = getRandom(items);
            int slot;

            do {
                slot = random.nextInt(chestInventory.getSize());
            } while (slots.contains(slot));

            chestInventory.setItem(slot, item);
            slots.add(slot);
        }

        return chestData;
    }

    /**
     * Get all blocks in a radius around a location
     * @param center The center location
     * @param radius The radius to get blocks from
     * @return A list of blocks in the radius
     */
    public static List<Block> getBlocksInRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if(world == null) return List.of();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    /**
     * Format a time in seconds to a string
     * @param time The time in seconds
     * @return The formatted time string
     */
    public static Component formatTime(int time){
        int minutes = time / 60;
        int seconds = time % 60;
        return Component.text(String.format("%02d:%02d", minutes, seconds));
    }

    /**
     * Move an entity to a location over a duration
     * @param entity The entity to move
     * @param newLocation The location to move the entity to
     * @param duration The duration to move the entity over
     */
    public static void moveEntity(Entity entity, Location newLocation, int duration) {
        new BukkitRunnable() {
            private final Location startLocation = entity.getLocation();
            private final double deltaX = (newLocation.getX() - startLocation.getX()) / duration;
            private final double deltaY = (newLocation.getY() - startLocation.getY()) / duration;
            private final double deltaZ = (newLocation.getZ() - startLocation.getZ()) / duration;
            private int ticksElapsed = 0;

            @Override
            public void run() {
                if (ticksElapsed >= duration) {
                    entity.teleport(newLocation);
                    this.cancel();
                    return;
                }

                Location currentLocation = entity.getLocation();

                currentLocation.setYaw(newLocation.getYaw());
                currentLocation.setPitch(newLocation.getPitch());

                currentLocation.add(deltaX, deltaY, deltaZ);
                entity.teleport(currentLocation);

                ticksElapsed++;
            }
        }.runTaskTimer(getPlugin(), 0, 1);
    }

    /**
     * Check if a location is within a range of two other locations
     * @param loc The location to check
     * @param point1 The first point
     * @param point2 The second point
     * @return If the location is within the range
     */
    public static boolean isWithin(Location loc, Location point1, Location point2) {
        double minX = Math.min(point1.getX(), point2.getX());
        double minY = Math.min(point1.getY(), point2.getY());
        double minZ = Math.min(point1.getZ(), point2.getZ());
        double maxX = Math.max(point1.getX(), point2.getX());
        double maxY = Math.max(point1.getY(), point2.getY());
        double maxZ = Math.max(point1.getZ(), point2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getY() >= minY && loc.getY() <= maxY &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public static Duration ticksToMilliseconds(Number ticks) {
        return Duration.ofMillis(ticks.longValue() * 50);
    }
}
