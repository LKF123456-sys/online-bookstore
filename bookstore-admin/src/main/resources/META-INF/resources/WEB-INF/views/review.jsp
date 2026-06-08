<%-- 评价页面 review.jsp --%>
<%-- 功能：用户对已购买商品进行评价，包括评分（1-5星）和文字评论 --%>
<%-- 用户可在订单完成后对商品进行评价 --%>

<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>发表评价 - BookVerse</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css" />
    <style>
        :root {
            --primary: #4f46e5; --primary-dark: #3730a3; --accent: #f59e0b;
            --danger: #ef4444; --success: #10b981; --warning: #f59e0b;
            --gray-50: #f9fafb; --gray-100: #f3f4f6; --gray-200: #e5e7eb;
            --gray-300: #d1d5db; --gray-400: #9ca3af; --gray-500: #6b7280;
            --gray-600: #4b5563; --gray-700: #374151; --gray-800: #1f2937;
            --gray-900: #111827; --white: #ffffff; --shadow: 0 1px 6px rgba(0,0,0,0.08);
            --shadow-lg: 0 8px 30px rgba(0,0,0,0.12); --radius: 8px;
            --radius-lg: 12px; --radius-xl: 16px;
        }
        * { box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: var(--gray-50); color: var(--gray-800); }

        .review-container { max-width: 600px; margin: 40px auto; padding: 0 20px; }
        .review-card { background: var(--white); border-radius: var(--radius-xl); box-shadow: var(--shadow-lg); padding: 40px 30px; border-top: 5px solid var(--accent); }
        .review-card h2 { font-size: 22px; font-weight: 800; color: var(--gray-800); margin: 0 0 8px; text-align: center; }
        .review-card .review-subtitle { color: var(--gray-500); font-size: 14px; text-align: center; margin-bottom: 25px; }

        .product-preview { display: flex; gap: 14px; background: var(--gray-50); border-radius: var(--radius-lg); padding: 16px; margin-bottom: 24px; border: 1px solid var(--gray-100); }
        .product-img { width: 60px; height: 78px; border-radius: var(--radius); object-fit: cover; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
        .product-info { flex: 1; }
        .product-info .product-name { font-weight: 700; color: var(--gray-800); font-size: 15px; margin-bottom: 4px; }
        .product-info .product-order { font-size: 12px; color: var(--gray-400); }

        .star-rating { text-align: center; margin-bottom: 24px; }
        .star-rating label { display: block; font-weight: 700; color: var(--gray-700); margin-bottom: 10px; font-size: 14px; }
        .star-group { display: flex; justify-content: center; gap: 6px; flex-direction: row-reverse; }
        .star-group input { display: none; }
        .star-group label.star { font-size: 32px; color: var(--gray-200); cursor: pointer; transition: color 0.2s; line-height: 1; }
        .star-group label.star:hover,
        .star-group label.star:hover ~ label.star,
        .star-group input:checked ~ label.star { color: var(--accent); }

        .review-textarea { width: 100%; min-height: 120px; border: 2px solid var(--gray-200); border-radius: var(--radius-lg); padding: 14px 16px; font-size: 15px; font-family: inherit; resize: vertical; transition: border-color 0.3s; margin-bottom: 12px; }
        .review-textarea:focus { border-color: var(--primary); outline: none; box-shadow: 0 0 0 4px rgba(79,70,229,0.1); }
        .review-textarea::placeholder { color: var(--gray-400); }

        .char-count { text-align: right; font-size: 12px; color: var(--gray-400); margin-top: -6px; margin-bottom: 20px; }

        .upload-area { border: 2px dashed var(--gray-300); border-radius: var(--radius-lg); padding: 24px; text-align: center; cursor: pointer; transition: all 0.3s; margin-bottom: 24px; position: relative; }
        .upload-area:hover { border-color: var(--primary); background: rgba(79,70,229,0.02); }
        .upload-area input[type="file"] { position: absolute; top: 0; left: 0; width: 100%; height: 100%; opacity: 0; cursor: pointer; }
        .upload-icon { font-size: 36px; margin-bottom: 8px; }
        .upload-text { font-size: 14px; color: var(--gray-500); }
        .upload-hint { font-size: 12px; color: var(--gray-400); margin-top: 4px; }

        .image-preview { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 12px; }
        .image-preview .preview-item { position: relative; width: 80px; height: 80px; border-radius: var(--radius); overflow: hidden; }
        .image-preview .preview-item img { width: 100%; height: 100%; object-fit: cover; }
        .image-preview .preview-item .remove-img { position: absolute; top: 2px; right: 2px; width: 20px; height: 20px; background: rgba(0,0,0,0.6); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; cursor: pointer; border: none; }

        .btn-submit { width: 100%; padding: 14px; border: none; border-radius: var(--radius); font-size: 16px; font-weight: 700; background: linear-gradient(135deg, var(--accent), #d97706); color: #fff; cursor: pointer; transition: all 0.3s; box-shadow: 0 4px 15px rgba(245,158,11,0.3); }
        .btn-submit:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(245,158,11,0.4); }
        .btn-submit:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

        .btn-back { display: block; text-align: center; margin-top: 16px; color: var(--gray-400); font-size: 14px; text-decoration: none; }
        .btn-back:hover { color: var(--gray-600); }

        .item-select { margin-bottom: 20px; }
        .item-select label { display: block; font-weight: 700; color: var(--gray-700); margin-bottom: 8px; font-size: 14px; }
        .item-select select { width: 100%; padding: 10px 14px; border: 2px solid var(--gray-200); border-radius: var(--radius); font-size: 14px; background: #fff; }
        .item-select select:focus { border-color: var(--primary); outline: none; }

        .tip-box { background: #fef3c7; border: 1px solid #fde68a; border-radius: var(--radius); padding: 12px 16px; font-size: 13px; color: #92400e; margin-bottom: 20px; }

        .btn-back-home { background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important; position: relative; overflow: hidden; }
        .btn-back-home::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent); transition: left 0.5s; }
        .btn-back-home:hover { transform: translateY(-2px) !important; box-shadow: 0 8px 25px rgba(79,70,229,0.4) !important; color: #fff !important; text-decoration: none !important; }
        .btn-back-home:hover::before { left: 100%; }
        .btn-back-home:active { transform: translateY(0) !important; }
    </style>
</head>
<body>

<div class="review-container">
    <div style="margin-bottom:20px;">
        <a href="${pageContext.request.contextPath}/order/history" class="btn-back-home" style="display:inline-flex;align-items:center;gap:8px;padding:12px 28px;background:linear-gradient(135deg,var(--primary),var(--primary-dark));color:#fff;border:none;border-radius:50px;font-size:14px;font-weight:600;cursor:pointer;text-decoration:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:0 4px 15px rgba(79,70,229,0.3);position:relative;overflow:hidden;">
            <span style="position:relative;z-index:1;display:flex;align-items:center;gap:8px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/></svg>
                返回订单
            </span>
        </a>
    </div>
    <div class="review-card">
        <h2>⭐ 发表评价</h2>
        <p class="review-subtitle">分享您的购物体验，帮助其他读者</p>

        <div class="tip-box">
            💡 您可以选择对整个订单进行评价，或者选择特定商品进行评价
        </div>

        <div class="product-preview">
            <img src="${pageContext.request.contextPath}/img/books/${items[0].productId}.jpg" alt="<c:out value="${items[0].productName}"/>" class="product-img"
                 onerror="this.onerror=null;this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2260%22 height=%2278%22><rect fill=%22%23e5e7eb%22 width=%2260%22 height=%2278%22 rx=%228%22/><text fill=%22%239ca3af%22 font-size=%2220%22 text-anchor=%22middle%22 x=%2230%22 y=%2245%22>📚</text></svg>'">
            <div class="product-info">
                <div class="product-name">订单号：<c:out value="${orderId}"/></div>
                <div class="product-order">共 ${items.size()} 件商品</div>
            </div>
        </div>

        <c:if test="${items.size() > 1}">
            <div class="item-select">
                <label>选择评价对象</label>
                <select id="itemSelect">
                    <option value="">📦 评价整个订单</option>
                    <c:forEach items="${items}" var="item">
                        <option value="${item.productId}">📚 <c:out value="${item.productName}"/></option>
                    </c:forEach>
                </select>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/review/submit" method="post" enctype="multipart/form-data" id="reviewForm">
            <input type="hidden" name="orderId" value="${orderId}" />
            <input type="hidden" name="productId" id="productIdInput" value="" />

            <div class="star-rating">
                <label>满意度评分</label>
                <div class="star-group">
                    <input type="radio" name="rating" value="5" id="star5" required /><label for="star5" class="star">★</label>
                    <input type="radio" name="rating" value="4" id="star4" /><label for="star4" class="star">★</label>
                    <input type="radio" name="rating" value="3" id="star3" /><label for="star3" class="star">★</label>
                    <input type="radio" name="rating" value="2" id="star2" /><label for="star2" class="star">★</label>
                    <input type="radio" name="rating" value="1" id="star1" /><label for="star1" class="star">★</label>
                </div>
            </div>

            <textarea class="review-textarea" name="content" id="reviewContent" placeholder="分享您的使用感受，评价字数不少于5字..." maxlength="500" required oninput="updateCharCount()"></textarea>
            <div class="char-count"><span id="charCount">0</span>/500</div>

            <div class="upload-area" id="uploadArea">
                <input type="file" name="imageFile" id="imageFile" accept="image/*" />
                <div class="upload-icon">📷</div>
                <div class="upload-text">点击或拖拽上传晒图</div>
                <div class="upload-hint">支持 JPG、PNG 格式，最多 5MB</div>
            </div>
            <div class="image-preview" id="imagePreview"></div>

            <button type="submit" class="btn-submit" id="submitBtn">✨ 提交评价</button>
        </form>

        <a href="${pageContext.request.contextPath}/order/detail?orderId=${orderId}" class="btn-back">← 返回订单详情</a>
    </div>
</div>

<script>
    function updateCharCount() {
        var content = document.getElementById('reviewContent').value;
        document.getElementById('charCount').textContent = content.length;
    }

    var itemSelect = document.getElementById('itemSelect');
    var productIdInput = document.getElementById('productIdInput');
    if (itemSelect && productIdInput) {
        itemSelect.addEventListener('change', function() {
            productIdInput.value = this.value;
        });
    }

    var imageFile = document.getElementById('imageFile');
    var imagePreview = document.getElementById('imagePreview');
    if (imageFile) {
        imageFile.addEventListener('change', function(e) {
            var file = e.target.files[0];
            if (file) {
                var reader = new FileReader();
                reader.onload = function(ev) {
                    imagePreview.innerHTML = '<div class="preview-item"><img src="' + ev.target.result + '" /><button type="button" class="remove-img" onclick="removeImage()">&times;</button></div>';
                };
                reader.readAsDataURL(file);
            }
        });
    }
    function removeImage() {
        imageFile.value = '';
        imagePreview.innerHTML = '';
    }

    document.getElementById('reviewForm').addEventListener('submit', function(e) {
        var rating = document.querySelector('input[name="rating"]:checked');
        if (!rating) {
            e.preventDefault();
            alert('请选择满意度评分');
            return false;
        }
        var content = document.getElementById('reviewContent').value.trim();
        if (content.length < 5) {
            e.preventDefault();
            alert('评价内容至少5个字哦');
            return false;
        }
        var btn = document.getElementById('submitBtn');
        btn.disabled = true;
        btn.textContent = '提交中...';
    });
</script>

</body>
</html>
