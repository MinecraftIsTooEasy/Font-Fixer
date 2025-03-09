package de.thexxturboxx.blockhelper.mixin;

import java.util.HashMap;
import java.util.Map;

import de.thexxturboxx.blockhelper.FontFixer;
import net.minecraft.FontRenderer;
import net.minecraft.GameSettings;
import net.minecraft.ResourceLocation;
import net.minecraft.TextureManager;
import net.xiaoyu233.fml.util.ReflectHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FontRenderer.class})
public class FontRendererMixin {
    private static final Map<FontRenderer, FontFixer> registeredFixers = new HashMap();

    @Inject(method = {"<init>"}, at = {@At("TAIL")})
    private void init2(GameSettings par1GameSettings, ResourceLocation par2ResourceLocation, TextureManager par3TextureManager, boolean par4, CallbackInfo ci) {
        registeredFixers.put(ReflectHelper.dyCast(this), new FontFixer(par1GameSettings, par2ResourceLocation, par3TextureManager, par4));
    }

    @ModifyConstant(method = {"<init>"}, constant = {@Constant(intValue = 256)})
    private int modifyChanceTableSize(int val) {
        return Short.MAX_VALUE;
    }

    @Overwrite
    public int drawStringWithShadow(String text, int x, int y, int color) {
        return (registeredFixers.get(ReflectHelper.dyCast(this))).drawStringWithShadow(text, x, y, color);
    }

    @Overwrite
    public int drawString(String string, int i, int j, int k) {
        return (registeredFixers.get(ReflectHelper.dyCast(this))).drawString(string, i, j, k);
    }

    @Overwrite
    public int getStringWidth(String text) {
        return (registeredFixers.get(ReflectHelper.dyCast(this))).getStringWidth(text);
    }

    @Overwrite
    public int getCharWidth(char c) {
        return (registeredFixers.get(ReflectHelper.dyCast(this))).getCharWidth(c);
    }

//    @Overwrite
//    public String trimStringToWidth(String text, int width) {
//        return (registeredFixers.get(ReflectHelper.dyCast(this))).trimStringToWidth(text, width);
//    }
//
//    @Overwrite
//    public String trimStringToWidth(String text, int width, boolean bl) {
//        return (registeredFixers.get(ReflectHelper.dyCast(this))).trimStringToWidth(text, width, bl);
//    }

//    @Overwrite
//    public void drawSplitString(String text, int x, int y, int maxWidth, int color) {
//        (registeredFixers.get(ReflectHelper.dyCast(this))).drawSplitString(text, x, y, maxWidth, color);
//    }

//    @Overwrite
//    public int splitStringWidth(String string, int i) {
//        return (registeredFixers.get(ReflectHelper.dyCast(this))).splitStringWidth(string, i);
//    }

    @Overwrite
    public void setUnicodeFlag(boolean bl) {
        (registeredFixers.get(ReflectHelper.dyCast(this))).setUnicodeFlag(bl);
    }

    @Overwrite
    public void setBidiFlag(boolean rightToLeft) {
        (registeredFixers.get(ReflectHelper.dyCast(this))).setBidiFlag(rightToLeft);
    }

//    @Overwrite
//    public List listFormattedStringToWidth(String text, int width) {
//        return (registeredFixers.get(ReflectHelper.dyCast(this))).listFormattedStringToWidth(text, width);
//    }

//    @Overwrite
//    public String wrapFormattedStringToWidth(String text, int width) {
//        return (registeredFixers.get(ReflectHelper.dyCast(this))).wrapFormattedStringToWidth(text, width);
//    }

//    @Overwrite
//    public boolean getBidiFlag() {
//        return (registeredFixers.get(ReflectHelper.dyCast(this))).getBidiFlag();
//    }
}
