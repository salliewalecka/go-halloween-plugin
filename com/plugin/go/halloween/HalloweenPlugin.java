/**
 * Created by alisonpolton-simon on 10/4/16.
 */
package plugin.go.halloween;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
public class HalloweenPlugin implements GoPlugin {
    private GoApplicationAccessor accessor;
    Logger logger = Logger.getLoggerFor(HalloweenPlugin.class);

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("task", Arrays.asList("1.0"));
    }

    public GoPluginApiResponse handle(GoPluginApiRequest requestMessage) {
        if("configuration".equals(requestMessage.requestName())) {
            return handleGetConfigRequest();
        } else if ("validate".equals(requestMessage.requestName())) {
            return handleValidation(requestMessage);
        } else if ("view".equals(requestMessage.requestName())) {
            return handleGetViewRequest();
        } else if("execute".equals(requestMessage.requestName())) {
            return handleTaskExecution(requestMessage);
        }
        else return null;
    }

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.accessor = goApplicationAccessor;
    }

    private GoPluginApiResponse handleGetConfigRequest() {
        HashMap config = new HashMap();
        HashMap pumpkin = new HashMap();
        pumpkin.put("display-order", "0");
        pumpkin.put("default-value", "");
        pumpkin.put("secure", false);
        pumpkin.put("required", true);
        config.put("pumpkin", pumpkin);

        return createResponse(config);
    }

    private GoPluginApiResponse handleGetViewRequest() {
        HashMap view = new HashMap();
        view.put("displayValue", "Halloween");
        try {
            view.put("template", IOUtils.toString(getClass().getResourceAsStream("/views/task.template.html"), "UTF-8"));
        } catch (IOException e) {
            logger.info(e.toString());
        }
        return createResponse(view);
    }

    private GoPluginApiResponse handleValidation(GoPluginApiRequest requestMessage) {
        HashMap validation = new HashMap();
        Map requestMap = (Map) new GsonBuilder().create().fromJson(requestMessage.requestBody(), Object.class);
        logger.info("request map " + requestMap.toString());
        Map pumpkinMap = (Map) requestMap.get("pumpkin");
        logger.info("pumpkin map " + pumpkinMap.toString());
        String pumpkinType = (String) pumpkinMap.get("value");
        Map errors = new HashMap();

        if(!PumpkinFactory.getPumpkinTypes().contains(pumpkinType)){
            errors.put("pumpkin","Please only choose from Lisa, Cat, or Toothy");
        }
        logger.info("errors " + errors.toString());
        validation.put("errors", errors);
        return createResponse(validation);
    }

    private GoPluginApiResponse handleTaskExecution(GoPluginApiRequest requestMessage) {
        Map requestMap = (Map) new GsonBuilder().create().fromJson(requestMessage.requestBody(), Object.class);
        Map configMap = (Map) requestMap.get("config");
        Map pumpkinMap = (Map) configMap.get("pumpkin");
        String pumpkinType = (String) pumpkinMap.get("value");
        String pumpkin = PumpkinFactory.getPumpkin(pumpkinType);

        JobConsoleLogger.getConsoleLogger().printLine(pumpkin);
        HashMap result = new HashMap();
        result.put("success", true);
        result.put("message", "Have a Spoooooky Saturday");
        return createResponse(result);
    }

    private GoPluginApiResponse createResponse(HashMap body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(200);
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
        return response;
    }
}
