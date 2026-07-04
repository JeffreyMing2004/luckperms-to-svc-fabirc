package net.ming.luckperms_to_svcFabirc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Luckperms_to_svcFabirc implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("luckperms_to_svc-fabirc");
    private static final String LUCKPERMS_MOD_ID = "luckperms";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!FabricLoader.getInstance().isModLoaded(LUCKPERMS_MOD_ID)) {
                LOGGER.warn("LuckPerms 未安装！此模组需要 LuckPerms 才能正常工作。");
                server.getPlayerManager().getPlayerList().forEach(player -> {
                    player.sendMessage(Text.literal("§c[警告] LuckPerms 未安装！luckperms_to_svc-fabirc 模组需要 LuckPerms 才能正常工作。"), false);
                });
            } else {
                LOGGER.info("LuckPerms 已检测到，模组正常运行。");
            }
        });
    }
}
