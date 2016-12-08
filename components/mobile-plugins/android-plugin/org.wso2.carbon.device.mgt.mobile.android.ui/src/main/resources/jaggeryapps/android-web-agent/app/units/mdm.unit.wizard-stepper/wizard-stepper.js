/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onRequest (context) {
    var log = new Log("wizard-stepper-unit");
    log.debug("Calling wizard-stepper-unit backend js");

    context.handlebars.registerHelper('equal', function (lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if( lvalue!=rvalue ) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });

    context.handlebars.registerHelper('unequal', function (lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if ( lvalue == rvalue ) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });

    //TODO: remove these logical calculations from helpers as it violates the vision of handlebars
    context.handlebars.registerHelper("math", function (lvalue, operator, rvalue) {
        if (arguments.length < 4)
            throw new Error("Handlebars Helper math needs 3 parameters");

        lvalue = parseFloat(lvalue);
        rvalue = parseFloat(rvalue);

        return {
            "+": lvalue + rvalue,
            "-": lvalue - rvalue,
            "*": lvalue * rvalue,
            "/": lvalue / rvalue,
            "%": lvalue % rvalue
        }[operator];
    });

    var viewModel = {};
    // converting the comma-separated list of steps in a string format in to an array
    var wizardSteps;
    if (context.unit.params.steps) {
        wizardSteps = context.unit.params.steps.split(",");
    }
    viewModel.steps = wizardSteps;
    return viewModel;
}