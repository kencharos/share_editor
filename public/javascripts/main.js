function px2int(s) {
  if (s === undefined) {
    return 0;
  }
  if(s.indexOf("px") >= 0) {
    return s.slice(0, -2) -0;
  } else {
    return s -0;
  }
}

/***** ドラッグ開始時の処理 *****/
function dragstart(event){
  //ドラッグするデータのid名をDataTransferオブジェクトにセット
  event.dataTransfer.setData("text", event.target.id);
  // 要素の右上とクリック位置の差分を保持しておく
  event.dataTransfer.setData("diffy", event.pageY - px2int(event.target.style.top));
  event.dataTransfer.setData("diffx", event.pageX - px2int(event.target.style.left));
}

/***** ドラッグ要素がドロップ要素に重なっている間の処理 *****/
function dragover(event){
  //dragoverイベントをキャンセルして、ドロップ先の要素がドロップを受け付けるようにする
  event.preventDefault();
}

/***** ドロップ時の処理 *****/
function drop(event){

  var id_name = event.dataTransfer.getData("text");
  var drag_elm =document.getElementById(id_name);
  //debugger;
  drag_elm.style.top= event.pageY - event.dataTransfer.getData("diffy") + "px";
  drag_elm.style.left=event.pageX - event.dataTransfer.getData("diffx")+ "px";
  //エラー回避のため、ドロップ処理の最後にdropイベントをキャンセルしておく
  event.preventDefault();
}
