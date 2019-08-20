var listbox,chatwith,historybox,txtMessage;

var chat={};
var currentConv=null;

function initChat(){
	listbox=document.getElementById("listbox");
    chatwith=document.getElementById("chatwith");
    historybox=document.getElementById("history");
    txtMessage=document.getElementById("message");
    
    updateChat_tick();
    
    txtMessage.onkeypress=function(e){
        if (e.keyCode==13){
            if (currentConv==null) return;
            if (txtMessage.value=="") return;
            var destination=chat[currentConv].numb;
            sendSms(destination, txtMessage.value);
            txtMessage.value="";
        }
    };    
}

function updateChat_tick(){
    getChat();
    setTimeout(function(){updateChat_tick();}, 20000);
}

function getChat(){    
    
	var xhr=(window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    xhr.onreadystatechange=function(){
        if (xhr.readyState==4 && xhr.status==200){
            chat={};
            
            var response=xhr.responseText.split("|");
            for(var i=0; i<response.length-1; i+=5){
                var numb=response[i];
                var name=response[i+1];
                var date=parseInt(response[i+2]);
                
                var key=name+numb.substring(numb.length-3,numb.length);
                
                if (chat[key]==undefined){
                   chat[key]={
                       numb:numb,
                       name:name,
                       conversation:[],
                       lastdate:0
                   };
                }
                
                chat[key].conversation.push({
                    date:date,
                    body:response[i+3],
                    type:response[i+4],
                });
                
                if (chat[key].lastdate<date) chat[key].lastdate=date;
            }
            
            plotChat();
        }
    };

    xhr.open("GET", "getchat&"+new Date().getTime(), true);
    xhr.send();
}

var lastRecieve=0;
function plotChat(){
    listbox.innerHTML="";
    
    for(var o in chat){
        var container=document.createElement("div");
        container.id="id"+o;
        container.className="listitem";
        listbox.appendChild(container);
        
        var divName=document.createElement("div");
        divName.innerHTML="<b>"+chat[o].name+"</b>";
        container.appendChild(divName);
        
        var divNumb=document.createElement("div");
        divNumb.innerHTML=chat[o].numb;
        container.appendChild(divNumb);
        
        container.onclick=function(){
            var index=this.id.substring(2);
            currentConv=index;
            
            chatwith.innerHTML=chat[currentConv].name;
            historybox.innerHTML="";
            
            lastRecieve=0;
                            
            var conversation=chat[currentConv].conversation;
            for(var i=conversation.length-1; i>-1; i--){
                lastRecieve=conversation[i].date;
                addMessageBox(conversation[i].body, conversation[i].type=="1");
            }            
        };
    }
    
    if (listbox.firstChild==null){
        var divEmpty=document.createElement("div");
        divEmpty.innerHTML="<i>No messages</i>";
        divEmpty.style.fontSize="large";
        divEmpty.style.margin="40px auto";
        divEmpty.style.textAlign="center";
        divEmpty.style.color="#888";
        listbox.appendChild(divEmpty);
        return;
    }
    
    if (currentConv!=null){
        var conversation=chat[currentConv].conversation;
        for(var i=conversation.length-1; i>-1; i--)
            if (conversation[i].date>lastRecieve){
                lastRecieve=conversation[i].date;
                addMessageBox(conversation[i].body, conversation[i].type=="1");
            }            
    }
    
}

function addMessageBox(message, income){
    var newBox=document.createElement("div");
    newBox.innerHTML=message.replace("\n", "<br>");    
    newBox.className=(income)?"messageget":"messagesend";
    
    historybox.appendChild(newBox);
    
    historybox.scrollTop=historybox.scrollHeight*2+1000;
}

function sendSms(destination, message){
    if (destination=="") return;
    if (message=="") return;
    
	var xhr=(window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    xhr.onreadystatechange=function(){
        if (xhr.readyState==4 && xhr.status==200){
            getChat();
        }
    };
    
    while (message.indexOf("&",0) > 0) message=message.replace("&","%26");
    while (message.indexOf("#",0) > 0) message=message.replace("#","%23");
    while (message.indexOf("?",0) > 0) message=message.replace("?","%3F");
        
    xhr.open("GET", "sendsms&"+destination+"&"+message, true);
    xhr.send();
}