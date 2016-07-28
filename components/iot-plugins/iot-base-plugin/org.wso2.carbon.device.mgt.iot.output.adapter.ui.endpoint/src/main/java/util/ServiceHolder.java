package util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.output.adapter.ui.UIOutputCallbackControllerService;
import org.wso2.carbon.device.mgt.iot.output.adapter.ui.service.WebsocketValidationService;

public class ServiceHolder {

    private static ServiceHolder instance;
    private UIOutputCallbackControllerService uiOutputCallbackControllerService;
    private static final Log log = LogFactory.getLog(ServiceHolder.class);

    private ServiceHolder(){
        uiOutputCallbackControllerService = (UIOutputCallbackControllerService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(UIOutputCallbackControllerService.class, null);
    }

    public synchronized static ServiceHolder getInstance(){
        if (instance==null){
            instance= new ServiceHolder();
        }
        return instance;
    }

    public UIOutputCallbackControllerService getUiOutputCallbackControllerService() {
        return uiOutputCallbackControllerService;
    }

    public static WebsocketValidationService getWebsocketValidationService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        WebsocketValidationService deviceManagementProviderService =
                (WebsocketValidationService) ctx.getOSGiService(WebsocketValidationService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "Websocket Validation service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }
}
