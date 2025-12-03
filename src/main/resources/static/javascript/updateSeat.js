function changeSeat(spaceId, timeIndex, changeValue) {
　　
　　// DOM要素の取得
  const seatId = `seatCount-${spaceId}-${timeIndex}`;
  const elem = document.getElementById(seatId);

　　// HTML上で該当する座席数の表示要素（id="seatCount-1-0"など）をサーチ
  if (!elem) {
    console.warn(`座席要素が見つかりません: id=${seatId}`);
    return;
  }

　　/*
   * サーバーにリクエスト送信
   *  サーバーの /admin/updateSeat に JSON形式でPOST
   *  changeValue によって +1 または -1 などを送る
  */
 
  fetch("/admin/updateSeat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      spaceId: spaceId,
      timeIndex: timeIndex,
      diff: changeValue
    })
  })

  // レスポンス受信後に画面を更新
  .then(response => {
    if (!response.ok) {
      alert("更新に失敗しました");
      return;
    }

    // 座席数の表示を更新
    let current = parseInt(elem.innerText);
    current = current + changeValue;
    if (current < 0) current = 0;
    elem.innerText = current;
  })
  .catch(error => {
    alert("通信エラーが発生しました");
    console.error(error);
  });
}
