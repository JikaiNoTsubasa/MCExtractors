package com.mcextractors.menu;

import com.mcextractors.blockentities.IronExtractorBlockEntity;
import com.mcextractors.init.ModBlocks;
import com.mcextractors.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Container menu for Iron Extractor GUI
 */
public class IronExtractorMenu extends AbstractContainerMenu {
    private final IronExtractorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // Client constructor
    public IronExtractorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(3));
    }

    // Server constructor
    public IronExtractorMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.IRON_EXTRACTOR.get(), containerId);
        checkContainerSize(playerInventory, IronExtractorBlockEntity.TOTAL_SLOTS);
        this.blockEntity = (IronExtractorBlockEntity) blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        // Add block entity slots
        // Input slot (stones) - position 8, 17
        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), IronExtractorBlockEntity.INPUT_SLOT, 56, 17) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE)
                        || stack.is(Items.DIORITE);
            }
        });

        // Fuel slot (redstone) - position 8, 53
        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), IronExtractorBlockEntity.FUEL_SLOT, 56, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.REDSTONE);
            }
        });

        // Output slots (5 slots) - positions starting at 116, 17
        for (int i = 0; i < IronExtractorBlockEntity.OUTPUT_SLOT_COUNT; i++) {
            int slotIndex = IronExtractorBlockEntity.OUTPUT_SLOT_START + i;
            int x = 116;
            int y = 17 + (i * 18);
            this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), slotIndex, x, y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false; // Output only
                }
            });
        }

        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = 200; // PROCESS_TIME
        int progressArrowSize = 24; // Height of progress arrow in pixels

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getScaledFuelProgress() {
        int fuelTime = this.data.get(1);
        int maxFuelTime = this.data.get(2);
        int fuelBarSize = 14; // Height of fuel bar in pixels

        return maxFuelTime != 0 ? fuelTime * fuelBarSize / maxFuelTime : 0;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            // Slots: 0-35 = player inventory, 36 = input, 37 = fuel, 38-42 = output
            // int containerSlots = IronExtractorBlockEntity.TOTAL_SLOTS;
            int playerInventoryEnd = 36;

            if (index >= playerInventoryEnd) {
                // From container to player inventory
                if (!this.moveItemStackTo(originalStack, 0, playerInventoryEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to container
                if (originalStack.is(Items.COBBLESTONE) || originalStack.is(Items.COBBLED_DEEPSLATE)
                        || originalStack.is(Items.DIORITE)) {
                    // Input stones
                    if (!this.moveItemStackTo(originalStack, playerInventoryEnd, playerInventoryEnd + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (originalStack.is(Items.REDSTONE)) {
                    // Fuel
                    if (!this.moveItemStackTo(originalStack, playerInventoryEnd + 1, playerInventoryEnd + 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 27) {
                    if (!this.moveItemStackTo(originalStack, 27, playerInventoryEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < playerInventoryEnd) {
                    if (!this.moveItemStackTo(originalStack, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, originalStack);
        }

        return newStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.IRON_EXTRACTOR.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
