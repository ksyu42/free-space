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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.entity.Admin;
import com.entity.Login;
import com.entity.Reservation;
import com.entity.Seat;
import com.entity.Space;
import com.entity.SpaceTime;
import com.form.TimeSeatDto;
import com.repository.AdminRepository;
import com.repository.LoginRepository;
import com.repository.ReservationRepository;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;

/*
 * 管理者画面用コントローラ
 */
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

    /* ===============================
     * ◆ 管理者ログイン画面
     * =============================== */
    @GetMapping("/login")
    public String adminLogin() {
        return "admin/login";
    }

    /* ===============================
     * ◆ 管理者ログイン処理
     * =============================== */
    @PostMapping("/login")
    public String adminLoginCheck(
            @RequestParam("adminNumber") String adminNumber,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        Admin adminUser = adminRep.findByAdminNumberAndPassword(adminNumber, password);
        if (adminUser == null) {
            model.addAttribute("error", "管理番号またはパスワードが違います。");
            return "admin/login";
        }

        session.setAttribute("adminUser", adminUser);
        return "redirect:/admin/dashboard";
    }

    /* ===============================
     * ◆ 管理者ログアウト
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
    public String dashboard(HttpSession session, Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        List<Space> mySpaces = spaceRep.findByAdminId((long) adminUser.getID());
        model.addAttribute("mySpaces", mySpaces);

        return "admin/dashboard";
    }

    /* ===============================
     * ◆ 予約一覧（管理者）
     * =============================== */
    @GetMapping("/reservations")
    public String reservationList(HttpSession session, Model model) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        // 自分のスペースID一覧
        List<Space> mySpaces = spaceRep.findByAdminId((long) adminUser.getID());
        List<Integer> mySpaceIds = new ArrayList<>();
        for (Space s : mySpaces) {
            mySpaceIds.add(s.getId().intValue());
        }

        // 予約一覧（自分のスペース分）
        List<Reservation> reservationList;
        if (mySpaceIds.isEmpty()) {
            reservationList = new ArrayList<>();
        } else {
            reservationList = reservationRep.findBySpaceIdIn(mySpaceIds);
        }

        // 表示用にスペース名/場所/時間/ユーザー名を埋める
        for (Reservation r : reservationList) {

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

        model.addAttribute("reservationList", reservationList);
        return "admin/admin_reservations";
    }

    /* ===============================
     * ◆ 予約承認（ステータス更新）
     * =============================== */
    @PostMapping("/reservations/{id}/approve")
    public String approveReservation(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Reservation reservation = reservationRep.findById(id).orElse(null);
        if (reservation == null) {
            return "redirect:/admin/reservations";
        }

        // 自分のスペースの予約かどうかチェック
        Space space = spaceRep.findById((long) reservation.getSpaceId()).orElse(null);
        if (space == null || !space.getAdminId().equals((long) adminUser.getID())) {
            return "redirect:/admin/reservations";
        }

        // すでに却下・キャンセルなどの場合は承認しない
        if ("REJECTED".equals(reservation.getStatus()) || "CANCELLED".equals(reservation.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "この予約は承認できない状態です。");
            return "redirect:/admin/reservations";
        }

        reservation.setStatus("APPROVED");
        reservationRep.save(reservation);

        redirectAttributes.addFlashAttribute("successMessage", "予約を承認しました。");
        return "redirect:/admin/reservations";
    }

    /* ===============================
     * ◆ 予約却下（座席数を戻す）
     * =============================== */
    @PostMapping("/reservations/{id}/reject")
    @Transactional
    public String rejectReservation(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        Reservation reservation = reservationRep.findById(id).orElse(null);
        if (reservation == null) {
            return "redirect:/admin/reservations";
        }

        // 自分のスペースの予約かどうかチェック
        Space space = spaceRep.findById((long) reservation.getSpaceId()).orElse(null);
        if (space == null || !space.getAdminId().equals((long) adminUser.getID())) {
            return "redirect:/admin/reservations";
        }

        // すでに却下済みなら何もしない
        if ("REJECTED".equals(reservation.getStatus())) {
            return "redirect:/admin/reservations";
        }

        // 仮押さえした座席数を戻す（seat をロックして安全に更新）
        Seat seat = seatRep.findBySpaceIdAndSpaceTimesIdForUpdate(
                reservation.getSpaceId(),
                reservation.getSpaceTimesId()
        );
        if (seat != null && seat.getSeatCount() != null) {
            seat.setSeatCount(seat.getSeatCount() + 1);
            seatRep.save(seat);
        }

        reservation.setStatus("REJECTED");
        reservationRep.save(reservation);

        redirectAttributes.addFlashAttribute("successMessage", "予約を却下しました。");
        return "redirect:/admin/reservations";
    }

    /* ===============================
     * ◆ 時間帯×座席数（Ajax更新などに利用している場合のため残し）
     * =============================== */

    @ResponseBody
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

        /*
         * TimeSeatDto は「time」と「seatCount」しか持っておらず、
         * dto.getSpaceTimesId() は存在しない。
         * 
         * そのため、DTOの time(文字列) から space_times_id を特定して更新する。
         */
        List<SpaceTime> timeList = timeRep.findAll();

        // timeId -> seatCount を更新
        for (TimeSeatDto dto : seatDtoList) {

            int spaceTimesId = -1;

            // DTOの time と space_times の time を突合してIDを求める
            for (SpaceTime t : timeList) {
                if (t.getTime() != null && t.getTime().equals(dto.getTime())) {
                    spaceTimesId = t.getSpaceTimesId();
                    break;
                }
            }

            // 該当時間帯が見つからない場合はスキップ
            if (spaceTimesId == -1) {
                continue;
            }

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

    /* ===============================
     * ◆ 注意（重要）
     * ===============================
     * 以前はこのAdminControllerに「/admin/spaces」のGETを置いていたが、
     * SpaceController 側でも同じ「GET /admin/spaces」を持っているため、
     * URLが衝突して起動時に Ambiguous mapping エラーになる。
     * 
     * スペース管理の画面（一覧/新規/編集/削除）は SpaceController(/admin/spaces/**)に集約する。
     */
}
