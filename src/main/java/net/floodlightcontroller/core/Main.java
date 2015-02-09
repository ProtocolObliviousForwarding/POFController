package net.floodlightcontroller.core;

import net.floodlightcontroller.core.internal.CmdLineSettings;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.FloodlightModuleLoader;
import net.floodlightcontroller.core.module.IFloodlightModuleContext;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Modified by Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *      Remove launching the RestApiService for POF
 *      If run controller failed, exit.
 */

/**
 * Host for the Floodlight main method
 * @author alexreimers
 */
public class Main {

    /**
     * Main method to load configuration and modules
     * @param args
     * @throws FloodlightModuleException 
     */
    public static void main(String[] args) throws FloodlightModuleException {
        // Setup logger
        CmdLineSettings settings = new CmdLineSettings();
        CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            System.exit(1);
        }
        
        // Load modules
        FloodlightModuleLoader fml = new FloodlightModuleLoader();
        IFloodlightModuleContext moduleContext = fml.loadModulesFromConfig(settings.getModuleFile());

        // Run the main floodlight module
        IFloodlightProviderService controller =
                moduleContext.getServiceImpl(IFloodlightProviderService.class);
        // This call blocks, it has to be the last line in the main
        if(null != controller){
            try{
            	Thread.sleep(3000);
                controller.run();
            }catch (Exception e) {
                e.printStackTrace();
                
                //in case launch controller fail, exit.
                System.err.println("Controller start failed! exit.");
                System.exit(1);
            }
        }else {
            System.err.println("Controller start failed! exit.");
            System.exit(1);
        } 
    }
}
