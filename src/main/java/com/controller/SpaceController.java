package com.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.entity.Admin;
import com.entity.Seat;
import com.entity.Space;
import com.entity.SpaceTime;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;

@Controller
@RequestMapping("/admin")
public class SpaceController {

    @Autowired
    private SpaceRepository spaceRep;
    @Autowired
    private SpaceTimeRepository timeRep;
    @Autowired
    private SeatRepository seatRep;

    /* ===============================
     * ◆ スペース管理一覧（座席数含む）
     * =============================== */
    @GetMapping("/spaces")
    public String spaces(Model model, HttpSession session) {

        Admin admin = (Admin) session.getAttribute("adminUser");
        if (admin == null) return "redirect:/admin/login";

        List<Space> spaceList = spaceRep.findByAdminId((long) admin.getID());
        List<SpaceTime> timeList = timeRep.findAll();

        Map<String, Integer> seatMap = new HashMap<>();
        for (Seat seat : seatRep.findAll()) {
            seatMap.put(seat.getSpaceId() + "-" + seat.getSpaceTimesId(),
                        seat.getSeatCount());
        }

        model.addAttribute("spaceList", spaceList);
        model.addAttribute("timeList", timeList);
        model.addAttribute("seatMap", seatMap);

        return "admin/admin_spaces";
    }

    /* ===============================
     * ◆ 座席数更新（AJAX）
     * =============================== */
    @PostMapping("/updateSeat")
    @ResponseBody
    public ResponseEntity<String> updateSeat(@RequestBody Map<String, Object> body) {

        int spaceId = (int) body.get("spaceId");
        int spaceTimesId = (int) body.get("spaceTimesId");
        int diff = (int) body.get("diff");

        Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);
        if (seat == null) {
            seat = new Seat();
            seat.setSpaceId(spaceId);
            seat.setSpaceTimesId(spaceTimesId);
            seat.setSeatCount(0);
        }

        seat.setSeatCount(Math.max(seat.getSeatCount() + diff, 0));
        seatRep.save(seat);

        return ResponseEntity.ok("OK");
    }
}
