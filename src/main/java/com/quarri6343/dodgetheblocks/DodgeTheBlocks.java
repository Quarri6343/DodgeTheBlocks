package com.quarri6343.dodgetheblocks;

import com.kamesuta.physxmc.DisplayedPhysxBox;
import com.kamesuta.physxmc.PhysxMc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DodgeTheBlocks extends JavaPlugin {

    public static Location generatorPos1;
    public static Location generatorPos2;
    public static Location generatorDirection;
    
    private static boolean isActive;
    
    public static final Random random = new Random();
    
    public static List<BukkitTask> activeBlockTasks = new ArrayList<>();
    
    public static int frequency = 20;
    
    public static Config config;
    
    public static DodgeTheBlocks instance;
    
    public DodgeTheBlocks(){
        instance = this;
    }
    
    @Override
    public void onEnable() {
        PhysxMc.playerTriggerHolder.playerTriggerReceivers.add(this::onPlayerEnterBox);
        config = new Config();
        config.loadConfig();
        
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
            }
        }.runTaskTimer(this, 1, 1);
    }

    @Override
    public void onDisable(){
        if(isActive)
            deActivate();
        
        config.saveConfig();
    }
    
    public static void activate(){
        isActive = true;
    }
    
    public static void deActivate(){
        isActive = false;
        PhysxMc.displayedBoxHolder.destroyAll();
        activeBlockTasks.forEach(BukkitTask::cancel);
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
}