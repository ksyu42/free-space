$(function(){
	
	//ドラッグ時にファイルの開かないようにする
	$('.dragDropArea').on('dragover', function(e) {
	   e.preventDefault();
	});
	

	//ドロップした時の処理
	$('.dragDropArea').on('drop', function(e) {
		e.preventDefault();
  		//ファイル取得・画像表示呼び出し
		inpFile(e);
	});
	
	//ファイル取得
	function inpFile(e){
		//ドロップしたファイルの取得
    	let files = e.originalEvent.dataTransfer.files;
	    // 取得したファイルをinput[type=file]へ
	    file.files = files;
		//フォームデータを作成する
		let form = new FormData();
		//フォームデータにアップロードファイルの情報追加
		form.append("file", files);
	}
});