package org.iop.version_1.structure.util;


import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.BlockPackages;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.common.Block;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.common.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.nio.ByteBuffer;

/**
 * <p/>
 * Created by Matias Furszyfer
 *
 * @version 1.0
 * */
public class BlockDecoder2 implements Decoder.Binary<BlockPackages>{

    private static final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(BlockDecoder2.class));

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

    @Override
    public BlockPackages decode(ByteBuffer bytes) throws DecodeException {
        Block block = Block.getRootAsBlock(bytes);
        try {
            //todo: acá hace falta hacer validaciones de que los datos no esten mal..
            BlockPackages blockPackages = new BlockPackages();

            for (int i=0;i<block.packagesLength();i++){
                try {
                    Package pack = block.packages(i);
//                    NetworkServiceType networkServiceType = NetworkServiceType.getByCode(pack.networkServiceType());
                    PackageType packageType = PackageType.buildWithInt(pack.packageType());
//                    LOG.info("####### DECODE: PackageType: " + packageType + " Network service type: " + networkServiceType);
//                    blockPackages.add(com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package.rebuildInstance(
//                            UUID.fromString(
//                                    pack.id()),
//                            pack.content(),
//                            networkServiceType,
//                            packageType,
//                            pack.destinationPk()
//                    ));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }



            return blockPackages;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public boolean willDecode(ByteBuffer bytes) {
        return true;
    }



//    /**
//     * (non-javadoc)
//     * @see Text#decode(String)
//     */
//    @Override
//    public Package decode(String s) throws DecodeException {
//        return GsonProvider.getGson().fromJson(s, Package.class);
//    }
//
//    /**
//     * (non-javadoc)
//     * @see Text#willDecode(String)
//     */
//    @Override
//    public boolean willDecode(String s) {
//        try{
//            //todo sacar esto...
//            GsonProvider.getJsonParser().parse(s);
//            return true;
//
//        }catch (Exception ex){
//            return false;
//        }
//    }

}
