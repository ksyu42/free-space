package com.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.entity.Admin;
import com.entity.Login;
import com.entity.Reservation;
import com.entity.Space;
import com.entity.SpaceTime;
import com.repository.AdminRepository;
import com.repository.LoginRepository;
import com.repository.ReservationRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRep;
    @Autowired
    private ReservationRepository reservationRep;
    @Autowired
    private SpaceRepository spaceRep;
    @Autowired
    private SpaceTimeRepository spaceTimeRep;
    @Autowired
    private LoginRepository loginRep;

    /* ===============================
     * ◆ 管理者ログイン画面
     * =============================== */
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    /* ===============================
     * ◆ 管理者ログイン処理
     * =============================== */
    @PostMapping("/check")
    public String check(String adminNumber, String password,
                        HttpSession session, Model model) {

        Admin admin = adminRep.findByAdminNumberAndPassword(adminNumber, password);
        if (admin == null) {
            model.addAttribute("error", "管理番号またはパスワードが違います");
            return "admin/login";
        }

        session.setAttribute("adminUser", admin);
        return "redirect:/admin/dashboard";
    }

    /* ===============================
     * ◆ ダッシュボード
     * =============================== */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }
        return "admin/dashboard";
    }

    /* ===============================
     * ◆ 予約一覧（管理者）
     * =============================== */
    @GetMapping("/reservations")
    public String reservations(Model model, HttpSession session) {

        Admin admin = (Admin) session.getAttribute("adminUser");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        List<Space> mySpaces = spaceRep.findByAdminId((long) admin.getID());
        List<Integer> spaceIds = new ArrayList<>();
        for (Space s : mySpaces) {
            spaceIds.add(s.getId().intValue());
        }

        List<Reservation> result = new ArrayList<>();
        Map<Integer, SpaceTime> timeMap = new HashMap<>();
        for (SpaceTime t : spaceTimeRep.findAll()) {
            timeMap.put(t.getSpaceTimesId(), t);
        }

        Map<Integer, Login> userMap = new HashMap<>();
        for (Login u : loginRep.findAll()) {
            userMap.put(u.getID(), u);
        }

        for (Reservation r : reservationRep.findAll()) {
            if (!spaceIds.contains(r.getSpaceId())) continue;

            Space space = mySpaces.stream()
                    .filter(s -> s.getId().intValue() == r.getSpaceId())
                    .findFirst().orElse(null);

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
     * ◆ 予約承認
     * =============================== */
    @PostMapping("/reservations/{id}/approve")
    public String approve(@PathVariable int id,
                           RedirectAttributes ra) {

        Reservation r = reservationRep.findById(id).orElse(null);
        if (r == null) return "redirect:/admin/reservations";

        r.setStatus("APPROVED");
        reservationRep.save(r);

        return "redirect:/admin/reservations";
    }

    /* ===============================
     * ◆ 予約却下
     * =============================== */
    @PostMapping("/reservations/{id}/reject")
    public String reject(@PathVariable int id) {

        Reservation r = reservationRep.findById(id).orElse(null);
        if (r == null) return "redirect:/admin/reservations";

        r.setStatus("REJECTED");
        reservationRep.save(r);

        return "redirect:/admin/reservations";
    }

    /* ===============================
     * ◆ ログアウト
     * =============================== */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
