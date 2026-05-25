package com.platform.service;

import com.platform.model.User;
import com.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: id=" + userId));
    }

    @Transactional
    public User checkIn(Integer userId) {
        User user = findById(userId);
        LocalDate today = LocalDate.now();
        if (!today.equals(user.getLastCheckinDate())) {
            user.setLastCheckinDate(today);
            user.setTotalScore(user.getTotalScore() + 1);
            updateRank(user);
            userRepository.save(user);
        }
        return user;
    }

    @Transactional
    public void addScore(Integer userId, int score) {
        User user = findById(userId);
        user.setTotalScore(user.getTotalScore() + score);
        updateRank(user);
        userRepository.save(user);
    }

    private void updateRank(User user) {
        int score = user.getTotalScore();
        if (score >= 50) user.setRank("王者");
        else if (score >= 40) user.setRank("钻石");
        else if (score >= 30) user.setRank("铂金");
        else if (score >= 20) user.setRank("黄金");
        else if (score >= 10) user.setRank("白银");
        else user.setRank("青铜");
    }
}
