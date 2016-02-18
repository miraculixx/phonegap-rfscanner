/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
         controller = new Controller();

        console.log('Received Event: ' + id);
    }
};

app.initialize();
var watchID = null;
var Controller = function() {
    var controller = {
        self: null,
        initialize: function() {
            self = this;
            this.bindEvents();            
        },

         bindEvents: function() {
            console.log("Listener started");
			$(".start").on("click", this.onStart);
			$(".stop").on("click", this.onStop);
             $("#selectNum").on("click",this.addCoords);
           // navigator.geolocation.getCurrentPosition(this.onSuccess, this.onError);
           //watchID = navigator.geolocation.watchPosition(this.onWatchSuccess, this.onError, { timeout: 30000 });
        },
    addCoords: function(){
        
        var coordNum = $("#coordNumber").val();
        var DivHtml = "";
        for (var i=1; i<=coordNum; i++)
        {
            DivHtml+="<h2>Coord"+i+":</h2>";
            DivHtml+="<div class='coord'><label value='Coord"+i+":'></label>";
            DivHtml+="<input value='Office'><input value='45.5'><input value='8.2'><input value='100'></div>";
        }
        $("#coordDiv").html(DivHtml);
        
    },
		onStart: function(){
            var interval = parseInt($("#interval").val())*1000;
            if(interval <60000 && interval>0)
            {
            var coords = ""; var coordValues = [];
            $(".coord").each(function(index){
                             $(this).children("input").each(function(ind){
                                                            coordValues[ind] = $(this).val();
                                                            });
                             coords +="{'identfier':" +coordValues[0]+",'lat':"+coordValues[1]+",'lon':"+coordValues[2]+",'radius':"+coordValues[3]+"}"+";";
                             });
            /*$(".coord1 input").each(function(index){
                                    coord[index]=$(this).val();
                                    });
            
            var coord1 = "{'identfier':" +coord[0]+",'lat':"+coord[1]+",'lon':"+coord[2]+",'radius':"+coord[3]+"}";
            
            
            $(".coord2 input").each(function(index){
                                    coord[index]=$(this).val();
                                    });
            var coord2 = "{'identfier':" +coord[0]+",'lat':"+coord[1]+",'lon':"+coord[2]+",'radius':"+coord[3]+"}";
            */
                alert(coords);
            var serverUrl = $(".server input").val();
			cordova.exec(success, failure, 'ScanDevice', 'start', [coords,serverUrl,interval]);
            }
            else
                showAlert("You have to set interval between 0~60s");
		},
		onStop: function() {
			cordova.exec(success, failure, 'ScanDevice', 'stop', []);
		},
        
        onError: function() {
           

        }
    }
    controller.initialize();
    return controller;
}
function success(message){
     showAlert(message);
}
function failure(){
     showAlert("Action was failured.");
}
function showAlert(message) {
        navigator.notification.alert(
            message,  // message
            alertDismissed,         // callback
            'Get Gelocation',            // title
            'Done'                  // buttonName
        );
    }
function alertDismissed(){
    
}