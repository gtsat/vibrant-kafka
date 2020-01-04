(function () {
    'use strict';

    angular.module('myApp', ['ngCookies'])
        .constant('VERSION', '0.4.5')
        .constant('REST_SERVICE_URL', 'http://localhost:8080/vibrant-kafka/rest/')
        //.constant('REST_SERVICE_URL', 'http://homegrown.ddns.net:8080/vibrant-kafka/rest/')
        .service('restService', ['$http','$q','$log','REST_SERVICE_URL',restService])
        .controller('editController',['$scope','$cookieStore','$window','$log','restService','VERSION',editController])
        .controller('registerController',['$scope','$cookieStore','$window','$log','restService','VERSION',registerController])
        .controller('adminController',['$scope','$cookieStore','$window','$log','$timeout','restService','VERSION',adminController])
        .controller('userController',['$scope','$rootScope','$cookieStore','$window','$log','restService','VERSION',userController])
        .controller('topController',['$scope','$cookieStore','$window','VERSION',
            function ($scope,$cookieStore,$window,VERSION) {
                $scope.version = VERSION;
                var credentials = $cookieStore.get('credentials');
                if (!credentials) $window.location.href = 'index.html';
            }])
        .controller('validateCtrl', ['$scope','$cookieStore','$window','$log','restService','VERSION',
            function($scope,$cookieStore,$window,$log,restService,VERSION) {
                $scope.version = VERSION;
                $scope.errorMessages = [];
                $scope.login = function () {
                    $log.log ('User '+$scope.username+' tries to log in...');

                    restService.post('authenticate',{
                        'username': $scope.username,
                        'password': $scope.password
                    }).then(
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
                                $scope.errorMessages.push (response.message);
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

    function userController ($scope,$rootScope,$cookieStore,$window,$log,restService,VERSION) {
        $scope.version = VERSION;
        $scope.errorMessages = [];
        var userCookie = $cookieStore.get('credentials');
        if (!userCookie) $window.location.href = 'index.html';
        $scope.username = userCookie.username;
        $scope.isAdmin = userCookie.admin;
        $scope.data = userCookie;

        $scope.selectedCategory = 'All Categories';
        $scope.dropdownCategories = ['All Categories'];
        $scope.cardinalities = {'All Categories': 0};

        $scope.tests = null;
        $scope.trains = null;
        $scope.testFFTs = null;
        $scope.trainFFTs = null;
        $scope.timestamps = null;
        $scope.benchmarks = null;
        $scope.categories = null;
        $scope.motionUrls = null;
        $scope.frequencies = null;
        $scope.similarities = null;
        $scope.events = {};

        $scope.hiddenPlots = [];
        $scope.hidePlots = function (producer) {
          if (!$scope.hiddenPlots.includes(producer)) {
              $scope.hiddenPlots.push(producer);
          }
        };
        $scope.showPlots = function (producer) {
            var pos = $scope.hiddenPlots.indexOf(producer);
            if (pos >= 0) {
                $scope.hiddenPlots.splice(pos,1);
            }
        };

        $rootScope.hiddenProducers = [];
        $scope.hideProducer = function (producer) {
            if (!$rootScope.hiddenProducers.includes(producer)) {
                $rootScope.hiddenProducers.push(producer);
            }
        };
        $scope.showProducers = function () {
            while($rootScope.hiddenProducers.length>0) {
                var tmp = $rootScope.hiddenProducers.pop();
            }
        };

        $scope.timelines = [];
        $scope.toggleTimeline = function (producer) {
            if ($scope.timelines.includes(producer)) {
                var pos = $scope.timelines.indexOf(producer);
                $scope.timelines.splice(pos,1);
            }else{
                $scope.timelines.push(producer);
                plotTimeline (producer);
            }
        };

        function plotTimelineWithoutBenchmarks (xEvents,yEvents) {
            var timeline = [{
                x: xEvents,
                y: yEvents,
                name: 'sample',
                type: 'scatter',
                mode: 'lines',
                line: {
                    color: 'rgb(255,100,0)',
                    width: 1
                }
            }];
            Plotly.newPlot(producer + "Timeline", timeline, {
                xaxis: {
                    autorange: true,
                    automargin: true,
                    title: {
                        text: 'Date',
                        font: {
                            family: 'Courier New, monospace',
                            size: 15,
                            color: '#5f5f5f'
                        }
                    }
                },
                yaxis: {
                    scaleratio: 1.0,
                    autorange: true,
                    automargin: true,
                    title: {
                        text: 'Similarity',
                        font: {
                            family: 'Courier New, monospace',
                            size: 15,
                            color: '#5f5f5f'
                        }
                    }
                },
                autosize: true,
                plot_bgcolor: '#c7c7c7',
                paper_bgcolor: '#c7c7c7'
            }, {responsive: true});
        }

        function plotTimeline (producer) {
            restService.post('getEvents', {
                'username': userCookie.username,
                'password': userCookie.password,
                'producer': producer
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        var events = response.events;
                        var yEvents = [], xEvents = [];
                        for (var i in events) {
                            yEvents.push(events[i].similarity);
                            xEvents.push(events[i].creationDate);
                        }

                        restService.post('getBenchmarkHistory', {
                            'username': userCookie.username,
                            'password': userCookie.password,
                            'producer': producer
                        }).then(
                            function (response) {
                                if (response.status === 'SUCCESS') {
                                    var benchmarks = response.benchmarks;
                                    var yBenchmarks = [], xBenchmarks = [];
                                    for (var i in benchmarks) {
                                        yBenchmarks.push(99);
                                        xBenchmarks.push(benchmarks[i].creationDate);
                                    }

                                    var timeline = [{
                                        x: xBenchmarks,
                                        y: yBenchmarks,
                                        name: 'reset',
                                        type: 'scatter',
                                        mode: 'markers',
                                        marker: {
                                            color: 'rgb(0,72,186)',
                                            size: 15
                                        }
                                     }, {
                                        x: xEvents,
                                        y: yEvents,
                                        name: 'sample',
                                        type: 'scatter',
                                        mode: 'lines',
                                        line: {
                                            color: 'rgb(255,100,0)',
                                            width: 1
                                        }
                                    }];
                                    Plotly.newPlot(producer + "Timeline", timeline, {
                                        xaxis: {
                                            autorange: true,
                                            automargin: true,
                                            title: {
                                                text: 'Date',
                                                font: {
                                                    family: 'Courier New, monospace',
                                                    size: 15,
                                                    color: '#5f5f5f'
                                                }
                                            }
                                        },
                                        yaxis: {
                                            scaleratio: 1.0,
                                            autorange: true,
                                            automargin: true,
                                            title: {
                                                text: 'Similarity',
                                                font: {
                                                    family: 'Courier New, monospace',
                                                    size: 15,
                                                    color: '#5f5f5f'
                                                }
                                            }
                                        },
                                        autosize: true,
                                        plot_bgcolor: '#c7c7c7',
                                        paper_bgcolor: '#c7c7c7'
                                    }, {responsive: true});
                                }else{
                                    $log.log(response);
                                    $scope.errorMessages.push (response.message);
                                    plotTimelineWithoutBenchmarks(xEvents,yEvents);
                                    $scope.errorMessageShow = true;
                                }
                            },
                            function (error) {
                                $log.log(error);
                                $scope.errorMessages.push (error.data);
                                plotTimelineWithoutBenchmarks(xEvents,yEvents);
                                $scope.errorMessageShow = true;
                            }
                        );
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
                    $scope.errorMessageShow = true;
                }
            );
        }

        $scope.resetAllProducers = function () {
            restService.post('resetAllProducers', {
                'username': userCookie.username,
                'password': userCookie.password
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        ;
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
                    $scope.errorMessageShow = true;
                }
            );
        };

        $scope.resetProducer = function (producer) {
            restService.post('resetProducer', {
                'username': userCookie.username,
                'password': userCookie.password,
                'producer': producer
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        ;
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
                    $scope.errorMessageShow = true;
                }
            );
        };

        $scope.clearEvents = function (producer) {
            restService.post('clearEvents', {
                'username': userCookie.username,
                'password': userCookie.password,
                'producer': producer
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        ;
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
                    $scope.errorMessageShow = true;
                }
            );
        };

        function updateProducer (producer,threshold,limit) {
            restService.post('getEvents', {
                'username': userCookie.username,
                'password': userCookie.password,
                'producer': producer,
                'threshold': threshold,
                'limit': limit
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        $scope.events[producer] = response.events;
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
                    $scope.errorMessageShow = true;
                }
            );
        }

        function refresh () {
            if ($scope.intervalID) {
                restService.post('updateConsumer', {
                    'username': userCookie.username,
                    'password': userCookie.password,
                    'category': ($scope.selectedCategory != 'All Categories' ? $scope.selectedCategory : null)
                }).then(
                    function (response) {
                        if (response.status === 'SUCCESS') {
                            $scope.tests = response.tests;
                            $scope.trains = response.trains;
                            $scope.testFFTs = response.testFFTs;
                            $scope.trainFFTs = response.trainFFTs;
                            $scope.timestamps = response.timestamps;
                            $scope.benchmarks = response.benchmarks;
                            $scope.categories = response.categories;
                            $scope.frequencies = response.frequencies;
                            $scope.similarities = response.similarities;
                            $scope.motionUrls = response.motionUrls;

                            var N = 256;
                            var producers = $('.producer');
                            if (producers.length > 0) {
                                for (var i in producers) {
                                    var producer = producers[i].id;
                                    if (!producer) {
                                        continue;
                                    }
                                    if (!producer || !$scope.similarities.hasOwnProperty(producer)) {
                                        document.getElementById(producer).setAttribute('class', 'callout callout-gray producer');
                                        //console.log('NOT FOUND PRODUCER: ' + producer);
                                        continue;
                                    }
                                    if ($scope.selectedCategory != 'All Categories' && $scope.categories[producer] != $scope.selectedCategory) {
                                        console.log('1. SKIPPING PRODUCER: ' + producer+'('+$scope.selectedCategory+')');
                                        continue;
                                    }
                                    if ($rootScope.hiddenProducers.includes(producer)) {
                                        console.log('2. SKIPPING PRODUCER: ' + producer+'('+$rootScope.hiddenProducers+')');
                                        continue;
                                    }
                                    console.log('PROCESSING PRODUCER: ' + producer);

                                    var freq = $scope.frequencies[producer];
                                    updateProducer(producer, 70, 14);

                                    var div = document.getElementById(producer);
                                    if (div) {
                                        if (parseInt($scope.similarities[producer]) >= 90) {
                                            div.setAttribute('class', 'callout callout-success producer');
                                        } else if (parseInt($scope.similarities[producer]) >= 70) {
                                            div.setAttribute('class', 'callout callout-warning producer');
                                        } else {
                                            div.setAttribute('class', 'callout callout-danger producer');
                                        }

                                        if ($scope.tests[producer] && $scope.trains[producer] && document.getElementById(producer + "Hist")) {
                                            var x = [];
                                            for (var j = -($scope.tests[producer].length >> 1); j < $scope.tests[producer].length >> 1; j += 1) {
                                                x.push(j / freq);
                                            }
                                            var ampl = [{
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
                                            Plotly.newPlot(producer + "Hist", ampl, {
                                                xaxis: {
                                                    autorange: true,
                                                    automargin: true,
                                                    title: {
                                                        text: 'Amplitude (V)',
                                                        font: {
                                                            family: 'Courier New, monospace',
                                                            size: 15,
                                                            color: '#5f5f5f'
                                                        }
                                                    }
                                                },
                                                yaxis: {
                                                    type: 'log',
                                                    scaleratio: 1.0,
                                                    autorange: true,
                                                    automargin: true,
                                                    title: {
                                                        text: 'Occurrences (#Samples)',
                                                        font: {
                                                            family: 'Courier New, monospace',
                                                            size: 15,
                                                            color: '#5f5f5f'
                                                        }
                                                    }
                                                },
                                                autosize: true,
                                                plot_bgcolor: '#c7c7c7',
                                                paper_bgcolor: '#c7c7c7'
                                            }, {responsive: true});
                                        }

                                        if ($scope.testFFTs[producer] && $scope.trainFFTs[producer] && document.getElementById(producer + "Spectral")) {
                                            var X = [];
                                            for (var j = -.5; j < .5 - 1 / N; j += 1 / N) {
                                                X.push(j * freq);
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
                                                    autorange: true,
                                                    automargin: true,
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
                                                    scaleratio: 1.0,
                                                    autorange: true,
                                                    automargin: true,
                                                    title: {
                                                        text: 'Amplitude (V)',
                                                        font: {
                                                            family: 'Courier New, monospace',
                                                            size: 15,
                                                            color: '#5f5f5f'
                                                        }
                                                    }
                                                },
                                                autosize: true,
                                                plot_bgcolor: '#c7c7c7',
                                                paper_bgcolor: '#c7c7c7'
                                            }, {responsive: true});
                                        }
                                    }
                                }
                                $scope.dropdownCategories = ['All Categories'];
                                var categoriesWithDuplicates = Object.values($scope.categories);
                                $scope.cardinalities = {'All Categories': categoriesWithDuplicates.length};
                                for (var c in categoriesWithDuplicates) {
                                    if (!$scope.dropdownCategories.includes(categoriesWithDuplicates[c])) {
                                        $scope.dropdownCategories.push(categoriesWithDuplicates[c]);
                                        $scope.cardinalities[categoriesWithDuplicates[c]] = 1;
                                    } else {
                                        $scope.cardinalities[categoriesWithDuplicates[c]] += 1;
                                    }
                                }
                            }
                        } else {
                            $log.log(response);
                            $scope.errorMessages.push(response.message);
                            $scope.errorMessageShow = true;
                        }
                    },
                    function (error) {
                        $log.log(error);
                        $scope.errorMessages.push(error.data);
                        $scope.errorMessageShow = true;
                    }
                );
            }
        }
        $scope.convertUnixTime = function (timestamp) {return new Timestamp(timestamp).toISOString().replace(/[a-zA-Z]/g,' ');};
        $scope.goEdit = function () {$window.location.href = 'edit.html';};

        $scope.intervalID = setInterval(function(){refresh();},2500);
        $log.log('intervalID: '+$scope.intervalID);
        $scope.freeze = function () {$log.log('%% FROZEN!');clearInterval($scope.intervalID);$scope.intervalID=null;$log.log('intervalID: '+$scope.intervalID);};
        $scope.unfreeze = function () {$log.log('%% UNFROZEN!'); $scope.intervalID = setInterval(function(){refresh();},2500);$log.log('intervalID: '+$scope.intervalID);};
    }

    function registerController ($scope,$cookieStore,$window,$log,restService,VERSION) {
        $scope.version = VERSION;
        $scope.errorMessages = [];
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
                }).then(
                    function (response) {
                        if (response.status === 'SUCCESS') {
                            $cookieStore.put('credentials', [$scope.username,$scope.password1,false]);
                            $scope.modalImage = 'images/thumb.jpg';
                            $scope.modalTitle = 'Congratulations';
                            $scope.modalEnable = true;
                        } else {
                            $log.log(response);
                            $scope.errorMessages.push (response.message);
                            $scope.errorMessageShow = true;
                        }
                    },
                    function (error) {
                        $log.log(error);
                        $scope.errorMessages.push (error.data);
                        $scope.errorMessageShow = true;
                    }
                );
            }else{
                $log.log('Passwords do not match!');
                $scope.errorMessages.push ('Passwords do not match!');
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
        $scope.errorMessages = [];
        var userCookie = $cookieStore.get('credentials');
        if (!userCookie) $window.location.href = 'index.html';
        $scope.username = userCookie.username;
        $scope.isAdmin = userCookie.admin;
        var getUsers = function () {
            if ($scope.isAdmin) {
                restService.post('getUsers', {
                    'username': userCookie.username,
                    'password': userCookie.password
                }).then(
                    function (response) {
                        if (response.status === 'SUCCESS') {
                            $scope.users = response.users;
                            $timeout(function () {
                                $("#exampleX").DataTable();
                            }, 1000);
                        } else {
                            $log.log(response);
                            $scope.errorMessages.push (response.message);
                            $scope.errorMessageShow = true;
                        }
                    },
                    function (error) {
                        $log.log(error);
                        $scope.errorMessages.push (error.data);
                        $scope.errorMessageShow = true;
                    }
                );
            }else{
                $scope.users = [];
                $scope.errorMessages.push ("You are not authorized to view this page...");
                $scope.errorMessageShow = true;
            }
        };
        $scope.activateUser = function (userToActivate) {
            restService.post ('activateUser',{
                'username': userCookie.username,
                'password': userCookie.password,
                'toActivate': userToActivate.username
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        userToActivate.status = 1;
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
                    $scope.errorMessageShow = true;
                }
            );
        };
        $scope.disableUser = function (userToDisable) {
            restService.post ('disableUser',{
                'username': userCookie.username,
                'password': userCookie.password,
                'toDisable': userToDisable.username
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        userToDisable.status = 0;
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
                    $scope.errorMessageShow = true;
                }
            );
        };
        getUsers();
    }

    function editController ($scope,$cookieStore,$window,$log,restService,VERSION) {
        $scope.version = VERSION;
        $scope.errorMessages = [];
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
            }).then(
                function (response) {
                    if (response.status === 'SUCCESS') {
                        $window.location.href = 'user.html';
                    }else{
                        $log.log(response);
                        $scope.errorMessages.push (response.message);
                        $scope.errorMessageShow = true;
                    }
                    $cookieStore.put('credentials',$scope.data);
                    document.getElementsByClassName('modal')[0].style.display = 'none';
                },
                function (error) {
                    $log.log(error);
                    $scope.errorMessages.push (error.data);
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
