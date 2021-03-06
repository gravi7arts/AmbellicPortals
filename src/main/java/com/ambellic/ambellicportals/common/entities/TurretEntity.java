package com.ambellic.ambellicportals.common.entities;

import com.ambellic.ambellicportals.common.init.APEntities;
import com.ambellic.ambellicportals.common.init.APSounds;
import com.ambellic.ambellicportals.common.init.APSource;
import com.ambellic.ambellicportals.util.EntityHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static com.ambellic.ambellicportals.common.init.APSounds.TURRET_FOUND;

public class TurretEntity extends CreatureEntity {

    private static final DataParameter<Float> LASER_LENGTH = EntityDataManager.createKey(ZombieEntity.class, DataSerializers.FLOAT);

    public TurretEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerData() {
        super.registerData();
        getDataManager().register(LASER_LENGTH, 0F);
    }

    /* Used for setting the current laser length, please do not update this manually, it gets updated every tick */
    private void setLaserLength(float length){
        getDataManager().set(LASER_LENGTH, length);
    }

    /* Returns the turrets current laser length */
    public float getLaserLength(){
        return getDataManager().get(LASER_LENGTH);
    }

    /* Called consistently in tick to update the laser length */
    private void updateLaserSight(){
        RayTraceResult lookingAt = EntityHelper.getPosLookingAt(this, 60);
        float length = 0;

        if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) lookingAt;
            BlockPos lookingAtPos = blockRayTraceResult.getPos();
            length = (float) getEyePosition(1).distanceTo(new Vec3d(lookingAtPos));
        }
        setLaserLength(length);
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 0.95F * sizeIn.height;
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return getCustomName() != APEntities.SENTRY_TURRET.get().getName();
    }


    @Override
    public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setAttackTarget(entitylivingbaseIn);
        if (entitylivingbaseIn == null) {

        } else {
            playSound(TURRET_FOUND.get(), 1, 1);
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateLaserSight();

        RayTraceResult rayTraceResult = EntityHelper.getPosLookingAt(this, 60);
        if (rayTraceResult != null && rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
            EntityRayTraceResult entityRayTraceResult = (EntityRayTraceResult) rayTraceResult;
            Entity living = entityRayTraceResult.getEntity();
            if (living instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) living;
                if (ticksExisted % 20 == 0) {
                    setAttackTarget(livingEntity);
                }
                livingEntity.attackEntityFrom(APSource.TURRET, 1);
                if (ticksExisted % 5 == 0) {
                    playSound(APSounds.TURRET_FIRE.get(), 1, 1);
                }
            }
        } else {
            if (getAttackTarget() != null) {
                setAttackTarget(null);
            }
        }


    }

}
