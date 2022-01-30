/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/ .
 */
package com.github.crashdemons.wgitemframebreakflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class for WGItemFrameBreakFlagsPlugin
 *
 * @author crash
 */
public class WGItemFrameBreakFlagsPlugin extends JavaPlugin implements Listener {
    public WGItemFrameBreakFlagsPlugin instance = null;
    
    WorldGuardPlugin wgp = null;
    WorldGuard wg = null;
    
    private static final String SUFFIX_DESTROY = "-destroy";
    private static final EntityType[] SUPPORTED_HANGING_ENTITIES = new EntityType[]{
        EntityType.ITEM_FRAME,EntityType.PAINTING, EntityType.GLOW_ITEM_FRAME
    };
    
    private static final HashMap<HangingBreakEvent.RemoveCause,HashMap<EntityType,StateFlag>> causeFlagMap = new HashMap<>();
    private static final ArrayList<StateFlag> flags = new ArrayList<>();
   
    
    static{
        for(HangingBreakEvent.RemoveCause cause : HangingBreakEvent.RemoveCause.values()){
            if(cause==HangingBreakEvent.RemoveCause.ENTITY) continue;//entity-item-frame-destroy already handled by WorldGuard.
            HashMap<EntityType,StateFlag> entityMap = new HashMap<>();
            for(EntityType hangingType : SUPPORTED_HANGING_ENTITIES){
                if(hangingType==EntityType.GLOW_ITEM_FRAME) continue;
                String flagName = getFlagName(cause,hangingType);
                StateFlag flag = new StateFlag(flagName,true);
                entityMap.put(hangingType, flag);
                flags.add(flag);
            }
            causeFlagMap.put(cause, entityMap);
        }
    }
    
    private static String getCauseName(HangingBreakEvent.RemoveCause cause){
        return cause.name().toLowerCase().replace("_", "-");
    }
    private static String getEntityName(EntityType hangingType){
        if(hangingType==EntityType.GLOW_ITEM_FRAME) hangingType = EntityType.ITEM_FRAME;
        return hangingType.name().toLowerCase().replace("_", "-");
    }
    private static String getFlagName(HangingBreakEvent.RemoveCause cause, EntityType hangingType){
        return getCauseName(cause)+"-"+getEntityName(hangingType)+SUFFIX_DESTROY;
    }
    
    private static StateFlag getRelevantFlag(HangingBreakEvent.RemoveCause cause, EntityType hangingType){
        if(hangingType==EntityType.GLOW_ITEM_FRAME) hangingType = EntityType.ITEM_FRAME;
        HashMap<EntityType,StateFlag> entityFlagMap = causeFlagMap.get(cause);
        if(entityFlagMap==null) return null;
        return entityFlagMap.get(hangingType);
    }
    
    public WorldGuard getWorldGuard(){ return wg; }
    public WorldGuardPlugin getWorldGuardPlugin(){ return wgp; }
    
    private WorldGuardPlugin findWorldGuardPlugin() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }
    
    
    private boolean wgInit(){
        wgp = findWorldGuardPlugin();
        wg = WorldGuard.getInstance();
        if(wgp==null || wg==null){
            return false; 
        }
        
        FlagRegistry registry = wg.getFlagRegistry();
        try {
            // register our flag with the registry
            for(StateFlag flag : flags){
                registry.register(flag);
            }
            return true;
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you may want to re-register with a different name, but this
            // could cause issues with saved flags in region files. it's better
            // to print a message to let the server admin know of the conflict
            getLogger().severe("Could not register WG flags due to a conflict with another plugin");
            return false;
        }
    }
    
    private boolean pluginInit(){
        return true;
    }
    
    
    @Override
    public void onLoad() {
        instance = this;
        if(!wgInit()) return;
        //Do Stuff here
    }

    @Override
    public void onEnable() {
        //Do Stuff here
        //if(getConfig().getBoolean("dothething")){ }
        if(!pluginInit()) return;
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Enabled.");

    }

    @Override
    public void onDisable() {
        //saveConfig();

        //Cleanup here
        getLogger().info("Disabled.");
    }

    /*
    //Example command handler
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("YourCommand")) {
            return false;
        }
        if (!sender.hasPermission("yourplugin.yourpermission")) {
            sender.sendMessage("You do not have permission for this command.");
            return true;
        }

        //Do stuff
        return false;
    }*/

    
    //Example event handler
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent event) {
        Player causePlayer = null;
        Entity causeEntity = null;
        HangingBreakEvent.RemoveCause cause = event.getCause();
        if(cause==HangingBreakEvent.RemoveCause.ENTITY) return;//this is already handled by WorldGuard
        if(event instanceof HangingBreakByEntityEvent){//probably can't run because of the above check anyway
            HangingBreakByEntityEvent eevent = (HangingBreakByEntityEvent) event;
            causeEntity = eevent.getRemover();
            if(causeEntity instanceof Player) causePlayer = (Player) causeEntity;
        }
        
        Hanging hangingEntity = event.getEntity();
        Location loc = hangingEntity.getLocation();
        
        LocalPlayer wgPlayer = causePlayer==null? null : getWorldGuardPlugin().wrapPlayer(causePlayer);
        
        StateFlag flag = getRelevantFlag(cause,hangingEntity.getType());
        if(flag==null){
            this.getLogger().warning("Unknown HangingBreakEvent combination: "+cause.name()+" "+hangingEntity.getType().name());
            return;
        }
        
        com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(loc);
        RegionQuery query = getWorldGuard().getPlatform().getRegionContainer().createQuery();
        StateFlag.State state = query.queryState(wgLoc, wgPlayer, flag);
        StateFlag.State warningstate = StateFlag.State.ALLOW;//query.queryState(wgLoc, wgPlayer, FLAG_ITEM_FRAME_BREAK_WARNING);
        if(state==StateFlag.State.DENY){
           if(warningstate!=StateFlag.State.DENY && causePlayer!=null){
               causePlayer.sendMessage(ChatColor.RED+"Hey! "+ChatColor.GRAY+"Sorry, but you can't break that here.");
           }
           event.setCancelled(true);
        }
    }

}
