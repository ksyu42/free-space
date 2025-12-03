package com.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.entity.Space;
import com.service.SpaceService;
import com.service.AdminService;

@Controller
@RequestMapping("/admin/spaces")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private AdminService adminService;

    @GetMapping
    public String listSpaces(Model model, Principal principal) {
        Long adminId = adminService.getAdminIdByName(principal.getName());
        List<Space> spaces = spaceService.getSpacesByAdmin(adminId);
        model.addAttribute("spaces", spaces);
        return "admin/space_list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("space", new Space());
        return "admin/space_form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Space space = spaceService.getById(id);
        if (space == null) {
            return "redirect:/admin/spaces";
        }
        model.addAttribute("space", space);
        return "admin/space_form";
    }

    @PostMapping("/save")
    public String saveSpace(@ModelAttribute Space space, Principal principal) {
        Long adminId = adminService.getAdminIdByName(principal.getName());
        space.setAdminId(adminId);
        spaceService.save(space);
        return "redirect:/admin/spaces";
    }

    @GetMapping("/delete/{id}")
    public String deleteSpace(@PathVariable Long id) {
        spaceService.delete(id);
        return "redirect:/admin/spaces";
    }
}
