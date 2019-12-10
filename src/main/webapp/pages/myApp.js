(function () {
    'use strict';

    angular.module('myApp', ['ngCookies'])
        .constant('VERSION', '0.1.5')
        .constant('REST_SERVICE_URL', 'http://localhost:8080/vibrant-kafka/rest/')
        .service('restService', ['$http','$q','$log','REST_SERVICE_URL',restService])
        .controller('userController',['$scope','$cookieStore','$window','$log','restService','VERSION',userController])
        .controller('editController',['$scope','$cookieStore','$window','$log','restService','VERSION',editController])
        .controller('registerController',['$scope','$cookieStore','$window','$log','restService','VERSION',registerController])
        .controller('adminController',['$scope','$cookieStore','$window','$log','$timeout','restService','VERSION',adminController])
        .controller('topController',['$scope','$cookieStore','$window','VERSION',
            function ($scope,$cookieStore,$window,VERSION) {
                $scope.version = VERSION;
                var credentials = $cookieStore.get('credentials');
                if (!credentials) $window.location.href = 'index.html';
            }])
        .controller('validateCtrl', ['$scope','$cookieStore','$window','$log','restService','VERSION',
            function($scope,$cookieStore,$window,$log,restService,VERSION) {
                $scope.version = VERSION;
                $scope.login = function () {
                    $log.log ('User '+$scope.username+' tries to log in...');

                    restService.post('authenticate',{
                        'username': $scope.username,
                        'password': $scope.password
                    })
                        .then(
                        function (response) {
                            if (response.status === 'SUCCESS' &&
                                response.user && response.user.username && response.user.password) {
                                $cookieStore.put('credentials',response.user);
                                $log.log('Successful login for user: ' + $scope.username);
                                $window.location.href = 'user.html';
                            }else{
                                $log.log('Unsuccessful login attempt for user: ' + $scope.username);
                                $scope.username = '';
                                $scope.password = '';
                                $scope.errorMessage= response.message;
                                $scope.errorMessageShow = true;
                            }
                        },
                        function (error) {
                            $log.log('Unable to login user: ' + $scope.username);
                            $log.log(error);
                            $scope.username = '';
                            $scope.password = '';
                        }
                    );
                };
            }]);

    function userController ($scope,$cookieStore,$window,$log,restService,VERSION) {
        $scope.version = VERSION;
        var userCookie = $cookieStore.get('credentials');
        if (!userCookie) $window.location.href = 'index.html';
        $scope.username = userCookie.username;
        $scope.isAdmin = userCookie.admin;
        $scope.selectedGuid = null;
        $scope.data = userCookie;
        $scope.goEdit = function () {$window.location.href = 'edit.html';};


        $scope.tests = null;
        $scope.trains = null;
        $scope.testFFTs = null;
        $scope.trainFFTs = null;
        $scope.timestamps = null;
        $scope.benchmarks = null;
        $scope.frequencies = null;
        $scope.similarities = null;
        $scope.alerts = {};

        function updateProducer (producer,threshold,limit) {
            restService.post('getSimilaritiesByProducer', {
                'username': userCookie.username,
                'password': userCookie.password,
                'producer': producer,
                'threshold': threshold,
                'limit': limit
            })
                .then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        //$log.log (response);
                        $scope.alerts[producer] = response.similarities;
                    }else{
                        $log.log(response);
                        $scope.errorMessage = response.message;
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessage = error;
                    $scope.errorMessageShow = true;
                }
            );
        }

        function refresh () {
                        restService.post('refreshSimilarities', {
                            'username': userCookie.username,
                            'password': userCookie.password
                        })
                            .then(
                            function (response) {
                                if (response.status === 'SUCCESS') {
                                    $scope.tests = response.tests;
                                    $scope.trains = response.trains;
                                    $scope.testFFTs = response.testFFTs;
                                    $scope.trainFFTs = response.trainFFTs;
                                    $scope.timestamps = response.timestamps;
                                    $scope.benchmarks = response.benchmarks;
                                    $scope.frequencies = response.frequencies;
                                    $scope.similarities = response.similarities;

                                    var N = 1024;
                                    var producers = $('.producer'); //Object.keys($scope.similarities);
                                    if (producers.length > 0) {
                                        for (var i in producers) {
                                            var producer = producers[i].id;
                                            if (!producer) {
                                                continue;
                                            }
                                            if (!producer || !$scope.similarities.hasOwnProperty(producer)) {
                                                document.getElementById(producer).setAttribute('class', 'callout callout-gray producer');
                                                console.log('NOT FOUND PRODUCER: ' + producer);
                                                continue;
                                            }
                                            console.log('PROCESSING PRODUCER: ' + producer);
                                            var freq = $scope.frequencies[producer];
                                            updateProducer(producer, 70, 10);

                                            var div = document.getElementById(producer);
                                            if (div) {
                                                if (parseInt($scope.similarities[producer]) >= 90) {
                                                    div.setAttribute('class', 'callout callout-success producer');
                                                } else if (parseInt($scope.similarities[producer]) >= 70) {
                                                    div.setAttribute('class', 'callout callout-warning producer');
                                                } else {
                                                    div.setAttribute('class', 'callout callout-danger producer');
                                                }

                                                if (document.getElementById(producer + "Time")) {
                                                    var x = [];
                                                    for (var j = 0; j < $scope.tests[producer].length; j += 1) {
                                                        x.push(j / freq);
                                                    }
                                                    var time = [{
                                                        x: x,
                                                        y: $scope.trains[producer],
                                                        name: 'benchmark',
                                                        type: 'scatter',
                                                        mode: 'lines',
                                                        line: {
                                                            color: 'rgb(0,72,186)',
                                                            width: 1
                                                        }
                                                    }, {
                                                        x: x,
                                                        y: $scope.tests[producer],
                                                        name: 'current',
                                                        type: 'scatter',
                                                        mode: 'lines',
                                                        line: {
                                                            color: 'rgb(255,100,0)',
                                                            width: 1
                                                        }
                                                    }];
                                                    Plotly.newPlot(producer + "Time", time, {
                                                        xaxis: {
                                                            title: {
                                                                text: 'Time (sec)',
                                                                font: {
                                                                    family: 'Courier New, monospace',
                                                                    size: 15,
                                                                    color: '#5f5f5f'
                                                                }
                                                            }
                                                        },
                                                        yaxis: {
                                                            title: {
                                                                text: 'Amplitude (V)',
                                                                font: {
                                                                    family: 'Courier New, monospace',
                                                                    size: 15,
                                                                    color: '#5f5f5f'
                                                                }
                                                            }
                                                        },
                                                        autosize: false,
                                                        width: 600,
                                                        height: 350,
                                                        plot_bgcolor: '#c7c7c7',
                                                        paper_bgcolor: '#c7c7c7' //'#1f1f1f'
                                                    });
                                                }

                                                if (document.getElementById(producer + "Spectral")) {
                                                    var X = [];
                                                    for (var j = -.5; j < .5 - 1 / N; j += 1 / N) {
                                                        X.push(j * freq);
                                                    }
                                                    for (var j = 0; j < $scope.tests[producer].length; j += 1) {
                                                        x.push(j / freq);
                                                    }
                                                    var spectral = [{
                                                        x: X,
                                                        y: $scope.trainFFTs[producer],
                                                        name: 'benchmark',
                                                        type: 'scatter',
                                                        mode: 'lines',
                                                        line: {
                                                            color: 'rgb(0,72,186)',
                                                            width: 1
                                                        }
                                                    }, {
                                                        x: X,
                                                        y: $scope.testFFTs[producer],
                                                        name: 'current',
                                                        type: 'scatter',
                                                        mode: 'lines',
                                                        line: {
                                                            color: 'rgb(255,100,0)',
                                                            width: 1
                                                        }
                                                    }];
                                                    Plotly.newPlot(producer + "Spectral", spectral, {
                                                        xaxis: {
                                                            title: {
                                                                text: 'Frequency (Hz)',
                                                                font: {
                                                                    family: 'Courier New, monospace',
                                                                    size: 15,
                                                                    color: '#5f5f5f'
                                                                }
                                                            }
                                                        },
                                                        yaxis: {
                                                            type: 'log',
                                                            autorange: true,
                                                            title: {
                                                                text: 'Amplitude (V)',
                                                                font: {
                                                                    family: 'Courier New, monospace',
                                                                    size: 15,
                                                                    color: '#5f5f5f'
                                                                }
                                                            }
                                                        },
                                                        autosize: false,
                                                        width: 600,
                                                        height: 350,
                                                        plot_bgcolor: '#c7c7c7',
                                                        paper_bgcolor: '#c7c7c7' //#'#1f1f1f'
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    $log.log(response);
                                    $scope.errorMessage = response.message;
                                    $scope.errorMessageShow = true;
                                }
                            },
                            function (error) {
                                $log.log(error);
                                $scope.errorMessage = error;
                                $scope.errorMessageShow = true;
                            }
                        );
        }
        refresh();
        var intervalID = setInterval(function(){refresh();},2000);
    }

    function registerController ($scope,$cookieStore,$window,$log,restService,VERSION) {
        $scope.version = VERSION;
        for (var key in $cookieStore) {
            $cookieStore.remove(key);
        }

        $scope.register = function () {
            if ($scope.password1 === $scope.password2) {
                var concatenatedSelectedCountries = "";
                for (var i=0; i<$scope.selectCountries.length; i++) {
                    concatenatedSelectedCountries += ","+$scope.selectCountries[i];
                }
                concatenatedSelectedCountries = concatenatedSelectedCountries.substr(1);
                restService.post('register', {
                    'username': $scope.username,
                    'password': $scope.password1,
                    'firstname': $scope.firstname,
                    'lastname': $scope.lastname,
                    'msisdn': $scope.msisdn,
                    'email': $scope.email,
                    'region': concatenatedSelectedCountries
                })
                    .then(
                    function (response) {
                        if (response.status === 'SUCCESS') {
                            $cookieStore.put('credentials', [$scope.username,$scope.password1,false]);
                            $scope.modalImage = 'images/thumb.jpg';
                            $scope.modalTitle = 'Congratulations';
                            $scope.modalEnable = true;
                        } else {
                            $log.log(response);
                            $scope.errorMessage = response.message;
                            $scope.errorMessageShow = true;
                        }
                    },
                    function (error) {
                        $log.log(error);
                        $scope.errorMessage = error;
                        $scope.errorMessageShow = true;
                    }
                );
            }else{
                $log.log('Passwords do not match!');
                $scope.errorMessage = 'Passwords do not match!';
                $scope.errorMessageShow = true;
            }
        };


        $scope.countries = ['Afghanistan','Albania','Algeria','Andorra','Angola','Antigua and Deps','Argentina','Armenia','Australia','Austria','Azerbaijan','Bahamas','Bahrain','Bangladesh','Barbados','Belarus','Belgium','Belize','Benin','Bhutan','Bolivia','Bosnia Herzegovina','Botswana','Brazil','Brunei','Bulgaria','Burkina','Burundi','Cambodia','Cameroon','Canada','Cape Verde','Central African Rep','Chad','Chile','China','Colombia','Comoros','Congo','Congo Democratic Rep','Costa Rica','Croatia','Cuba','Cyprus','Czech Republic','Denmark','Djibouti','Dominica','Dominican Republic','East Timor','Ecuador','Egypt','El Salvador','Equatorial Guinea','Eritrea','Estonia','Ethiopia','Fiji','Finland','France','Gabon','Gambia','Georgia','Germany','Ghana','Greece','Grenada','Guatemala','Guinea','Guinea Bissau','Guyana','Haiti','Honduras','Hungary','Iceland','India','Indonesia','Iran','Iraq','Ireland Republic','Israel','Italy','Ivory Coast','Jamaica','Japan','Jordan','Kazakhstan','Kenya','Kiribati','Korea North','Korea South','Kosovo','Kuwait','Kyrgyzstan','Laos','Latvia','Lebanon','Lesotho','Liberia','Libya','Liechtenstein','Lithuania','Luxembourg','Macedonia North','Madagascar','Malawi','Malaysia','Maldives','Mali','Malta','Marshall Islands','Mauritania','Mauritius','Mexico','Micronesia','Moldova','Monaco','Mongolia','Montenegro','Morocco','Mozambique','Myanmar (Burma)','Namibia','Nauru','Nepal','Netherlands','New Zealand','Nicaragua','Niger','Nigeria','Norway','Oman','Pakistan','Palau','Panama','Papua New Guinea','Paraguay','Peru','Philippines','Poland','Portugal','Qatar','Romania','Russian Federation','Rwanda','St Kitts & Nevis','St Lucia','Saint Vincent & the Grenadines','Samoa','San Marino','Sao Tome & Principe','Saudi Arabia','Senegal','Serbia','Seychelles','Sierra Leone','Singapore','Slovakia','Slovenia','Solomon Islands','Somalia','South Africa','South Sudan','Spain','Sri Lanka','Sudan','Suriname','Swaziland','Sweden','Switzerland','Syria','Taiwan','Tajikistan','Tanzania','Thailand','Togo','Tonga','Trinidad and Tobago','Tunisia','Turkey','Turkmenistan','Tuvalu','Uganda','Ukraine','United Arab Emirates','United Kingdom','United States','Uruguay','Uzbekistan','Vanuatu','Vatican City','Venezuela','Vietnam','Yemen','Zambia','Zimbabwe'];
        $scope.selectCountries = [];

        $scope.$on('recycleIn', function () {
            var newone = document.getElementById('myOptionBinding').value;
            if ($scope.countries.indexOf(newone) >= 0) {
                if ($scope.selectCountries.indexOf(newone) < 0) {
                    $scope.selectCountries.push(newone);
                }
            }
            console.log ($scope.selectCountries);
        });
        $scope.$on('recycleOut', function () {
            var newone = document.getElementById('myOptionBinding').value;
            var position = $scope.selectCountries.indexOf(newone);
            if (position >= 0) {
                $scope.selectCountries.splice(position,1);
            }
            console.log ($scope.selectCountries);
        });
        $scope.$on('recyclePop', function () {
            if ($scope.selectCountries.length > 0) {
                $scope.selectCountries.splice(-1,1);
            }
            console.log ($scope.selectCountries);
        });
    }

    function adminController ($scope,$cookieStore,$window,$log,$timeout,restService,VERSION) {
        $scope.version = VERSION;
        var userCookie = $cookieStore.get('credentials');
        if (!userCookie) $window.location.href = 'index.html';
        $scope.username = userCookie.username;
        $scope.isAdmin = userCookie.admin;
        var getUsers = function () {
            if ($scope.isAdmin) {
                restService.post('getUsers', {
                    'username': userCookie.username,
                    'password': userCookie.password
                })
                    .then(
                    function (response) {
                        if (response.status === 'SUCCESS') {
                            $scope.users = response.users;
                            $timeout(function () {
                                $("#exampleX").DataTable();
                            }, 1000);
                        } else {
                            $log.log(response);
                            $scope.errorMessage = response.message;
                            $scope.errorMessageShow = true;
                        }
                    },
                    function (error) {
                        $log.log(error);
                        $scope.errorMessage = error;
                        $scope.errorMessageShow = true;
                    }
                );
            }else{
                $scope.users = [];
                $scope.errorMessage = "* You are not authorized to view this page...";
                $scope.errorMessageShow = true;
            }
        };
        $scope.activateUser = function (userToActivate) {
            restService.post ('activateUser',{
                'username': userCookie.username,
                'password': userCookie.password,
                'toActivate': userToActivate.username})
                .then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        userToActivate.status = 1;
                    }else{
                        $log.log(response);
                        $scope.errorMessage= response.message;
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessage= error;
                    $scope.errorMessageShow = true;
                }
            );
        };
        $scope.disableUser = function (userToDisable) {
            restService.post ('disableUser',{
                'username': userCookie.username,
                'password': userCookie.password,
                'toDisable': userToDisable.username})
                .then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        userToDisable.status = 0;
                    }else{
                        $log.log(response);
                        $scope.errorMessage= response.message;
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessage= error;
                    $scope.errorMessageShow = true;
                }
            );
        };
        getUsers();
    }

    function editController ($scope,$cookieStore,$window,$log,restService,VERSION) {
        $scope.version = VERSION;
        $scope.countries = ['Afghanistan','Albania','Algeria','Andorra','Angola','Antigua and Deps','Argentina','Armenia','Australia','Austria','Azerbaijan','Bahamas','Bahrain','Bangladesh','Barbados','Belarus','Belgium','Belize','Benin','Bhutan','Bolivia','Bosnia Herzegovina','Botswana','Brazil','Brunei','Bulgaria','Burkina','Burundi','Cambodia','Cameroon','Canada','Cape Verde','Central African Rep','Chad','Chile','China','Colombia','Comoros','Congo','Congo Democratic Rep','Costa Rica','Croatia','Cuba','Cyprus','Czech Republic','Denmark','Djibouti','Dominica','Dominican Republic','East Timor','Ecuador','Egypt','El Salvador','Equatorial Guinea','Eritrea','Estonia','Ethiopia','Fiji','Finland','France','Gabon','Gambia','Georgia','Germany','Ghana','Greece','Grenada','Guatemala','Guinea','Guinea Bissau','Guyana','Haiti','Honduras','Hungary','Iceland','India','Indonesia','Iran','Iraq','Ireland Republic','Israel','Italy','Ivory Coast','Jamaica','Japan','Jordan','Kazakhstan','Kenya','Kiribati','Korea North','Korea South','Kosovo','Kuwait','Kyrgyzstan','Laos','Latvia','Lebanon','Lesotho','Liberia','Libya','Liechtenstein','Lithuania','Luxembourg','Macedonia North','Madagascar','Malawi','Malaysia','Maldives','Mali','Malta','Marshall Islands','Mauritania','Mauritius','Mexico','Micronesia','Moldova','Monaco','Mongolia','Montenegro','Morocco','Mozambique','Myanmar (Burma)','Namibia','Nauru','Nepal','Netherlands','New Zealand','Nicaragua','Niger','Nigeria','Norway','Oman','Pakistan','Palau','Panama','Papua New Guinea','Paraguay','Peru','Philippines','Poland','Portugal','Qatar','Romania','Russian Federation','Rwanda','St Kitts & Nevis','St Lucia','Saint Vincent & the Grenadines','Samoa','San Marino','Sao Tome & Principe','Saudi Arabia','Senegal','Serbia','Seychelles','Sierra Leone','Singapore','Slovakia','Slovenia','Solomon Islands','Somalia','South Africa','South Sudan','Spain','Sri Lanka','Sudan','Suriname','Swaziland','Sweden','Switzerland','Syria','Taiwan','Tajikistan','Tanzania','Thailand','Togo','Tonga','Trinidad and Tobago','Tunisia','Turkey','Turkmenistan','Tuvalu','Uganda','Ukraine','United Arab Emirates','United Kingdom','United States','Uruguay','Uzbekistan','Vanuatu','Vatican City','Venezuela','Vietnam','Yemen','Zambia','Zimbabwe'];
        $scope.selectCountries = [];

        var userCookie = $cookieStore.get('credentials');
        if (!userCookie) $window.location.href = 'index.html';
        $scope.username = userCookie.username;
        $scope.isAdmin = userCookie.admin;
        $scope.data = userCookie;
        var whoami = function () {
            if (userCookie) {
                var user = userCookie;
                if (user.region.indexOf(',') >= 0) {
                    var splitRegions = user.region.split(',');
                     for (var i=0; i<splitRegions.length; i+=1) {
                        $scope.selectCountries.push(splitRegions[i]);
                     }
                }else{
                    $scope.selectCountries.push(user.region);
                }
                console.log('$scope.selectCountries: ' + $scope.selectCountries);
            }else{
                $window.location.href = 'index.html';
            }
            document.getElementsByClassName('modal')[0].style.display = 'none';
        };
        whoami();

        $scope.update = function () {
            restService.post ('updateUserDetails',{
                'username': userCookie.username,
                'password': userCookie.password,
                'firstname': $scope.data.firstname,
                'lastname': $scope.data.lastname,
                'email': $scope.data.email,
                'msisdn': $scope.data.msisdn,
                'region': $scope.data.region
            })
                .then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        $window.location.href = 'user.html';
                    }else{
                        $log.log(response);
                        $scope.errorMessage= response.message;
                        $scope.errorMessageShow = true;
                    }
                    document.getElementsByClassName('modal')[0].style.display = 'none';
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessage= error;
                    $scope.errorMessageShow = true;
                    document.getElementsByClassName('modal')[0].style.display = 'none';
                }
            );
        };

        $scope.$on('recycleIn', function () {
            var newone = document.getElementById('myOptionBinding').value;
            if ($scope.countries.indexOf(newone) >= 0) {
                if ($scope.selectCountries.indexOf(newone) < 0) {
                    $scope.selectCountries.push(newone);
                }
            }
        });
        $scope.$on('recycleOut', function () {
            var newone = document.getElementById('myOptionBinding').value;
            var position = $scope.selectCountries.indexOf(newone);
            if (position >= 0) {
                $scope.selectCountries.splice(position,1);
            }
        });
    }

    function restService ($http,$q,$log,REST_SERVICE_URL) {
        return {
            get : function (suffix) {
                var deferred = $q.defer();
                $http({
                    method: 'GET',
                    url: REST_SERVICE_URL + suffix,
                    parameters: {}
                }).then(
                    function (response) {
                        //$log.log(response.data);
                        deferred.resolve(response.data);
                    }, function (error) {
                        $log.log("ERROR COMMUNICATION WITH " + REST_SERVICE_URL + suffix);
                        $log.log(error);
                        deferred.reject(error);
                    });
                return deferred.promise;
            },
            post : function (suffix,body) {
                var deferred = $q.defer();
                $http({
                    method: 'POST',
                    url: REST_SERVICE_URL + suffix,
                    headers: {'Content-Type': 'application/json'},
                    data: body
                }).then(
                    function (response) {
                        //$log.log(response.data);
                        deferred.resolve(response.data);
                    }, function (error) {
                        $log.log("ERROR COMMUNICATION WITH " + REST_SERVICE_URL + suffix);
                        $log.log(error);
                        deferred.reject(error);
                    });
                return deferred.promise;
            }
        };
    }
})();
