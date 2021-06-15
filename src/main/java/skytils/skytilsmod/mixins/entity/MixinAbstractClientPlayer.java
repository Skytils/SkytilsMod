/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.mixins.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import kotlin.collections.CollectionsKt;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.utils.Utils;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer {
    public MixinAbstractClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    private static final ResourceLocation sychicSkin = new ResourceLocation("skytils:sychicskin.png");
    private static final String phoenixSkinObject = "eyJ0aW1lc3RhbXAiOjE1NzU0NzAyNzE3MTUsInByb2ZpbGVJZCI6ImRlNTcxYTEwMmNiODQ4ODA4ZmU3YzlmNDQ5NmVjZGFkIiwicHJvZmlsZU5hbWUiOiJNSEZfTWluZXNraW4iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM2YTAzODNhNTI3ODAzZDk5YjY2MmFkMThiY2FjNzhjMTE5MjUwZWJiZmIxNDQ3NWI0ZWI0ZDRhNjYyNzk2YjQifX19";

    private Boolean isSummonMob = null;

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("RETURN"), cancellable = true)
    private void replaceSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        if (isSummonMob()) cir.setReturnValue(sychicSkin);
    }

    @Inject(method = "hasSkin", at = @At("RETURN"), cancellable = true)
    private void replaceHasSkin(CallbackInfoReturnable<Boolean> cir) {
        if (isSummonMob()) cir.setReturnValue(true);
    }

    @Inject(method = "getSkinType", at = @At("RETURN"), cancellable = true)
    private void replaceSkinType(CallbackInfoReturnable<String> cir) {
        if (isSummonMob()) cir.setReturnValue("slim");
    }

    private boolean isSummonMob() {
        if (!Utils.inSkyblock) return false;
        try {
            if (isSummonMob == null) {
                if ("Lost Adventurer".equals(this.getName())) {
                    Property textures = CollectionsKt.firstOrNull(this.getGameProfile().getProperties().get("textures"));
                    if (textures != null) {
                        isSummonMob = phoenixSkinObject.equals(textures.getValue());
                    }
                }
            }
        } catch(Exception e) {
            isSummonMob = false;
        }
        if (isSummonMob == null) isSummonMob = false;
        return isSummonMob;
    }
}
