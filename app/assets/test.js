/**
*h5调用app示例
 WalletPay为h5调用app的指定名称，
 该名称应保持app与h5一致，否则h5无法调用app对应的方法
*/

/**页面加载完毕后向app注入回调
*如果不设置回调，app将默认调用js端paymentCallBack方法
*/
document.addEventListener('DOMContentLoaded', function() {
    WalletPay.paymentCallBackFunc("paymentCallBack");
}, false);

//开始支付,调用app端支付
//url: 服务端订单地址
//channel:支付类型， 请参照ping++支付类型
//amount：支付价格, double类型
function executePayment(url, channel, amount) {
    WalletPay.doPayment(url, channel, amount);
}

//支付结果回调方法，务必存在
//results：支付结果
function paymentCallBack(results) {
    alert("前端获取支付结果：" + results);
}