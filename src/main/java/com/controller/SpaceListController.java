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
    private SpaceTimeRepository timeRep;
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
        if (loginUser == null && adminUser == null) {
            return "redirect:/login";
        }

        // 全スペース取得
        List<Space> spaceList = spaceRep.findAll();

        // 残席があるスペースかどうかだけ判定（席の数はサービスで計算）
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
            space.setHasVacancy(hasVacancy);
        }

        model.addAttribute("spaceList", spaceList);
        return "space";
    }

    /**
     * 予約確認画面
     */
    @RequestMapping("/conf")
    public String reserve(Model model, @RequestParam("spaceId") Integer spaceId, HttpSession session) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Space selectedSpace = spaceRep.findById(Long.valueOf(spaceId)).orElse(null);
        if (selectedSpace == null) {
            return "redirect:/space";
        }

        List<SpaceTime> timeList = timeRep.findAll();

        // 残席数をサービスから計算してセット
        for (SpaceTime t : timeList) {
            int available = availableSeatService.getAvailableSeats(spaceId, t.getSpaceTimesId());
            t.setSeatCount(available);
            t.setHasVacancy(available > 0);
        }

        model.addAttribute("selectItems", Collections.singletonList(selectedSpace));
        model.addAttribute("spaceTimeList", timeList);

        return "reservation";
    }

    /**
     * 予約完了処理
     */
    @Transactional
    @PostMapping("/reserve/complete")
    public String reserveComplete(
            @RequestParam("spaceId") int spaceId,
            @RequestParam("spaceTimesId") int timeId,
            HttpSession session,
            Model model) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 残席数チェック
        int available = availableSeatService.getAvailableSeats(spaceId, timeId);
        if (available <= 0) {
            model.addAttribute("message", "満席のため予約できませんでした。");
            return "reserve_comp";
        }

        // 予約登録
        Reservation r = new Reservation();
        r.setSpaceId(spaceId);
        r.setSpaceTimesId(timeId);
        r.setUserId(loginUser.getID());
        reservationRep.save(r);

        model.addAttribute("message", "予約が完了しました。");
        return "reserve_comp";
    }
}
