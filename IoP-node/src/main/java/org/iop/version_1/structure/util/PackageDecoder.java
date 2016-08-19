package org.iop.version_1.structure.util;


import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.bitdubai.fermat_api.layer.all_definition.exceptions.InvalidParameterException;
import com.bitdubai.fermat_api.layer.all_definition.network_service.enums.NetworkServiceType;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;

/**
 * <p/>
 * Created by Matias Furszyfer
 *
 * @version 1.0
 * */
public class PackageDecoder implements Decoder.Binary<Package>{

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
    public Package decode(ByteBuffer bytes) throws DecodeException {
        com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.common.Package pack = com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.common.Package.getRootAsPackage(bytes);
        try {
            return Package.rebuildInstance(
                    UUID.fromString(pack.id()),
                    pack.content(),
                    NetworkServiceType.getByCode(pack.networkServiceType()),
                    PackageType.buildWithInt(pack.packageType()),
                    pack.destinationPk()
            );
        } catch (InvalidParameterException e) {
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
