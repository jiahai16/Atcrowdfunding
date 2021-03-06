<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/font-awesome.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/doc.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/ztree/zTreeStyle.css">
    <style>
        .tree li {
            list-style-type: none;
            cursor:pointer;
        }
    </style>
</head>

<body>

<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container-fluid">
        <div class="navbar-header">
            <div><a class="navbar-brand" style="font-size:32px;" href="#">众筹平台 - 角色维护</a></div>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                <%@include file="/WEB-INF/jsp/common/top.jsp"%>
            </ul>
            <form class="navbar-form navbar-right">
                <input type="text" class="form-control" placeholder="Search...">
            </form>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
            <div class="tree">
                <ul style="padding-left:0px;" class="list-group">
                    <%@include file="/WEB-INF/jsp/common/botton.jsp"%>
                </ul>
            </div>
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

            <div class="panel panel-default">
                <div class="panel-heading"><i class="glyphicon glyphicon-th-list"></i> 权限分配列表<div style="float:right;cursor:pointer;" data-toggle="modal" data-target="#myModal"><i class="glyphicon glyphicon-question-sign"></i></div></div>
                <div class="panel-body">
                    <button id="assignPermissionBtn" class="btn btn-success">分配许可</button>
                    <br><br>
                    <ul id="treeDemo" class="ztree"></ul>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="myModalLabel">帮助</h4>
            </div>
            <div class="modal-body">
                <div class="bs-callout bs-callout-info">
                    <h4>没有默认类</h4>
                    <p>警告框没有默认类，只有基类和修饰类。默认的灰色警告框并没有多少意义。所以您要使用一种有意义的警告类。目前提供了成功、消息、警告或危险。</p>
                </div>
                <div class="bs-callout bs-callout-info">
                    <h4>没有默认类</h4>
                    <p>警告框没有默认类，只有基类和修饰类。默认的灰色警告框并没有多少意义。所以您要使用一种有意义的警告类。目前提供了成功、消息、警告或危险。</p>
                </div>
            </div>
            <!--
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              <button type="button" class="btn btn-primary">Save changes</button>
            </div>
            -->
        </div>
    </div>
</div>
<script src="${pageContext.request.contextPath}/jquery/jquery-2.1.1.min.js"></script>
<script src="${pageContext.request.contextPath}/bootstrap/js/bootstrap.min.js"></script>
<script src="${pageContext.request.contextPath}/script/docs.min.js"></script>
<script src="${pageContext.request.contextPath}/ztree/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath }/jquery/layer/layer.js"></script>
<script type="text/javascript">
    $(function () {
        $(".list-group-item").click(function(){
            if ( $(this).find("ul") ) {
                $(this).toggleClass("tree-closed");
                if ( $(this).hasClass("tree-closed") ) {
                    $("ul", this).hide("fast");
                } else {
                    $("ul", this).show("fast");
                }
            }
        });

        var setting = {
            check : {
                enable : true//在树的节点前显示复选框
            },
            view: {
                selectedMulti: false,//只允许选取一个节点
                addDiyDom: function(treeId, treeNode){
                    var icoObj = $("#" + treeNode.tId + "_ico"); // tId = permissionTree_1, $("#permissionTree_1_ico")
                    if ( treeNode.icon ) {
                        icoObj.removeClass("button ico_docu ico_open").addClass(treeNode.icon).css("background","");
                    }
                },
            },
            async: {
                enable: true,//采用异步开发
                url:"${pageContext.request.contextPath}/role/loadDataAsync.do?roleid=${param.roleid}",
                autoParam:["id", "name=n", "level=lv"]
            },
            callback: {//回调作用
                onClick : function(event, treeId, json) {

                }
            }
        };

        //异步加载树：注意问题，服务器端返回的数据必须是一个数组
        $.fn.zTree.init($("#treeDemo"), setting); //异步访问数据
        /*$.fn.zTree.init($("#treeDemo"),setting,treeJson);  同步访问数据*/

    });

    $("#assignPermissionBtn").click(function () {
        var jsonObj={
            "roleid":${param.roleid}
        }
        var treeObj=$.fn.zTree.getZTreeObj("treeDemo");
        var checkedNodes=treeObj.getCheckedNodes(true);

        $.each(checkedNodes,function (i,n) {
            jsonObj["ids["+i+"]"]=n.id;
        })

        if (checkedNodes.length==0){
            layer.msg("至少分配一个许可",{time:1000,icon:5,shift:6});
        }else {
            var Integer=-1;
            $.ajax({
                type:"POST",
                url:"${pageContext.request.contextPath}/role/doAssignPermission.do",
                data:jsonObj,
                beforeSend:function(){
                    Integer=layer.msg("正在分配许可....",{icon:16});
                    return true;
                },
                success:function (ajaxResult) {
                    layer.close(Integer);
                    if(ajaxResult.success){
                        layer.msg("分配成功",{time:1000,icon:6});
                    }else {
                        layer.msg("分配失败",{time:1000,icon:5,shift:6});
                    }
                },
                error:function(){
                    layer.msg("操作失败",{time:1000,icon:5,shift:6});
                }
            });
        }
    })
</script>
</body>
</html>

