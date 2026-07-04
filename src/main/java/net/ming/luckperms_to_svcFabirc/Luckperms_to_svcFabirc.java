package net.ming.luckperms_to_svcFabirc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.types.PermissionNode;
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
                setupDefaultPermissions();
            }
        });
    }

    private void setupDefaultPermissions() {
        try {
            LuckPerms api = LuckPermsProvider.get();

            Group defaultGroup = api.getGroupManager().getGroup("default");
            if (defaultGroup == null) {
                LOGGER.error("无法找到 default 权限组");
                return;
            }

            defaultGroup.data().add(PermissionNode.builder("voicechat.speak").build());
            defaultGroup.data().add(PermissionNode.builder("voicechat.listen").build());

            api.getGroupManager().saveGroup(defaultGroup);
            LOGGER.info("已为 default 权限组设置 voicechat.speak 和 voicechat.listen 权限");
        } catch (Exception e) {
            LOGGER.error("设置默认权限时出错: " + e.getMessage());
        }
    }
}
