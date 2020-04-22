function show() {
	document.getElementById("PLEASEWAIT").style.display = 'inline';
	document.getElementById("PLEASEWAIT_BG").style.display = 'inline';
}

function blockInput(event) {
	if (event.status == "begin") {
		document.getElementById("PLEASEWAIT").style.display = 'inline';
		document.getElementById("PLEASEWAIT_BG").style.display = 'inline';
	} else if (event.status == "complete") {
		document.getElementById("PLEASEWAIT").style.display = 'none';
		document.getElementById("PLEASEWAIT_BG").style.display = 'none';
	}
}

function confirmDelete() {
//	return confirm("Do you really want do delete?");
	return confirm("작업공간을 삭제하시겠습니까?");
}

function maximize() {
	var mainDiv = document.getElementById('main');
	mainDiv.className = 'main_maximized';
}

// BY_JIN
function checkResources(frm) {
	var newConfigId = $("input[type='text']").val();
	var chcekCount = 0;
	$( ".configId" ).each(function( index ) {
		if (newConfigId.toUpperCase() === $( this ).text().toUpperCase()) {
			alert("이미 등록되어 있습니다.");
			chcekCount++;
		}
	});
	
	if (chcekCount === 0) {
		frm.submit();
	}
}

function korTextCheck($str){
    var str = $str;
    var check = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/;
    var result = str.match(check);
    if(result) return true; //한글일 경우
    return false; //한글이 아닐경우
}

// console.log( korTextCheck("한글이다") ); //true
// console.log( korTextCheck("english123") ); //false



function korCodeCheck($str){
    var str = $str;
    for(i=0; i<str.length; i++){
        if(!((str.charCodeAt(i) > 0x3130 && str.charCodeAt(i) < 0x318F) || (str.charCodeAt(i) >= 0xAC00 && str.charCodeAt(i) <= 0xD7A3)))
        return false; //한글이 아닐경우
        else return true; //한글일 경우
    }
}

// console.log( korCodeCheck("한글이다") ); //true
// console.log( korCodeCheck("english123") ); //false

//BY_JIN
function checkFileName() {
	var fileName = $("input[type='file']").val();
	
	if (korTextCheck(fileName)) {
		$("input[type='file']").val("");
		alert("작업공간 파일명에 한글을 포함할 수 없습니다.");
	}
	
}
