package com.controller;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entity.Login;
import com.entity.Reservation;
import com.entity.Review;
import com.repository.ReservationRepository;
import com.repository.ReviewRepository;
import com.repository.SpaceRepository;

/*
 * レビュー機能
 * 
 * ・利用済み（status = USED）の予約に対してレビューを登録できる
 */
@Controller
public class ReviewController {

    @Autowired
    private ReservationRepository reservationRep;

    @Autowired
    private ReviewRepository reviewRep;

    @Autowired
    private SpaceRepository spaceRep;

    /**
     * レビュー入力画面
     */
    @GetMapping("/review/{reservationId}")
    public String reviewForm(
            @PathVariable int reservationId,
            HttpSession session,
            Model model) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Reservation reservation = reservationRep.findById(reservationId).orElse(null);
        if (reservation == null || reservation.getUserId() != loginUser.getID()) {
            return "redirect:/mypage";
        }

        // 利用済みのみレビュー可能
        if (!"USED".equals(reservation.getStatus())) {
            model.addAttribute("error", "利用済みの予約のみレビューできます。");
            return "redirect:/mypage";
        }

        Optional<Review> existing = reviewRep.findByReservationId(reservationId);

        Review review = existing.orElseGet(Review::new);

        // 画面表示用
        model.addAttribute("reservation", reservation);
        model.addAttribute("space", spaceRep.findById((long) reservation.getSpaceId()).orElse(null));
        model.addAttribute("review", review);

        return "review";
    }

    /**
     * レビュー保存
     */
    @PostMapping("/review/save")
    public String saveReview(
            @RequestParam("reservationId") int reservationId,
            @RequestParam("rating") int rating,
            @RequestParam(value = "comment", required = false) String comment,
            HttpSession session,
            Model model) {

        Login loginUser = (Login) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Reservation reservation = reservationRep.findById(reservationId).orElse(null);
        if (reservation == null || reservation.getUserId() != loginUser.getID()) {
            return "redirect:/mypage";
        }

        if (!"USED".equals(reservation.getStatus())) {
            return "redirect:/mypage";
        }

        // 入力チェック（1〜5）
        if (rating < 1 || rating > 5) {
            model.addAttribute("error", "評価は1〜5で入力してください。");
            model.addAttribute("reservation", reservation);
            model.addAttribute("space", spaceRep.findById((long) reservation.getSpaceId()).orElse(null));
            Review tmp = new Review();
            tmp.setReservationId(reservationId);
            tmp.setRating(rating);
            tmp.setComment(comment);
            model.addAttribute("review", tmp);
            return "review";
        }

        Review review = reviewRep.findByReservationId(reservationId).orElseGet(Review::new);

        review.setReservationId(reservationId);
        review.setSpaceId(reservation.getSpaceId());
        review.setUserId(loginUser.getID());
        review.setRating(rating);
        review.setComment(comment);

        reviewRep.save(review);

        return "redirect:/mypage";
    }
}
