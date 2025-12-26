package com.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.entity.Admin;
import com.entity.Reservation;
import com.entity.Space;
import com.entity.Seat;
import com.repository.ReservationRepository;
import com.repository.ReviewRepository;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;

/**
 * 管理者側：スペース管理（一覧・登録・編集・削除）
 */
@Controller
@RequestMapping("/admin/spaces")
public class SpaceController {

    @Autowired
    private SpaceRepository spaceRep;

    @Autowired
    private SpaceTimeRepository timeRep;

    @Autowired
    private SeatRepository seatRep;

    @Autowired
    private ReservationRepository reservationRep;

    @Autowired
    private ReviewRepository reviewRep;

    /* ===============================
     * ◆ スペース一覧
     * =============================== */
    @GetMapping("")
    public String list(HttpSession session, Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        List<Space> spaces = spaceRep.findByAdminId((long) adminUser.getID());
        model.addAttribute("spaces", spaces);

        return "admin/space_list";
    }

    /* ===============================
     * ◆ 新規作成フォーム
     * =============================== */
    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("space", new Space());
        model.addAttribute("timeList", timeRep.findAll());

        return "admin/space_form";
    }

    /* ===============================
     * ◆ 編集フォーム
     * =============================== */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Space space = spaceRep.findById(id).orElse(null);
        if (space == null || !space.getAdminId().equals((long) adminUser.getID())) {
            return "redirect:/admin/spaces";
        }

        model.addAttribute("space", space);
        model.addAttribute("timeList", timeRep.findAll());

        return "admin/space_form";
    }

    /* ===============================
     * ◆ 登録/更新
     * =============================== */
    @PostMapping("/save")
    @Transactional
    public String save(
            @ModelAttribute Space space,
            @RequestParam(value = "seatCounts", required = false) List<Integer> seatCounts,
            @RequestParam(value = "timeIds", required = false) List<Integer> timeIds,
            HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        // admin_id を必ず自分にする（画面改ざん防止）
        space.setAdminId((long) adminUser.getID());

        Space saved = spaceRep.save(space);

        // 座席数登録（時間帯ごと）
        if (seatCounts != null && timeIds != null && seatCounts.size() == timeIds.size()) {
            for (int i = 0; i < timeIds.size(); i++) {

                Integer timeId = timeIds.get(i);
                Integer seatCount = seatCounts.get(i);

                // seat_count 未入力は0扱い
                if (seatCount == null) seatCount = 0;

                Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(saved.getId().intValue(), timeId);
                if (seat == null) {
                    seat = new Seat();
                    seat.setSpaceId(saved.getId().intValue());
                    seat.setSpaceTimesId(timeId);
                }
                seat.setSeatCount(seatCount);
                seatRep.save(seat);
            }
        }

        return "redirect:/admin/spaces";
    }

    /* ===============================
     * ◆ スペース削除
     * =============================== */
    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteSpace(@PathVariable Long id, HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Space space = spaceRep.findById(id).orElse(null);
        if (space == null || !space.getAdminId().equals((long) adminUser.getID())) {
            return "redirect:/admin/spaces";
        }

        // 予約が残っている場合の扱い：今回は「関連をまとめて削除」する
        // ※運用として「過去データを残したい」場合は論理削除にするなど検討する
        List<Reservation> reservations = reservationRep.findBySpaceId(space.getId().intValue());
        if (reservations != null && !reservations.isEmpty()) {
            // レビューも削除（reviews.space_id）
            reviewRep.deleteBySpaceId(space.getId().intValue());
            // 予約削除
            reservationRep.deleteBySpaceId(space.getId().intValue());
        }

        // seat（時間帯×座席）削除
        seatRep.deleteBySpaceId(space.getId().intValue());

        // space 削除
        spaceRep.deleteById(id);

        return "redirect:/admin/spaces";
    }
}
