<%-- 订单确认页 order.jsp --%>
<%-- 功能：用户确认订单信息，包括收货地址、商品列表、优惠券、支付方式等 --%>
<%-- 提交后生成正式订单 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>订单确认 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.css" type="text/css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.js"></script>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-dark: #3730a3;
            --accent: #f59e0b;
            --danger: #ef4444;
            --success: #10b981;
            --warning: #f59e0b;
            --gray-50: #f9fafb;
            --gray-100: #f3f4f6;
            --gray-200: #e5e7eb;
            --gray-300: #d1d5db;
            --gray-400: #9ca3af;
            --gray-500: #6b7280;
            --gray-600: #4b5563;
            --gray-700: #374151;
            --gray-800: #1f2937;
            --gray-900: #111827;
            --white: #ffffff;
            --shadow: 0 1px 6px rgba(0,0,0,0.08);
            --shadow-lg: 0 8px 30px rgba(0,0,0,0.12);
            --radius: 8px;
            --radius-lg: 12px;
            --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: var(--gray-50); color: var(--gray-800); }

        .navbar-custom { background: linear-gradient(135deg, var(--primary), var(--primary-dark)); border: none; border-radius: 0; box-shadow: 0 2px 12px rgba(79,70,229,0.3); }
        .navbar-custom .navbar-brand { color: #fff !important; font-weight: 800; font-size: 20px; }
        .navbar-custom .nav > li > a { color: rgba(255,255,255,0.9) !important; font-weight: 600; transition: all 0.3s; }
        .navbar-custom .nav > li > a:hover { color: #fff !important; background: rgba(255,255,255,0.1) !important; border-radius: 6px; }
        .cart-badge { display: inline-block; background: var(--danger); color: #fff; border-radius: 50%; min-width: 20px; height: 20px; line-height: 20px; text-align: center; font-size: 11px; font-weight: 700; padding: 0 5px; margin-left: 4px; }

        .steps-progress { display: flex; align-items: center; justify-content: center; margin-bottom: 30px; padding: 24px 0; }
        .step-item { display: flex; align-items: center; gap: 10px; }
        .step-circle { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 800; transition: all 0.4s; }
        .step-circle.active { background: var(--primary); color: #fff; box-shadow: 0 4px 15px rgba(79,70,229,0.4); }
        .step-circle.done { background: var(--success); color: #fff; }
        .step-circle.pending { background: var(--gray-200); color: var(--gray-400); }
        .step-label { font-size: 14px; font-weight: 700; }
        .step-label.active-l { color: var(--primary); }
        .step-label.pending-l { color: var(--gray-400); }
        .step-label.done-l { color: var(--success); }
        .step-line { width: 80px; height: 3px; background: var(--gray-200); margin: 0 12px; border-radius: 2px; transition: all 0.4s; }
        .step-line.done { background: var(--success); }

        .order-layout { display: flex; gap: 24px; }
        .order-main { flex: 1; min-width: 0; }
        .order-sidebar { width: 400px; flex-shrink: 0; }

        .card-white { background: var(--white); border-radius: var(--radius-xl); box-shadow: var(--shadow); padding: 28px; margin-bottom: 20px; }
        .card-white h3 { font-size: 18px; font-weight: 700; color: var(--gray-800); margin: 0 0 20px; padding-bottom: 14px; border-bottom: 2px solid var(--gray-100); }

        .order-table { width: 100%; border-collapse: collapse; }
        .order-table thead th { padding: 12px 14px; font-size: 13px; font-weight: 700; color: var(--gray-500); text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid var(--gray-100); text-align: left; }
        .order-table tbody td { padding: 14px; border-bottom: 1px solid var(--gray-100); vertical-align: middle; }
        .order-table tbody tr:last-child td { border-bottom: none; }
        .order-item-img { width: 56px; height: 72px; object-fit: cover; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
        .order-item-name { font-weight: 700; color: var(--gray-800); font-size: 14px; }
        .order-item-price { color: var(--gray-500); font-size: 13px; }
        .order-item-qty { color: var(--gray-500); font-size: 13px; }
        .order-item-subtotal { font-weight: 700; color: var(--danger); }

        .form-group-custom { margin-bottom: 18px; }
        .form-group-custom label { display: block; font-size: 13px; font-weight: 700; color: var(--gray-600); margin-bottom: 6px; }
        .form-control-custom { width: 100%; padding: 12px 16px; border: 2px solid var(--gray-200); border-radius: var(--radius); font-size: 14px; color: var(--gray-800); outline: none; transition: all 0.3s; background: var(--gray-50); -webkit-appearance:none; -moz-appearance:none; appearance:none; }
        .form-control-custom:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); }
        select.form-control-custom { background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%236b7280' d='M6 8L1 3h10z'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 12px center; padding-right: 36px; cursor: pointer; }
        select.form-control-custom:focus { background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%234f46e5' d='M6 8L1 3h10z'/%3E%3C/svg%3E"); }
        .form-row-2 { display: flex; gap: 12px; }
        .form-row-2 .form-group-custom { flex: 1; }

        .summary-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; font-size: 14px; color: var(--gray-600); border-bottom: 1px dashed var(--gray-100); }
        .summary-row:last-child { border-bottom: none; }
        .summary-row.total { font-size: 20px; font-weight: 800; color: var(--danger); padding: 16px 0; border-top: 2px solid var(--gray-200); margin-top: 8px; }

        .coupon-section { padding: 14px 0; display: flex; gap: 8px; }
        .coupon-input { flex: 1; padding: 10px 14px; border: 2px dashed var(--gray-300); border-radius: var(--radius); font-size: 14px; outline: none; transition: all 0.3s; }
        .coupon-input:focus { border-color: var(--primary); }
        .btn-coupon { padding: 10px 20px; border-radius: var(--radius); font-size: 14px; font-weight: 700; border: none; cursor: pointer; transition: all 0.3s; background: var(--accent); color: #fff; }
        .btn-coupon:hover { background: #d97706; }
        .coupon-applied { display: flex; align-items: center; gap: 8px; padding: 8px 14px; background: #fef3c7; border-radius: 20px; font-size: 13px; font-weight: 600; color: #92400e; margin-top: 8px; }
        .coupon-applied .remove-coupon { cursor: pointer; color: var(--danger); font-weight: 800; margin-left: 4px; }

        .btn-submit { display: block; width: 100%; padding: 18px; border-radius: 50px; font-size: 18px; font-weight: 800; border: none; cursor: pointer; transition: all 0.3s; background: linear-gradient(135deg, var(--primary), var(--primary-dark)); color: #fff; box-shadow: 0 6px 20px rgba(79,70,229,0.35); margin-top: 16px; }
        .btn-submit:hover { transform: translateY(-3px); box-shadow: 0 12px 30px rgba(79,70,229,0.45); }

        .savings-text { color: var(--success); font-size: 13px; font-weight: 600; }

        @media (max-width: 768px) {
            .order-layout { flex-direction: column; }
            .order-sidebar { width: 100%; }
            .steps-progress { flex-wrap: wrap; gap: 8px; }
            .step-line { width: 40px; }
        }
    </style>
</head>
<body>
<nav class="navbar navbar-default navbar-custom navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/">📚 BookVerse</a>
        </div>
        <ul class="nav navbar-nav navbar-right" style="margin-right: 20px;">
            <li><a href="${pageContext.request.contextPath}/">首页</a></li>
            <c:if test="${not empty sessionScope.user}">
                <li><a href="${pageContext.request.contextPath}/cart"><span class="glyphicon glyphicon-shopping-cart"></span> 购物车 <span class="cart-badge">${cartSize}</span></a></li>
            </c:if>
        </ul>
    </div>
</nav>
<div style="height: 80px;"></div>

<div class="container">
    <div class="steps-progress">
        <div class="step-item">
            <div class="step-circle active">1</div>
            <span class="step-label active-l">确认订单</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item">
            <div class="step-circle pending">2</div>
            <span class="step-label pending-l">填写地址</span>
        </div>
        <div class="step-line"></div>
        <div class="step-item">
            <div class="step-circle pending">3</div>
            <span class="step-label pending-l">提交订单</span>
        </div>
    </div>

    <form action="${pageContext.request.contextPath}/order/submit" method="post" id="orderForm">
        <div class="order-layout">
            <div class="order-main">
                <div class="card-white">
                    <h3>📦 商品清单</h3>
                    <table class="order-table">
                        <thead>
                            <tr>
                                <th style="width: 70px;">图片</th>
                                <th>图书名称</th>
                                <th style="width: 90px;">单价</th>
                                <th style="width: 70px;">数量</th>
                                <th style="width: 90px;">小计</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="total" value="0"/>
                            <c:forEach items="${cart}" var="item">
                                <c:set var="total" value="${total + item.subtotal}"/>
                                <tr>
                                    <td>
                                        <img src="${pageContext.request.contextPath}/img/books/${item.productId}.jpg"
                                             alt="${item.name}" class="order-item-img"
                                             onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2256%22 height=%2272%22><rect fill=%22%23e5e7eb%22 width=%2256%22 height=%2272%22/><text fill=%22%239ca3af%22 font-size=%2210%22 text-anchor=%22middle%22 x=%2228%22 y=%2240%22>Book</text></svg>'" />
                                    </td>
                                    <td><span class="order-item-name">${item.name}</span></td>
                                    <td><span class="order-item-price">¥<fmt:formatNumber value="${item.price}" pattern="#0.00"/></span></td>
                                    <td><span class="order-item-qty">×${item.quantity}</span></td>
                                    <td><span class="order-item-subtotal">¥<fmt:formatNumber value="${item.subtotal}" pattern="#0.00"/></span></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="order-sidebar">
                <div class="card-white">
                    <h3>📍 收货信息</h3>
                    <div class="form-group-custom">
                        <label>收货地址 *</label>
                        <input type="text" name="addr1" class="form-control-custom" placeholder="请输入详细收货地址" value="${user.addr1}" required>
                    </div>
                    <div class="form-row-2">
                        <div class="form-group-custom">
                            <label>省份 *</label>
                            <select name="state" id="provinceSelect" class="form-control-custom" required>
                                <option value="">请选择省份</option>
                            </select>
                        </div>
                        <div class="form-group-custom">
                            <label>城市 *</label>
                            <select name="city" id="citySelect" class="form-control-custom" required>
                                <option value="">请先选择省份</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group-custom">
                        <label>邮编 *</label>
                        <input type="text" name="zip" class="form-control-custom" placeholder="邮政编码" value="${user.zip}" required>
                    </div>
                </div>

                <div class="card-white">
                    <h3>💰 订单汇总</h3>
                    <div class="summary-row">
                        <span>商品总金额</span>
                        <span>¥<fmt:formatNumber value="${total}" pattern="#0.00"/></span>
                    </div>
                    <div class="coupon-section" style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
                        <select class="coupon-input" id="couponSelect" style="flex:1;min-width:150px;padding:8px 12px;border:1px solid var(--border);border-radius:8px;font-size:14px;">
                            <option value="">-- 选择优惠券 (可选) --</option>
                        </select>
                        <input type="text" class="coupon-input" id="couponCode" placeholder="或输入优惠码" style="flex:1;min-width:120px;" />
                        <button type="button" class="btn-coupon" id="btnApplyCoupon">使用</button>
                    </div>
                    <div id="couponApplied" style="display: none;"></div>
                    <div class="summary-row" id="discountRow" style="display: none;">
                        <span>优惠折扣</span>
                        <span class="savings-text" id="discountAmount">-¥0.00</span>
                    </div>
                    <div class="summary-row total">
                        <span>应付金额</span>
                        <span id="finalTotal">¥<fmt:formatNumber value="${total}" pattern="#0.00"/></span>
                    </div>
                    <input type="hidden" name="couponCode" id="hiddenCouponCode" />
                    <button type="submit" class="btn-submit">✅ 提交订单</button>
                </div>
            </div>
        </div>
    </form>
</div>

<script>
    var provinceCityData = {
        "北京市": ["东城区","西城区","朝阳区","丰台区","石景山区","海淀区","门头沟区","房山区","通州区","顺义区","昌平区","大兴区"],
        "上海市": ["黄浦区","徐汇区","长宁区","静安区","普陀区","虹口区","杨浦区","闵行区","宝山区","嘉定区","浦东新区","松江区","青浦区"],
        "天津市": ["和平区","河东区","河西区","南开区","河北区","红桥区","东丽区","西青区","津南区","北辰区","武清区","宝坻区","滨海新区"],
        "重庆市": ["万州区","涪陵区","渝中区","大渡口区","江北区","沙坪坝区","九龙坡区","南岸区","北碚区","渝北区","巴南区","长寿区","江津区","合川区","永川区"],
        "河北省": ["石家庄市","唐山市","秦皇岛市","邯郸市","邢台市","保定市","张家口市","承德市","沧州市","廊坊市","衡水市"],
        "山西省": ["太原市","大同市","阳泉市","长治市","晋城市","朔州市","晋中市","运城市","忻州市","临汾市","吕梁市"],
        "内蒙古自治区": ["呼和浩特市","包头市","乌海市","赤峰市","通辽市","鄂尔多斯市","呼伦贝尔市","巴彦淖尔市","乌兰察布市"],
        "辽宁省": ["沈阳市","大连市","鞍山市","抚顺市","本溪市","丹东市","锦州市","营口市","阜新市","辽阳市","盘锦市","铁岭市","朝阳市","葫芦岛市"],
        "吉林省": ["长春市","吉林市","四平市","辽源市","通化市","白山市","松原市","白城市"],
        "黑龙江省": ["哈尔滨市","齐齐哈尔市","鸡西市","鹤岗市","双鸭山市","大庆市","伊春市","佳木斯市","七台河市","牡丹江市","黑河市","绥化市"],
        "江苏省": ["南京市","无锡市","徐州市","常州市","苏州市","南通市","连云港市","淮安市","盐城市","扬州市","镇江市","泰州市","宿迁市"],
        "浙江省": ["杭州市","宁波市","温州市","嘉兴市","湖州市","绍兴市","金华市","衢州市","舟山市","台州市","丽水市"],
        "安徽省": ["合肥市","芜湖市","蚌埠市","淮南市","马鞍山市","淮北市","铜陵市","安庆市","黄山市","滁州市","阜阳市","宿州市","六安市","亳州市","池州市","宣城市"],
        "福建省": ["福州市","厦门市","莆田市","三明市","泉州市","漳州市","南平市","龙岩市","宁德市"],
        "江西省": ["南昌市","景德镇市","萍乡市","九江市","新余市","鹰潭市","赣州市","吉安市","宜春市","抚州市","上饶市"],
        "山东省": ["济南市","青岛市","淄博市","枣庄市","东营市","烟台市","潍坊市","济宁市","泰安市","威海市","日照市","临沂市","德州市","聊城市","滨州市","菏泽市"],
        "河南省": ["郑州市","开封市","洛阳市","平顶山市","安阳市","鹤壁市","新乡市","焦作市","濮阳市","许昌市","漯河市","三门峡市","南阳市","商丘市","信阳市","周口市","驻马店市"],
        "湖北省": ["武汉市","黄石市","十堰市","宜昌市","襄阳市","鄂州市","荆门市","孝感市","荆州市","黄冈市","咸宁市","随州市","恩施土家族苗族自治州"],
        "湖南省": ["长沙市","株洲市","湘潭市","衡阳市","邵阳市","岳阳市","常德市","张家界市","益阳市","郴州市","永州市","怀化市","娄底市","湘西土家族苗族自治州"],
        "广东省": ["广州市","韶关市","深圳市","珠海市","汕头市","佛山市","江门市","湛江市","茂名市","肇庆市","惠州市","梅州市","汕尾市","河源市","阳江市","清远市","东莞市","中山市","潮州市","揭阳市","云浮市"],
        "广西壮族自治区": ["南宁市","柳州市","桂林市","梧州市","北海市","防城港市","钦州市","贵港市","玉林市","百色市","贺州市","河池市","来宾市","崇左市"],
        "海南省": ["海口市","三亚市","三沙市","儋州市"],
        "四川省": ["成都市","自贡市","攀枝花市","泸州市","德阳市","绵阳市","广元市","遂宁市","内江市","乐山市","南充市","眉山市","宜宾市","广安市","达州市","雅安市","巴中市","资阳市","阿坝藏族羌族自治州","甘孜藏族自治州","凉山彝族自治州"],
        "贵州省": ["贵阳市","六盘水市","遵义市","安顺市","毕节市","铜仁市","黔西南布依族苗族自治州","黔东南苗族侗族自治州","黔南布依族苗族自治州"],
        "云南省": ["昆明市","曲靖市","玉溪市","保山市","昭通市","丽江市","普洱市","临沧市","楚雄彝族自治州","红河哈尼族彝族自治州","文山壮族苗族自治州","西双版纳傣族自治州","大理白族自治州","德宏傣族景颇族自治州","怒江傈僳族自治州","迪庆藏族自治州"],
        "西藏自治区": ["拉萨市","日喀则市","昌都市","林芝市","山南市","那曲市"],
        "陕西省": ["西安市","铜川市","宝鸡市","咸阳市","渭南市","延安市","汉中市","榆林市","安康市","商洛市"],
        "甘肃省": ["兰州市","嘉峪关市","金昌市","白银市","天水市","武威市","张掖市","平凉市","酒泉市","庆阳市","定西市","陇南市","临夏回族自治州","甘南藏族自治州"],
        "青海省": ["西宁市","海东市","海北藏族自治州","黄南藏族自治州","海南藏族自治州","果洛藏族自治州","玉树藏族自治州","海西蒙古族藏族自治州"],
        "宁夏回族自治区": ["银川市","石嘴山市","吴忠市","固原市","中卫市"],
        "新疆维吾尔自治区": ["乌鲁木齐市","克拉玛依市","吐鲁番市","哈密市","昌吉回族自治州","博尔塔拉蒙古自治州","巴音郭楞蒙古自治州","阿克苏地区","克孜勒苏柯尔克孜自治州","喀什地区","和田地区","伊犁哈萨克自治州","塔城地区","阿勒泰地区"],
        "台湾省": ["台北市","高雄市","台中市","台南市","新北市","桃园市"],
        "香港特别行政区": ["中西区","湾仔区","东区","南区","油尖旺区","深水埗区","九龙城区","黄大仙区","观塘区","荃湾区","屯门区","元朗区","北区","大埔区","沙田区","西贡区","葵青区","离岛区"],
        "澳门特别行政区": ["花地玛堂区","花王堂区","望德堂区","大堂区","风顺堂区"]
    };

    var $province = $('#provinceSelect');
    var $city = $('#citySelect');
    var savedState = '${user.state}';
    var savedCity = '${user.city}';

    $.each(provinceCityData, function(province) {
        var selected = (province === savedState) ? ' selected' : '';
        $province.append('<option value="' + province + '"' + selected + '>' + province + '</option>');
    });

    if (savedState && provinceCityData[savedState]) {
        $city.empty().append('<option value="">请选择城市</option>');
        $.each(provinceCityData[savedState], function(i, city) {
            var selected = (city === savedCity) ? ' selected' : '';
            $city.append('<option value="' + city + '"' + selected + '>' + city + '</option>');
        });
    }

    $province.on('change', function() {
        var province = $(this).val();
        $city.empty().append('<option value="">请选择城市</option>');
        if (province && provinceCityData[province]) {
            $.each(provinceCityData[province], function(i, city) {
                $city.append('<option value="' + city + '">' + city + '</option>');
            });
        }
    });

    var originalTotal = parseFloat('<fmt:formatNumber value="${total}" pattern="#0.00"/>');
    var appliedDiscount = 0;
    var myCoupons = [];

    // Load user coupons on page load
    $(document).ready(function() {
        $.ajax({
            url: '${pageContext.request.contextPath}/api/coupon/my',
            dataType: 'json',
            success: function(data) {
                if (data.success && data.coupons) {
                    myCoupons = data.coupons;
                    var $select = $('#couponSelect');
                    $.each(data.coupons, function(i, c) {
                        var label = c.name + ' (满' + c.threshold + '减' + c.discount + ')';
                        $select.append('<option value="' + c.id + '" data-discount="' + c.discount + '">' + label + '</option>');
                    });
                }
            }
        });
    });

    // When user selects a coupon from dropdown
    $('#couponSelect').on('change', function() {
        var val = $(this).val();
        if (!val) {
            removeAppliedCoupon();
            return;
        }
        var opt = $(this).find('option:selected');
        appliedDiscount = parseFloat(opt.data('discount')) || 0;
        var name = opt.text();
        $('#couponApplied').show().html(
            '<div class="coupon-applied">🎫 ' + name + ' <span class="remove-coupon" id="removeCoupon">✕</span></div>'
        );
        $('#discountRow').show();
        $('#discountAmount').text('-¥' + appliedDiscount.toFixed(2));
        var final = originalTotal - appliedDiscount;
        $('#finalTotal').text('¥' + Math.max(0, final).toFixed(2));
        $('#hiddenCouponCode').val(val);
        $('#couponCode').val('').prop('disabled', true);
        $('#btnApplyCoupon').prop('disabled', true).css('opacity', '0.5');
    });

    $('#btnApplyCoupon').on('click', function() {
        var code = $.trim($('#couponCode').val());
        if (!code) { alert('请输入优惠码'); return; }

        $.ajax({
            url: '${pageContext.request.contextPath}/api/coupon/validate',
            data: { code: code, total: originalTotal },
            dataType: 'json',
            success: function(data) {
                if (data.valid) {
                    appliedDiscount = data.discount || 0;
                    $('#couponApplied').show().html(
                        '<div class="coupon-applied">' +
                        '🎫 ' + (data.name || code) + ' - ¥' + appliedDiscount.toFixed(2) +
                        ' <span class="remove-coupon" id="removeCoupon">✕</span>' +
                        '</div>'
                    );
                    $('#discountRow').show();
                    $('#discountAmount').text('-¥' + appliedDiscount.toFixed(2));
                    var final = originalTotal - appliedDiscount;
                    $('#finalTotal').text('¥' + Math.max(0, final).toFixed(2));
                    $('#hiddenCouponCode').val(code);
                    $('#couponCode').prop('disabled', true);
                    $('#btnApplyCoupon').prop('disabled', true).css('opacity', '0.5');
                    $('#couponSelect').prop('disabled', true).val('');
                } else {
                    alert(data.message || '优惠码无效');
                }
            },
            error: function() {
                alert('优惠码验证失败，请稍后重试');
            }
        });
    });

    function removeAppliedCoupon() {
        appliedDiscount = 0;
        $('#couponApplied').hide().html('');
        $('#discountRow').hide();
        $('#discountAmount').text('-¥0.00');
        $('#finalTotal').text('¥' + originalTotal.toFixed(2));
        $('#hiddenCouponCode').val('');
        $('#couponCode').val('').prop('disabled', false);
        $('#btnApplyCoupon').prop('disabled', false).css('opacity', '1');
        $('#couponSelect').prop('disabled', false).val('');
    }

    $(document).on('click', '#removeCoupon', function() {
        removeAppliedCoupon();
    });
</script>

</body>
</html>
