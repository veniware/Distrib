var me;

var viewas="tiles";
var current="/";
var l=[];

var isBusy=false;
var isUploading=false;

var listbox;
var uploadFrame=null;

var lblPath;

function init(){
    me=parent.windowsArray[parent.windowsArray.length-1];
    listbox=document.getElementById("listbox");
    lblPath=document.getElementById("lblPath");

    var split=document.URL.split("#");
    if (split.length > 1)
        for(var i=1; i < split.length; i++){
            if (split[i].indexOf("path=", 0) > -1) current=split[i].replace("path=", "");
        }

    if (current=="Photos") viewas="large";

    getFolder(current);
    
    /*keypress*/
    document.onkeydown=function(e){ document_backspacepressed(e) };    
    
    /*drag and drop - upload*/
    listbox.ondragover=function(){
        if (uploadFrame==null){
            if (isUploading) return;
            
            uploadFrame=document.createElement("div");
            uploadFrame.className="uploadframe";
            listbox.appendChild(uploadFrame);
            
            uploadFrame.ondragleave=function(){
                if (isUploading) return;
                if (uploadFrame!=null){
                    listbox.removeChild(uploadFrame);
                    uploadFrame=null;
                } 
                return false;
            };
            
            uploadFrame.ondrop=function(e){
                if (isUploading) return;
                
                if (e.dataTransfer.files.length==0 ||
                    current.substring(0,1)!="/") {
                    listbox.removeChild(uploadFrame);
                    uploadFrame=null;
                    e.preventDefault();
                    return;
                }
                
                upload(e.dataTransfer.files);
                e.preventDefault();
            };
           
        }
		return false;
    };

}

function upload(files){
    if (isUploading) return;
    
    /*file exists*/
    for(var i=0; i<l.length; i++) 
        if (l[i].name==files[0].name){
            alert("File already exist");
            
            if (uploadFrame!=null){
                listbox.removeChild(uploadFrame);
                uploadFrame=null;
            }
            return;
        }
    
    
    isUploading=true;
    uploadFrame.style.backgroundColor="rgba(0,0,0,.8)";
    
    var container=document.createElement("div");
    container.style.margin="128px auto";
    container.style.textAlign="center";
    container.style.width="100%";
    container.innerHTML="<u>Uploading:</u><br>"+files[0].name;
    container.style.fontSize="large";
    container.style.color="white";
    container.style.textShadow="black 0 0 8px";
    uploadFrame.appendChild(container);
    
    var wait=document.createElement("div");
    wait.className="wait";
    for(var i=0; i<3; i++) wait.appendChild(document.createElement("div"));
    container.appendChild(wait);
       
    var xhr=new XMLHttpRequest();
    xhr.onreadystatechange=function(){
        if (xhr.readyState==4){
            if (uploadFrame!=null){
                listbox.removeChild(uploadFrame);
                uploadFrame=null;
            }
            isUploading=false;
            
            if (xhr.status==200) getFolder(current);
        }
    };

    var formData=new FormData();
    formData.append("file", files[0]);
        
    xhr.open("post", "upload&"+current+"/"+files[0].name+"&"+new Date().getTime(), true);
	xhr.send(formData);
}

function document_backspacepressed(e){
    if (e.which==8) goUp();/*backspace*/
    if (e.which != 116) e.preventDefault();
}

function goUp(){
    if (current.length==3){
        getFolder("/");
        return;
    }

    var split=current.split("/");

    if (split.length > 1){
        var up="";
        for(var i=0; i < split.length-1 ; i++){
            up += split[i];
            if (i < split.length-2) up += "/";
        }
        if (up.length < 3) up += "/";
        getFolder(up);
    }
}

