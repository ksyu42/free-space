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
import com.form.TimeSeatDto;
import com.repository.AdminRepository;
import com.repository.SeatRepository;
import com.repository.SpaceRepository;
import com.repository.SpaceTimeRepository;

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

    /**
     * 管理者ログインフォーム表示
     * 遷移元：「管理者はこちら」リンク
     */
    @GetMapping("/login")
    public String adminLoginForm() {
        return "admin_login";  // templates/admin_login.html
    }

    /**
     * 管理者ログイン処理
     * 遷移元：ログインフォーム
     * 成功時：ダッシュボードにリダイレクト
     */
    @PostMapping("/check")
    public String adminCheck(@RequestParam("adminNumber") String adminNumber,
                             @RequestParam("password") String password,
                             HttpSession session,
                             Model model) {

        // 管理者認証
        if (!adminRep.existsByAdminNumberAndPassword(adminNumber, password)) {
            model.addAttribute("error", "※ 管理番号またはパスワードが正しくありません。");
            return "admin_login";
        }

        // セッションに管理者情報を保存
        Admin admin = adminRep.findByAdminNumberAndPassword(adminNumber, password);
        session.setAttribute("adminUser", admin);

        // 管理者ダッシュボードに遷移（テンプレート直下に配置している場合）
        return "redirect:/admin/dashboard";
    }

    /**
     * 管理者ログアウト処理
     * 遷移元：ログアウトボタン（今後の実装）
     */
    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();  // セッション破棄
        return "redirect:/admin/login";
    }

    /**
     * 管理者用ダッシュボード画面表示
     * 遷移元：ログイン成功後のリダイレクト
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
    	
        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminUser", adminUser);
        return "dashboard";  // templates/dashboard.html に遷移
    }
    
    @GetMapping("/space")
    public String adminSpaceList(Model model, HttpSession session) {
        // ログインしていなければログイン画面へ
        Admin adminUser = (Admin) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        // データを取得
        List<Space> spaceList = spaceRep.findAll();
        List<SpaceTime> timeList = spaceTimeRep.findAll();
        List<Seat> seatList = seatRep.findAll();

        // 表示用のデータを作る
        Map<Integer, List<TimeSeatDto>> spaceTimeMap = new HashMap<>();

        // スペースごとに処理
        for (Space space : spaceList) {
            List<TimeSeatDto> timeSeatList = new ArrayList<>();

            // 各時間帯ごとに座席数を調べる
            for (SpaceTime time : timeList) {
                int totalSeats = 0;

                // 該当する座席を1つずつチェック
                for (Seat seat : seatList) {
                	boolean sameSpace = seat.getSpaceId() == space.getId().intValue();
                	boolean sameTime  = seat.getSpaceTimesId() == time.getSpaceTimesId();
                    if (sameSpace && sameTime) {
                        totalSeats += seat.getSeatCount();
                    }
                }

                // 時間帯と座席数の組み合わせをリストに追加
                TimeSeatDto dto = new TimeSeatDto(time.getTime(), totalSeats);
                timeSeatList.add(dto);
            }
            // スペースIDごとのマップに追加
            spaceTimeMap.put(space.getId().intValue(), timeSeatList);

        }

        // 画面へデータを渡す
        model.addAttribute("spaceList", spaceList);
        model.addAttribute("spaceTimeMap", spaceTimeMap);

        return "admin_space";
    }
    
    /*
     * 管理者権限：座席の増減処理
     * updateSeat.js→DB処理
     */
    //@ResponseBody:メソッドの戻り値を「テンプレート名」としてではなく、HTTPのボディ内容としてそのまま返す指示
    @PostMapping("/updateSeat")
    @ResponseBody
    public ResponseEntity<String> updateSeat(@RequestBody Map<String, Object> body) {
        int spaceId = (int) body.get("spaceId");
        int timeIndex = (int) body.get("timeIndex"); //時間帯内のリストのインデックス番号
        int diff = (int) body.get("diff"); //増減させる数値

        // 時間帯リストから該当ID取得
        List<SpaceTime> timeList = spaceTimeRep.findAll();
        // timeIndexがリストの範囲外である場合→エラー返却
        if (timeIndex < 0 || timeIndex >= timeList.size()) {
            return ResponseEntity.badRequest().body("時間帯が見つかりません");
        }
        // 時間帯のリストから、指定インデックスのIDを取得
        int timeId = timeList.get(timeIndex).getSpaceTimesId();

        // 既に該当する spaceId と timeId のレコードがあるかチェック
        Seat seat = seatRep.findBySpaceIdAndSpaceTimesId(spaceId, timeId);

        // null チェック
        if (seat != null) {
        	// 現在の座席数に diff を加算
            int newCount = seat.getSeatCount() + diff;
            // マイナスにならないように 0 以下は切り捨て
            if (newCount < 0) newCount = 0;
            seat.setSeatCount(newCount);
        } else { // 存在しない場合　各値を設定(diffがマイナスでも0を設定)
            seat = new Seat();
            seat.setSpaceId(spaceId);
            seat.setSpaceTimesId(timeId);
            seat.setSeatCount(Math.max(diff, 0));
        }

        seatRep.save(seat);
        //ResponseEntity.ok():HTTPステータスやボディなどのレスポンスを細かく制御できるクラス
        return ResponseEntity.ok("更新完了");
    }

    /*
     * 新規スペースの作成
     * スペース一覧(admin_space.html)_スペース作成ボタン
     * 
     * 
     */
    @GetMapping("/space/new")
    public String newSpaceForm() {
        return "space_form";
    }

    // 保存処理
    @PostMapping("/space/save")
    public String saveSpace(@RequestParam String name,
                            @RequestParam String location) {
        Space space = new Space();
        space.setName(name);
        space.setLocation(location);
        spaceRep.save(space);
        return "redirect:/space"; // 一覧画面に戻る
    }

    /**
     * スペース削除処理
     * 該当スペースの座席（Seat）と時間帯（SpaceTime）も合わせて削除
     * 遷移元：admin_space.html のスペース削除フォーム
     */
    @PostMapping("/space/delete/{id}")
    public String deleteSpace(@PathVariable int id) {
        // 座席だけ削除すればよい
        seatRep.deleteBySpaceId(id);

        // SpaceTimeは共通時間帯として残す
        spaceRep.deleteById(Long.valueOf(id));

        return "redirect:/admin/space";
    }

}
