package com.quarri6343.dodgetheblocks;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.kamesuta.physxmc.command.CommandBase;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DTBCommands extends CommandBase implements Listener {

    private static final String commandName = "dtb";
    private static final String generatorArgument = "generatorpos";
    private static final String directionArgument = "generatordirection";
    private static final String toggleActiveArgument = "toggleactive";

    private static final String frequencyArgument = "frequency";
    
    private static final String toggleShrinkArgument = "toggleshrink";
    private static final String platformArgument = "platform";
    private static final String goalArgument = "goal";
    
    private static final List<String> goalColor = List.of("red", "blue", "yellow", "green");

    /**
     * 引数のリスト
     */
    private static final List<String> arguments = List.of(generatorArgument, directionArgument, toggleActiveArgument, frequencyArgument, 
            toggleShrinkArgument, platformArgument, goalArgument);

    public DTBCommands() {
        super(commandName, 1, 2, true);
    }

    @Override
    public boolean onCommand(CommandSender sender, @Nullable String[] arguments) {
        if(arguments[0].equals(generatorArgument)){
            if(arguments.length == 1){
                sendUsage(sender);
                return true;
            }
            
            if(!Objects.equals(arguments[1], "start") && !Objects.equals(arguments[1], "end")){
                sendUsage(sender);
                return true;
            }
            
            if(Objects.equals(arguments[1], "start")){
                DodgeTheBlocks.generatorPos1 = ((Player)sender).getLocation();
                sender.sendMessage("ジェネレータの始点を今立っている位置で設定しました");
            }
            else{
                DodgeTheBlocks.generatorPos2 = ((Player)sender).getLocation();
                sender.sendMessage("ジェネレータの終点を今立っている位置で設定しました");
            }
            return true;
        }
        else if(arguments[0].equals(directionArgument)){
            DodgeTheBlocks.generatorDirection = ((Player)sender).getEyeLocation();
            sender.sendMessage("ジェネレータの向きを今の向きで設定しました");
            return true;
        }
        else if(arguments[0].equals(toggleActiveArgument)){
            if(!DodgeTheBlocks.getIsActive()){
                DodgeTheBlocks.activate();
            }
            else{
                DodgeTheBlocks.deActivate();
            }
            sender.sendMessage("射出を" + (DodgeTheBlocks.getIsActive() ? "有効化" : "無効化") + "しました");
            return true;
        }
        else if(arguments[0].equals(frequencyArgument)){
            if(arguments.length == 1){
                sendUsage(sender);
                return true;
            }
            
            int frequency;
            try{
                frequency = Integer.parseInt(arguments[1]);
            }
            catch (NumberFormatException e){
                sendUsage(sender);
                return true;
            }
            if(frequency <= 0){
                sendUsage(sender);
                return true;
            }
            DodgeTheBlocks.frequency = frequency;
            sender.sendMessage("射出頻度を" + frequency + "tickにしました");
            return true;
        }
        else if(arguments[0].equals(toggleShrinkArgument)){
            if(DodgeTheBlocks.getIsActive()){
                sender.sendMessage("ゲーム中は変更できません");
                return true;
            }
            
            DodgeTheBlocks.doShrinkPlatform = !DodgeTheBlocks.doShrinkPlatform;
            sender.sendMessage("足場縮小を" + (DodgeTheBlocks.doShrinkPlatform ? "有効化" : "無効化") + "しました");
            return true;
        }
        else if(arguments[0].equals(platformArgument)){
            if(arguments.length == 1){
                sendUsage(sender);
                return true;
            }

            if(DodgeTheBlocks.getIsActive()){
                sender.sendMessage("ゲーム中は変更できません");
                return true;
            }

            if(!Objects.equals(arguments[1], "start") && !Objects.equals(arguments[1], "end")){
                sendUsage(sender);
                return true;
            }

            if(Objects.equals(arguments[1], "start")){
                DodgeTheBlocks.platformPos1 = ((Player)sender).getLocation();
                sender.sendMessage("縮小する足場の始点を今立っている位置で設定しました");
            }
            else{
                DodgeTheBlocks.platformPos2 = ((Player)sender).getLocation();
                sender.sendMessage("縮小する足場の終点を今立っている位置で設定しました");
            }
            return true;
        }
        else if(arguments[0].equals(goalArgument)){
            if(arguments.length == 1){
                sendUsage(sender);
                return true;
            }

            for (int i = 0; i < goalColor.size(); i++) {
                if(goalColor.get(i).equals(arguments[1])){
                    Location loc  = ((Player)sender).getLocation();
                    DodgeTheBlocks.createGoalBox(i, loc);
                    sender.sendMessage("ゴールを登録しました");
                    return true;
                }
            }
            
            sendUsage(sender);
            return true;
        }
        
        
        sendUsage(sender);
        return true;
    }

    @Override
    public void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("/dtb generator {start|end}: ブロックジェネレータの始点と終点を設定する\n" +
                "/dtb direction: ブロックジェネレータがブロックを射出する方向を設定する\n" +
                "/dtb toggleactive: 射出を開始/終了する\n" +
                "/dtb frequency {int}: 射出する頻度(tick)を設定する\n" +
                "/dtb toggleshrink : 足場縮小を有効化/無効化する\n" +
                "/dtb platform {start|end}: 縮小する足場の始点と終点を設定する\n"));
    }

    @EventHandler
    public void AsyncTabCompleteEvent(AsyncTabCompleteEvent e) {
        if (e.getBuffer().startsWith("/" + commandName + " ")) {
            List<String> suggestions = new ArrayList<>();
            String pureBuffer = e.getBuffer().replace("/" + commandName + " ", "");
            arguments.forEach(s -> {
                if(s.startsWith(pureBuffer))
                    suggestions.add(s);
            });
            e.setCompletions(suggestions);
        }
    }
}
