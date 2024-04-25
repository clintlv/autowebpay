/**
 * Created by Administrator on 2016/12/28.
 * 需引入jquery,md5
 *          <script src="./jquery-1.9.1.min.js"></script>
 *          <script src="./md5.min.js"></script>
 */

function submit(){
    var orderNo=$('#orderNo').val();
    var payAmount=$('#payAmount').val();
    var payChannelType=$('#payChannelType').val();
    var currencyType=$('#currencyType').val();
    var extendData=$('#extendData').val();
    var storedCard=$('#storedCard').val();
    var storedId=$('#storedId').val();
    var storedToken=$('#storedToken').val();

    var XSID=$('#XSID').val();
    var XSIGN=$('#XSIGN').val();
    var XTOKEN=$('#XTOKEN').val();
    var XTIMESTAMP=new Date().getTime();

    var checkbox = document.getElementById("storedCard");
    if (checkbox.checked) {
    storedCard = 1;
}

    var checkbox = document.getElementById("storedCardPay");
    if (!checkbox.checked) {
    storedToken = '';
}

    var entity={
    orderNo:orderNo,
    payAmount:payAmount,
    currencyType:currencyType,
    extendData:extendData,
    storedCard:storedCard,
    storedId:storedId,
    token:storedToken
}
    // 先组装成数组
    var arr=[];
    for(var key in entity){
    arr.push(key);
}
    console.log(arr);
    arr.sort();
    console.log(arr);
    var entityStr='';
    for(var i=0;i<arr.length;i++){
    entityStr+=entity[arr[i]];
    console.log('arr[i]',arr[i]);
}
    console.log('entityStr...',entityStr);
    // var XALG = md5(XSIGN+orderNo+payAmount+payChannelType+currencyType);
    var XALG = md5(XSIGN+XTIMESTAMP+entityStr);
    console.log('XALG...',XALG);

    console.log('entity...',entity);
    $.ajax({
    type:"get",
    url:`/merchant-api/${payChannelType}/order`,
    data:entity,
    contentType:"application/json",
    dataType:"json",
    headers:{
    "X-SID":XSID,
    "X-TOKEN":XTOKEN,
    "X-TIMESTAMP":XTIMESTAMP,
    "X-ALG":XALG,
},
    success:function(data){
    console.log('成功...',data);
    window.location.href=data.data.payUrl;
},
    error:function(data){
    console.log('失败...',data);
}
})
}