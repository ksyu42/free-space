$(function(){
	
	let searchBtn = $('#searchBtn');
	searchBtn.on('click', function(){
		$("#searchArea").slideToggle('slow');
        $("#searchArea").toggleClass('active');
	});
	
	//検索内(検索ボタン)id要素取得/返納
	let resultBtn = $('#resultBtn');
	//検索ボタンクリック時イベントと付与
	resultBtn.on('click input', function(){

		let searchName = $('#searchName').val();　//検索内入力値を取得/格納
		let tbody = $('tbody');　//tbodyタグ取得/格納
		let tr = tbody.children();　//tbodyの子要素(tr)を取得
		
		//tr要素数分for文回す
		for(let i = 0; i < tr.length; i ++ ){

			let tdList = tr.eq(i).children(); //iの番号の子要素(td)取得
			
			//検索入力値に入力ある場合
			if(searchName !== ''){
				
				var name = tdList.eq(1);　//tdの2番目(名前)の取得/格納
				//文字/入力値のインデックス取得　→　数値　(値がない場合は-1で表示)
				var result = name.text().includes(searchName);
				
				// 氏名に一致する場合
				if(result){
					//チャックボックス処理呼び出し
					check(tdList);
				// 氏名に一致しない場合
				}else{
					// 行非表示
					tdList.hide();
				}
			//検索入力欄が未入力の場合
			}else{
				//チャックボックス処理呼び出し
				check(tdList);
			}
			
		}
	});
	
	function check(tdList){	
		
		// 検索チェックボックス値
		let check = $('input[name="program"]:checked'); //チェック回数
		let checkArray = check.parents('label').children('span'); 
		
		//チェックと所持のtrue数カウント用配列/ユーザー分生成
		let count = [];
		
		//チェックした分だけ回す
		for(let n = 0; n < checkArray.length; n++){
			let checkbox = checkArray[n].innerText;　//値を配列に格納
				
			let langNum = tdList.eq(3); //tdの4番目(言語)を取得/格納
			let langArray = langNum.text().split(',') //該当テキストを配列で取得/格納
			
			//各ユーザー技術名数文繰り返す
			for(let j　= 0; j < langArray.length;  j++ ){
				
				let langName = langArray[j]; //ユーザー所持の言語を取得
				let judge = langName === checkbox;
				//tureの場合count配列にpush
				if(judge){
					count.push(judge);
				}
			}	
		}
		// チャック回数と所持数が一致している場合
		if(count.length == check.length){
			// 行表示
			tdList.show();
		}else{
			// 行非表示
			tdList.hide();
		}
	}
});