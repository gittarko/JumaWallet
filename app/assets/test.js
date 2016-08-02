//页面加载完毕后向app注入回调
//如果不设置回调，app将默认调用js端paymentCallBack方法
document.addEventListener('DOMContentLoaded', function() {
    WalletPay.paymentCallBackFunc("paymentCallBack");
}, false);

//开始支付,调用app端支付
function executePayment(url, channel, amount) {
    WalletPay.doPayment(url, channel, amount);
}

//支付结果回调方法，务必存在
//results：支付结果
function paymentCallBack(results) {
    alert("前端获取支付结果：" + results);
}