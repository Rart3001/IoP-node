package org.iop.version_1.structure.util;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.BlockPackages;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 * The Class <code>com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.PackageDecoder</code>
 * decode the json string to a package object
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 30/11/15.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class BlockDecoder implements Decoder.Text<BlockPackages>{

    /**
     * (non-javadoc)
     * @see Text#decode(String)
     */
    @Override
    public BlockPackages decode(String s) throws DecodeException {
        BlockPackages blockPackages = null;
        try {
            blockPackages = GsonProvider.getGson().fromJson(s, BlockPackages.class);
        }catch (Exception e){
            System.err.println("Decode error, json: "+ s);
            e.printStackTrace();
        }
        return blockPackages;
    }

    /**
     * (non-javadoc)
     * @see Text#willDecode(String)
     */
    @Override
    public boolean willDecode(String s) {
        try{
            //todo sacar esto...
            GsonProvider.getJsonParser().parse(s);
            return true;

        }catch (Exception ex){
            return false;
        }
    }

    /**
     * (non-javadoc)
     * @see Text#init(EndpointConfig)
     */
    @Override
    public void init(EndpointConfig config) {

    }

    /**
     * (non-javadoc)
     * @see Text#destroy()
     */
    @Override
    public void destroy() {

    }
}