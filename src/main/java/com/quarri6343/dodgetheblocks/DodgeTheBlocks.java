package com.quarri6343.dodgetheblocks;

import com.kamesuta.physxmc.DisplayedPhysxBox;
import com.kamesuta.physxmc.PhysxMc;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public final class DodgeTheBlocks extends JavaPlugin implements Listener {

    public static Location generatorPos1;
    public static Location generatorPos2;
    public static Location generatorDirection;
    
    private static boolean isActive;
    
    public static final Random random = new Random();
    
    public static List<BukkitTask> activeBlockTasks = new ArrayList<>();
    
    public static int frequency = 20;
    
    public static boolean doShrinkPlatform = false;
    
    public static Location platformPos1;
    public static Location platformPos2;
    
    public static Config config;
    
    public static DodgeTheBlocks instance;
    
    private static Map<Location, BlockData> savedPlatform = new HashMap<>();
    
    public DodgeTheBlocks(){
        instance = this;
    }
    
    @Override
    public void onEnable() {
        PhysxMc.playerTriggerHolder.playerTriggerReceivers.add(this::onPlayerEnterBox);
        config = new Config();
        config.loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        
        new DTBCommands();
        getServer().getPluginManager().registerEvents(new DTBCommands(), this);
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if(!isActive)
                    return;
                
                count++;
                if(count % frequency == 0)
                    launch();
                
                if(!doShrinkPlatform)
                    return;
                
                if(count % 60 == 0)
                    doShrink(count / 60);
            }
        }.runTaskTimer(this, 1, 1);
    }

    @Override
    public void onDisable(){
        if(isActive)
            deActivate();
        
        config.saveConfig();
        restorePlatform();
    }
    
    public static void activate(){
        isActive = true;
        savePlatform();
    }
    
    public static void deActivate(){
        isActive = false;
        PhysxMc.displayedBoxHolder.destroyAll();
        activeBlockTasks.forEach(BukkitTask::cancel);
        restorePlatform();
    }
    
    public static boolean getIsActive(){
        return isActive;
    }
    
    private void launch(){
        if(generatorPos1 == null || generatorPos2 == null || generatorDirection == null)
            return;
        double x, y, z;
        
        if(generatorPos1.x() > generatorPos2.x()){
            x = random.nextDouble(generatorPos2.x(), generatorPos1.x());
        }
        else if(generatorPos1.x() < generatorPos2.x()){
            x = random.nextDouble(generatorPos1.x(), generatorPos2.x());
        }
        else{
            x = generatorPos1.x();
        }
        
        if(generatorPos1.y() > generatorPos2.y()){
            y = random.nextDouble(generatorPos2.y(), generatorPos1.y());
        }
        else if(generatorPos1.y() < generatorPos2.y()){
            y = random.nextDouble(generatorPos1.y(), generatorPos2.y());
        }
        else{
            y = generatorPos1.y();
        }

        if(generatorPos1.z() > generatorPos2.z()){
            z = random.nextDouble(generatorPos2.z(), generatorPos1.z());
        }
        else if(generatorPos1.z() < generatorPos2.z()){
            z = random.nextDouble(generatorPos1.z(), generatorPos2.z());
        }
        else{
            z = generatorPos1.z();
        }

        Material material;
        do {
            material = Material.values()[random.nextInt(0, Material.values().length)];
        }while(!material.isSolid() || material == Material.BARRIER || material == Material.AIR);

        Location launchLocation = new Location(generatorDirection.getWorld(), x,y,z);
        launchLocation.setDirection(getRandomDirection());
        Vector scale = new Vector(random.nextInt(2,6),random.nextInt(2,6),random.nextInt(2,6));
        ItemStack itemStack = new ItemStack(material);
        DisplayedPhysxBox box = PhysxMc.displayedBoxHolder.createDisplayedBox(launchLocation, scale, itemStack);
        
        box.throwBox(generatorDirection);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                PhysxMc.displayedBoxHolder.destroySpecific(box);
            }
        }.runTaskLater(this, 100);
        activeBlockTasks.add(task);
    }
    
    private Vector getRandomDirection(){
        double x = random.nextDouble() * 2.0 - 1.0;
        double y = random.nextDouble() * 2.0 - 1.0;
        double z = random.nextDouble() * 2.0 - 1.0; 

        // 方向ベクトルを正規化
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;
        
        return new Vector(x,y,z);
    }
    
    //箱と接触したプレイヤーを弾き飛ばす
    private void onPlayerEnterBox(Player player, DisplayedPhysxBox box){
        if(!isActive)
            return;
        
        Location playerPushLocation = player.getLocation().clone().subtract(box.getLocation());
        Vector playerPushVector = new Vector(playerPushLocation.x(), playerPushLocation.y(), playerPushLocation.z());
        playerPushVector.normalize();
        player.setVelocity(playerPushVector.multiply(1.5f));
    }

    @org.bukkit.event.EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(isActive)
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    /**
     * 足場を縮小させる
     * @param level 縮小する段階
     */
    private void doShrink(int level){
        if(platformPos1 == null || platformPos2 == null)
            return;

        World world = platformPos1.getWorld();
        
        int beginX, endX, beginZ, endZ;
        if(platformPos1.getBlockX() > platformPos2.getBlockX()){
            beginX = platformPos2.getBlockX();
            endX = platformPos1.getBlockX();
        }
        else{
            beginX = platformPos1.getBlockX();
            endX = platformPos2.getBlockX();
        }
        if(platformPos1.getBlockZ() > platformPos2.getBlockZ()){
            beginZ = platformPos2.getBlockZ();
            endZ = platformPos1.getBlockZ();
        }
        else{
            beginZ = platformPos1.getBlockZ();
            endZ = platformPos2.getBlockZ();
        }
        
        if(endX - beginX > 100 || endZ - beginZ > 100){
            return; //消す足場が広すぎるのは多分指定が間違っている
        }
        if(beginX + level * 2 > endX || beginZ + level * 2 > endZ){
            return; //これ以上消す足場がない
        }

        for (int i = beginX; i <= endX; i++) {
            for (int j = -64; j < 256; j++) {
                for (int k = beginZ; k < endZ; k++) {
                    if ((i >= beginX + level && i <= endX - level) && (k >= beginZ + level && k <= endZ - level))
                        continue;
                    
                    world.setBlockData(i,j,k, Material.AIR.createBlockData());
                }
            }
        }
    }

    /**
     * 足場を保存する
     */
    private static void savePlatform(){
        if(platformPos1 == null || platformPos2 == null || !doShrinkPlatform)
            return;

        World world = platformPos1.getWorld();
        savedPlatform = new HashMap<>();

        int beginX, endX, beginZ, endZ;
        if(platformPos1.getBlockX() > platformPos2.getBlockX()){
            beginX = platformPos2.getBlockX();
            endX = platformPos1.getBlockX();
        }
        else{
            beginX = platformPos1.getBlockX();
            endX = platformPos2.getBlockX();
        }
        if(platformPos1.getBlockZ() > platformPos2.getBlockZ()){
            beginZ = platformPos2.getBlockZ();
            endZ = platformPos1.getBlockZ();
        }
        else{
            beginZ = platformPos1.getBlockZ();
            endZ = platformPos2.getBlockZ();
        }

        if(endX - beginX > 100 || endZ - beginZ > 100){
            return; //保存する足場が広すぎるのは多分指定が間違っている
        }

        for (int i = beginX; i <= endX; i++) {
            for (int j = -64; j < 256; j++) {
                for (int k = beginZ; k < endZ; k++) {
                    Location loc = new Location(world, i,j,k);
                    savedPlatform.put(loc, world.getBlockData(loc).clone());
                }
            }
        }
    }

    /**
     * 足場を復元する
     */
    private static void restorePlatform(){
        if(platformPos1 == null || platformPos2 == null || !doShrinkPlatform)
            return;
        
        savedPlatform.forEach((location, blockData) -> {
            location.getWorld().setBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockData);
        });
    }
}