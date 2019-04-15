angular.module('App').controller(
    'homeCtrl',
    [
        '$scope',
        '$state',
        '$http',
        '$stateParams',
        'Popup',
        'SERVER_API_URL',
        function($scope, $state, $http, $stateParams, Popup, SERVER_API_URL) {

            $scope.toDisplayed = function() {
                if(typeof $scope.singer == 'undefined'){
                    $scope.singer='';
                }
                if(typeof $scope.song == 'undefined'){
                    $scope.song='';
                }
                // console.log("singer22::", $scope.singer);
                // console.log("song22::", $scope.song);
                $http({
                    url: SERVER_API_URL + "/recommend/get_recommend_res",
                    method: 'post',
                    params:{
                        'singer': $scope.singer,
                        'song': $scope.song
                    }
                }).success(function (response) {
                    $scope.recommend_res = response;
                    // console.log(response);
                    if(response.length == 0){
                        $scope.tableStatus = 2;
                    }else{
                        $scope.tableStatus = 1;
                    }
                })
            };

            $scope.openNotice = function() {
                Popup.notice('歌手/歌曲名/关键词不在数据库中，详见"使用说明"，谢谢！', 2000, function () {
                    $scope.tableStatus = 0;
                });
            }

            $scope.note = function(){
                //tab层

                layer.tab({
                    area: ['600px', '480px'],
                    tab: [{
                        title: '使用说明',
                        content:
                            '<ul><li>' +
                            '<b>查询形式一：</b>输入歌手名和歌曲名称/关键词<br />' +
                            '周杰伦 给我一首歌的时间 <br />' +
                            '邓紫棋 浪漫 <br />' +
                            '</li><li>' +
                            '<b>查询形式二：</b>只输入歌曲名称/关键词<br />' +
                            '有多少爱可以重来<br />' +
                            '甜蜜<br />' +
                            '</li><li>' +
                            '<b>查询形式三：</b>只输入歌手名<br />' +
                            '郑中基<br />' +
                            '陈楚生<br />' +
                            '<b>支持歌手名：</b><br />'+
                            '胡夏,林俊杰,李宗盛,林宥嘉,罗大佑,林志炫,王菲<br />' +
                            '李健,李荣浩,周笔畅,薛之谦,许茹芸,汪苏泷,光良<br />' +
                            '邓丽君,胡彦斌,王力宏,邓紫棋,陈奕迅,赵雷,许嵩<br />' +
                            '陈楚生,徐佳莹,丁当,安又琪,陶喆,五月天,梁咏琪<br />' +
                            '庾澄庆,周杰伦,张国荣,张学友,朴树,汪峰,莫文蔚<br />' +
                            '张碧晨,杨宗纬,张靓颖,蔡健雅,郑中基,梁静茹<br />' +
                            '田馥甄,张惠妹,花粥,蔡依林,张韶涵,李玉刚<br />' +
                            '</li></ul>'
                    }
                    ]
                });
            };
        } ])