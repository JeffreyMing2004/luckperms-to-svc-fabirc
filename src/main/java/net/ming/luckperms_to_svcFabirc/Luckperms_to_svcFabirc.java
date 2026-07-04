package net.ming.luckperms_to_svcFabirc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

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
            Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method getMethod = luckPermsProviderClass.getMethod("get");
            Object api = getMethod.invoke(null);

            Method getGroupManagerMethod = api.getClass().getMethod("getGroupManager");
            Object groupManager = getGroupManagerMethod.invoke(api);

            Method getGroupMethod = groupManager.getClass().getMethod("getGroup", String.class);
            Object defaultGroup = getGroupMethod.invoke(groupManager, "default");

            if (defaultGroup == null) {
                LOGGER.error("无法找到 default 权限组");
                return;
            }

            Method dataMethod = defaultGroup.getClass().getMethod("data");
            Object data = dataMethod.invoke(defaultGroup);

            Class<?> permissionNodeClass = Class.forName("net.luckperms.api.node.types.PermissionNode");
            Method builderMethod = permissionNodeClass.getMethod("builder", String.class);
            Object builder1 = builderMethod.invoke(null, "voicechat.speak");
            Object builder2 = builderMethod.invoke(null, "voicechat.listen");

            Method buildMethod = builder1.getClass().getMethod("build");
            Object node1 = buildMethod.invoke(builder1);
            Object node2 = buildMethod.invoke(builder2);

            Method addMethod = data.getClass().getMethod("add", Class.forName("net.luckperms.api.node.Node"));
            addMethod.invoke(data, node1);
            addMethod.invoke(data, node2);

            Method saveGroupMethod = groupManager.getClass().getMethod("saveGroup", defaultGroup.getClass());
            saveGroupMethod.invoke(groupManager, defaultGroup);

            LOGGER.info("已为 default 权限组设置 voicechat.speak 和 voicechat.listen 权限");
        } catch (Exception e) {
            LOGGER.error("设置默认权限时出错: " + e.getMessage());
        }
    }
}
