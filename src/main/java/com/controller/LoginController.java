package com.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entity.Login;
import com.entity.Reservation;
import com.entity.Seat;
import com.entity.Space;
import com.entity.SpaceTime;
import com.repository.LoginRepository;
import com.repository.ReservationRepository;
import com.repository.ReviewRepository;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;

/**
 * ログイン/会員登録/マイページ/プロフィールを扱うコントローラ
 */
@Controller
public class LoginController {

    @Autowired
    LoginRepository loginRep;

    @Autowired
    ReservationRepository reservationRep;

    @Autowired
    SpaceRepository spaceRep;

    @Autowired
    SpaceTimeRepository spaceTimeRep;

    @Autowired
    SeatRepository seatRep;

    @Autowired
    ReviewRepository reviewRep;

    /* ===============================
     * ◆ ログイン画面
     * =============================== */
    @GetMapping("/login")
    public String login(Model model) {

        /*
         * 【修正点】
         * login.html の th:object で参照するオブジェクトが Model に無いと、
         * TemplateProcessingException が発生するため、必ず詰める。
         * 
         * ※今回 login.html を th:object="${loginclass}" に合わせる
         */
        model.addAttribute("loginclass", new Login());

        return "login";
    }

    /*
     * ログイン処理
     */
    @PostMapping("/login")
    public String login_check(
            @Validated @ModelAttribute("loginclass") Login loginclass,
            BindingResult result,
            Model model,
            HttpSession session) {

        String val1 = loginclass.getMail();
        String val2 = loginclass.getPass();

        ArrayList<String> errorlist = new ArrayList<>();

        if (result.hasErrors()) {
            return "login";
        }

        // DB にユーザーが存在するか確認
        boolean existsBy = loginRep.existsByMailAndPass(val1, val2);
        if (!existsBy) {
            errorlist.add("※入力されたメールアドレスまたはパスワードが誤っているためログインできません");
            model.addAttribute("errorlist", errorlist);

            /*
             * 画面に戻す際も th:object 用のオブジェクトが必要になるため、
             * 念のためセットしておく（既に入っている場合でも上書きされるだけ）
             */
            model.addAttribute("loginclass", loginclass);

            return "login";
        }

        // セッションにログインユーザー保存
        Login loginUser = loginRep.findByMailAndPass(val1, val2);
        session.setAttribute("loginUser", loginUser);

        return "redirect:/space";
    }

    /* ===============================
     * ◆ ログアウト
     * =============================== */
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /* ===============================
     * ◆ 新規登録
     * =============================== */
    @RequestMapping("/signup")
    public String signup(Model model) {

        /*
         * 【修正点】
         * signup.html 側で th:object を使っている場合に備え、Model を詰めておく
         */
        model.addAttribute("loginclass", new Login());

        return "signup";
    }

    @PostMapping("/signup/add")
    public String signup_add(
            @Validated @ModelAttribute("loginclass") Login loginclass,
            BindingResult result,
            Model model) {

        ArrayList<String> errorlist = new ArrayList<>();

        if (result.hasErrors()) {
            return "signup";
        }

        // 重複チェック（メール）
        if (loginRep.existsByMail(loginclass.getMail())) {
            errorlist.add("※入力されたメールアドレスは既に登録されています。");
            model.addAttribute("errorlist", errorlist);
            model.addAttribute("loginclass", loginclass);
            return "signup";
        }

        // DB保存
        loginRep.save(loginclass);

        model.addAttribute("message", "登録が完了しました。ログインしてください。");
        model.addAttribute("loginclass", new Login());
        return "login";
    }

    /* ===============================
     * ◆ マイページ（予約一覧）
     * =============================== */
    @RequestMapping("/mypage")
    public String mypage(HttpSession session, Model model) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        List<Reservation> reservationList = reservationRep.findByUserId(loginUser.getID());

        // 予約一覧にスペース名・場所・時間を埋める
        for (Reservation r : reservationList) {
            Space space = spaceRep.findById((long) r.getSpaceId()).orElse(null);
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

            // レビュー済み判定
            r.setReviewed(reviewRep.findByReservationId(r.getId()).isPresent());
        }

