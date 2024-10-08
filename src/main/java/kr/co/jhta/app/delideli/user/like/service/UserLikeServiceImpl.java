package kr.co.jhta.app.delideli.user.like.service;

import kr.co.jhta.app.delideli.user.like.domain.Like;
import kr.co.jhta.app.delideli.user.like.mapper.UserLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLikeServiceImpl implements UserLikeService {

    private final UserLikeMapper userLikeMapper;

    @Override
    public Like findLikeByUserAndStore(int userKey, int storeInfoKey) {
        return userLikeMapper.findLikeByUserAndStore(userKey, storeInfoKey);
    }

    @Override
    public void toggleLike(int userKey, int storeInfoKey) {
        Like like = userLikeMapper.findLikeByUserAndStore(userKey, storeInfoKey);
        if (like != null) {
            userLikeMapper.deleteLike(like.getLikeKey());  // '찜하기 취소' -> 해당 항목 삭제
        } else {
            like = new Like();
            like.setUserKey(userKey);
            like.setStoreInfoKey(storeInfoKey);
            like.setLikeStatus(1); // 기본으로 '찜하기' 상태
            userLikeMapper.insertLike(like);  // '찜하기' -> 새 항목 추가
        }
    }

}
