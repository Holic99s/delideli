package kr.co.jhta.app.delideli.user.control;

import kr.co.jhta.app.delideli.user.account.domain.UserAccount;
import kr.co.jhta.app.delideli.user.account.service.UserService;
import kr.co.jhta.app.delideli.user.order.domain.Order;
import kr.co.jhta.app.delideli.user.order.domain.OrderDetail;
import kr.co.jhta.app.delideli.user.order.service.OrderService;
import kr.co.jhta.app.delideli.user.review.domain.Review;
import kr.co.jhta.app.delideli.user.review.service.ReviewService;
import kr.co.jhta.app.delideli.user.store.domain.StoreInfo;
import kr.co.jhta.app.delideli.user.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final OrderService orderService;
    @Autowired
    private final StoreService storeService;
    @Autowired
    private final ReviewService reviewService;

    @GetMapping("/myReview")
    public String myReview(@AuthenticationPrincipal User user, Model model) {
        // 현재 사용자 정보를 가져옴
        UserAccount userAccount = userService.findUserById(user.getUsername());

        // 오늘 날짜를 기준으로 7일 전 날짜를 계산
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 리뷰 작성이 가능한 주문 목록을 가져옴 (조건: order_method가 '완료'이고, 주문일이 7일 이내인 경우)
        ArrayList<Order> reviewableOrders = orderService.getReviewableOrders(userAccount.getUserKey(), sevenDaysAgo);

        // 작성 가능한 리뷰 목록과 작성된 리뷰 목록을 구분하기 위한 리스트 생성
        ArrayList<Order> availableForReviewOrders = new ArrayList<>();
        ArrayList<Order> writtenReviewOrders = new ArrayList<>();

        // 각 주문에 대한 상세 정보를 가져옴
        for (Order order : reviewableOrders) {
            ArrayList<OrderDetail> orderDetails = orderService.getOrderDetailsByOrderKey(order.getOrderKey());
            order.setOrderDetails(orderDetails);

            StoreInfo storeInfo = storeService.getStoreInfoById(order.getStoreInfoKey());
            order.setStoreInfo(storeInfo);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            String formattedDate = order.getOrderRegdate().format(formatter);
            order.setFormattedOrderDate(formattedDate);

            // 남은 리뷰 작성 기간 계산
            int remainingDays = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), order.getOrderRegdate().plusDays(7));
            order.setRemainingDays(remainingDays);

            // 리뷰 작성 여부 확인 후 리스트에 추가
            if (reviewService.isReviewWritten(order.getOrderKey())) {
                Review review = reviewService.getReviewByOrderKey(order.getOrderKey());
                order.setReview(review); // 리뷰 정보 설정
                writtenReviewOrders.add(order);
            } else {
                availableForReviewOrders.add(order);
            }
        }

        // 사용자 정보와 리뷰 가능한 주문 목록을 모델에 추가
        model.addAttribute("user", userAccount);
        model.addAttribute("availableForReviewOrders", availableForReviewOrders);
        model.addAttribute("writtenReviewOrders", writtenReviewOrders);

        return "user/mypage/myReview";
    }

    // 리뷰 작성 페이지로 이동
    @GetMapping("/writeReview")
    public String writeReview(@AuthenticationPrincipal User user, @RequestParam("orderKey") int orderKey, Model model) {
        // 현재 사용자 정보를 가져옴
        UserAccount userAccount = userService.findUserById(user.getUsername());

        // 해당 주문 정보와 가게 정보 가져오기
        Order order = orderService.getOrderByKey(orderKey);
        StoreInfo storeInfo = storeService.getStoreInfoById(order.getStoreInfoKey());

        // 주문 정보와 가게 정보를 모델에 추가
        model.addAttribute("user", userAccount);
        model.addAttribute("order", order);
        model.addAttribute("storeInfo", storeInfo);

        return "user/mypage/writeReview"; // 리뷰 작성 페이지로 이동
    }

    // 리뷰 제출
    @PostMapping("/saveReview")
    public String submitReview(@RequestParam("orderKey") int orderKey,
                               @RequestParam("clientKey") int clientKey,
                               @RequestParam("storeInfoKey") int storeInfoKey,
                               @RequestParam("reviewRating") int reviewRating,
                               @RequestParam("reviewDesc") String reviewDesc,
                               @RequestParam("reviewPhoto1") MultipartFile reviewPhoto1,
                               @RequestParam("reviewPhoto2") MultipartFile reviewPhoto2,
                               @AuthenticationPrincipal User user) {
        // 현재 사용자 정보를 가져옴
        UserAccount userAccount = userService.findUserById(user.getUsername());


        // 리뷰 생성 및 저장
        Review review = new Review();
        review.setOrderKey(orderKey);
        review.setStoreInfoKey(storeInfoKey);
        review.setClientKey(clientKey);
        review.setUserKey(userAccount.getUserKey());
        review.setReviewRating(reviewRating);
        review.setReviewDesc(reviewDesc);

        // 사진 파일 저장 처리
        if (!reviewPhoto1.isEmpty()) {
            String fileName1 = saveProfileImage(reviewPhoto1);
            review.setReviewPhoto1(fileName1);
        }
        if (!reviewPhoto2.isEmpty()) {
            String fileName2 = saveProfileImage(reviewPhoto2);
            review.setReviewPhoto2(fileName2);
        }

        reviewService.saveReview(review);
        return "redirect:/user/myReview";
    }

    // 프로필 이미지 저장
    private String saveProfileImage(MultipartFile file) {
        String uploadDir = "src/main/resources/static/user/images/uploads/";
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String filePath = uploadDir + uniqueFilename;

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(filePath);
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "/user/images/uploads/" + uniqueFilename;
    }
}
