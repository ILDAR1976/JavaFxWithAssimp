package edu.lwjgl_fx_01.ui.model.engine.loaders.assimp;

import java.util.HashMap;
import java.util.Map;
import edu.lwjgl_fx_01.ui.model.engine.graph.TextureFx;

public class TextureCache {

    private static TextureCache INSTANCE;

    private Map<String, TextureFx> texturesMap;
    
    private TextureCache() {
        texturesMap = new HashMap<>();
    }
    
    public static synchronized TextureCache getInstance() {
        if ( INSTANCE == null ) {
            INSTANCE = new TextureCache();
        }
        return INSTANCE;
    }
    
    public TextureFx getTexture(String path) throws Exception {
        TextureFx texture = texturesMap.get(path);
        if ( texture == null ) {
            texture = new TextureFx(path);
            texturesMap.put(path, texture);
        }
        return texture;
    }
}
