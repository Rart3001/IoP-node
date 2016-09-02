package org.iop.version_1;

import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginFileSystem;
import com.bitdubai.fermat_api.layer.osa_android.location_system.LocationManager;
import com.bitdubai.fermat_osa_addon.layer.linux.device_location.developer.bitdubai.version_1.DeviceLocationSystemAddonRoot;
import com.bitdubai.fermat_osa_addon.layer.linux.file_system.developer.bitdubai.version_1.PluginFileSystemLinuxAddonRoot;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;


import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by mati on 12/08/16.
 */
public class AppMain {

    /**
     * Represent the LOG
     */
    private static final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(AppMain.class));
    /**
     * org.iop.version_1.AppMain method
     *
     * @param args
     */
    public static void main(String[] args) {

        try {

            LOG.info("***********************************************************************");
            LOG.info("* FERMAT - Network Node - Version 1.0 (2016)                          *");
            LOG.info("* www.fermat.org                                                      *");
            LOG.info("***********************************************************************");
            LOG.info("");
            LOG.info("- Starting process ...");

            ScheduledExecutorService scheduledExecutorScheduler = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Runtime runtime = Runtime.getRuntime();

                    NumberFormat format = NumberFormat.getInstance();

                    StringBuilder sb = new StringBuilder();
                    long maxMemory = runtime.maxMemory();
                    long allocatedMemory = runtime.totalMemory();
                    long freeMemory = runtime.freeMemory();

                    sb.append("free memory: " + format.format((freeMemory / 1024 / 1024) ) + " mb / ");
                    sb.append("allocated memory: " + format.format((allocatedMemory / 1024 / 1024)) + " mb / ");
                    sb.append("max memory: " + format.format((maxMemory / 1024 / 1024)) + " mb / ");
                    sb.append("total free memory: " + format.format(((freeMemory + (maxMemory - allocatedMemory)) / 1024 / 1024)) + " mb ");

                    LOG.info(sb.toString());
                }
            },0,30, TimeUnit.SECONDS);

            DeviceLocationSystemAddonRoot deviceLocationSystemAddonRoot = new DeviceLocationSystemAddonRoot();
            deviceLocationSystemAddonRoot.start();

            PluginFileSystemLinuxAddonRoot pluginFileSystemLinuxAddonRoot = new PluginFileSystemLinuxAddonRoot();
            pluginFileSystemLinuxAddonRoot.start();

            IoPNodePluginRoot ioPNodePluginRoot = new IoPNodePluginRoot();
            ioPNodePluginRoot.setLocationManager((LocationManager) deviceLocationSystemAddonRoot.getManager());
            ioPNodePluginRoot.setPluginFileSystem((PluginFileSystem) pluginFileSystemLinuxAddonRoot.getManager());
            ioPNodePluginRoot.start();

            LOG.info("FERMAT - Network Node - started satisfactory...");

        } catch (Exception e) {

            LOG.error("***********************************************************************");
            LOG.error("* FERMAT - ERROR                                                      *");
            LOG.error("***********************************************************************");
            e.printStackTrace();
            System.exit(1);
        }

    }
}
