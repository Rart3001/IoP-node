package org.iop.version_1.structure.util.logger;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.STATUS;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.log4j.Logger;

/**
 * Created by mati on 31/08/16.
 */
public class ReportLogger {

    private static final Logger resultLog = Logger.getLogger("reportsLogger");


    public static void infoProcessor(Class clazz, PackageType packageType, STATUS status,String extra){
        resultLog.info(":C:"+clazz+":P:"+packageType+":S:"+status+":E:"+extra);
    }

    public static void infoProcessor(Class clazz, PackageType packageType, STATUS status, String extra, Exception e) {
        resultLog.info(":C:"+clazz+":P:"+packageType+":S:"+status+":E:"+extra,e);
    }
}
