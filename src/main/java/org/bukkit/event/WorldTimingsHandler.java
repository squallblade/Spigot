package org.bukkit.event;

import net.minecraft.server.World;

public class WorldTimingsHandler {
    public CustomTimingsHandler mobSpawn;
    public CustomTimingsHandler doTickRest;
    public CustomTimingsHandler entityBaseTick;
    public CustomTimingsHandler entityTick;
    public CustomTimingsHandler tileEntityTick;
    public CustomTimingsHandler activationCheck;
    public WorldTimingsHandler(World server) {
        String name = server.worldData.getName() +" - ";

        mobSpawn       = new CustomTimingsHandler(name + "mobSpawn");
        doTickRest     = new CustomTimingsHandler(name + "doTickRest");
        entityBaseTick = new CustomTimingsHandler(name + "entityBaseTick");
        entityTick     = new CustomTimingsHandler(name + "entityTick");
        tileEntityTick = new CustomTimingsHandler(name + "tileEntityTick");
        activationCheck = new CustomTimingsHandler("** " + name + "activateEntities");
    }
}
