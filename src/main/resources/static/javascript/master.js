$(function() {
	var open = $('.modal-open');
    close = $('.modal-close');
    container = $('.modal-container');

    //モーダル表示
    open.on('click',function(){	
        container.addClass('active');
        return false;
    });

    //モーダル閉じる
    close.on('click',function(){	
        container.removeClass('active');
    });

    //モーダルの外側をクリックしたらモーダルを閉じる
    $(document).on('click',function(e) {
        if(!$(e.target).closest('.modal-body').length) {
            container.removeClass('active');
        }
    });
	/* ajax値受け渡し実装例
	//モーダル送信ボタン押下
	$("#mdl_form").on("submit", function(e) {
	    e.preventDefault();  // デフォルトのイベント(ページの遷移やデータ送信など)を無効にする
		
		$.ajax({
			url: "/ajax",  // コントローラー指定
			type: "POST",  // HTTPメソッドを指定
			data: {
				note: $("#mdllogin").val()  // 送信データ
			}
		})
		.done(function(data) {　//通信成功時　引数にコントローラーreturn値が入る
		  console.log(data);
		})
		.fail(function() {
		  alert("error!");  // 通信に失敗した場合の処理
		})
	});
	*/
	
	//モーダル送信ボタン押下
	$("#btnSend").click(function() {
	    $("#btnSend").prop("disabled", true);
        // フォームのデータをJSONに変換
		//form内のすべての入力値を取得/格納
        var rawData = $('#contactform').serializeArray();
        var data = {};
        jQuery.each(rawData, function(e) {　//取得した値の各value取得/格納
            data[e.name] = e.value;
        });
        // Ajaxを使ってメールを送信
        $.ajax({
            type: "POST",
            url: "./sendmail",
            dataType: "json",
            data: JSON.stringify(data), //文字列に変換
            contentType: 'application/json',
            scriptCharset: 'utf-8',
            success: function(outdata) {
                if (outdata[0] == "OK") alert("メール送信しました");
                $("#btnSend").prop("disabled", false);
				container.removeClass('active');　//モーダル閉じる
            },
            error: function(errorThrown) {
                alert("Error : " + errorThrown);
                $("#btnSend").prop("disabled", false);
            }
        });
	});
	
});