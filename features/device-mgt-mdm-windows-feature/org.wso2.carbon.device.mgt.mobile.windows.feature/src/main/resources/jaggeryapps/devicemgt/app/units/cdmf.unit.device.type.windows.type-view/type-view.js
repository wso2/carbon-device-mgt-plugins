function onRequest(context){
    var viewModel = {};
    var devicemgtProps = require('/app/conf/devicemgt-props.js').config();
    viewModel.enrollmentURL = devicemgtProps.enrollmentURL;
    return viewModel;
}