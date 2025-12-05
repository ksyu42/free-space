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
import com.service.AvailableSeatService;

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
    @Autowired
    private AvailableSeatService availableSeatService;


    /**
     * スペース一覧表示
     */
    @RequestMapping("/space")
    public String disp(Model model, HttpSession session) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        Admin adminUser = (Admin) session.getAttribute("adminUser");

        // ユーザーまたは管理者のどちらもログインしていない場合はログインページへ
        if (loginUser == null && adminUser == null) {
            return "redirect:/login";
        }

        List<Space> spaceTable = spaceRep.findAll();
        List<Seat> seatTable = seatRep.findAll();
        List<SpaceTime> timeList = spaceTimeRep.findAll();

        // 各スペースの空席有無をチェック
        for (Space space : spaceTable) {

            boolean hasVacancy = false;

            for (SpaceTime time : timeList) {

                int totalSeat = 0;

                for (Seat seat : seatTable) {

                    // Space.id は Long のため int に変換して比較
                    boolean sameSpace = (seat.getSpaceId() == space.getId().intValue());
                    boolean sameTime = (seat.getSpaceTimesId() == time.getSpaceTimesId());

                    if (sameSpace && sameTime) {
                        totalSeat += seat.getSeatCount();
                    }
                }

                // 任意の時間帯で 1つでも seat > 0 があれば空席あり
                if (totalSeat > 0) {
                    hasVacancy = true;
                    break;
                }
            }

            space.setHasVacancy(hasVacancy);
        }

        model.addAttribute("spaceList", spaceTable);

        return "space";
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
        Space selectedSpace = spaceRep.findById(spaceId.longValue()).orElse(null);

        // 無効なスペースIDの場合、一覧へリダイレクト
        if (selectedSpace == null) {
            return "redirect:/space"; // 一覧画面
        }

        // 全時間帯データ取得
        List<SpaceTime> spaceTimeList = spaceTimeRep.findAll();

        // 各時間帯について空席数を計算してセット
        for (SpaceTime time : spaceTimeList) {

            // ★ 新ロジック：空席数をサービスで算出
            int availableSeats = availableSeatService.getAvailableSeats(
                    spaceId,
                    time.getSpaceTimesId()
            );

            time.setSeatCount(availableSeats);      // 空席数
            time.setHasVacancy(availableSeats > 0); // 空席あり判定
        }

        // 選択スペースを View 用にリスト化（既存ロジック維持）
        model.addAttribute("selectItems", Collections.singletonList(selectedSpace));
        model.addAttribute("spaceTimeList", spaceTimeList);

        return "reservation"; // 予約画面
    }


    /**
     * 予約完了処理
     */
    @Transactional
    @PostMapping("/reserve/complete")
    public String reserveComplete(
            @RequestParam("spaceId") int spaceId,
            @RequestParam("spaceTimesId") int spaceTimesId,
            HttpSession session,
            Model model) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        int userId = loginUser.getID();

        // 予約を保存
        Reservation reservation = new Reservation();
        reservation.setSpaceId(spaceId);
        reservation.setSpaceTimesId(spaceTimesId);
        reservation.setUserId(userId);
        reservationRep.save(reservation);

        // 残席更新
        Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);

        if (seat == null) {
            model.addAttribute("message", "座席情報が存在しません。");
            return "reserve_comp";
        }

        if (seat.getSeatCount() <= 0) {
            model.addAttribute("message", "満席のため予約できませんでした。");
            return "reserve_comp";
        }

        seat.setSeatCount(seat.getSeatCount() - 1);
        seatRep.save(seat);

        model.addAttribute("message", "予約が完了しました。");
        return "reserve_comp";
    }
}
