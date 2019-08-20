function updateStatus_tick(){
    updateStatus();
    setTimeout(function(){updateStatus_tick();}, 90000);
}

function updateStatus(){
    var xhr=(window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");

    xhr.onreadystatechange=function(){
        if (xhr.readyState==4 && xhr.status==200){
            var split=xhr.responseText.split("|");
            
            var divTime=document.getElementById("divTime");
            var divBattery=document.getElementById("divBattery");
            var divBatteryFill=document.getElementById("divBatteryFill");
            
            divTime.innerHTML=split[0];
            
            divBattery.title=split[1]+"%";
            divBatteryFill.style.height=(100-split[1])+"%";            
            
            switch (split[2]){
                case "0":
                    document.getElementById("wifi0").style.fill="#444";
                    document.getElementById("wifi1").style.fill="#444";
                    document.getElementById("wifi2").style.fill="#444";
                    document.getElementById("wifi3").style.fill="#fff";
                    break;
                    
                case "1":
                    document.getElementById("wifi0").style.fill="#444";
                    document.getElementById("wifi1").style.fill="#444";
                    document.getElementById("wifi2").style.fill="#fff";
                    document.getElementById("wifi3").style.fill="#fff";
                    break;
                    
                case "2":
                    document.getElementById("wifi0").style.fill="#444";
                    document.getElementById("wifi1").style.fill="#fff";
                    document.getElementById("wifi2").style.fill="#fff";
                    document.getElementById("wifi3").style.fill="#fff";
                    break;
                    
                case "3":
                    document.getElementById("wifi0").style.fill="#fff";
                    document.getElementById("wifi1").style.fill="#fff";
                    document.getElementById("wifi2").style.fill="#fff";
                    document.getElementById("wifi3").style.fill="#fff";
                    break;
            }
            
            adjustDeskbar();
        }
    };

    xhr.open("GET", "getstatus"+"&"+new Date().getTime(), true);  
    xhr.send();
}