function getFolder(path){
    if (isBusy) return;
    
    var split = path.split("/");
    var name = split[split.length-1];
    
    me.src="files#path="+path;
    me.title=(name==""||name=="/")? "root" : name;
    me.icon=getFolderIcon(path);
    me.update();
    
    current=path;
    constactPath(path);

    while (listbox.firstChild) listbox.removeChild(listbox.firstChild);
    l=[];

    var wait=document.createElement("div");
    wait.className="wait";
    for(var i=0; i<3; i++) wait.appendChild(document.createElement("div"));
    listbox.appendChild(wait);

    var xhr=(window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    xhr.onreadystatechange=function(){
        if (xhr.readyState==4 && xhr.status==200){
            var response=xhr.responseText.split("|");

            for(var i=0; i < response.length-1; i += 6)
                l.push({
                    isFolder: response[i]=="true",
                    name: response[i+1],
                    fullname: response[i+2],
                    size: response[i+3],
                    canWrite: response[i+4] == "true",
                    modified: response[i+5]
                });
                
             listbox.removeChild(wait);
                
             plot();
             isBusy=false;
        }        
    };

    isBusy=true;
    xhr.open("GET", "getfiles&"+path+"&"+new Date().getTime(), true);
    xhr.send();
}

function constactPath(path){
    lblPath.innerHTML="";

    if (current.toLowerCase()=="photos" || current.toLowerCase()=="videos"){
        document.getElementById("goUp").style.visibility="hidden";
        lblPath.style.left="8px";
    }

    var s=path.split("/");

    for(var i=0; i<s.length; i++)
        if (s[i].length>0){
            var newButton=document.createElement("div");
            newButton.innerHTML=s[i];
            newButton.id="i"+i;
            newButton.className="toolbaricon";
            newButton.style.marginLeft="0";
            newButton.style.marginRight="0";
            newButton.style.paddingLeft="2px";
            newButton.style.PaddingRight="2px";
            lblPath.appendChild(newButton);

            if (i < s.length-1){
                var slash=document.createElement("div");
                slash.innerHTML += " ";
                slash.className="triangle";
                lblPath.appendChild(slash);
            }

            if (i < s.length-1) newButton.onclick=function(){
                var index=parseInt(this.id.replace("i", ""));
                var goto="";
                for(var j=0; j < index+1; j++) goto += s[j]+"/";
                if (goto[goto.length-1]=="/") goto=goto.substring(0, goto.length-1);
                getFolder(goto);
            };
        }

    if (lblPath.innerHTML=="") lblPath.innerHTML="/";
}

function getFolderIcon(path){
    if (path=="/storage/external_SD") return "sd.png";
    if (path=="/sdcard") return "sd.png";
    
    if (path=="/storage/emulated/0") return "phone.png";

    var split=path.split("/");
    
    if (split[split.length-1].toLowerCase()=="photos") return "photos.png";
    if (split[split.length-1].toLowerCase()=="pictures") return "photos.png";
    if (split[split.length-1].toLowerCase()=="dcim") return "photos.png";
    
    if (split[split.length-1].toLowerCase()=="alarms") return "audio.png";
    if (split[split.length-1].toLowerCase()=="music") return "audio.png";
    if (split[split.length-1].toLowerCase()=="ringtones") return "audio.png";
    if (split[split.length-1].toLowerCase()=="audio") return "audio.png";
    
    if (split[split.length-1].toLowerCase()=="video") return "video.png";
    if (split[split.length-1].toLowerCase()=="videos") return "video.png";
    if (split[split.length-1].toLowerCase()=="movies") return "video.png";
    
    return "folder.png";
}

function viewFile(index){
    var currentIndex=index;
   
    var disable=document.createElement("div");
    disable.className="disable";
    document.body.appendChild(disable); 
    
    var viewframe=document.createElement("div");
    viewframe.className="viewform";
    disable.appendChild(viewframe);
    
    var cmdClose=document.createElement("div");
    cmdClose.innerHTML="x";
    cmdClose.className="cmdClose";
    cmdClose.title="Close";
    disable.appendChild(cmdClose);
    
    cmdClose.onclick=function(){closeViewFile(disable, viewframe)};

    document.onkeydown=function(e){
        switch (e.which){
            case 37: /*left*/
                try{        
                    window.stop();
                } catch (e){}
                if (currentIndex > 0) currentIndex--;
                else break;
                previewFile(viewframe, currentIndex);
                break;

            case 39: /*right*/
                try{        
                    window.stop();
                } catch (e){}
                if (currentIndex < l.length-1) currentIndex++;
                else break;
                previewFile(viewframe, currentIndex);
                break;

            case 27: case 8: /*esc or backspace*/
                closeViewFile(disable, viewframe);
                break;
        }

        if (e.which != 116) e.preventDefault();
    };
    
    previewFile(viewframe, currentIndex);
}

function closeViewFile(disable, viewframe){
    document.onkeydown=function(e){ document_backspacepressed(e) };

    try{   
        /*Internet Explorer and his lil-brother Edge don't like this command*/     
        window.stop();
    } catch (e){}

    disable.style.transition=".4s";
    disable.style.webkitTransition=".4s";
    disable.style.visibility="hidden";
    disable.style.opacity="0";

    viewframe.style.transform="rotateX(-30deg)";
    viewframe.style.webkitTransform="rotateX(-30deg)";

    setTimeout(function(){
        document.body.removeChild(disable);
    }, 400);
}

function previewFile(viewframe, index){
    viewframe.innerHTML="";
    
    var divDetails=document.createElement("div");
    divDetails.className="viewDetails";
    viewframe.appendChild(divDetails);
    
    var icon=document.createElement("img");
    icon.className="viewIcon";
    icon.src=(l[index].isFolder)? getFolderIcon(l[index].fullname):"file.png";    
    divDetails.appendChild(icon);
    
    var title=document.createElement("div");
    title.className="viewTitle";
    title.innerHTML=l[index].name;
    divDetails.appendChild(title);
    
    var table=document.createElement("table");
    table.className="viewTable";
    divDetails.appendChild(table);
    
    table.appendChild(addViewDetail("Path:", l[index].fullname));
    table.appendChild(addViewDetail("Size:", l[index].size));
    table.appendChild(addViewDetail("Modified:", l[index].modified));
    
    var download=document.createElement("div");
    download.className="viewDownload";
    download.innerHTML="<b>Download</b>";
    download.onclick=function(){ window.open("download&"+l[index].fullname); };
    divDetails.appendChild(download);
    
    var exSplit=l[index].fullname.split(".");
    var extention=exSplit[exSplit.length-1].toLowerCase();
    if (extention.length > 5) extention="";
    
    if (extention!=""){
        var divExtention=document.createElement("div");
        divExtention.className="lblExtention";
        divExtention.innerHTML=extention;
        divExtention.style.padding="2px 8px";
        divExtention.style.position="absolute";
        divExtention.style.left="124px";
        divExtention.style.top="64px";
        divExtention.style.cursor="default";
        divExtention.style.textTransform="uppercase";
        divExtention.style.backgroundColor=getExtensionColor(extention.toUpperCase());
        divDetails.appendChild(divExtention);  
    }

    switch (extention.toLowerCase()){
        
        case "png":
        case "jpg": case "jpe": case "jpeg": case "jfif":
        case "gif":
        case "tif": case "tiff":
        case "webp":
            var divPreview=document.createElement("div");
            divPreview.className="viewPreview";
            divPreview.style.backgroundImage="url('getfile&"+l[index].fullname +"')";
            viewframe.appendChild(divPreview);
            
            var imageLoader=document.createElement("img");
            imageLoader.src="getfile&"+l[index].fullname;
            imageLoader.onload=function(){
                table.appendChild(addViewDetail("Size:", imageLoader.width+" x "+imageLoader.height+" px"));
            };
            
            break;
        
        case "avi":
        case "mp4":
        case "3gp":
            var videoContainer=document.createElement("div");
            videoContainer.className="viewPreview";
            viewframe.appendChild(videoContainer);
            
            var videoPlayer=document.createElement("video");
            videoPlayer.setAttribute("controls", "");
            videoPlayer.style.width="100%";
            videoPlayer.style.height="100%";
            videoContainer.appendChild(videoPlayer);
                
            var source=document.createElement("source");
            source.src="getfile&"+l[index].fullname;
            if (extention=="avi") source.type="video/x-msvideo";
            if (extention=="mp4") source.type="video/mp4";
            if (extention=="3gp") source.type="video/3gpp";
            videoPlayer.appendChild(source);
            
            for(var i=0; i<l.length; i++){   
                var subSplit=l[i].fullname.split(".");
                var subExtention=subSplit[subSplit.length-1].toLowerCase();
                
                if (subExtention=="vtt"){                    
                    var track=document.createElement("track");
                    track.label=l[i].name.substring(0, l[i].name.length-4);
                    track.kind="subtitles";
                    track.src="getfile&"+l[i].fullname;
                    videoPlayer.appendChild(track);
                }
            }
            break;
        
        case "aif": case "aiff": case "aifc":
        case "mid": case "rmi":
        case "mp3":
        case "ogg":
        case "wav":
            var audioContainer=document.createElement("div");
            audioContainer.className="viewPreview";
            viewframe.appendChild(audioContainer);
            
            var audioPlayer=document.createElement("audio");
            audioPlayer.setAttribute("controls", "");
            audioPlayer.style.position="absolute";
            audioPlayer.style.width="50%";
            audioPlayer.style.height="48%";
            audioPlayer.style.left="25%";
            audioContainer.appendChild(audioPlayer);

            var audiosource=document.createElement("source");
            audiosource.src="getfile&"+l[index].fullname;
            if (extention=="aif" || extention=="aiff" || extention=="aifc") audiosource.type="audio/aiff";
            if (extention=="mid" || extention=="rmi") audiosource.type="audio/midi";
            if (extention=="mp3") audiosource.type="audio/mpeg";
            if (extention=="ogg") audiosource.type="audio/ogg";
            if (extention=="wav") audiosource.type="audio/wav";
            audioPlayer.appendChild(audiosource);
            
            break;        
    }
    
}

function addViewDetail(name, value){
    var row=document.createElement("tr");

    var colName=document.createElement("td");
    colName.innerHTML=name;
    row.appendChild(colName);
    
    var colValue=document.createElement("td");
    colValue.innerHTML=value;
    row.appendChild(colValue);
    
    return row;
}

function deleteFile(filename){
    var xhr=new XMLHttpRequest();
    xhr.onreadystatechange=function(){
        if (xhr.readyState==4 && xhr.status==200){
            getFolder(current);
        }
    };
        
    xhr.open("get", "delete&"+filename+"&"+new Date().getTime(), true);
	xhr.send();
}

function plot(){
    if (l.length==0){
        var divEmpty=document.createElement("div");
        divEmpty.id="ignore";
        divEmpty.innerHTML="<i>No files</i>";
        divEmpty.style.fontSize="large";
        divEmpty.style.margin="40px auto";
        divEmpty.style.textAlign="center";
        divEmpty.style.color="#888";
        listbox.appendChild(divEmpty);
        return;
    }
    
    for(var i=0; i < l.length; i++){
        var div=document.createElement("div");
        div.className=viewas+" icon";
        div.id="i"+i;

        if (l[i].isFolder){
            div.onclick=function(e){
                var index=parseInt(this.id.replace("i", ""));
                if (e.button==0) getFolder(l[index].fullname);
                else parent.addWindow("files#path="+l[index].fullname, l[index].name, getFolderIcon(l[index].fullname));
            };
        } else{
            div.onclick=function(e){
                var index=parseInt(this.id.replace("i", ""));
                viewFile(index);
            };
        }
        
        var split=l[i].name.split(".");
        var extentionText=split[split.length-1].toUpperCase();
        
        /*thumbnail*/                
        if (!l[i].isFolder && 
            extentionText=="PNG" ||
            extentionText=="JPG" || extentionText=="JPE" || extentionText=="JPEG" || extentionText=="JFIF" ||
            extentionText=="GIF" ||
            extentionText=="TIF" || extentionText=="TIFF" ||
            extentionText=="WEBP"){
            
            var thumbnail=document.createElement("div");
            thumbnail.className="itemicon";
            thumbnail.style.backgroundPosition="center";
            thumbnail.style.backgroundRepeat="no-repeat";
            thumbnail.style.backgroundSize="contain";
            thumbnail.style.backgroundImage="url('getthumbnail&"+l[i].fullname+"')";
            div.appendChild(thumbnail);

        } else {
            var ico=document.createElement("img");
            if (l[i].isFolder){
                ico.src=getFolderIcon(l[i].fullname);
            }else{
                ico.src="file.png";
            }
            ico.className="itemicon";
            div.appendChild(ico);
        }

        if (!l[i].isFolder && l[i].name.indexOf(".", 0) > -1){
            var extention=document.createElement("div");
            extention.className="lblExtention";
            extention.innerHTML=extentionText;
            extention.style.backgroundColor=getExtensionColor(extentionText);
            div.appendChild(extention);
        }
        
        if (l[i].name.length > 0){
            var newDiv1=document.createElement("div");
            newDiv1.className="lblName";
            newDiv1.innerHTML=l[i].name;
            div.appendChild(newDiv1);
        }

        if (l[i].size.length > 0){
            var newDiv2=document.createElement("div");
            newDiv2.className="lblSize";
            newDiv2.innerHTML=l[i].size;
            div.appendChild(newDiv2);
        }

        if (l[i].modified.length > 0){
            var newDiv3=document.createElement("div");
            newDiv3.className="lblModified";
            newDiv3.innerHTML=l[i].modified;
            div.appendChild(newDiv3);
        }
        
        /*delete*/
        if (l[i].canWrite && !l[i].isFolder){
            var divDelete=document.createElement("div");
            divDelete.className="delete";
            divDelete.innerHTML="x";
            divDelete.id="d"+i;
            divDelete.title="Delete";
            div.appendChild(divDelete);
            
            divDelete.onclick=function(e){
                e.stopPropagation();
                var index=parseInt(this.id.replace("d", ""));
                if (confirm("Are you sure you want to delete '"+l[index].name+"'"))
                    deleteFile(l[index].fullname);
            };
        }
        
        if (i < 100){
            div.style.animationDuration=i*0.02+"s";
            div.style.webkitAnimationDuration=(i*0.02)+"s";
        }

        listbox.appendChild(div);
    }
}

function getExtensionColor(extention){
    var color="rgba("+((extention.charCodeAt(0)*5) % 192+63)+"," +
                      ((extention.charCodeAt(1 % extention.length)*5) % 192+63)+","+
                      ((extention.charCodeAt(2 % extention.length)*5) % 192+63)+",.5)";
              
    return color;
}

function viewAsLarge(){
    viewas="large";
    var listbox=document.getElementById("listbox");
    for(var i=0; i < listbox.childNodes.length; i++)
        if (listbox.childNodes[i].id !="ignore") listbox.childNodes[i].className="large icon";
}

function viewAsTiles(){
    viewas="tiles";
    var listbox=document.getElementById("listbox");
    for(var i=0; i < listbox.childNodes.length; i++)
        if (listbox.childNodes[i].id !="ignore") listbox.childNodes[i].className="tiles icon";
}

function viewAsList(){
    viewas="list";
    var listbox=document.getElementById("listbox");
    for(var i=0; i < listbox.childNodes.length; i++)
        if (listbox.childNodes[i].id !="ignore") listbox.childNodes[i].className="list icon";
}