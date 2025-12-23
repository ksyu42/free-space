package com.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entity.Admin;
import com.entity.Login;
import com.entity.Reservation;
import com.entity.Space;
import com.entity.SpaceTime;
import com.repository.ReservationRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;
import com.service.AvailableSeatService;

@Controller
@RequestMapping("/admin")
public class SpaceListController {

    @Autowired
    private SpaceRepository spaceRep;
    @Autowired
    private SpaceTimeRepository timeRep;
    @Autowired
    private ReservationRepository reservationRep;
    @Autowired
    private AvailableSeatService availableSeatService;

    /**
     * ◆ 管理者用 スペース一覧表示
     */
    @GetMapping("/space")
    public String disp(Model model, HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        // 管理者が作成したスペースのみ取得
        List<Space> spaceList =
                spaceRep.findByAdminId((long) adminUser.getID());

        List<SpaceTime> timeList = timeRep.findAll();

        // 空席があるかどうかだけ判定
        for (Space space : spaceList) {
            boolean hasVacancy = false;

            for (SpaceTime t : timeList) {
                int vacancy = availableSeatService.getAvailableSeats(
                        space.getId().intValue(),
                        t.getSpaceTimesId()
                );
                if (vacancy > 0) {
                    hasVacancy = true;
                    break;
                }
            }
            space.setHasVacancy(hasVacancy);
        }

        model.addAttribute("spaceList", spaceList);
        return "admin/space_list";
    }

    /**
     * ◆ 予約確認画面（利用者）
     */
    @RequestMapping("/conf")
    public String reserve(
            Model model,
            @RequestParam("spaceId") Integer spaceId,
            @RequestParam(value = "reservationDay", required = false)
            @DateTimeFormat(iso = ISO.DATE) LocalDate reservationDay,
            HttpSession session
    ) {
        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 未指定なら今日
        if (reservationDay == null) {
            reservationDay = LocalDate.now();
        }

        Space selectedSpace = spaceRep.findById(Long.valueOf(spaceId)).orElse(null);
        if (selectedSpace == null) {
            return "redirect:/space";
        }

        List<SpaceTime> timeList = timeRep.findAll();

        // 残席数をサービスから計算してセット（★日付を渡す）
        for (SpaceTime t : timeList) {
            int available = availableSeatService.getAvailableSeats(spaceId, t.getSpaceTimesId(), reservationDay);
            t.setSeatCount(available);
            t.setHasVacancy(available > 0);
        }

        model.addAttribute("selectItems", Collections.singletonList(selectedSpace));
        model.addAttribute("spaceTimeList", timeList);

        // ★ 予約日を画面に渡す
        model.addAttribute("reservationDay", reservationDay);

        return "reservation";
    }

    @Transactional
    @PostMapping("/reserve/complete")
    public String reserveComplete(
            @RequestParam("spaceId") int spaceId,
            @RequestParam("spaceTimesId") int timeId,
            @RequestParam("reservationDay")
            @DateTimeFormat(iso = ISO.DATE) LocalDate reservationDay,
            HttpSession session,
            Model model
    ) {
        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 念のため未指定防止（HTMLで必須にするがサーバ側でも守る）
        if (reservationDay == null) {
            model.addAttribute("message", "利用日が未選択のため予約できませんでした。");
            return "reserve_comp";
        }

        // 残席数チェック（★日付を渡す）
        int available = availableSeatService.getAvailableSeats(spaceId, timeId, reservationDay);
        if (available <= 0) {
            model.addAttribute("message", "満席のため予約できませんでした。");
            return "reserve_comp";
        }

        // 予約登録（申請中で登録）
        Reservation r = new Reservation();
        r.setSpaceId(spaceId);
        r.setSpaceTimesId(timeId);
        r.setUserId(loginUser.getID());
        r.setReservationDay(reservationDay); // ★ ここで利用日を保存
        r.setStatus("PENDING");
        reservationRep.save(r);

        model.addAttribute("message", "予約が完了しました。");
        return "reserve_comp";
    }
}
