package org.iop.version_1.structure.util;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.common.com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.nio.ByteBuffer;

/**
 * The Class <code>com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.PackageEncoder</code>
 * encode the package object to json string format
 * <p/>
 * Created by Matias Furszyfer
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class PackageEncoder implements Encoder.Binary<Package>{

    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(PackageEncoder.class.getName()));
    /**
     * (non-javadoc)
     * @see Text#encode(Object)
     */
    @Override
    public ByteBuffer encode(Package packageToSend) throws EncodeException {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        int packageId = flatBufferBuilder.createString(packageToSend.getPackageId().toString());
        int content = flatBufferBuilder.createString(packageToSend.getContent());
        int networkServiceType = (packageToSend.getNetworkServiceTypeSource()!=null)? flatBufferBuilder.createString(packageToSend.getNetworkServiceTypeSource().getCode()) : 0 ;
        int destinationPublicKey = (packageToSend.getDestinationPublicKey()!=null)? flatBufferBuilder.createString(packageToSend.getDestinationPublicKey()) : 0;
        int pack = com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.common.Package.createPackage(
                flatBufferBuilder,
                packageId,
                content,
                packageToSend.getPackageType().getPackageTypeAsShort(),
                networkServiceType,
                destinationPublicKey);
        flatBufferBuilder.finish(pack);
        return flatBufferBuilder.dataBuffer();
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
        LOG.info("PackageEnconder destroy method");
    }

}
