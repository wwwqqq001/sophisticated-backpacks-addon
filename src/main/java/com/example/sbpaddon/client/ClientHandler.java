package com.example.sbpaddon.client;

import com.example.sbpaddon.SophisticatedBackpacksAddon;
import com.example.sbpaddon.network.PacketHandler;
import com.example.sbpaddon.network.PacketStoreAll;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class ClientHandler {

    public static final KeyMapping KEY_STORE_ALL = new KeyMapping(
            "key.sbpaddon.store_all",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.sbpaddon"
    );

    private static Field leftPosField;
    private static Field topPosField;
    private static Field imageWidthField;
    private static Field imageHeightField;

    static {
        try {
            // 1.19.2 SRG 字段名
            leftPosField = ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97735_");
            topPosField = ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97736_");
            imageWidthField = ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97726_");
            imageHeightField = ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97727_");
        } catch (Exception e) {
            try {
                // 开发环境回退
                leftPosField = AbstractContainerScreen.class.getDeclaredField("leftPos");
                leftPosField.setAccessible(true);
                topPosField = AbstractContainerScreen.class.getDeclaredField("topPos");
                topPosField.setAccessible(true);
                imageWidthField = AbstractContainerScreen.class.getDeclaredField("imageWidth");
                imageWidthField.setAccessible(true);
                imageHeightField = AbstractContainerScreen.class.getDeclaredField("imageHeight");
                imageHeightField.setAccessible(true);
            } catch (Exception ignored) {}
        }
    }

    @Mod.EventBusSubscriber(modid = SophisticatedBackpacksAddon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(KEY_STORE_ALL);
        }
    }

    @Mod.EventBusSubscriber(modid = SophisticatedBackpacksAddon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (!(mc.screen instanceof AbstractContainerScreen)) return;
            if (mc.screen instanceof InventoryScreen || mc.screen instanceof CreativeModeInventoryScreen) return;

            if (KEY_STORE_ALL.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
                if (mc.screen.getFocused() instanceof EditBox editBox && editBox.isFocused()) {
                    return; 
                }
                triggerStore();
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) return;
                
                String screenClassName = screen.getClass().getSimpleName();
                if (screenClassName.contains("Backpack")) {
                    return; // Don't show in backpack itself
                }

                int guiLeft = 0;
                int guiTop = 0;
                int xSize = 176;
                int ySize = 166;

                try {
                    if (leftPosField != null) guiLeft = leftPosField.getInt(screen);
                    if (topPosField != null) guiTop = topPosField.getInt(screen);
                    if (imageWidthField != null) xSize = imageWidthField.getInt(screen);
                    if (imageHeightField != null) ySize = imageHeightField.getInt(screen);
                } catch (Exception e) {
                    guiLeft = (screen.width - 176) / 2;
                    guiTop = (screen.height - 166) / 2;
                }

                int maxY = guiTop + ySize;
                if (screen.getMenu() != null && !screen.getMenu().slots.isEmpty()) {
                    for (Slot slot : screen.getMenu().slots) {
                        int slotBottom = guiTop + slot.y + 18;
                        if (slotBottom > maxY) {
                            maxY = slotBottom;
                        }
                    }
                }

                int btnWidth = 60;
                int btnHeight = 18;
                int btnX = guiLeft + (xSize / 2) - (btnWidth / 2);
                int btnY = maxY + 4;

                event.addListener(new Button(btnX, btnY, btnWidth, btnHeight, Component.literal("存入背包"), b -> triggerStore()));
            }
        }
    }

    private static void triggerStore() {
        if (Minecraft.getInstance().player != null) {
            PacketHandler.INSTANCE.sendToServer(new PacketStoreAll());
        }
    }
}
