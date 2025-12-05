package com.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.entity.Admin;
import com.entity.Space;
import com.service.SpaceService;

@Controller
@RequestMapping("/admin/spaces")
public class SpaceController {

    // スペース情報を扱うサービス
    @Autowired
    private SpaceService spaceService;

    /**
     * 管理者が登録したスペース一覧を表示する
     */
    @GetMapping
    public String listSpaces(Model model, HttpSession session) {

        // セッションから管理者情報を取得
        Admin adminUser = (Admin) session.getAttribute("adminUser");

        // 未ログインなら管理者ログイン画面へリダイレクト
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        // Admin の ID は int → Long 変換
        Long adminId = (long) adminUser.getID();

        // 管理者に紐づくスペース一覧取得
        List<Space> spaces = spaceService.getSpacesByAdmin(adminId);

        model.addAttribute("spaces", spaces);
        return "admin/space_list";
    }

    /**
     * スペース新規登録画面表示
     */
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("space", new Space());
        return "admin/space_form";
    }

    /**
     * スペース編集画面表示
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Space space = spaceService.getById(id);
        if (space == null) {
            // 指定IDのスペースが存在しない場合は一覧へ戻る
            return "redirect:/admin/spaces";
        }
        model.addAttribute("space", space);
        return "admin/space_form";
    }

    /**
     * スペース新規登録／更新処理
     */
    @PostMapping("/save")
    public String saveSpace(@ModelAttribute Space space, HttpSession session) {
        // セッションからログイン中の管理者情報を取得
        Admin adminUser = (Admin) session.getAttribute("adminUser");

        // ログインしていない場合は管理者ログイン画面へリダイレクト
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        // AdminエンティティのIDは int 型なので、Long に変換してセット
        Long adminId = (long) adminUser.getID();
        space.setAdminId(adminId);

        // スペース情報を保存
        spaceService.save(space);

        // 一覧画面へリダイレクト
        return "redirect:/admin/spaces";
    }

    /**
     * スペース削除処理
     */
    @GetMapping("/delete/{id}")
    public String deleteSpace(@PathVariable Long id) {
        spaceService.delete(id);
        return "redirect:/admin/spaces";
    }
}
