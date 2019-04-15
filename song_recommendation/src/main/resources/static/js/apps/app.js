var Myapp = angular.module('App', [ 'ui.router','angular-popups' ])
// , 'line-chart'

// 网页统一url设置
    .constant("SERVER_API_URL", "http://localhost:8080")
// .constant("SERVER_API_URL", "http://127.0.0.1:8888")
// .constant("SERVER_API_URL", "http://dase.ecnu.edu.cn/AuthorAnalysis");
// .constant("SERVER_API_URL", "http://10.11.6.117:8888")

// 页面跳转设置
// .constant("SERVER_API_URL", "http://2b25214y86.iask.in:55680")

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
        PopupProvider.title = '提示';
        PopupProvider.okValue = '确定';
        PopupProvider.cancelValue = '取消';
    } ]);