        model.addAttribute("reservationList", reservationList);
        return "mypage";
    }

    /*
     * マイページ画面「プロフィール編集」ボタン押下時
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
    public String updateProfile(
            @RequestParam("name") String name,
            @RequestParam(value = "mail", required = false) String mail,
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

        // ↓ start パスワード欄の入力有無/入力ケース毎の処理 
        boolean isCurrentPassEntered = currentPass != null && !currentPass.isEmpty();
        boolean isNewPassEntered = newPass != null && !newPass.isEmpty();

        // どちらか片方だけ入力されている場合はエラー
        if (isCurrentPassEntered ^ isNewPassEntered) {
            model.addAttribute("error", "パスワードを変更する場合は、現在のパスワードと新しいパスワードを両方入力してください。");
            model.addAttribute("loginUser", dbUser);
            return "profile";
        }

        // パスワード変更ありの場合
        if (isCurrentPassEntered && isNewPassEntered) {

            // 現在のパスワード一致チェック
            if (!dbUser.getPass().equals(currentPass)) {
                model.addAttribute("error", "現在のパスワードが正しくありません。");
                model.addAttribute("loginUser", dbUser);
                return "profile";
            }

            // 新しいパスワードが同じならエラー
            if (currentPass.equals(newPass)) {
                model.addAttribute("error", "新しいパスワードが現在のパスワードと同じです。");
                model.addAttribute("loginUser", dbUser);
                return "profile";
            }

            dbUser.setPass(newPass);
        }
        // ↑ end

        // 名前の更新
        dbUser.setName(name);

        // メール更新（画面に項目がある場合だけ）
        if (mail != null && !mail.isBlank()) {

            // 変更している場合のみ重複チェック
            if (!mail.equals(dbUser.getMail()) && loginRep.existsByMail(mail)) {
                model.addAttribute("error", "このメールアドレスは既に登録されています。");
                model.addAttribute("loginUser", dbUser);
                return "profile";
            }

            dbUser.setMail(mail);
        }

        // DB保存とセッション更新
        loginRep.save(dbUser);
        session.setAttribute("loginUser", dbUser);

        model.addAttribute("success", "プロフィールを更新しました。");
        model.addAttribute("loginUser", dbUser);
        return "profile";
    }

    /* ===============================
     * ◆ 予約キャンセル（ユーザー）
     * =============================== */
    @PostMapping("/reservation/{id}/cancel")
    @Transactional
    public String cancelReservation(
            @PathVariable int id,
            HttpSession session,
            Model model) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Reservation reservation = reservationRep.findById(id).orElse(null);
        if (reservation == null || reservation.getUserId() != loginUser.getID()) {
            return "redirect:/mypage";
        }

        // 却下/キャンセル済みは何もしない
        if ("REJECTED".equals(reservation.getStatus()) || "CANCELLED".equals(reservation.getStatus())) {
            return "redirect:/mypage";
        }

        // 過去日の予約はキャンセル不可（簡易）
        if (reservation.getReservationDay() != null && reservation.getReservationDay().isBefore(LocalDate.now())) {
            return "redirect:/mypage";
        }

        // 仮押さえしている座席数を戻す（seat をロックして安全に更新）
        Seat seat = seatRep.findBySpaceIdAndSpaceTimesIdForUpdate(
                reservation.getSpaceId(),
                reservation.getSpaceTimesId()
        );
        if (seat != null && seat.getSeatCount() != null) {
            seat.setSeatCount(seat.getSeatCount() + 1);
            seatRep.save(seat);
        }

        reservation.setStatus("CANCELLED");
        reservationRep.save(reservation);

        return "redirect:/mypage";
    }

    /* ===============================
     * ◆ 利用済みにする（ユーザー）
     * =============================== */
    @PostMapping("/reservation/{id}/used")
    public String markUsed(
            @PathVariable int id,
            HttpSession session) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Reservation reservation = reservationRep.findById(id).orElse(null);
        if (reservation == null || reservation.getUserId() != loginUser.getID()) {
            return "redirect:/mypage";
        }

        // 承認済みのみ利用済みにできる
        if (!"APPROVED".equals(reservation.getStatus())) {
            return "redirect:/mypage";
        }

        // 予約日が今日以前なら利用済みOK（必要に応じて厳密化）
        if (reservation.getReservationDay() != null && reservation.getReservationDay().isAfter(LocalDate.now())) {
            return "redirect:/mypage";
        }

        reservation.setStatus("USED");
        reservationRep.save(reservation);

        return "redirect:/mypage";
    }
}
