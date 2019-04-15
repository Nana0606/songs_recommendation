var Myapp = angular.module('App', [ 'ui.router','angular-popups' ])

// 网页统一url设置
    .constant("SERVER_API_URL", "http://localhost:8080")


// 页面跳转设置
Myapp.config([ "$stateProvider", "$urlRouterProvider", "PopupProvider", "SERVER_API_URL",
    function($stateProvider, $urlRouterProvider, PopupProvider, SERVER_API_URL) {

        // 页面跳转
        $urlRouterProvider.otherwise("/home");
        $stateProvider.state("home", {
            url : "/home",
            templateUrl : SERVER_API_URL + "/html/home.html",
            params : {
                "singer": '',
                "song": ''
            },
            controller : "homeCtrl"
        });
        //Popup提示框设置
        PopupProvider.title = '提示';
        PopupProvider.okValue = '确定';
        PopupProvider.cancelValue = '取消';
    } ]);