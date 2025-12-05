
package com.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entity.Admin;
import com.entity.Login;
import com.entity.Reservation;
import com.entity.Space;
import com.entity.SpaceTime;
import com.form.LoginClass;
import com.repository.AdminRepository;
import com.repository.LoginRepository;
import com.repository.ReservationRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;


/**
 * login.html
 */
import javax.servlet.http.HttpSession; // ← 追加

@Controller
public class LoginController {

    @Autowired
    private LoginRepository loginRep;
    @Autowired
    private AdminRepository adminRep;
    @Autowired
    private ReservationRepository reservationRep;
    @Autowired
    private SpaceRepository spaceRep;
    @Autowired
    private SpaceTimeRepository spaceTimeRep;
    
    

    @RequestMapping("/login")
    public String disp(Model model,
                       @ModelAttribute("user") LoginClass loginclass) {
        return "login";
    }
    

    /*
     * ログインチェック/セッション保存処理
     * 遷移元：ログイン画面/「ログイン」ボタン押下時
     * 遷移先:スペース一覧(space.html)
     */
    @RequestMapping("/check")
    public String store(Model model,
                        @Validated @ModelAttribute("user") LoginClass loginclass,
                        BindingResult result,
                        HttpSession session) {

        String val1 = loginclass.getUserMail();
        String val2 = loginclass.getUserPass();

        ArrayList<String> errorlist = new ArrayList<>();

        if (result.hasErrors()) {
            return "login";
        }

        // DB にユーザーが存在するか確認
        boolean existsBy = loginRep.existsByMailAndPass(val1, val2);
        if (!existsBy) {
            errorlist.add("※入力されたメールアドレスまたはパスワードが誤っているためログインできません");
            model.addAttribute("errorlist", errorlist);
            return "login";
        }

        // ログインユーザー情報の取得（詳細が必要な場合）
        Login loginUser = loginRep.findByMailAndPass(val1, val2);
        session.setAttribute("loginUser", loginUser);  // ← セッションに保存

        return "redirect:/space";
    }
    
    /**
     * ログアウト/セッション破棄の処理
     * 　遷移元：「ログアウトボタン」ボタン
     * 　遷移先：ログイン画面(login.html)
     */
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // ← セッション破棄
        return "redirect:/login";
    }

    /**
     * マイページ画面の表示処理
     * 　遷移元：「マイページ」ボタン
     * 　遷移先：マイページ画面（mypage.html）
     */
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        
        // セッションからログインユーザー情報を取得
        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            // ログインしていない場合はログイン画面へリダイレクト
            return "redirect:/login";
        }

        // ログインユーザーのIDを取得
        int userId = loginUser.getID();

        // ユーザーに紐づく予約データを全件取得
        List<Reservation> reservationList = reservationRep.findByUserId(userId);

        // 各予約に対して、スペース情報と時間帯情報を付加する
        for (Reservation r : reservationList) {

            // スペース情報取得（スペース名・場所）
            Space space = spaceRep.findById(Long.valueOf(r.getSpaceId())).orElse(null);

            // 時間帯情報取得（時間）
            SpaceTime time = spaceTimeRep.findById(r.getSpaceTimesId()).orElse(null);

            // スペース情報が存在する場合
            if (space != null) {
                r.setSpaceName(space.getName());
                r.setLocation(space.getLocation());
            }

            // 時間帯情報が存在する場合
            if (time != null) {
                r.setTime(time.getTime());
            }
        }

        model.addAttribute("reservationList", reservationList);

        return "mypage";
    }
    
    /*
     * プロフィール画面の表示
     * 	画面遷移元：マイページ画面「プロフィール編集」ボタン押下時
     * 　画面遷移先：プロフィール画面(profile.html)
     */
    @RequestMapping("/profile")
    public String profile_info(HttpSession session, Model model) {
    	
    	Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
        	return "redirect:/login";
        }
        model.addAttribute("loginUser", loginUser);
    	
    	return "profile";
    }
    
    /*
     * プロフィール編集機能
     * 　画面遷移元：プロフィール画面(profile.html)
     * 　画面遷移先：プロフィール画面(profile.html)
     */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("currentPass") String currentPass,
                                @RequestParam("newPass") String newPass,
                                HttpSession session,
                                Model model) {

        // セッション確認
        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
        	return "redirect:/login";
        }

        // DBから最新ユーザー情報を取得
        Login dbUser = loginRep.findById(loginUser.getID()).orElse(null);
        if (dbUser == null) {
        	return "redirect:/login";
        }

//　　　　　↓　start パスワード欄の入力有無/入力ケース毎の処理 
        // パスワード変更の入力があるかどうかを判定
        boolean isCurrentPassEntered = !currentPass.isEmpty();
        boolean isNewPassEntered = !newPass.isEmpty();

        // パターン1　片方だけ入力されていたらエラー
        if ((isCurrentPassEntered && !isNewPassEntered) || (!isCurrentPassEntered && isNewPassEntered)) {
            model.addAttribute("error", "パスワードを変更するには両方の項目を入力してください。");
            model.addAttribute("loginUser", dbUser);
            return "profile";
        }

        // パターン2　両方入力された場合のみ、パスワードチェックと更新
        if (isCurrentPassEntered && isNewPassEntered) {
            if (!dbUser.getPass().equals(currentPass)) {
                model.addAttribute("error", "現在のパスワードが正しくありません。");
                model.addAttribute("loginUser", dbUser);
                return "profile";
            }
            dbUser.setPass(newPass);
        }
//　　　　　↑　end 

        // 名前の更新
        dbUser.setName(name);

        // DB保存とセッション更新
        loginRep.save(dbUser);
        session.setAttribute("loginUser", dbUser);

        model.addAttribute("success", "プロフィールを更新しました。");
        model.addAttribute("loginUser", dbUser);
        return "profile";
    }



}
