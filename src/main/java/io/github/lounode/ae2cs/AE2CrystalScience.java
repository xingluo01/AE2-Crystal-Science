package io.github.lounode.ae2cs;

import io.github.lounode.ae2cs.api.IngredientReplacer;
import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.api.util.PatternAccessTermQuickMoveHelper;
import io.github.lounode.ae2cs.common.init.*;
import io.github.lounode.ae2cs.common.me.AEPlugin;
import io.github.lounode.ae2cs.integration.patterndisk.PatternDiskSupport;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(AECSConstants.MODID)
public class AE2CrystalScience {

    public static final Logger LOGGER = LogUtils.getLogger();

    public AE2CrystalScience(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        Config.register(modContainer);

        AECSItems.register(modEventBus);
        AECSParts.register(modEventBus);
        AECSBlocks.register(modEventBus);
        AECSBlockEntities.register(modEventBus);
        AECSDataComponents.register(modEventBus);
        AECSCreativeModeTabs.register(modEventBus);
        AECSMenus.registerMenus(modEventBus);
        AECSRecipeTypes.register(modEventBus);
        AECSRecipeSerializers.register(modEventBus);

        AEPlugin.onInit();
        AEPlugin.onRegister(modEventBus, NeoForge.EVENT_BUS);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(
                () -> {
                    AEPlugin.onCommonSetup();
                    PatternAccessTermQuickMoveHelper.init();
                    IngredientReplacer.onCommonSetUp();
                    if (ModList.get().isLoaded(AECSConstants.PATTERN_DISK_ID)) {
                        PatternDiskSupport.registerDiskHosts();
                    }
                });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("AE2CrystalScience - Server started");
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        // The cached terminal views hold the machines and the levels they were built from, so they cannot
        // outlive the server that owned them.
        if (ModList.get().isLoaded(AECSConstants.PATTERN_DISK_ID)) {
            PatternDiskSupport.clearCaches();
        }
    }

    public static ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(AECSConstants.MODID, path);
    }

    public static ResourceLocation parseOrMakeId(String path) {
        if (path.indexOf(':') >= 0) {
            return ResourceLocation.tryParse(path);
        }
        return AE2CrystalScience.makeId(path);
    }
}
