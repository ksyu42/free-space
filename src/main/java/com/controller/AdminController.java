package com.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.entity.Admin;
import com.entity.Seat;
import com.entity.Space;
import com.entity.SpaceTime;
import com.entity.Reservation;
import com.entity.Login;
import com.form.TimeSeatDto;
import com.repository.AdminRepository;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;
import com.repository.ReservationRepository;
import com.repository.LoginRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRep;
    @Autowired
    private SpaceRepository spaceRep;
    @Autowired
    private SeatRepository seatRep;
    @Autowired
    private SpaceTimeRepository spaceTimeRep;
    @Autowired
    private ReservationRepository reservationRep;
    @Autowired
    private LoginRepository loginRep;

    /* ===============================
     * ◆ 管理者ログイン画面
     * =============================== */
    @GetMapping("/login")
    public String adminLoginForm() {
        return "admin/login";
    }

    /* ===============================
     * ◆ 管理者ログイン処理
     * =============================== */
    @PostMapping("/check")
    public String adminCheck(
            @RequestParam("adminNumber") String adminNumber,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        if (!adminRep.existsByAdminNumberAndPassword(adminNumber, password)) {
            model.addAttribute("error", "※ 管理番号またはパスワードが正しくありません。");
            return "admin/login";
        }

        Admin admin = adminRep.findByAdminNumberAndPassword(adminNumber, password);
        session.setAttribute("adminUser", admin);

        return "redirect:/admin/dashboard";
    }

    /* ===============================
     * ◆ ログアウト
     * =============================== */
    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    /* ===============================
     * ◆ ダッシュボード
     * =============================== */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminUser", adminUser);
        return "admin/dashboard";
    }

    /* ===============================
     * ◆ 予約一覧表示
     * =============================== */
    @GetMapping("/reservations")
    public String adminReservations(Model model, HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Long adminId = (long) adminUser.getID();
        List<Space> mySpaces = spaceRep.findByAdminId(adminId);

        if (mySpaces.isEmpty()) {
            model.addAttribute("reservationList", new ArrayList<>());
            return "admin/admin_reservations";
        }

        // 管理者のスペースID一覧
        List<Integer> mySpaceIds = new ArrayList<>();
        for (Space s : mySpaces) {
            mySpaceIds.add(s.getId().intValue());
        }

        // ★ いったん全予約を取得
        List<Reservation> all = reservationRep.findAll();
        List<Reservation> result = new ArrayList<>();

        Map<Integer, SpaceTime> timeMap = new HashMap<>();
        for (SpaceTime t : spaceTimeRep.findAll()) {
            timeMap.put(t.getSpaceTimesId(), t);
        }

        Map<Integer, Login> userMap = new HashMap<>();
        for (Login u : loginRep.findAll()) {
            userMap.put(u.getID(), u);
        }

        for (Reservation r : all) {

            // 管理者のスペースかどうかをJava側で判定
            if (!mySpaceIds.contains(r.getSpaceId())) {
                continue;
            }

            Space space = mySpaces.stream()
                    .filter(s -> s.getId().intValue() == r.getSpaceId())
                    .findFirst()
                    .orElse(null);

            if (space != null) {
                r.setSpaceName(space.getName());
                r.setLocation(space.getLocation());
            }

            SpaceTime time = timeMap.get(r.getSpaceTimesId());
            if (time != null) {
                r.setTime(time.getTime());
            }

            Login user = userMap.get(r.getUserId());
            if (user != null) {
                r.setUserName(user.getName());
            }

            result.add(r);
        }

        model.addAttribute("reservationList", result);
        return "admin/admin_reservations";
    }



    /* ===============================
     * ◆ スペース一覧（座席数表示）
     * =============================== */
    @GetMapping("/space")
    public String adminSpaceList(Model model, HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) return "redirect:/admin/login";

        List<Space> spaceList = spaceRep.findAll();
        List<SpaceTime> timeList = spaceTimeRep.findAll();
        List<Seat> seatList = seatRep.findAll();

        Map<Integer, List<TimeSeatDto>> spaceTimeMap = new HashMap<>();

        for (Space space : spaceList) {
            List<TimeSeatDto> timeSeatList = new ArrayList<>();

            for (SpaceTime time : timeList) {
                int totalSeats = 0;

                for (Seat seat : seatList) {
                    if (seat.getSpaceId() == space.getId().intValue()
                            && seat.getSpaceTimesId() == time.getSpaceTimesId()) {
                        totalSeats += seat.getSeatCount();
                    }
                }

                timeSeatList.add(new TimeSeatDto(time.getTime(), totalSeats));
            }

            // ★ 修正点：Long → int に変換
            spaceTimeMap.put(space.getId().intValue(), timeSeatList);
        }

        model.addAttribute("spaceList", spaceList);
        model.addAttribute("spaceTimeMap", spaceTimeMap);

        return "admin/space_list";
    }

    /* ===============================
     * ◆ 座席数更新（AJAX）
     * =============================== */
    @PostMapping("/updateSeat")
    @ResponseBody
    public ResponseEntity<String> updateSeat(@RequestBody Map<String, Object> body) {

        int spaceId = (int) body.get("spaceId");
        int timeIndex = (int) body.get("timeIndex");
        int diff = (int) body.get("diff");

        List<SpaceTime> timeList = spaceTimeRep.findAll();
        if (timeIndex < 0 || timeIndex >= timeList.size()) {
            return ResponseEntity.badRequest().body("時間帯が見つかりません");
        }

        int timeId = timeList.get(timeIndex).getSpaceTimesId();
        Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(spaceId, timeId);

        if (seat != null) {
            int newCount = seat.getSeatCount() + diff;
            seat.setSeatCount(Math.max(newCount, 0));
        } else {
            seat = new Seat();
            seat.setSpaceId(spaceId);
            seat.setSpaceTimesId(timeId);
            seat.setSeatCount(Math.max(diff, 0));
        }

        seatRep.save(seat);
        return ResponseEntity.ok("更新完了");
    }

    /* ===============================
     * ◆ スペース作成
     * =============================== */
    @GetMapping("/space/new")
    public String newSpaceForm() {
        return "admin/space_form";
    }

    @PostMapping("/space/save")
    public String saveSpace(
            @RequestParam("name") String name,
            @RequestParam("location") String location,
            @RequestParam("availableFrom") String availableFrom,
            @RequestParam("availableTo") String availableTo,
            HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null || adminUser.getID() == 0) {
            return "redirect:/admin/login";
        }


        // --- ① Space を保存 ---
        Space space = new Space();
        space.setName(name);
        space.setLocation(location);
        space.setAvailableFrom(availableFrom);
        space.setAvailableTo(availableTo);
        space.setAdminId((long) adminUser.getID());

        spaceRep.save(space);   // ← Space の ID はここで確定

        // --- ② Seat 初期データ作成（既存チェック付き）---
        List<SpaceTime> timeList = spaceTimeRep.findAll();

        for (SpaceTime t : timeList) {

            // ★ すでに Seat が存在するか確認
            Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(
                    space.getId().intValue(),
                    t.getSpaceTimesId()
            );

            if (seat == null) {
                // 存在しない場合のみ INSERT
                seat = new Seat();
                seat.setSpaceId(space.getId().intValue());
                seat.setSpaceTimesId(t.getSpaceTimesId());
                seat.setSeatCount(0);
                seatRep.save(seat);
            }
        }

        return "redirect:/admin/space";
    }


    /* ===============================
     * ◆ スペース削除
     * =============================== */
    @PostMapping("/space/delete/{id}")
    public String deleteSpace(@PathVariable int id) {

        // スペースに紐づく座席データを削除
        seatRep.deleteBySpaceId(id);
        // スペース本体を削除
        spaceRep.deleteById((long) id);

        // スペース一覧画面へ戻す
        return "redirect:/admin/space";
    }
    
    /* ===============================
     * ◆ 予約承認
     * =============================== */
    @PostMapping("/reservations/{id}/approve")
    public String approveReservation(
            @PathVariable int id,
            HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Reservation reservation = reservationRep.findById(id).orElse(null);
        if (reservation == null) {
            return "redirect:/admin/reservations";
        }

        reservation.setStatus("APPROVED");
        reservationRep.save(reservation);

        return "redirect:/admin/reservations";
    }


    /* ===============================
     * ◆ 予約却下
     * =============================== */
    @PostMapping("/reservations/{id}/reject")
    public String rejectReservation(
            @PathVariable int id,
            HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Reservation reservation = reservationRep.findById(id).orElse(null);
        if (reservation == null) {
            return "redirect:/admin/reservations";
        }

        reservation.setStatus("REJECTED");
        reservationRep.save(reservation);

        return "redirect:/admin/reservations";
    }


}
