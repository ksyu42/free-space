package com.controller;

import java.time.LocalDate;
import java.util.ArrayList;
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

/**
 * ユーザー側：スペース一覧～予約完了までの画面制御
 */
@Controller
public class SpaceListController {

    @Autowired
    private SpaceRepository spaceRep;

    @Autowired
    private SpaceTimeRepository timeRep;

    @Autowired
    private ReservationRepository reservationRep;

    @Autowired
    private SeatRepository seatRep;

    @Autowired
    private AvailableSeatService availableSeatService;

    /**
     * スペース一覧表示
     */
    @RequestMapping("/space")
    public String spaceList(HttpSession session, Model model) {

        // ログインチェック（ユーザー or 管理者）
        Login loginUser = (Login) session.getAttribute("loginUser");
        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (loginUser == null && adminUser == null) {
            return "redirect:/login";
        }

        // 全スペース取得
        List<Space> spaceList = spaceRep.findAll();

        // 残席があるスペースかどうか判定
        List<SpaceTime> timeList = timeRep.findAll();
        for (Space space : spaceList) {

            boolean hasVacancy = false;

            for (SpaceTime t : timeList) {
                int vacancy = availableSeatService.getAvailableSeats(space.getId().intValue(), t.getSpaceTimesId());
                if (vacancy > 0) {
                    hasVacancy = true;
                    break;
                }
            }

            // 画面表示用（Spaceに transient で持っている）
            space.setHasVacancy(hasVacancy);
        }

        model.addAttribute("spaceList", spaceList);
        return "space";
    }

    /**
     * 予約画面へ遷移（スペース選択後）
     * 
     * ・時間帯リストを表示
     * ・残席が0の時間帯は選択不可（表示だけは出す）
     */
    @PostMapping("/conf")
    public String reservationPage(
            @RequestParam("spaceId") Long spaceId,
            HttpSession session,
            Model model) {

        // ログインチェック（ユーザーのみ）
        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Space space = spaceRep.findById(spaceId).orElse(null);
        if (space == null) {
            return "redirect:/space";
        }

        List<SpaceTime> timeList = timeRep.findAll();

        // 時間帯ごとの残席を計算して、画面で選択制御に使う
        for (SpaceTime t : timeList) {
            int vacancy = availableSeatService.getAvailableSeats(space.getId().intValue(), t.getSpaceTimesId());
            t.setHasVacancy(vacancy > 0);
        }

        model.addAttribute("item", space);
        model.addAttribute("timeList", timeList);

        // 予約日（初期値：今日）
        model.addAttribute("defaultDate", LocalDate.now().toString());

        return "reservation";
    }

    /**
     * 予約確定（PENDING登録 + seat_count 減算）
     * 
     * 重要：
     * ・空席確認は画面側だけでなく、確定時にも再チェックする
     * ・同時予約で seat_count がマイナスにならないよう、対象行をロックして処理する
     */
    @PostMapping("/reserve/complete")
    @Transactional
    public String reserveComplete(
            @RequestParam("spaceId") int spaceId,
            @RequestParam("spaceTimesId") int spaceTimesId,
            @RequestParam("reservationDay") String reservationDay,
            HttpSession session,
            Model model) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 日付パース（未入力時は今日）
        LocalDate day;
        try {
            day = (reservationDay == null || reservationDay.isBlank()) ? LocalDate.now() : LocalDate.parse(reservationDay);
        } catch (Exception e) {
            day = LocalDate.now();
        }

        // 対象 seat 行をロックして取得（同時予約対策）
        Seat seat = seatRep.findBySpaceIdAndSpaceTimesIdForUpdate(spaceId, spaceTimesId);

        List<String> errorlist = new ArrayList<>();

        // seat が無い、または残席なし
        if (seat == null || seat.getSeatCount() == null || seat.getSeatCount() <= 0) {
            errorlist.add("※選択された時間帯は満席のため予約できません。");
        }

        // 過去日は予約不可（簡易チェック）
        if (day.isBefore(LocalDate.now())) {
            errorlist.add("※過去の日付は予約できません。");
        }

        if (!errorlist.isEmpty()) {
            // 予約画面に戻すため、再度必要情報をセット
            Space space = spaceRep.findById((long) spaceId).orElse(null);
            List<SpaceTime> timeList = timeRep.findAll();
            if (space != null) {
                for (SpaceTime t : timeList) {
                    int vacancy = availableSeatService.getAvailableSeats(space.getId().intValue(), t.getSpaceTimesId());
                    t.setHasVacancy(vacancy > 0);
                }
            }
            model.addAttribute("item", space);
            model.addAttribute("timeList", timeList);
            model.addAttribute("defaultDate", day.toString());
            model.addAttribute("errorlist", errorlist);
            return "reservation";
        }

        // 予約登録（PENDING）
        Reservation r = new Reservation();
        r.setUserId(loginUser.getID());
        r.setSpaceId(spaceId);
        r.setSpaceTimesId(spaceTimesId);
        r.setReservationDay(day);
        r.setStatus("PENDING");

        reservationRep.save(r);

        // 残席を1減らす（予約の仮押さえ）
        seat.setSeatCount(seat.getSeatCount() - 1);
        seatRep.save(seat);

        model.addAttribute("message", "予約が完了しました。");
        return "reserve_comp";
    }

    /* ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
     * ※管理者の承認/却下は AdminController 側で扱う
     * ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝ */

    /**
     * 互換のため残しているメソッド（過去のURLが残っていた場合用）
     * ※今後は /admin/reservations から操作する
     */
    @RequestMapping("/conf_dummy")
    public String confDummy() {
        return "redirect:/space";
    }
}
