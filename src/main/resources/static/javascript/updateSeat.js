/*
 * 管理者用：座席数を増減する処理
 * ＋ / − ボタンから呼び出される
 */
function changeSeat(spaceId, spaceTimesId, diff) {

    // 座席数表示用の span 要素を取得
    const seatElemId = `seatCount-${spaceId}-${spaceTimesId}`;
    const seatElem = document.getElementById(seatElemId);

    if (!seatElem) {
        console.warn("座席数表示要素が見つかりません:", seatElemId);
        return;
    }

    /*
     * サーバーへ更新リクエスト送信
     * /admin/updateSeat に JSON で POST
     */
    fetch("/admin/updateSeat", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            spaceId: spaceId,
            spaceTimesId: spaceTimesId,
            diff: diff
        })
    })
    .then(response => {
        if (!response.ok) {
            alert("座席数の更新に失敗しました");
            throw new Error("Update failed");
        }
        return response.text();
    })
    .then(() => {
        // 表示上の座席数を更新
        let current = parseInt(seatElem.innerText, 10);
        current += diff;
        if (current < 0) current = 0;
        seatElem.innerText = current;
    })
    .catch(error => {
        console.error(error);
        alert("通信エラーが発生しました");
    });
}
