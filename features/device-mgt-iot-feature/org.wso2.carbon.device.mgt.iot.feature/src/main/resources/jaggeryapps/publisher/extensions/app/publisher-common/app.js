/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
app.server = function(ctx) {
    return {
        endpoints: {
            pages: [{
                title: 'Publisher | Splash page',
                url: 'splash',
                path: 'splash.jag'
            },{
                url:'sso-login',
                path:'sso-auth-login-controller.jag'
            },{
                url:'basic-auth-login',
                path:'basic-auth-login-controller.jag'
            },{
                url:'sso-logout',
                path:'sso-auth-logout-controller.jag'
            },{
                url:'basic-auth-logout',
                path:'basic-auth-logout-controller.jag'
            },{
                url:'basic-authenticator',
                path:'basic-authenticator.jag'
            },{
                title:'Advanced Search',
                url:'advanced-search',
                path:'advanced-search.jag'
            }]
        },
        configs: {
            landingPage: '/assets/gadget/list',
            disabledAssets: ['ebook', 'api', 'wsdl', 'service','policy','proxy','schema','sequence','servicex','uri','wadl','endpoint','swagger','restservice','comments','soapservice'],
            uiDisabledAssets: []
        },
        onLoadedServerConfigs:function(configs){
        }
    }
};

app.renderer = function(ctx) {
    var decoratorApi = require('/modules/page-decorators.js').pageDecorators;
    return {
        pageDecorators: {
            navigationBar: function(page) {
                return decoratorApi.navigationBar(ctx, page, this);
            }
        }
    }
};