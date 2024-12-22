package de.thexxturboxx.blockhelper;

import java.lang.String;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.Bidi;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

import net.minecraft.*;
import org.lwjgl.opengl.GL11;

public class FontFixer {
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    public static String FIXER_VERSION = "1";
    private int[] charWidth = new int[256];
    public int fontTextureName = 0;
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    private byte[] glyphWidth = new byte[65536];
    private final int[] glyphTextureName = new int[256];
    private int[] colorCode = new int[32];
    private int boundTextureName;
    private final TextureManager renderEngine;
    private float posX;
    private float posY;
    private boolean unicodeFlag;
    private boolean bidiFlag;
    private float red;
    private float blue;
    private float green;
    private float alpha;
    private int textColor;
    private boolean randomStyle = false;
    private boolean boldStyle = false;
    private boolean italicStyle = false;
    private boolean underlineStyle = false;
    private boolean strikethroughStyle = false;
    private final ResourceLocation locationFontTexture;

//    public FontFixer() {
//        this.renderEngine = null;
//    }

    public FontFixer(GameSettings par1GameSettings, ResourceLocation par2ResourceLocation, TextureManager par3RenderEngine, boolean par4) {
        this.locationFontTexture = par2ResourceLocation;
        this.renderEngine = par3RenderEngine;
        this.unicodeFlag = par4;
        par3RenderEngine.bindTexture(this.locationFontTexture);
        this.readGlyphSizes();

        BufferedImage var5;
        try {
            var5 = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(this.locationFontTexture).getInputStream());
        } catch (IOException var17) {
            throw new RuntimeException(var17);
        }

        int var19 = var5.getWidth();
        int var7 = var5.getHeight();
        int[] var8 = new int[var19 * var7];
        var5.getRGB(0, 0, var19, var7, var8, 0, var19);

        int var9;
        int var10;
        int var11;
        int var12;
        int var13;
        int var15;
        int var16;
        for(var9 = 0; var9 < 256; ++var9) {
            var10 = var9 % 16;
            var11 = var9 / 16;

            for(var12 = 7; var12 >= 0; --var12) {
                var13 = var10 * 8 + var12;
                boolean var14 = true;

                for(var15 = 0; var15 < 8 && var14; ++var15) {
                    var16 = (var11 * 8 + var15) * var19;
                    int var17 = var8[var13 + var16] & 255;
                    if (var17 > 0) {
                        var14 = false;
                    }
                }

                if (!var14) {
                    break;
                }
            }

            if (var9 == 32) {
                var12 = 2;
            }

            this.charWidth[var9] = var12 + 2;
        }

        this.fontTextureName = 1;
//                par3RenderEngine.method_1417(var5);

