package com.controller;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entity.Admin;
import com.entity.Login;
import com.entity.Reservation;
import com.entity.Seat;
import com.entity.Space;
import com.entity.SpaceTime;
import com.repository.ReservationRepository;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;

@Controller
public class SpaceListController {

    @Autowired
    private SpaceRepository spaceRep;
    @Autowired
    private SeatRepository seatRep;
    @Autowired
    private SpaceTimeRepository spaceTimeRep;
    @Autowired
    private ReservationRepository reservationRep;

    // スペース一覧表示
    @RequestMapping("/space")
    public String disp(Model model,
    		HttpSession session) {
    	
    	Login loginUser = (Login) session.getAttribute("loginUser");
    	Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (loginUser == null && adminUser == null) {
            return "redirect:/login";
        }
        
    	
    	// 全スペースと座席を取得
        List<Space> spaceTable = spaceRep.findAll();
        List<Seat> seatTable = seatRep.findAll();

        // 各スペースについて、空席があるかどうかを判定
        for (Space space : spaceTable) {
            int totalSeats = 0;
            for (Seat seat : seatTable) {
                if (seat.getSpaceId() == space.getId()) {
                    totalSeats += seat.getSeatCount();
                }
            }
            space.setHasVacancy(totalSeats > 0);// 空席ありかどうか設定
        }

        model.addAttribute("spaceList", spaceTable);
        
        return "space";//一覧画面
    }

    /**
     * 予約確認画面（選択スペースとその時間帯を表示）
     */
    @RequestMapping("/conf")
    public String reserve(Model model,
    		@RequestParam("spaceId") Integer spaceId,
    		HttpSession session) {

    	Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        
    	// 選択されたスペースを取得
        Space selectedSpace = spaceRep.findById(spaceId).orElse(null);
        
        // 無効なスペースIDの場合、一覧へリダイレクト
        if (selectedSpace == null) {
            return "redirect:/space";//一覧画面
        }

        // 全座席と時間帯のデータ取得
        List<Seat> seatTable = seatRep.findAll();
        List<SpaceTime> spaceTimeList = spaceTimeRep.findAll();

        // 各時間帯の座席数をカウント
        for (SpaceTime time : spaceTimeList) {
            int totalSeats = 0;
            for (Seat seat : seatTable) {
                if (seat.getSpaceId() == spaceId && seat.getSpaceTimesId() == time.getSpaceTimesId()) {
                    totalSeats += seat.getSeatCount();// 対象のスペースかつ時間帯の席数を加算
                }
            }
            time.setSeatCount(totalSeats);
        }

        /*
         * 合計座席数　
         * →予約画面で座席数をプルダウンに取り入れ(上記処理)の為、不要
         */
//        int totalSeatCount = 0;
//        for (Seat seat : seatTable) {
//            if (seat.getSpaceId() == selectedSpace.getId()) {
//                totalSeatCount += seat.getSeatCount();
//            }
//        }

//        selectedSpace.setHasVacancy(totalSeatCount > 0);
//        selectedSpace.setSeatCount(totalSeatCount);

        model.addAttribute("selectItems", Collections.singletonList(selectedSpace));
        model.addAttribute("spaceTimeList", spaceTimeList);

        return "reservation";//　予約画面
    }

    /**
     * 予約完了処理 
     * 　遷移元：スペース一覧space.html
     *  遷移先：予約完了画面reserve_comp.html
     */
    @Transactional// （座席更新と予約登録を一括で管理）
    @PostMapping("/reserve/complete")
    public String reserveComplete(
        @RequestParam("spaceId") int spaceId,
        @RequestParam("spaceTimesId") int spaceTimesId,
        HttpSession session,
        Model model) {

    	// セッション：ログイン中のnameを表示
        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        //　ログイン者のID取得
        int userId = loginUser.getID();
        // 予約情報の登録
        Reservation reservation = new Reservation();
        reservation.setSpaceId(spaceId);
        reservation.setSpaceTimesId(spaceTimesId);
        reservation.setUserId(userId);
        // reservationDay は @PrePersist で自動設定
        reservationRep.save(reservation);

        // 該当スペース・時間帯の座席を取得
        Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);
        if (seat != null && seat.getSeatCount() > 0) {
            seat.setSeatCount(seat.getSeatCount() - 1);// 残席があれば 1 減らして、下記保存（更新）
            seatRep.save(seat);
        } else {
        	// エラー処理（// 残席がない（満席）場合のエラーメッセージ）
            model.addAttribute("message", "満席のため予約できませんでした。");
            return "reserve_comp";
        }

        model.addAttribute("message", "予約が完了しました。");
        
        return "reserve_comp";
    }
    

}
