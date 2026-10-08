package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.mixin.aw;

import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("jumping")
    boolean isJumping();

}
