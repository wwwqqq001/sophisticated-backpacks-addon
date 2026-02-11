package com.example.sbpaddon.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public class StoreLogic {

    public static void transferItemsToBackpack(ServerPlayer player, AbstractContainerMenu container) {
        if (container instanceof InventoryMenu) {
            return;
        }

        ItemStack backpackStack = findBackpack(player);
        if (backpackStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c未找到背包，请穿戴或在背包栏位中放置背包！"), true);
            return;
        }

        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        int movedCount = 0;
        for (Slot slot : container.slots) {
            if (slot.container == player.getInventory()) {
                continue;
            }

            if (slot.hasItem() && slot.mayPickup(player)) {
                ItemStack stack = slot.getItem();
                ItemStack remaining = backpackItem.stash(backpackStack, stack);
                
                if (remaining.getCount() < stack.getCount()) {
                    movedCount++;
                    slot.set(remaining);
                }
            }
        }

        if (movedCount > 0) {
            container.broadcastChanges();
            player.sendSystemMessage(Component.literal("§a⬇ 已存入 " + movedCount + " 组物品到背包"), true);
        } else {
            player.sendSystemMessage(Component.literal("§7没有可存入的物品或背包已满"), true);
        }
    }

    private static ItemStack findBackpack(ServerPlayer player) {
        // 1. Check Curios
        Optional<ItemStack> curioBackpack = CuriosApi.getCuriosHelper().findFirstCurio(player, stack -> stack.getItem() instanceof BackpackItem)
                .map(slotResult -> slotResult.stack());
        
        if (curioBackpack.isPresent()) return curioBackpack.get();

        // 2. Check Inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackpackItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