        for(var9 = 0; var9 < 32; ++var9) {
            var10 = (var9 >> 3 & 1) * 85;
            var11 = (var9 >> 2 & 1) * 170 + var10;
            var12 = (var9 >> 1 & 1) * 170 + var10;
            var13 = (var9 & 1) * 170 + var10;
            if (var9 == 6) {
                var11 += 85;
            }

            if (par1GameSettings.anaglyph) {
                int var20 = (var11 * 30 + var12 * 59 + var13 * 11) / 100;
                var15 = (var11 * 30 + var12 * 70) / 100;
                var16 = (var11 * 30 + var13 * 70) / 100;
                var11 = var20;
                var12 = var15;
                var13 = var16;
            }

            if (var9 >= 16) {
                var11 /= 4;
                var12 /= 4;
                var13 /= 4;
            }

            this.colorCode[var9] = (var11 & 255) << 16 | (var12 & 255) << 8 | var13 & 255;
        }

    }

    private void readGlyphSizes() {
        try {
            InputStream var1 = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("font/glyph_sizes.bin")).getInputStream();
            var1.read(this.glyphWidth);
        } catch (IOException var2) {
            throw new RuntimeException(var2);
        }
    }

    private float renderCharAtPos(int par1, char par2, boolean par3) {
        return par2 == ' ' ? 4.0F : (par1 > 0 && !this.unicodeFlag ? this.renderDefaultChar(par1 + 32, par3) : this.renderUnicodeChar(par2, par3));
    }

    private float renderDefaultChar(int par1, boolean par2) {
        float var3 = (float)(par1 % 16 * 8);
        float var4 = (float)(par1 / 16 * 8);
        float var5 = par2 ? 1.0F : 0.0F;
        this.renderEngine.bindTexture(this.locationFontTexture);

        float var6 = (float)this.charWidth[par1] - 0.01F;
        GL11.glBegin(5);
        GL11.glTexCoord2f(var3 / 128.0F, var4 / 128.0F);
        GL11.glVertex3f(this.posX + var5, this.posY, 0.0F);
        GL11.glTexCoord2f(var3 / 128.0F, (var4 + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - var5, this.posY + 7.99F, 0.0F);
        GL11.glTexCoord2f((var3 + var6) / 128.0F, var4 / 128.0F);
        GL11.glVertex3f(this.posX + var6 + var5, this.posY, 0.0F);
        GL11.glTexCoord2f((var3 + var6) / 128.0F, (var4 + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + var6 - var5, this.posY + 7.99F, 0.0F);
        GL11.glEnd();
        return (float)this.charWidth[par1];
    }

    private ResourceLocation getUnicodePageLocation(int par1) {
        if (unicodePageLocations[par1] == null) {
            unicodePageLocations[par1] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", par1));
        }

        return unicodePageLocations[par1];
    }

    public final void loadGlyphTexture(int par1) {
        this.renderEngine.bindTexture(this.getUnicodePageLocation(par1));
    }

    private float renderUnicodeChar(char par1, boolean par2) {
        if (this.glyphWidth[par1] == 0) {
            return 0.0F;
        } else {
            int var3 = par1 / 256;
            this.loadGlyphTexture(var3);
            int var4 = this.glyphWidth[par1] >>> 4;
            int var5 = this.glyphWidth[par1] & 15;
            float var6 = (float)var4;
            float var7 = (float)(var5 + 1);
            float var8 = (float)(par1 % 16 * 16) + var6;
            float var9 = (float)((par1 & 255) / 16 * 16);
            float var10 = var7 - var6 - 0.02F;
            float var11 = par2 ? 1.0F : 0.0F;
            GL11.glBegin(5);
            GL11.glTexCoord2f(var8 / 256.0F, var9 / 256.0F);
            GL11.glVertex3f(this.posX + var11, this.posY, 0.0F);
            GL11.glTexCoord2f(var8 / 256.0F, (var9 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - var11, this.posY + 7.99F, 0.0F);
            GL11.glTexCoord2f((var8 + var10) / 256.0F, var9 / 256.0F);
            GL11.glVertex3f(this.posX + var10 / 2.0F + var11, this.posY, 0.0F);
            GL11.glTexCoord2f((var8 + var10) / 256.0F, (var9 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + var10 / 2.0F - var11, this.posY + 7.99F, 0.0F);
            GL11.glEnd();
            return (var7 - var6) / 2.0F + 1.0F;
        }
    }

    public int drawStringWithShadow(String par1Str, int par2, int par3, int par4) {
        return this.drawString(par1Str, par2, par3, par4, true);
    }

    public int drawString(String par1Str, int par2, int par3, int par4) {
        return this.drawString(par1Str, par2, par3, par4, false);
    }

    public int drawString(String par1Str, int par2, int par3, int par4, boolean par5) {
        this.resetStyles();
        if (this.bidiFlag) {
            par1Str = this.bidiReorder(par1Str);
        }

        int var6;
        if (par5) {
            var6 = this.renderString(par1Str, par2 + 1, par3 + 1, par4, true);
            var6 = Math.max(var6, this.renderString(par1Str, par2, par3, par4, false));
        } else {
            var6 = this.renderString(par1Str, par2, par3, par4, false);
        }

        return var6;
    }

    private String bidiReorder(String par1Str) {
        if (par1Str != null && Bidi.requiresBidi(par1Str.toCharArray(), 0, par1Str.length())) {
            Bidi var2 = new Bidi(par1Str, -2);
            byte[] var3 = new byte[var2.getRunCount()];
            String[] var4 = new String[var3.length];

            int var7;
            for(int var5 = 0; var5 < var3.length; ++var5) {
                int var6 = var2.getRunStart(var5);
                var7 = var2.getRunLimit(var5);
                int var8 = var2.getRunLevel(var5);
                String var9 = par1Str.substring(var6, var7);
                var3[var5] = (byte)var8;
                var4[var5] = var9;
            }

            String[] var11 = (String[])var4.clone();
            Bidi.reorderVisually(var3, 0, var4, 0, var3.length);
            StringBuilder var12 = new StringBuilder();

            for(var7 = 0; var7 < var4.length; ++var7) {
                byte var13 = var3[var7];

                int var14;
                for(var14 = 0; var14 < var11.length; ++var14) {
                    if (var11[var14].equals(var4[var7])) {
                        var13 = var3[var14];
                        break;
                    }
                }

                if ((var13 & 1) == 0) {
                    var12.append(var4[var7]);
                } else {
                    for(var14 = var4[var7].length() - 1; var14 >= 0; --var14) {
                        char var10 = var4[var7].charAt(var14);
                        if (var10 == '(') {
                            var10 = ')';
                        } else if (var10 == ')') {
                            var10 = '(';
                        }

                        var12.append(var10);
                    }
                }
            }

            return var12.toString();
        } else {
            return par1Str;
        }
    }

    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    private void renderStringAtPos(String par1Str, boolean par2) {
        for(int var3 = 0; var3 < par1Str.length(); ++var3) {
            char var4 = par1Str.charAt(var3);
            int var5;
            int var6;
            if (var4 == 167 && var3 + 1 < par1Str.length()) {
                var5 = "0123456789abcdefklmnor".indexOf(par1Str.toLowerCase().charAt(var3 + 1));
                if (var5 < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    if (var5 < 0 || var5 > 15) {
                        var5 = 15;
                    }

                    if (par2) {
                        var5 += 16;
                    }

                    var6 = this.colorCode[var5];
                    this.textColor = var6;
                    GL11.glColor4f((float)(var6 >> 16) / 255.0F, (float)(var6 >> 8 & 255) / 255.0F, (float)(var6 & 255) / 255.0F, this.alpha);
                } else if (var5 == 16) {
                    this.randomStyle = true;
                } else if (var5 == 17) {
                    this.boldStyle = true;
                } else if (var5 == 18) {
                    this.strikethroughStyle = true;
                } else if (var5 == 19) {
                    this.underlineStyle = true;
                } else if (var5 == 20) {
                    this.italicStyle = true;
                } else if (var5 == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    GL11.glColor4f(this.red, this.blue, this.green, this.alpha);
                }

                ++var3;
            } else {
                var5 = ChatAllowedCharacters.allowedCharacters.indexOf(var4);
                if (this.randomStyle && var5 > 0) {
                    do {
                        var6 = this.fontRandom.nextInt(ChatAllowedCharacters.allowedCharacters.length());
                    } while(this.charWidth[var5 + 32] != this.charWidth[var6 + 32]);

                    var5 = var6;
                }

                float var9 = this.renderCharAtPos(var5, var4, this.italicStyle);
                if (this.boldStyle) {
                    ++this.posX;
                    this.renderCharAtPos(var5, var4, this.italicStyle);
                    --this.posX;
                    ++var9;
                }

                Tessellator var7;
                if (this.strikethroughStyle) {
                    var7 = Tessellator.instance;
                    GL11.glDisable(3553);
                    var7.startDrawingQuads();
                    var7.addVertex((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0);
                    var7.addVertex((double)(this.posX + var9), (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0);
                    var7.addVertex((double)(this.posX + var9), (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0);
                    var7.addVertex((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0);
                    var7.draw();
                    GL11.glEnable(3553);
                }

                if (this.underlineStyle) {
                    var7 = Tessellator.instance;
                    GL11.glDisable(3553);
                    var7.startDrawingQuads();
                    int var8 = this.underlineStyle ? -1 : 0;
                    var7.addVertex((double)(this.posX + (float)var8), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0);
                    var7.addVertex((double)(this.posX + var9), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0);
                    var7.addVertex((double)(this.posX + var9), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0);
                    var7.addVertex((double)(this.posX + (float)var8), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0);
                    var7.draw();
                    GL11.glEnable(3553);
                }

                this.posX += (float)((int)var9);
            }
        }

    }

    private int renderStringAligned(String par1Str, int par2, int par3, int par4, int par5, boolean par6) {
        if (this.bidiFlag) {
            par1Str = this.bidiReorder(par1Str);
            int var7 = this.getStringWidth(par1Str);
            par2 = par2 + par4 - var7;
        }

        return this.renderString(par1Str, par2, par3, par5, par6);
    }

    private int renderString(String par1Str, int par2, int par3, int par4, boolean par5) {
        if (par1Str == null) {
            return 0;
        } else {
            this.boundTextureName = 0;
            if ((par4 & -67108864) == 0) {
                par4 |= -16777216;
            }

            if (par5) {
                par4 = (par4 & 16579836) >> 2 | par4 & -16777216;
            }

            this.red = (float)(par4 >> 16 & 255) / 255.0F;
            this.blue = (float)(par4 >> 8 & 255) / 255.0F;
            this.green = (float)(par4 & 255) / 255.0F;
            this.alpha = (float)(par4 >> 24 & 255) / 255.0F;
            GL11.glColor4f(this.red, this.blue, this.green, this.alpha);
            this.posX = (float)par2;
            this.posY = (float)par3;
            this.renderStringAtPos(par1Str, par5);
            return (int)this.posX;
        }
    }

    public int getStringWidth(String par1Str) {
        if (par1Str == null) {
            return 0;
        } else {
            int var2 = 0;
            boolean var3 = false;

            for(int var4 = 0; var4 < par1Str.length(); ++var4) {
                char var5 = par1Str.charAt(var4);
                int var6 = this.getCharWidth(var5);
                if (var6 < 0 && var4 < par1Str.length() - 1) {
                    ++var4;
                    var5 = par1Str.charAt(var4);
                    if (var5 != 'l' && var5 != 'L') {
                        if (var5 == 'r' || var5 == 'R') {
                            var3 = false;
                        }
                    } else {
                        var3 = true;
                    }

                    var6 = 0;
                }

                var2 += var6;
                if (var3) {
                    ++var2;
                }
            }

            return var2;
        }
    }

    public int getCharWidth(char par1) {
        if (par1 == 167) {
            return -1;
        } else if (par1 == ' ') {
            return 4;
        } else {
            int var2 = ChatAllowedCharacters.allowedCharacters.indexOf(par1);
            if (var2 >= 0 && !this.unicodeFlag) {
                return this.charWidth[var2 + 32];
            } else if (this.glyphWidth[par1] != 0) {
                int var3 = this.glyphWidth[par1] >>> 4;
                int var4 = this.glyphWidth[par1] & 15;
                if (var4 > 7) {
                    var4 = 15;
                    var3 = 0;
                }

                ++var4;
                return (var4 - var3) / 2 + 1;
            } else {
                return 0;
            }
        }
    }

    public String trimStringToWidth(String par1Str, int par2) {
        return this.trimStringToWidth(par1Str, par2, false);
    }

    public String trimStringToWidth(String par1Str, int par2, boolean par3) {
        StringBuilder var4 = new StringBuilder();
        int var5 = 0;
        int var6 = par3 ? par1Str.length() - 1 : 0;
        int var7 = par3 ? -1 : 1;
        boolean var8 = false;
        boolean var9 = false;

        for(int var10 = var6; var10 >= 0 && var10 < par1Str.length() && var5 < par2; var10 += var7) {
            char var11 = par1Str.charAt(var10);
            int var12 = this.getCharWidth(var11);
            if (var8) {
                var8 = false;
                if (var11 != 'l' && var11 != 'L') {
                    if (var11 == 'r' || var11 == 'R') {
                        var9 = false;
                    }
                } else {
                    var9 = true;
                }
            } else if (var12 < 0) {
                var8 = true;
            } else {
                var5 += var12;
                if (var9) {
                    ++var5;
                }
            }

            if (var5 > par2) {
                break;
            }

            if (par3) {
                var4.insert(0, var11);
            } else {
                var4.append(var11);
            }
        }

        return var4.toString();
    }

    private String trimStringNewline(String par1Str) {
        while(par1Str != null && par1Str.endsWith("\n")) {
            par1Str = par1Str.substring(0, par1Str.length() - 1);
        }

        return par1Str;
    }

    public void drawSplitString(String par1Str, int par2, int par3, int par4, int par5) {
        this.resetStyles();
        this.textColor = par5;
        par1Str = this.trimStringNewline(par1Str);
        this.renderSplitString(par1Str, par2, par3, par4, false);
    }

    private void renderSplitString(String par1Str, int par2, int par3, int par4, boolean par5) {
        List var6 = this.listFormattedStringToWidth(par1Str, par4);

        for(Iterator var7 = var6.iterator(); var7.hasNext(); par3 += this.FONT_HEIGHT) {
            String var8 = (String)var7.next();
            this.renderStringAligned(var8, par2, par3, par4, this.textColor, par5);
        }

    }

    public int splitStringWidth(String par1Str, int par2) {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(par1Str, par2).size();
    }

    public void setUnicodeFlag(boolean par1) {
        this.unicodeFlag = par1;
    }

    public boolean getUnicodeFlag() {
        return this.unicodeFlag;
    }

    public void setBidiFlag(boolean par1) {
        this.bidiFlag = par1;
    }

    public List listFormattedStringToWidth(String par1Str, int par2) {
        return Arrays.asList(this.wrapFormattedStringToWidth(par1Str, par2).split("\n"));
    }

    public String wrapFormattedStringToWidth(String par1Str, int par2) {
        int var3 = this.sizeStringToWidth(par1Str, par2);
        if (par1Str.length() <= var3) {
            return par1Str;
        } else {
            String var4 = par1Str.substring(0, var3);
            char var5 = par1Str.charAt(var3);
            boolean var6 = var5 == ' ' || var5 == '\n';
            String var7 = getFormatFromString(var4) + par1Str.substring(var3 + (var6 ? 1 : 0));
            return var4 + "\n" + this.wrapFormattedStringToWidth(var7, par2);
        }
    }

    private int sizeStringToWidth(String par1Str, int par2) {
        int var3 = par1Str.length();
        int var4 = 0;
        int var5 = 0;
        int var6 = -1;

        for(boolean var7 = false; var5 < var3; ++var5) {
            char var8 = par1Str.charAt(var5);
            switch (var8) {
                case '\n':
                    --var5;
                    break;
                case ' ':
                    var6 = var5;
                default:
                    var4 += this.getCharWidth(var8);
                    if (var7) {
                        ++var4;
                    }
                    break;
                case '§':
                    if (var5 < var3 - 1) {
                        ++var5;
                        char var9 = par1Str.charAt(var5);
                        if (var9 != 'l' && var9 != 'L') {
                            if (var9 == 'r' || var9 == 'R' || isFormatColor(var9)) {
                                var7 = false;
                            }
                        } else {
                            var7 = true;
                        }
                    }
            }

            if (var8 == '\n') {
                ++var5;
                var6 = var5;
                break;
            }

            if (var4 > par2) {
                break;
            }
        }

        return var5 != var3 && var6 != -1 && var6 < var5 ? var6 : var5;
    }

    private static boolean isFormatColor(char par0) {
        return par0 >= '0' && par0 <= '9' || par0 >= 'a' && par0 <= 'f' || par0 >= 'A' && par0 <= 'F';
    }

    private static boolean isFormatSpecial(char par0) {
        return par0 >= 'k' && par0 <= 'o' || par0 >= 'K' && par0 <= 'O' || par0 == 'r' || par0 == 'R';
    }

    private static String getFormatFromString(String par0Str) {
        StringBuilder var1 = new StringBuilder();
        int var2 = -1;
        int var3 = par0Str.length();

        while((var2 = par0Str.indexOf(167, var2 + 1)) != -1) {
            if (var2 < var3 - 1) {
                char var4 = par0Str.charAt(var2 + 1);
                if (isFormatColor(var4)) {
                    var1 = new StringBuilder("§" + var4);
                } else if (isFormatSpecial(var4)) {
                    var1.append("§").append(var4);
                }
            }
        }

        return var1.toString();
    }

    public boolean getBidiFlag() {
        return this.bidiFlag;
    }
}
