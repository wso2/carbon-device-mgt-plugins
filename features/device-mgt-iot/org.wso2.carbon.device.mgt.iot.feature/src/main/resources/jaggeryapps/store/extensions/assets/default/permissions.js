var tenantLoad = function(ctx) {
    var log = new Log();
    var Utils = ctx.utils;
    var Permissions = ctx.permissions;
    var rxtManager = ctx.rxtManager;
    var DEFAULT_ROLE = 'Internal/store';
    var tenantId = ctx.tenantId;
    var ANON_ROLE = Utils.anonRole(tenantId);
    var listPermission = function(type) {
        return Utils.assetFeaturePermissionString('list', type); //'/permission/admin/manage/resources/govern/' + type + '/list';
    };
    var bookmarkPermission = function(type) {
        return Utils.assetFeaturePermissionString('bookmark', type);
    };
    var reviewPermission = function(type) {
        return Utils.assetFeaturePermissionString('reviews', type);
    };
    var storeLoginPermission = function(){
        return Utils.appFeaturePermissionString('login');
    };
    var myitemsPermission = function(){
        return Utils.appFeaturePermissionString('myitems');
    };
    var populateAssetPermissions = function(tenantId) {
        var types = rxtManager.listRxtTypes();
        var type;
        var permissions = Permissions;
        var permission;
        var features = ['list', 'bookmark', 'reviews'];
        var feature;
        var key;
        var obj;
        for (var index = 0; index < types.length; index++) {
            obj = {};
            type = types[index];
            for (var featureIndex = 0; featureIndex < features.length; featureIndex++) {
                feature = features[featureIndex];
                key = Utils.assetFeaturePermissionKey(feature, type);
                permission = Utils.assetFeaturePermissionString(feature, type);
                obj[key] = permission;
                permissions[key] = permission;
            }
            Utils.registerPermissions(obj, tenantId);
        }
    };
    var populateAppPermissions = function(tenantId) {
        var permissions = Permissions;
        var permission;
        var key;
        var features = ['myitems','login'];
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
    var assignAllPermissionsToDefaultRole = function() {
        var types = rxtManager.listRxtTypes();
        var type;
        var permissions;
        //Type specific permissions
        for (var index = 0; index < types.length; index++) {
            type = types[index];
            permissions = {};
            permissions.ASSET_LIST = listPermission(type);
            permissions.ASSET_BOOKMARK = bookmarkPermission(type);
            permissions.ASSET_REVIEWS = reviewPermission(type);
            permissions.APP_LOGIN = storeLoginPermission();
            permissions.APP_MYITEMS = myitemsPermission();
            Utils.addPermissionsToRole(permissions, DEFAULT_ROLE, tenantId);
        }
    };
    var assignPermissionsToAnonRole = function(tenantId) {
        var types = rxtManager.listRxtTypes();
        var type;
        var permissions;
        for (var index = 0; index < types.length; index++) {
            type = types[index];
            permissions = {};
            permissions.ASSET_LIST = listPermission(type);
            permissions.ASSET_REVIEWS = reviewPermission(type);
            Utils.addPermissionsToRole(permissions, ANON_ROLE, tenantId);
        }
    };
    if(log.isDebugEnabled()){
        log.debug('Starting permission operations and registering default permissions');
    }
    Permissions.ASSET_LIST = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_LIST permission';
        }
        return ctx.utils.assetFeaturePermissionString('list', ctx.type);
    };
    Permissions.ASSET_BOOKMARK = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_BOOKMARK permission';
        }
        return ctx.utils.assetFeaturePermissionString('bookmark', ctx.type);
    };
    Permissions.ASSET_REVIEWS = function(ctx) {
        if (!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_REVIEWS permission';
        }
        return ctx.utils.assetFeaturePermissionString('reviews', ctx.type);
    };
    Permissions.ASSET_DETAILS = function(ctx) {
        if(!ctx.type) {
            throw 'Unable to resolve type to determine the ASSET_DETAILS permission';
        }
        return ctx.utils.assetFeaturePermissionString('list', ctx.type);
    };
    Permissions.APP_MYITEMS = function(ctx){
        return ctx.utils.appFeaturePermissionString('myitems');
    };
    Permissions.APP_LOGIN = function(ctx){
        return ctx.utils.appFeaturePermissionString('login');
    };
    if(log.isDebugEnabled()){
        log.debug('Registering asset permissions not in the WSO2 permission tree');
    }
    populateAssetPermissions(tenantId);
    populateAppPermissions(tenantId);
    if(log.isDebugEnabled()){
        log.debug('Adding permissions to role: ' + DEFAULT_ROLE);
    }
    assignAllPermissionsToDefaultRole();
    if(log.isDebugEnabled()){
        log.debug('Registering store anonymous role : ' + ANON_ROLE);
        log.debug('Anonymous role registered successfully : ' + Utils.addRole(ANON_ROLE));
        log.debug('Assigning store permissions to anonymous role');
    }
    assignPermissionsToAnonRole(tenantId);
    if(log.isDebugEnabled()){
        log.debug('Permission operations have finished.');
    }
};