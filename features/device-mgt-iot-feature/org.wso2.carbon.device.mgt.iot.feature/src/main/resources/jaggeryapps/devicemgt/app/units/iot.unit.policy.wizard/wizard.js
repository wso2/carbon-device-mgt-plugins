function onRequest(context) {
    var userModule = require("/app/modules/user.js")["userModule"];
    var utility = require('/app/modules/utility.js').utility;
    var response = userModule.getRoles();
    var wizardPage = {};
    if (response["status"] == "success") {
        wizardPage["roles"] = response["content"];
    }
    var deviceType = context.uriParams.deviceType;
    var typesListResponse = userModule.getPlatforms();
    if (typesListResponse["status"] == "success") {
        for (var type in typesListResponse["content"]) {
            if (deviceType == typesListResponse["content"][type]["name"]){
                wizardPage["type"] = typesListResponse["content"][type];
            }
        }
    }
    return wizardPage;
}