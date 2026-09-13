package com.mcextractors.blocks;

import com.mcextractors.blockentities.IronExtractorBlockEntity;
import com.mcextractors.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Iron Extractor Block - processes cobblestone to extract iron nuggets
 * Uses redstone as fuel, has 90% chance to find iron nuggets
 */
public class IronExtractorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public IronExtractorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IronExtractorBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (IronExtractorBlockEntity) blockEntity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IronExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.IRON_EXTRACTOR.get(),
                IronExtractorBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IronExtractorBlockEntity) {
                ((IronExtractorBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 13 : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            Direction facing = state.getValue(FACING);

            // Randomly spawn redstone particles at specific coordinates (x=8-11, z=4, y=0 in pixel coordinates)
            if (random.nextDouble() < 0.3D) {
                // Random X between 8 and 11 pixels (in local block coordinates)
                double localX = (8.0D + random.nextDouble() * 3.0D) / 16.0D; // 8-11 pixels
                double localY = 0.0D / 16.0D;                                 // 0 pixels (base)
                double localZ = 4.0D / 16.0D;                                 // 4 pixels

                // Transform coordinates based on block rotation
                double[] worldCoords = transformCoordinates(localX, localZ, facing);

                // Create redstone dust particle (red color)
                DustParticleOptions dustOptions = new DustParticleOptions(
                    new Vector3f(1.0F, 0.0F, 0.0F), // RGB color (red)
                    1.0F // Size
                );

                level.addParticle(dustOptions,
                    pos.getX() + worldCoords[0],
                    pos.getY() + localY,
                    pos.getZ() + worldCoords[1],
                    0.0D, 0.02D, 0.0D); // Small upward velocity
            }

            // Smoke particles at specific coordinates (x=3, z=8, y=10 in pixel coordinates)
            if (random.nextDouble() < 0.5D) {
                // Local coordinates in pixels
                double localX = 3.0D / 16.0D;   // 3 pixels
                double localY = 10.0D / 16.0D;  // 10 pixels
                double localZ = 8.0D / 16.0D;   // 8 pixels

                // Transform coordinates based on block rotation
                double[] worldCoords = transformCoordinates(localX, localZ, facing);

                // Add campfire-style smoke that rises upward
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + worldCoords[0],
                    pos.getY() + localY,
                    pos.getZ() + worldCoords[1],
                    0.0D, 0.07D, 0.0D); // Upward velocity
            }
        }
    }

    /**
     * Transform local block coordinates to world coordinates based on block rotation
     * @param localX X coordinate in block space (0.0-1.0)
     * @param localZ Z coordinate in block space (0.0-1.0)
     * @param facing Block facing direction
     * @return [worldX, worldZ] transformed coordinates
     */
    private double[] transformCoordinates(double localX, double localZ, Direction facing) {
        double worldX, worldZ;

        switch (facing) {
            case NORTH: // Default orientation (0°)
                worldX = localX;
                worldZ = localZ;
                break;
            case SOUTH: // 180° rotation
                worldX = 1.0D - localX;
                worldZ = 1.0D - localZ;
                break;
            case WEST: // 90° counter-clockwise
                worldX = localZ;
                worldZ = 1.0D - localX;
                break;
            case EAST: // 90° clockwise
                worldX = 1.0D - localZ;
                worldZ = localX;
                break;
            default:
                worldX = localX;
                worldZ = localZ;
                break;
        }

        return new double[]{worldX, worldZ};
    }
}
