package org.sp.sss; 

import java.awt.Font; 
import java.awt.FontFormatException;
import java.io.InputStream;
import java.io.IOException;
 
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;
 
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.ResourceLoader;
 
public class LWJGLBackend {
    private TrueTypeFont currentFont;
    private boolean antiAlias = true;
    private long[] fps = {0, 0, 0, 0, 0, 0};
    private int fps_i = 0;
    private long last_cycle = System.currentTimeMillis();


    public void init() {
        this.setDefaultFont(systemFont("monofur", 15, true));
    }

    public void update() {
        Display.update();
    }
    
    public void render() {
        Color.white.bind();
        currentFont.drawString(100, 50, "THE LIGHTWEIGHT JAVA GAMES LIBRARY", Color.yellow);
    }

    public void start(int w, int h, int sync_to) {
        initGL(w, h);
        init();

        while (true) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            render();
            update();
            Display.sync(sync_to);
            fpsUpdate();

            if (Display.isCloseRequested()) {
                stop();
            }
        }
    }

    public void stop() {
        Display.destroy();
    }

    private void fpsUpdate() {
        long spent = System.currentTimeMillis() - last_cycle;
        fps[fps_i] = 1000 / spent;
        if (fps_i >= fps.length - 1)
            fps_i = 0;
        else
            fps_i++;

        last_cycle = System.currentTimeMillis();
    }

    public int currentFps() {
        int sum = 0;
        for (int i = 0; i < fps.length; i++)
            sum += fps[i];

        return sum / fps.length;
    }

    /**
     * Initialise the GL display
     * 
     * @param width The width of the display
     * @param height The height of the display
     */
    private void initGL(int width, int height) {
        try {
            Display.setDisplayMode(new DisplayMode(width,height));
            Display.create();
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);        
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);                    

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);                
        GL11.glClearDepth(1);                                       

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glViewport(0,0,width,height);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public TrueTypeFont systemFont(String name, int size, boolean aa) {
        return new TrueTypeFont(
                new Font(name, Font.PLAIN, size),
                aa);
    }

    public TrueTypeFont systemFont(String name, int size) {
        return systemFont(name, size, false);
    }

    public TrueTypeFont resourceFont(String file, float size) throws FontFormatException, IOException {
        return new TrueTypeFont(
                Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream("myfont.ttf"))
                .deriveFont(size),
                false);
    }

    public void setDefaultFont(TrueTypeFont font) {
        this.currentFont = font;
    }

    public void draw(String string, int x, int y) {
        currentFont.drawString(x, y, string);
    }

    public void draw(String string, int x, int y, Color color) {
        currentFont.drawString(x, y, string, color);
    }

    public TrueTypeFont getFont() {
        return this.currentFont;
    }
}
