

var camera, scene, renderer;
var cameraControls;
var clock = new THREE.Clock();

var ground = true;
var circle1, plate1;

var object_maker = function(){
    var make_object = this;
    make_object.fillScene = function() {
        scene = new THREE.Scene();
        scene.fog = new THREE.Fog( 0x808080, 2000, 4000 );


        var ambientLight = new THREE.AmbientLight( 0x222222 );
        var light = new THREE.DirectionalLight( 0xFFFFFF, 1.0 );
        light.position.set( 200, 400, 500 );

        var light2 = new THREE.DirectionalLight( 0xFFFFFF, 1.0 );
        light2.position.set( -500, 250, -200 );

        scene.add(ambientLight);
        scene.add(light);
        scene.add(light2);

        if (ground) {
            Coordinates.drawGround({size:10000});
        }


        var robotBaseMaterial = new THREE.MeshPhongMaterial( { color: 0x6E23BB, specular: 0x6E23BB, shininess: 20 } );
        var robotForearmMaterial = new THREE.MeshPhongMaterial( { color: 0xF4C154, specular: 0xF4C154, shininess: 100 } );
        //var robotUpperArmMaterial = new THREE.MeshPhongMaterial( { color: 0x95E4FB, specular: 0x95E4FB, shininess: 100 } );


        circle1 = new THREE.Object3D();
        var circleLength=40;
        make_object.addCircles(circle1,circleLength,robotBaseMaterial);
        circle1.position.y=circleLength*2;
        scene.add(circle1);

        plate1 = new THREE.Object3D();
        var plateLength=40;
        make_object.addPlates(plate1,plateLength,robotForearmMaterial);
        plate1.position.y=circleLength/8;
        circle1.add(plate1);

    },

    make_object.addPlates = function (part,plateLength,material){

        var cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.position.x = plateLength+15;
        part.add( cylinder );
        cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.rotation.z = 90 * Math.PI/180;
        cylinder.position.x = plateLength+15;
        part.add(cylinder);

        cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.position.x = -plateLength-15;
        part.add( cylinder );
        cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.rotation.z = 90 * Math.PI/180;
        cylinder.position.x = -plateLength-15;
        part.add( cylinder );

        cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.position.z = -plateLength-15;
        part.add( cylinder );
        cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.rotation.z = 90 * Math.PI/180;
        cylinder.position.z = -plateLength-15;
        part.add( cylinder );
        //
        cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.position.z = plateLength+15;
        part.add( cylinder );
        cylinder = new THREE.Mesh(
            new THREE.CylinderGeometry( 5, 5, 40, 32 ), material );
        cylinder.rotation.x = 90 * Math.PI/180;
        cylinder.rotation.z = 90 * Math.PI/180;
        cylinder.position.z = plateLength+15;
        part.add( cylinder );

    },

    make_object.addCircles = function (part,circleLength,material){
        var circle = new THREE.Mesh(
            new THREE.TorusGeometry(40,10,40,20,6.3), material );
        circle.position.x = circleLength+10;
        circle.rotation.x= 90 * Math.PI/180;
        part.add(circle );
        circle = new THREE.Mesh(
            new THREE.TorusGeometry(40,10,40,20,6.3), material );
        circle.position.x = -circleLength-10;
        circle.rotation.x= 90 * Math.PI/180;
        part.add(circle );
        circle = new THREE.Mesh(
            new THREE.TorusGeometry(40,10,40,20,6.3), material );
        circle.position.z = -circleLength-10;
        circle.rotation.x= 90 * Math.PI/180;
        part.add(circle );
        circle = new THREE.Mesh(
            new THREE.TorusGeometry(40,10,40,20,6.3), material );
        circle.position.z = circleLength+10;
        circle.rotation.x= 90 * Math.PI/180;
        part.add(circle );
    },

    make_object.init = function(holder, object_width, object_height) {
        //var canvasWidth = 325;
        //var canvasHeight = 220;
        var canvasRatio = 1;
        renderer = new THREE.WebGLRenderer( { antialias: true } );
        renderer.gammaInput = true;
        renderer.gammaOutput = true;
        renderer.setSize(object_width, object_height);
        renderer.setClearColorHex( 0xAAAAAA, 1.0 );

        $(holder).append(renderer.domElement);
        camera = new THREE.PerspectiveCamera( 30, canvasRatio, 1, 10000 );
        camera.position.set( -510, 240, 100 );
        cameraControls = new THREE.OrbitAndPanControls(camera, renderer.domElement);
        cameraControls.target.set(0,100,0);
        make_object.fillScene();

    },
    make_object.animate =function () {
        window.requestAnimationFrame(make_object.animate);
        make_object.render();
    },
    make_object.render = function () {
        var delta = clock.getDelta();
        cameraControls.update(delta);
        circle1.rotation.z = config_api.effectController.uz;
        circle1.rotation.y = config_api.effectController.uy;	// yaw
        circle1.rotation.x = config_api.effectController.ux;	// roll
        circle1.position.z = config_api.effectController.fz;
        circle1.position.x = config_api.effectController.fx;
        renderer.render(scene, camera);
    },
    make_object.get_heading_attitude_bank = function(data){
        /*return {"heading": Math.atan2(2*data.qy*data.qw-2*data.qx*data.qz , 1 - 2*data.qy*data.qy - 2*data.qz*data.qz),
                "attitude": Math.asin(2*data.qx*data.qy + 2*data.qz*data.qw),
                "bank" : Math.atan2(2*data.qx*data.qw-2*data.qy*data.qz, 1 - 2*data.qx*data.qx - 2*data.qz*data.qz)
        };*/
        if(data.length<4){
            return {"heading": data[0],"attitude": data[1], "bank":data[2]};
        }else{

            var heading = Math.atan2(2*data[1]*data[3]-2*data[0]*data[1] , 1 - 2*data[1]*data[1] - 2*data[2]*data[2]);
            var bank = Math.atan2(2*data[0]*data[3]-2*data[1]*data[2], 1 - 2*data[0]*data[0] - 2*data[2]*data[2]);
            var attitude = Math.asin(2*data[0]*data[1] + 2*data[2]*data[3]);

            return {"heading": isNaN(heading) ? 0: heading,
                    "bank": isNaN(bank) ? 0: bank,
                    "attitude": isNaN(attitude) ? 0: attitude
            };
        }
    },
    make_object.set_heading_attitude_bank = function(data){
        config_api.effectController.uy=data.heading;
        config_api.effectController.uz=data.attitude;
        config_api.effectController.ux=data.bank;
    },
    make_object.set_heading = function(holder, heading){
        //$(holder).rotate((180/Math.PI)*heading);
    },
    make_object.set_bank = function(holder, bank){
        //$(holder).rotate((180/Math.PI)*bank);
    }


};


