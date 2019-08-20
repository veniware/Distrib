var windowsArray=[];
var activeWindow=null;

var deskbar;
var desktop;
var statusbar;

function initDesk(){
    deskbar=document.getElementById("deskbar");
    desktop=document.getElementById("desktop");
    statusbar=document.getElementById("statusbar");

    /*load previous session*/
    var session=localStorage.getItem("session");
    if (session != null && session != ""){
        var l=session.split("|");
        for(var i=0; i < l.length-1; i += 3)
            addWindow(l[i], l[i+1], l[i+2]);
    }
   
    document.oncontextmenu=function(){ return false; };
    window.onunload=function(){ body_onclose(); };
    
    adjustDeskbar();
}

function body_onclose(){
    /*save session*/
    var session="";
    for(var i=0; i < windowsArray.length; i++)
        session += windowsArray[i].src+"|"+windowsArray[i].title+"|"+windowsArray[i].icon+"|";
    localStorage.setItem("session", session);
}

function addWindow(src, title, icon){
    var outerFrame=document.createElement("div");
    outerFrame.className="outerframe";

    var windowTitleBar=document.createElement("div");
    windowTitleBar.className="windowtitle";
    outerFrame.appendChild(windowTitleBar);
    
    var windowTitle=document.createElement("div");
    windowTitle.innerHTML=title;
    windowTitleBar.appendChild(windowTitle);

    var minimizeButton=document.createElement("div");
    minimizeButton.type="button";
    minimizeButton.className="winbutton";
    minimizeButton.title="Minimize";
    minimizeButton.style.right="28px";
    minimizeButton.innerHTML="-";
    windowTitleBar.appendChild(minimizeButton);

    var closeButton=document.createElement("div");
    closeButton.type="button";
    closeButton.className="winbutton";
    closeButton.title="Close";
    closeButton.style.right="2px";
    closeButton.innerHTML="x";
    windowTitleBar.appendChild(closeButton);

    var innerFrame=document.createElement("div");
    innerFrame.className="innerframe";
    outerFrame.appendChild(innerFrame);
    
    var iframe=document.createElement("iframe");
    iframe.src=src;
    iframe.setAttribute("allowfullscreen", "");
    iframe.sandbox.value="allow-same-origin allow-top-navigation allow-forms allow-scripts allow-popups allow-modals";
    innerFrame.appendChild(iframe);

    var deskicon=document.createElement("div");
    deskicon.className="deskicon tooltip deskiconactive";
    if (title.length > 0){
        deskicon.title=title;
    }else{
        deskicon.title="no name";
    }

    var iconimg=document.createElement("img");
    iconimg.src=icon;
    deskicon.appendChild(iconimg);

    deskbar.appendChild(deskicon);
    desktop.appendChild(outerFrame);

    var newWidnow={
        src: src,
        title: title,
        icon: icon,
        iframe: iframe,
        window: outerFrame,
        deskicon: deskicon,
        maximize: function(){ maximizeWindow(newWidnow); },
        minimize: function(){ minimizeWindow(newWidnow); },
        close: function(){ closeWindow(newWidnow); },
        update: function(){ updateWindow(newWidnow); },
        isActive: true
    };

    minimizeButton.onclick=function(){ minimizeWindow(newWidnow); };
    closeButton.onclick=function(){ closeWindow(newWidnow); };

    deskicon.onclick=function(event){
        if (event.button==0) maximizeWindow(newWidnow);
        if (event.button==1) closeWindow(newWidnow);
    };

    for(var i=0; i < windowsArray.length; i++)
        windowsArray[i].minimize();

    windowsArray.push(newWidnow);
    activeWindow=newWidnow;

    adjustDeskbar();
}

function maximizeWindow(o){
    if (o.isActive){
        minimizeWindow(o);
        return;
    }

    for(var i=0; i < windowsArray.length; i++) 
        windowsArray[i].minimize();
    
    o.window.style.transformOrigin="50% 50%";
    o.window.style.webkitTransformOrigin="50% 50%";
    o.window.style.transform="scale(1,1)";
    o.window.style.webkitTransform="scale(1,1)";
    o.window.style.opacity="1";
    o.window.style.visibility="visible";

    /*counter windows offset (for webkit browsers)*/
    o.iframe.style.height="0%";
    setTimeout(function(){ o.iframe.style.height="100%"; }, 50);

    activeWindow=o;
    o.isActive=true;
    o.deskicon.className="deskicon tooltip deskiconactive";
}

function minimizeWindow(o){
    o.window.style.transformOrigin="50% 100%";
    o.window.style.webkitTransformOrigin="50% 100%";
    o.window.style.transform="scale(.6,.6)";
    o.window.style.webkitTransform="scale(.6,.6)";
    o.window.style.left="0";
    o.window.style.top="0";
    o.window.style.opacity="0";
    o.window.style.visibility="hidden";

    o.isActive=false;
    o.deskicon.className="deskicon tooltip";
}

function closeWindow(o){
    adjustDeskbar();

    o.window.style.transform="scale(.8,.8)";
    o.window.style.webkitTransform="scale(.8,.8)";
    o.window.style.opacity="0";

    o.deskicon.style.transform="scale(.5,.5)";
    o.deskicon.style.webkitTransform="scale(.5,.5)";
    o.deskicon.style.opacity="0";
    o.deskicon.style.width="0";

    if (activeWindow==o) activeWindow=null;

    setTimeout(function(){ removeWindow(o); }, 333);
}

function removeWindow(o){
    var index=windowsArray.indexOf(o);
    if (index < 0) return;

    deskbar.removeChild(o.deskicon);
    desktop.removeChild(o.window);

    windowsArray.splice(index, 1);
}

function updateWindow(o){
    o.window.firstChild.firstChild.innerHTML=o.title;
    o.deskicon.title=o.title;
    o.deskicon.firstChild.src=o.icon;
}

function adjustDeskbar(){
    var iconSize=(window.innerWidth / (windowsArray.length+1) > 64) ? 64 : window.innerWidth / (windowsArray.length+1);

    for(var i=0; i < windowsArray.length; i++){
        windowsArray[i].deskicon.style.width=iconSize+"px";
        windowsArray[i].deskicon.style.height=iconSize+"px";
    }

    deskbar.style.height=iconSize+"px";
    desktop.style.bottom=iconSize+"px";
    
    statusbar.style.left=(window.innerWidth-statusbar.clientWidth)/2+"px";
}