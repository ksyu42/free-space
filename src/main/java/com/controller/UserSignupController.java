package com.controller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.entity.FileDB;
import com.entity.Team;
import com.entity.User;
import com.entity.UserLanguage;
import com.entity.UserMiddle;
import com.form.UserSignupForm;
import com.repository.FileRepository;
import com.repository.TeamRepository;
import com.repository.UserLanguageRepository;
import com.repository.UserMiddleRepository;
import com.repository.UserRepository;


@Controller
@EnableScheduling
public class UserSignupController {
	
	
	//userテーブル用リポジトリ
	@Autowired private UserRepository userRep;
	//user_teamテーブル用のリポジトリ
	@Autowired private TeamRepository teamRep;
	//UserLanguageテーブル用リポジトリ
	@Autowired private UserLanguageRepository userlangRep;
	//UserMiddleテーブル用リポジトリ
	@Autowired private UserMiddleRepository usermdlRep;
	//filesテーブル用リポジトリ
	@Autowired private FileRepository fileRep;


   

	// アクセスURL定義(http://localhost:8080/userSignup)
	//機能		：画面開設
	//画面遷移元	：ユーザー一覧　userSignup.html
	//画面遷移先	；
	@RequestMapping("/userSignup")
	public String user(Model model, 
			@ModelAttribute("usersign")UserSignupForm usersignfrm,
			@RequestParam("id") int id) {
		
		model.addAttribute("id", usersignfrm.getId());	
		return "userSignup";
	}
	
	
	//機能		：新規登録、入力チェック、DB保存
	//画面遷移元	：新規登録 userSignup.httml
	//画面遷移先	：ユーザー一覧　user.html
	@RequestMapping("/userSign")
	@Transactional
	public String userSign(Model model, 
			@Validated
			@ModelAttribute("usersign")UserSignupForm usersignfrm,
			BindingResult result,
			@RequestParam("upload_file") MultipartFile multipartFile) throws IOException{
		
		//Formフィールドで入力値取得・左辺格納　
		Integer idVal = usersignfrm.getId();
		String nameVal = usersignfrm.getName();
		String mailVal = usersignfrm.getMail();
		String passwordVal = usersignfrm.getPassword();
		String password2Val = usersignfrm.getPassword2();
		//チェックボックス指定した値を取得/リスト型に格納
		List<Integer>programList = usersignfrm.getProgramList();
		//エラー格納リスト
		List<String>errorlist = new ArrayList<String>();	
//-------------------------- 入力チェック開始　---------------------------
		/*	バリデーション適用　バリデーション無の場合下記コメントアウト記載
		 * //氏名未入力の場合
			if (nameVal.equals("")) {
				errorlist.add("氏名を入力してください");
			}
			//メールアドレス未入力の場合
			if (mailVal.equals("")) {
				errorlist.add("メールアドレスを入力してください");
			}
		*/
		//パスワード未入力の場合
		if (passwordVal.equals("") && idVal == 0) {
			errorlist.add("パスワードを入力してください");
		}
		//確認用パスワード未入力の場合
		if (password2Val.equals("") && idVal == 0) {
			errorlist.add("確認用パスワードを入力してください");
		}
		if(!(passwordVal.equals(password2Val)) ) {
			errorlist.add("パスワードと確認用パスワードが一致していません");
		}
		//エラー1以上ある&バリデーションエラーがある場合新規登録画面遷移にてエラー表示
		// ||更新時氏名空欄　
		if(errorlist.size() > 0 || result.hasErrors()) {
			model.addAttribute("errorlist", errorlist);
			model.addAttribute("id", usersignfrm.getId());
			List<Team> teamList = teamRep.findAll();
			model.addAttribute("teamList", teamList); 	
			//DBlanguageデータ取得/チェックボックス表示に値設定
			List<UserLanguage> languageList = userlangRep.findAll();
			model.addAttribute("languageList", languageList);
			return "UserSignup";
		}
//------------------------ 入力チェック終了　---------------------------
//------------------------ DB突合チェック開始　-------------------------
		//メール入力値がDB既存レコードに存在可否　ある場合エラー表示
		boolean existsByMail = userRep.existsByMail(mailVal);
		//新規登録時のみメールアドレス突合チェック
		if(existsByMail == true && idVal == 0) {
			errorlist.add("入力されたメールアドレスは既に使用されています");
		}
		//エラー1以上ある場合新規登録画面遷移
		if(errorlist.size() > 0) {
			model.addAttribute("errorlist", errorlist);
			model.addAttribute("id", usersignfrm.getId());
			List<Team> teamList = teamRep.findAll();
			model.addAttribute("teamList", teamList); 	
			//DBlanguageデータ取得/チェックボックス表示に値設定
			List<UserLanguage> languageList = userlangRep.findAll();
			model.addAttribute("languageList", languageList);
			return "UserSignup";
		}
//--------------------------- DB突合チェック終了　------------------------
//------------------------------ DB保存開始　-------------------------
		//UserEntityのインスタンス生成
		User user = new User();
		//Entityのフィールドで値設定
		user.setId(usersignfrm.getId());
		user.setMail(mailVal);
		user.setName(nameVal);
		//ロジック修正前
//		//新規登録時　入力値パスワード設定
//		if(idVal == 0) {
//			user.setPassword(passwordVal);	
//		//更新時　入力値パスワード設定
//		}else if(!(passwordVal.equals(""))&&!(password2Val.equals(""))){
//			user.setPassword(passwordVal);
//		//更新時パスワード未入力の場合　既存DBパスワード設定
//		}else if(passwordVal.equals("") && password2Val.equals("")) {
//			user.setPassword(upPass);
//		}
		//とりあえず入力値をパスワードに設定
		user.setPassword(passwordVal);
		//パスワードとパスワード(確認用)が一致しているとき
		if (passwordVal.equals("") && password2Val.equals("")) {
			//UserEntity変数　DBデータ取得、格納
			User userById = userRep.findById(usersignfrm.getId()).orElse(null);
			//FormフィールドでDBチェック項目パスワード取得・左辺格納　 
			String upPass = userById.getPassword();
			user.setPassword(upPass);
		//else文なくても起動する
		} else{
			user.setPassword(passwordVal);
		} 
		//DB保存
		userRep.save(user);
		
		/* チェックボックス */
		//新規登録時処理
		//新規時idが0で特定できないため、入力したメール(重複しない設定)から登録したid取得する
		User signMail = userRep.findByMail(mailVal).orElse(null);
		if(usersignfrm.getId() == 0) {
			for(Integer program : programList) {
				UserMiddle mdlsign = new UserMiddle();
				mdlsign.setPersonId(signMail.getId());
				mdlsign.setLanguageId(program);	
				usermdlRep.save(mdlsign);
			}
		}
		//更新時処理
		if(usersignfrm.getId() > 0) {
			usermdlRep.deleteByPersonId(usersignfrm.getId());
			//チェックボックスの値格納したリストから値取り出し/DB保存
			for(Integer program : programList) {
				UserMiddle usermdl = new UserMiddle();
				usermdl.setPersonId(usersignfrm.getId());
				usermdl.setLanguageId(program);
				usermdlRep.save(usermdl);
			}
		}
		/* 画像アップロード */
		//画像ファイルをバイト型に変換/格納
		byte[] bytes = multipartFile.getBytes();
		FileDB fieldb = new FileDB();
		
		//新規登録時処理
		if(idVal == 0) {
			//値の設定(重複しないMailでuserのidを特定)
			fieldb.setPersonId(signMail.getId());
			fieldb.setData(bytes);
			fileRep.save(fieldb);
		//更新時処理
		}else {
			//画像ファイルがある場合(既存の画像表示)
			if(bytes.length > 0) {
				//更新時重複避けるために元レコードを削除
				fileRep.deleteByPersonId(idVal);
				fieldb.setPersonId(idVal);
				fieldb.setData(bytes);
				fileRep.save(fieldb);
			}
		}
		
		
//------------------------ DB保存終了　-----------------------------
	    return "redirect:/user";
	    
	}
	
}
