package com.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import javax.transaction.Transactional;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.entity.Admin;
import com.entity.Seat;
import com.entity.Space;
import com.entity.SpaceTime;
import com.form.TimeSeatDto;
import com.repository.AdminRepository;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;
import com.entity.Reservation;
import com.entity.Login;
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
    private SpaceTimeRepository timeRep;

    @Autowired
    private SeatRepository seatRep;

    @Autowired
    private ReservationRepository reservationRep;

    @Autowired
    private LoginRepository loginRep;

    @GetMapping("/login")
    public String adminLogin() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLoginCheck(
            @RequestParam("adminNumber") String adminNumber,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        Admin admin = adminRep.findByAdminNumber(adminNumber);
        if (admin == null || !admin.getPassword().equals(password)) {
            model.addAttribute("error", "管理番号またはパスワードが違います");
            return "admin/login";
        }

        session.setAttribute("adminUser", admin);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {

        session.removeAttribute("adminUser");
        return "redirect:/admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminUser", adminUser);
        return "admin/dashboard";
    }

    @GetMapping("/reservations")
    public String reservationList(
            @RequestParam(value = "status", required = false) String status,
            HttpSession session,
            Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        // フィルタ未指定は ALL 扱い
        if (status == null || status.isBlank()) {
            status = "ALL";
        }

        // 自分のスペースID一覧
        List<Space> mySpaces = spaceRep.findByAdminId((long) adminUser.getID());
        List<Integer> mySpaceIds = new ArrayList<>();
        for (Space s : mySpaces) {
            mySpaceIds.add(s.getId().intValue());
        }

        // 予約一覧（自分のスペース分）
        List<Reservation> allReservations;
        if (mySpaceIds.isEmpty()) {
            allReservations = new ArrayList<>();
        } else {
            allReservations = reservationRep.findBySpaceIdIn(mySpaceIds);
        }

        // 表示用にスペース名/場所/時間/ユーザー名を埋める
        for (Reservation r : allReservations) {

            Space space = spaceRep.findById((long) r.getSpaceId()).orElse(null);
            if (space != null) {
                r.setSpaceName(space.getName());
                r.setLocation(space.getLocation());
            }

            SpaceTime time = timeRep.findById(r.getSpaceTimesId()).orElse(null);
            if (time != null) {
                r.setTime(time.getTime());
            }

            Login user = loginRep.findById(r.getUserId()).orElse(null);
            if (user != null) {
                r.setUserName(user.getName());
            }
        }

        /*
         * 【ブラッシュアップ】
         * 状態別の件数を出して、画面で絞り込みできるようにする
         */
        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("ALL", allReservations.size());
        statusCount.put("PENDING", 0);
        statusCount.put("APPROVED", 0);
        statusCount.put("REJECTED", 0);
        statusCount.put("CANCELLED", 0);
        statusCount.put("USED", 0);

        for (Reservation r : allReservations) {
            String st = r.getStatus();
            if (st == null) continue;

            if (statusCount.containsKey(st)) {
                statusCount.put(st, statusCount.get(st) + 1);
            }
        }

        // フィルタ適用（ALL の場合はそのまま）
        List<Reservation> viewList = new ArrayList<>();
        if ("ALL".equals(status)) {
            viewList = allReservations;
        } else {
            for (Reservation r : allReservations) {
                if (status.equals(r.getStatus())) {
                    viewList.add(r);
                }
            }
        }

        model.addAttribute("reservationList", viewList);
        model.addAttribute("statusCount", statusCount);
        model.addAttribute("selectedStatus", status);

        return "admin/admin_reservations";
    }

    @PostMapping("/reservations/{id}/approve")
    @Transactional
    public String approveReservation(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes ra) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Reservation r = reservationRep.findById(id).orElse(null);
        if (r == null) {
            ra.addFlashAttribute("error", "対象の予約が見つかりません");
            return "redirect:/admin/reservations";
        }

        // 自分のスペースの予約のみ操作可能
        Space s = spaceRep.findById((long) r.getSpaceId()).orElse(null);
        if (s == null || !s.getAdminId().equals((long) adminUser.getID())) {
            ra.addFlashAttribute("error", "権限がありません");
            return "redirect:/admin/reservations";
        }

        r.setStatus("APPROVED");
        reservationRep.save(r);

        ra.addFlashAttribute("message", "予約を承認しました");
        return "redirect:/admin/reservations";
    }

    @PostMapping("/reservations/{id}/reject")
    @Transactional
    public String rejectReservation(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes ra) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Reservation r = reservationRep.findById(id).orElse(null);
        if (r == null) {
            ra.addFlashAttribute("error", "対象の予約が見つかりません");
            return "redirect:/admin/reservations";
        }

        // 自分のスペースの予約のみ操作可能
        Space s = spaceRep.findById((long) r.getSpaceId()).orElse(null);
        if (s == null || !s.getAdminId().equals((long) adminUser.getID())) {
            ra.addFlashAttribute("error", "権限がありません");
            return "redirect:/admin/reservations";
        }

        r.setStatus("REJECTED");
        reservationRep.save(r);

        ra.addFlashAttribute("message", "予約を却下しました");
        return "redirect:/admin/reservations";
    }

    @PostMapping("/spaces/{spaceId}/seats")
    public ResponseEntity<?> updateSeats(
            @PathVariable int spaceId,
            @RequestBody List<TimeSeatDto> seatDtoList,
            HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Space space = spaceRep.findById((long) spaceId).orElse(null);
        if (space == null || !space.getAdminId().equals((long) adminUser.getID())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        for (TimeSeatDto dto : seatDtoList) {

            int spaceTimesId = dto.getSpaceTimesId();

            Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);
            if (seat == null) {
                seat = new Seat();
                seat.setSpaceId(spaceId);
                seat.setSpaceTimesId(spaceTimesId);
            }
            seat.setSeatCount(dto.getSeatCount());
            seatRep.save(seat);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("result", "ok");
        return ResponseEntity.ok(res);
    }
}
