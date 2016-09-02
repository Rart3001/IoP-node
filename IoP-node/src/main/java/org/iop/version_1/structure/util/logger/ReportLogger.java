package org.iop.version_1.structure.util.logger;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.STATUS;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.log4j.Logger;

/**
 * Created by mati on 31/08/16.
 */
public class ReportLogger {

    private static final Logger resultLog = Logger.getLogger("reportsLogger");

    private static final String CLASS_SEPARATOR = ":C:";
    private static final String PACKAGE_TYPE_SEPARATOR = ":P:";
    private static final String STATUS_SEPARATOR = ":S:";
    private static final String EXTRA_DATA_SEPARATOR = ":Extra:";
    private static final String EXCEPTION_SEPARATOR = ":Ex:";


    public static void infoProcessor(Class clazz, PackageType packageType, STATUS status,String extra){
        resultLog.info(CLASS_SEPARATOR+clazz+CLASS_SEPARATOR+PACKAGE_TYPE_SEPARATOR+packageType+PACKAGE_TYPE_SEPARATOR+STATUS_SEPARATOR+status+STATUS_SEPARATOR+EXTRA_DATA_SEPARATOR+extra+EXTRA_DATA_SEPARATOR);
    }

    public static void infoProcessor(Class clazz, PackageType packageType, STATUS status, String extra, Exception e) {
        resultLog.info(CLASS_SEPARATOR+clazz+CLASS_SEPARATOR+PACKAGE_TYPE_SEPARATOR+packageType+PACKAGE_TYPE_SEPARATOR+STATUS_SEPARATOR+status+STATUS_SEPARATOR+EXTRA_DATA_SEPARATOR+extra+EXTRA_DATA_SEPARATOR,e);
    }
}
