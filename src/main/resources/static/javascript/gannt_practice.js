// 初期表示時
$(function() {

	// CSS関連 - ヘッダー
	const element = document.querySelector('.column_header');
	// 横にスクロールした分、位置をずらす
	element.style.left = -window.pageXOffset + 'px';
	window.addEventListener('scroll', () => {
		element.style.left = -window.pageXOffset + 'px';
	});

	$("hoge").sortable({
		handle: 'button',
		cancel: '' // 空文字を指定
	})

/*
* 1複数行選択・2インデント機能
 */
//start----------------------------------------------------------------------------------------------------------------------------------------------------

	//選択下行を格納
	let selectTr = [];

	// → ボタン押下時
	$('#indent_right').on('click', function() {

		//選択した行分繰り返し
		$.each(selectTr, function(index, value) {
			
			//現インデントの値を数値で取得
			let currentIndent = parseInt(value.children('.task_title').css('text-indent')); // 現在のtext-indentを取得
			let increment = 12; // 増やしたいpx数
			if (currentIndent < 36) {
				// 新しいtext-indentの値を計算
				let newIndent = currentIndent + increment;
				// 計算した値を要素に設定
				value.children('.task_title').css('text-indent', newIndent + 'px');
			}

		})

	})
	// ← ボタン押下時
	$('#indent_left').on('click', function() {
		
		$.each(selectTr, function(index, value) {
			let currentIndent = parseInt(value.children('.task_title').css('text-indent')); // 現在のtext-indentを取得
			let increment = -12; // 増やしたいpx数
			if (currentIndent > 0) {
				// 新しいtext-indentの値を計算
				let newIndent = currentIndent + increment;
				// 計算した値を要素に設定
				value.children('.task_title').css('text-indent', newIndent + 'px');
			}
		})
	})
	//行の選択
	$('#column_table_body').selectable({
		cancel: '.sort-handle, .ui-selected',
		selected: function(event, ui) {

			selectTr.push($(ui.selected));

		},
		unselected: function(event, ui) {
			// 選択が解除されたらイベントを削除する
			$(ui.unselected).find('#indent_right').off('click');
			$(ui.unselected).find('#indent_left').off('click');
			//選択解除に選択格納用の配列を全削除
			selectTr.splice(0);
		}

	}).sortable({
		placeholder: "ui-state-highlight",
		axis: 'y',
		opacity: 0.9,
		items: "> tr",
		handle: 'td, .sort-handle, .ui-selected',
		helper: function(e, item) {
			if (!item.hasClass('ui-selected')) {
				item.parent().children('.ui-selected').removeClass('ui-selected');
				item.addClass('ui-selected');
			}
			var selected = item.parent().children('.ui-selected').clone();
			ph = item.outerHeight() * selected.length;
			item.data('multidrag', selected).siblings('.ui-selected').remove();
			return $('<tr/>').append(selected);
		},
		cursor: "move",
		start: function(e, ui) {
			ui.placeholder.css({ 'height': ph });
		},
		stop: function(e, ui) { //stop オプションで、ui.item.data('multidrag') で取得した移動対象を移動後にui-selected を外す。
			var selected = ui.item.data('multidrag');
			ui.item.after(selected);
			ui.item.remove();
			selected.removeClass('ui-selected');
			$(selected).children("td").removeClass('ui-selected');
		}

	});
//end----------------------------------------------------------------------------------------------------------------------------------------------------
/*
* 3日数計算
*/
//start----------------------------------------------------------------------------------------------------------------------------------------------------
	// 日数計算関数
    function calculateDays() {
		$('#column_table_body tr').each(function() {
			const planStDate = $(this).find('.planSt').val();
			const planEdDate = $(this).find('.planEd').val();
			const actStDate = $(this).find('.actSt').val();
			const actEdDate = $(this).find('.actEd').val();
			
			// 予定日数計算
			if (planStDate && planEdDate) {
			    const start = new Date(planStDate);
			    const end = new Date(planEdDate);
			    const daysDifference = Math.ceil((end - start) / (1000 * 60 * 60 * 24) + 1);
			    $(this).find('.planDayCount').text(daysDifference <= 0 ? '×'　: daysDifference); // 負の場合は空文字
			} else {
			    $(this).find('.planDayCount').text(''); // 空のときは何も表示しない
			}
			
			// 実績日数計算
			if (actStDate && actEdDate) {
			    const start = new Date(actStDate);
			    const end = new Date(actEdDate);
			    const daysDifference = Math.ceil((end - start) / (1000 * 60 * 60 * 24) + 1);
			    $(this).find('.actDayCount').text(daysDifference <= 0 ? '×' : daysDifference); // 負の場合は空文字
			} else {
			    $(this).find('.actDayCount').text(''); // 空のときは何も表示しない
			}
		});
    }

    // 日付変更時に計算を実行
    $('#column_table_body').on('change', '.planSt, .planEd, .actSt, .actEd', calculateDays);
//end----------------------------------------------------------------------------------------------------------------------------------------------------


});