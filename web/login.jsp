<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户登录</title>
    <link rel="stylesheet" href="css/bootstrap.css" type="text/css"/>
    <script type="text/javascript" src="js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="js/bootstrap.js"></script>
    <style type="text/css">
        #logo ul li{
            list-style:none ;
            float: left;
            padding: 5px 10px;
            line-height: 60px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-md-4">
            <h3>在线图书销售平台</h3>
        </div>
        <div class="col-md-4">
            <img src="img/header.jpg"/>
        </div>
        <div class="col-md-4" id="logo">
            <ul>
                <li>登陆</li>
                <li>注册</li>
                <li>购物车</li>
            </ul>
        </div>
    </div>

    <nav class="navbar navbar-inverse">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="index.jsp">首页</a>
        </div>

        <div class="collapse navbar-collapse navbar-ex1-collapse">
            <ul class="nav navbar-nav">
                <li><a href="bookList">科学</a></li>
                <li><a href="bookList">文学</a></li>
                <li><a href="bookList">动漫</a></li>
                <li><a href="bookList">计算机</a></li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">其他 <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="bookList">烹饪</a></li>
                        <li><a href="bookList">外国文学</a></li>
                        <li><a href="bookList">中国文学</a></li>
                        <li class="divider"></li>
                        <li><a href="bookList">杂志</a></li>
                        <li class="divider"></li>
                        <li><a href="bookList">体育</a></li>
                    </ul>
                </li>
            </ul>
            <form class="navbar-form navbar-right" role="search">
                <div class="form-group">
                    <input type="text" class="form-control" placeholder="Search">
                </div>
                <button type="submit" class="btn btn-default">Submit</button>
            </form>
        </div>
    </nav>

    <div style="margin-top: 90px;" >
        <form action="login" method="post" class="form-horizontal">
            <div class="form-group" style="margin-left: 200px;">
                <label class="col-sm-2 control-label">用户名</label>
                <div class="col-sm-5">
                    <input type="text" name="username" class="form-control" placeholder="请输入用户名">
                </div>
            </div>
            <div class="form-group" style="margin-left: 200px">
                <label class="col-sm-2 control-label">密码</label>
                <div class="col-sm-5" >
                    <input type="password" name="password" class="form-control" placeholder="请输入密码">
                </div>
            </div>
            <div class="form-group" style="margin-left: 200px;">
                <div class="col-sm-offset-2 col-sm-10">
                    <div class="checkbox">
                        <label><input type="checkbox"> Remember me</label>
                    </div>
                </div>
            </div>
            <div class="form-group" style="margin-left: 200px;">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" class="btn btn-primary">登录</button>
                </div>
            </div>
        </form>
    </div>

    <div class="row" align="bottom" style="margin-top:200px ;">
        <div class="col-md-4">
            <h2>Welcome</h2>
            Welcom to itcast.Welcom to itcast.Welcom to itcast.Welcom to itcast.Welcom to itcast.
            <button type="button" class="btn btn-primary pull-right" >see more</button>
        </div>
        <div class="col-md-4">
            <h2>Welcome</h2>
            Welcom to itcast.Welcom to itcast.Welcom to itcast.Welcom to itcast.Welcom to itcast.
            <button type="button" class="btn btn-primary pull-right" >see more</button>
        </div>
        <div class="col-md-4">
            <h2>Welcome</h2>
            Welcom to itcast.Welcom to itcast.Welcom to itcast.Welcom to itcast.Welcom to itcast.
            <button type="button" class="btn btn-primary pull-right" >see more</button>
        </div>
    </div>
</body>
</html>