package  edu.lwjgl_fx_01.ui.model.engine.graph;

import javax.imageio.ImageIO;
import java.nio.IntBuffer;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.*;
import edu.lwjgl_fx_01.ui.utils.Utils;
import javafx.scene.image.Image;

public class TextureFx {

    private final int id;

    private final int width;

    private final int height;

    private int numRows = 1;

    private int numCols = 1;
    
    private static int counterId = 0;
    
    private Image image;

    /**
     * Creates an empty texture.
     *
     * @param width Width of the texture
     * @param height Height of the texture
     * @param pixelFormat Specifies the format of the pixel data (GL_RGBA, etc.)
     * @throws Exception
     */
    public TextureFx(int width, int height, int pixelFormat) throws Exception {
        this.id = counterId++;
        this.width = width;
        this.height = height;
     }

    public TextureFx(String fileName, int numCols, int numRows) throws Exception {
        this(fileName);
        this.numCols = numCols;
        this.numRows = numRows;
    }

    public TextureFx(String fileName) throws Exception {
        this(Utils.ioResourceToByteBuffer(fileName, 1024));
    }

    public TextureFx(ByteBuffer imageData) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer avChannels = stack.mallocInt(1);

            // Decode texture image into a byte buffer
            ByteBuffer decodedImage = stbi_load_from_memory(imageData, w, h, avChannels, 4);

            this.width = w.get();
            this.height = h.get();

            this.image = getJavaFXImage(getByteArrayFromByteBuffer(decodedImage), this.width, this.height);
            
            this.id = counterId++;
        }
    }
  
    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }

    public Image getJavaFXImage(byte[] rawPixels, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write((RenderedImage) createBufferedImage(rawPixels, width, height), "png", out);
            out.flush();
            } catch (IOException ex) {
            }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
        }

    private BufferedImage createBufferedImage(byte[] pixels, int width, int height) {
        SampleModel sm = getIndexSampleModel(width, height);
        DataBuffer db = new DataBufferByte(pixels, width*height, 0);
        WritableRaster raster = Raster.createWritableRaster(sm, db, null);
        IndexColorModel cm = getDefaultColorModel();
        BufferedImage image = new BufferedImage(cm, raster, false, null);
        return image;
    }
    private SampleModel getIndexSampleModel(int width, int height) {
        IndexColorModel icm = getDefaultColorModel();
        WritableRaster wr = icm.createCompatibleWritableRaster(1, 1);
        SampleModel sampleModel = wr.getSampleModel();
        sampleModel = sampleModel.createCompatibleSampleModel(width, height);
        return sampleModel;
    }

    private IndexColorModel getDefaultColorModel() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        for(int i=0; i<256; i++) {
           r[i]=(byte)i;
           g[i]=(byte)i;
           b[i]=(byte)i;
        }
        IndexColorModel defaultColorModel = new IndexColorModel(8, 256, r, g, b);
        return defaultColorModel;
    }

    public void findMinAndMax(short[] pixels, int width, int height) {
        int size = width*height;
        int value;
        int min = 65535;
        int max = 0;
        for (int i=0; i<size; i++) {
            value = pixels[i]&0xffff;
            if (value<min)
               min = value;
            if (value>max)
               max = value;
            }
    }
    
    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
 
    public int getId() {
        return id;
    }

	public Image getImage() {
		return image;
	}

	@SuppressWarnings("restriction")
	public void setImage(Image image) {
		this.image = image;
	}
  
    
}
