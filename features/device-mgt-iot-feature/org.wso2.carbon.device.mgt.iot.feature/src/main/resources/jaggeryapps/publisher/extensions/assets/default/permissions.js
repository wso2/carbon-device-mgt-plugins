var tenantLoad = function(ctx) {
    var log = new Log('default-permissions');
    var Utils = ctx.utils;
    var Permissions = ctx.permissions;
    var rxtManager = ctx.rxtManager;
    var DEFAULT_ROLE = 'Internal/publisher';
    var REVIEWER_ROLE = 'Internal/reviewer';
    var tenantId = ctx.tenantId;
    var createPermission = function(type) {
        return '/permission/admin/manage/resources/govern/' + type + '/add';
    };
    var listPermission = function(type) {
        return '/permission/admin/manage/resources/govern/' + type + '/list';
    };
    var updatePermission = function(type) {
        return Utils.assetFeaturePermissionString('update', type);
    };
    var loginPermission = function() {
        return '/permission/admin/login';
    };
    var publisherLoginPermission = function(){
        return Utils.appFeaturePermissionString('login');
    };
    var assignAllPermissionsToDefaultRole = function() {
        var types = rxtManager.listRxtTypes();
        var type;
        var permissions;
        //Type specific permissions
        for (var index = 0; index < types.length; index++) {
            type = types[index];
            permissions = {};
            permissions.APP_LOGIN = publisherLoginPermission();
            permissions.ASSET_CREATE = createPermission(type);
            permissions.ASSET_LIST = listPermission(type);
            permissions.ASSET_UPDATE = updatePermission(type);
            Utils.addPermissionsToRole(permissions, DEFAULT_ROLE, tenantId);

            var staticPath = rxtManager.getStaticRxtStoragePath(type);
            staticPath = Utils.governanceRooted(staticPath);
            var actions = [constants.REGISTRY_ADD_ACTION];
            log.debug('authorized ' + DEFAULT_ROLE + ' for all actions in ' + staticPath);
            Utils.authorizeActionsForRole(tenantId, staticPath, DEFAULT_ROLE, actions);
        }
        //Non asset type specific permissions
        permissions = {};
        permissions.ASSET_LIFECYCLE = '/permission/admin/manage/resources/govern/lifecycles';
        Utils.addPermissionsToRole(permissions, DEFAULT_ROLE, tenantId);
    };
    var assignPermissionToReviewer = function(){
        var types  = rxtManager.listRxtTypes();
        var type;
        var permissions;
        for(var index = 0; index < types.length; index++){
            type = types[index];
            permissions = {};
            permissions.ASSET_LIST = listPermission(type);
            
            Utils.addPermissionsToRole(permissions,REVIEWER_ROLE,tenantId);
        }
        permissions = {};
        permissions.LOGIN = publisherLoginPermission ();
	permissions.ASSET_LIFECYCLE = '/permission/admin/manage/resources/govern/lifecycles';
        Utils.addPermissionsToRole(permissions,REVIEWER_ROLE,tenantId);
    };
    /**
     * Populates permissions that are not handled in the WSO2 permission tree
     */
    var populateAssetPermissions = function(tenantId) {
        var types = rxtManager.listRxtTypes();
        var type;
        var permissions = Permissions;
        var key;
        var permission;
        var features = ['update'];
        var feature;
        var obj;
        for (var index = 0; index < types.length; index++) {
            type = types[index];
            obj = {};
            for (var featureIndex = 0; featureIndex < features.length; featureIndex++) {
                feature = features[featureIndex];
                key = Utils.assetFeaturePermissionKey(feature, type);
                permission = Utils.assetFeaturePermissionString(feature, type);
                permissions[key] = permission;
                obj[key] = permission;
            }
            Utils.registerPermissions(obj, tenantId);
        }
    };
    var populateAppPermissions = function(tenantId) {
        var permissions = Permissions;
        var permission;
        var key;
        var features = ['login'];
        var feature;
        var obj = {};
        for(var index = 0; index < features.length; index++){
            feature = features[index];
            key = Utils.appFeaturePermissionKey(feature);
            permission = Utils.appFeaturePermissionString(feature);
            permissions[key] = permission;
            obj[key] = permission;
            //log.info('New app permission: '+key+' : '+permission);
        }
        Utils.registerPermissions(obj,tenantId);
    };

    if(log.isDebugEnabled()){
        log.debug('Starting permission operations and registering default permissions');
    }
    Permissions.APP_LOGIN = function(ctx){
        return ctx.utils.appFeaturePermissionString('login');
    };
    Permissions.ASSET_CREATE = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_CREATE permission';
        }
        return '/permission/admin/manage/resources/govern/' + ctx.type + '/add';
    };
    Permissions.ASSET_LIST = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_LIST permission';
        }
        return '/permission/admin/manage/resources/govern/' + ctx.type + '/list';
    };
    Permissions.ASSET_UPDATE = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_UPDATE permission';
        }
        return ctx.utils.assetFeaturePermissionString('update', ctx.type);
    };
    Permissions.ASSET_LIFECYCLE = '/permission/admin/manage/resources/govern/lifecycles';
    if(log.isDebugEnabled()){
        log.debug('Registering asset permissions not in the WSO2 permission tree');
    }
    populateAssetPermissions(tenantId);
    populateAppPermissions(tenantId);
    if(log.isDebugEnabled()){
        log.debug('Adding permissions to the role : ' + DEFAULT_ROLE);
    }
    assignAllPermissionsToDefaultRole();
    if(log.isDebugEnabled()){
        log.debug('Adding permissions to reviewer role');
    }
    assignPermissionToReviewer();
    if(log.isDebugEnabled()){
        log.debug('Permission operations have finished');
    }
};
