package de.fanta.cubeside.util.ChatSkullAPI;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;

public class Message {

    private final String[] lines;

    public Message(BufferedImage image, int height, char imgChar) {
        String[][] chatColors = toChatColorArray(image, height);
        this.lines = toImgMessage(chatColors, imgChar);
    }

    public Message appendText(String[] text) {
        for (int y = 0; y < this.lines.length; y++) {
            if (text.length > y) {
                String[] tmp16_12 = this.lines;
                tmp16_12[y] = tmp16_12[y] + " " + text[y];
            }
        }
        return this;
    }

    private String[][] toChatColorArray(BufferedImage image, int height) {
        double ratio = (image.getHeight() / image.getWidth());
        BufferedImage resized = resizeImage(image, (int) (height / ratio), height);
        String[][] chatImg = new String[resized.getWidth()][resized.getHeight()];
        for (int x = 0; x < resized.getWidth(); x++) {
            for (int y = 0; y < resized.getHeight(); y++) {
                int rgb = resized.getRGB(x, y);
                chatImg[x][y] = "&x" + Integer.toHexString(rgb).substring(2) + "&l";
            }
        }
        return chatImg;
    }

    private String[] toImgMessage(String[][] colors, char imgchar) {
        String[] lines = new String[(colors[0]).length];
        for (int y = 0; y < (colors[0]).length; y++) {
            StringBuilder line = new StringBuilder();
            for (String[] chatColors : colors) {
                String color = chatColors[y];
                line.append((color != null) ? (chatColors[y] + imgchar) : Character.valueOf(' '));
            }
            lines[y] = line + "&r";
        }
        return lines;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        AffineTransform af = new AffineTransform();
        af.scale((width / originalImage.getWidth()), (height / originalImage.getHeight()));
        AffineTransformOp operation = new AffineTransformOp(af, 1);
        return operation.filter(originalImage, null);
    }

    public void setItemLore() {
        byte b;
        int i;
        String[] arrayOfString;
        for (i = (arrayOfString = this.lines).length, b = 0; b < i;) {
            String line = arrayOfString[b];
            Minecraft.getInstance().player.connection.sendCommand("additemlore " + line);
            b++;
        }
    }

}
