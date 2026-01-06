package com.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
import com.entity.SpaceTime;
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

        /*
         * 【修正点】
         * 画面で「時間帯ごとの座席数」を一覧表示するため、
         * spaceList / timeList / seatMap を作って渡す
         */
        List<Space> spaces = spaceRep.findByAdminId((long) adminUser.getID());
        List<SpaceTime> timeList = timeRep.findAll();

        // key: "<spaceId>-<spaceTimesId>" / value: seatCount
        Map<String, Integer> seatMap = new HashMap<>();

        for (Space s : spaces) {
            for (SpaceTime t : timeList) {

                Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(s.getId().intValue(), t.getSpaceTimesId());
                int count = 0;
                if (seat != null) {
                    count = seat.getSeatCount();
                }

                seatMap.put(s.getId() + "-" + t.getSpaceTimesId(), count);
            }
        }

        // 既存で "spaces" を参照している画面があっても壊れないよう、両方入れる
        model.addAttribute("spaces", spaces);

        model.addAttribute("spaceList", spaces);
        model.addAttribute("timeList", timeList);
        model.addAttribute("seatMap", seatMap);

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

        // 編集時は他人のデータを更新できないようにする
        if (space.getId() != null && space.getId() > 0) {
            Space db = spaceRep.findById(space.getId()).orElse(null);
            if (db == null || !db.getAdminId().equals((long) adminUser.getID())) {
                return "redirect:/admin/spaces";
            }
        } else {
            // 新規時の id 受け取りが 0 の場合は null にして登録
            space.setId(null);
        }

        // 【要件対応】時間は1時間おき（分は00固定）にする
        space.setAvailableFrom(normalizeHour(space.getAvailableFrom()));
        space.setAvailableTo(normalizeHour(space.getAvailableTo()));

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
     * ◆ 削除
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

        // 予約・レビューが紐づいている場合は先に削除する
        List<Reservation> reservations = reservationRep.findBySpaceId(space.getId().intValue());
        if (reservations != null && !reservations.isEmpty()) {

            // レビュー削除
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

    /*
     * 入力された時間文字列を「HH:00」に丸める
     * 例）"09:30" → "09:00"
     *
     * ※HTML側も select で1時間おきにしているが、念のためサーバー側でも整形する
     */
    private String normalizeHour(String time) {

        if (time == null || time.isBlank()) {
            return time;
        }

        // "HH:mm" 想定（違う形式でも例外にならないようにする）
        try {
            String[] parts = time.split(":", 2);
            int hour = Integer.parseInt(parts[0]);
            if (hour < 0) hour = 0;
            if (hour > 23) hour = 23;
            return String.format("%02d:00", hour);
        } catch (Exception e) {
            return time;
        }
    }
}
