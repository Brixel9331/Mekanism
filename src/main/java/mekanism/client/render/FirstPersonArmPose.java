package mekanism.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public final class FirstPersonArmPose<T extends LivingEntity> {

    private final HumanoidModel<T> model = new HumanoidModel<>(LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 64).bakeRoot());

    public HumanoidModel<T> prepare(HumanoidModel<T> source, T entity, HumanoidArm arm) {
        source.copyPropertiesTo(model);
        if (arm == HumanoidArm.RIGHT) {
            model.rightArmPose = ArmPose.EMPTY;
        } else {
            model.leftArmPose = ArmPose.EMPTY;
        }
        model.attackTime = 0;
        model.crouching = false;
        model.swimAmount = 0;
        model.setupAnim(entity, 0, 0, 0, 0, 0);
        return model;
    }
}
