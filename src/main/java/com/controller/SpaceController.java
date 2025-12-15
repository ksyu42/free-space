package com.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.entity.Admin;
import com.entity.Space;
import com.entity.SpaceTime;
import com.entity.Seat;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;
import com.repository.SeatRepository;

@Controller
@RequestMapping("/admin/space")
public class SpaceController {

    @Autowired
    private SpaceRepository spaceRep;

    @Autowired
    private SpaceTimeRepository spaceTimeRep;

    @Autowired
    private SeatRepository seatRep;

    /* ===============================
     * ◆ スペース新規作成画面
     * =============================== */
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("space", new Space());
        return "admin/space_form";
    }

    /* ===============================
     * ◆ スペース保存
     * =============================== */
    @PostMapping("/save")
    public String saveSpace(@ModelAttribute Space space, HttpSession session) {

        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        space.setAdminId((long) adminUser.getID());
        spaceRep.save(space);

        /* --- 座席初期化 --- */
        List<SpaceTime> timeList = spaceTimeRep.findAll();
        for (SpaceTime t : timeList) {
            Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(
                    space.getId().intValue(),
                    t.getSpaceTimesId()
            );

            if (seat == null) {
                seat = new Seat();
                seat.setSpaceId(space.getId().intValue());
                seat.setSpaceTimesId(t.getSpaceTimesId());
                seat.setSeatCount(0);
                seatRep.save(seat);
            }
        }

        return "redirect:/admin/spaces";
    }

    /* ===============================
     * ◆ スペース削除
     * =============================== */
    @PostMapping("/delete/{id}")
    public String deleteSpace(@PathVariable Long id) {
        spaceRep.deleteById(id);
        return "redirect:/admin/spaces";
    }
}
