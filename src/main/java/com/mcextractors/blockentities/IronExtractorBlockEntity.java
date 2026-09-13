package com.mcextractors.blockentities;

import com.mcextractors.blocks.IronExtractorBlock;
import com.mcextractors.init.ModBlockEntities;
import com.mcextractors.menu.IronExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Iron Extractor Block Entity
 * Processes cobblestone/cobbled deepslate with redstone fuel to extract iron nuggets (90% chance)
 *
 * Hopper Configuration:
 * - Sides (North/South/East/West): Insert cobblestone/cobbled deepslate AND redstone
 * - Top: No hopper interaction
 * - Bottom: Extract iron nuggets (output)
 */
public class IronExtractorBlockEntity extends BlockEntity implements MenuProvider {
    // Slots: 0=input(cobble), 1=fuel(redstone), 2-6=output(iron nuggets)
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT_START = 2;
    public static final int OUTPUT_SLOT_COUNT = 3;
    public static final int TOTAL_SLOTS = 5;

    // Processing constants
    private static final int PROCESS_TIME = 200; // 10 seconds (200 ticks)
    private static final int FUEL_VALUE = 1600; // redstone dust gives 1600 ticks (80 seconds, like coal)
    private static final float IRON_CHANCE = 0.9F; // 90% chance

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE);
            } else if (slot == FUEL_SLOT) {
                return stack.is(Items.REDSTONE);
            } else {
                return false; // Output slots don't accept items
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> lazyInputHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> lazyFuelHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> lazyOutputHandler = LazyOptional.empty();

    // Processing data
    private int progress = 0;
    private int fuelTime = 0;
    private int maxFuelTime = 0;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> IronExtractorBlockEntity.this.progress;
                case 1 -> IronExtractorBlockEntity.this.fuelTime;
                case 2 -> IronExtractorBlockEntity.this.maxFuelTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> IronExtractorBlockEntity.this.progress = value;
                case 1 -> IronExtractorBlockEntity.this.fuelTime = value;
                case 2 -> IronExtractorBlockEntity.this.maxFuelTime = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public IronExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRON_EXTRACTOR.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mcextractors.iron_extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IronExtractorMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return lazyItemHandler.cast();
            } else if (side == Direction.DOWN) {
                // Bottom: can only extract from output slots
                return lazyOutputHandler.cast();
            } else if (side == Direction.UP) {
                // Top: no hopper interaction
                return super.getCapability(cap, side);
            } else {
                // Sides: can insert into input and fuel slots (cobblestone + redstone)
                return lazyInputHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        // Input handler: slots 0-1 (input + fuel: cobblestone + redstone) - for inserting from sides
        lazyInputHandler = LazyOptional.of(() -> new RangedWrapper(itemHandler, INPUT_SLOT, FUEL_SLOT + 1));
        // Fuel handler: not used anymore but kept for compatibility
        lazyFuelHandler = LazyOptional.of(() -> new RangedWrapper(itemHandler, FUEL_SLOT, FUEL_SLOT + 1));
        // Output handler: slots 2-4 (output only) - for extracting from bottom
        lazyOutputHandler = LazyOptional.of(() -> new RangedWrapper(itemHandler, OUTPUT_SLOT_START, OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyInputHandler.invalidate();
        lazyFuelHandler.invalidate();
        lazyOutputHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("fuelTime", fuelTime);
        tag.putInt("maxFuelTime", maxFuelTime);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        fuelTime = tag.getInt("fuelTime");
        maxFuelTime = tag.getInt("maxFuelTime");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IronExtractorBlockEntity blockEntity) {
        boolean wasLit = state.getValue(IronExtractorBlock.LIT);
        boolean isLit = blockEntity.isBurning();

        // Consume fuel if needed
        if (blockEntity.canProcess() && !blockEntity.isBurning()) {
            blockEntity.consumeFuel();
        }

        // Process item
        if (blockEntity.isBurning() && blockEntity.canProcess()) {
            blockEntity.progress++;
            blockEntity.fuelTime--;

            if (blockEntity.progress >= PROCESS_TIME) {
                blockEntity.processItem();
                blockEntity.progress = 0;
            }
        } else {
            blockEntity.progress = 0;
        }

        // Decrease fuel time
        if (blockEntity.isBurning() && !blockEntity.canProcess()) {
            blockEntity.fuelTime--;
        }

        // Update block state if lit status changed
        isLit = blockEntity.isBurning();
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(IronExtractorBlock.LIT, isLit), 3);
        }

        blockEntity.setChanged();
    }

    private boolean isBurning() {
        return this.fuelTime > 0;
    }

    private boolean canProcess() {
        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty() || (!input.is(Items.COBBLESTONE) && !input.is(Items.COBBLED_DEEPSLATE))) {
            return false;
        }

        // Check if there's space in output slots
        return hasSpaceInOutput();
    }

    private boolean hasSpaceInOutput() {
        for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT; i++) {
            ItemStack outputStack = itemHandler.getStackInSlot(i);
            if (outputStack.isEmpty()) {
                return true;
            }
            if (outputStack.is(Items.IRON_NUGGET) && outputStack.getCount() < outputStack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void consumeFuel() {
        ItemStack fuel = itemHandler.getStackInSlot(FUEL_SLOT);
        if (!fuel.isEmpty() && fuel.is(Items.REDSTONE)) {
            fuel.shrink(1);
            this.fuelTime = FUEL_VALUE;
            this.maxFuelTime = FUEL_VALUE;
            setChanged();
        }
    }

    private void processItem() {
        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return;

        // Consume cobblestone
        input.shrink(1);

        // 90% chance to extract iron nugget
        if (level.random.nextFloat() < IRON_CHANCE) {
            // Try to add iron nugget to output slots
            ItemStack ironNugget = new ItemStack(Items.IRON_NUGGET, 1);
            for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT; i++) {
                ItemStack outputStack = itemHandler.getStackInSlot(i);
                if (outputStack.isEmpty()) {
                    itemHandler.setStackInSlot(i, ironNugget);
                    break;
                } else if (outputStack.is(Items.IRON_NUGGET) && outputStack.getCount() < outputStack.getMaxStackSize()) {
                    outputStack.grow(1);
                    break;
                }
            }
        }

        setChanged();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}
