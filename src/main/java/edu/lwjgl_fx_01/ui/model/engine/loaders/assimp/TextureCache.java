package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import java.util.HashMap;
import java.util.Map;
import edu.lwjgl_fx_01.ui.model.engine.graph.LwjglTexture;

public class TextureCache {

    private static TextureCache INSTANCE;

    private Map<String, LwjglTexture> texturesMap;
    
    private TextureCache() {
        texturesMap = new HashMap<>();
    }
    
    public static synchronized TextureCache getInstance() {
        if ( INSTANCE == null ) {
            INSTANCE = new TextureCache();
        }
        return INSTANCE;
    }
    
    public LwjglTexture getTexture(String path) throws Exception {
        LwjglTexture texture = texturesMap.get(path);
        if ( texture == null ) {
            texture = new LwjglTexture(path);
            texturesMap.put(path, texture);
        }
        return texture;
    }
}
