package com.platform.service;

import com.platform.model.*;
import com.platform.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class LearningReportService {

    private final LearningReportRepository reportRepo;
    private final UserProgressRepository userProgressRepo;
    private final UserSkillsProgressRepository skillsRepo;
    private final UserWrongQuestionRepository wrongQuestionRepo;
    private final ProgressService progressService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LearningReportService(LearningReportRepository reportRepo,
                                  UserProgressRepository userProgressRepo,
                                  UserSkillsProgressRepository skillsRepo,
                                  UserWrongQuestionRepository wrongQuestionRepo,
                                  ProgressService progressService) {
        this.reportRepo = reportRepo;
        this.userProgressRepo = userProgressRepo;
        this.skillsRepo = skillsRepo;
        this.wrongQuestionRepo = wrongQuestionRepo;
        this.progressService = progressService;
    }

    @Transactional(readOnly = true)
    public List<LearningReport> getUserReports(Integer userId) {
        return reportRepo.findByUserIdOrderByGeneratedAtDesc(userId);
    }

    public LearningReport generateReport(Integer userId, Long courseId, String reportType) {
        Map<String, Object> content = new HashMap<>();
        content.put("generatedAt", LocalDateTime.now().toString());
        content.put("reportType", reportType);

        // Progress summary
        Map<String, Object> courseProgress = progressService.getCourseProgress(userId, courseId);
        content.put("courseProgress", courseProgress);

        // Skill tree status
        List<UserSkillsProgress> skills = skillsRepo.findByUserId(userId);
        long mastered = skills.stream().filter(s -> s.getStatus() >= 3).count();
        long unlocked = skills.stream().filter(s -> s.getStatus() >= 1).count();
        content.put("skills", Map.of(
                "total", skills.size(),
                "unlocked", unlocked,
                "mastered", mastered
        ));

        // Wrong questions summary
        List<UserWrongQuestion> wrongQuestions = wrongQuestionRepo.findByUserIdAndStatus(userId, 0);
        content.put("wrongQuestionsPending", wrongQuestions.size());

        // Study stats
        List<UserProgress> allProgress = userProgressRepo.findByUserId(userId);
        int totalScore = allProgress.stream().mapToInt(p -> p.getScore() != null ? p.getScore() : 0).sum();
        int totalTime = allProgress.stream().mapToInt(p -> p.getTimeSpent() != null ? p.getTimeSpent() : 0).sum();
        long completed = allProgress.stream().filter(p -> "completed".equals(p.getStatus())).count();
        content.put("stats", Map.of(
                "totalScore", totalScore,
                "totalTimeMinutes", totalTime / 60,
                "completedLessons", completed,
                "totalLessons", allProgress.size()
        ));

        // Study streak (simplified: count days with activity)
        Set<String> activeDays = new HashSet<>();
        for (UserProgress p : allProgress) {
            if (p.getCompletedAt() != null) {
                activeDays.add(p.getCompletedAt().toLocalDate().toString());
            }
        }
        content.put("streak", activeDays.size());

        // Recommendations based on weak areas
        List<String> recommendations = new ArrayList<>();
        if (wrongQuestions.size() > 5) {
            recommendations.add("你有 " + wrongQuestions.size() + " 道错题待消灭，建议优先复习错题本");
        }
        if (mastered < 3) {
            recommendations.add("继续完成课程学习以解锁更多技能");
        }
        if (totalTime < 1800) {
            recommendations.add("建议每天至少学习30分钟以保持良好的进度");
        }
        if (completed > 0 && completed < 5) {
            recommendations.add("已有一个好的开始，继续完成更多课时！");
        }
        if (completed >= 10 && wrongQuestions.size() == 0) {
            recommendations.add("表现优异！可以尝试 PvP 天梯挑战其他玩家");
        }
        content.put("recommendations", recommendations);

        // Save report
        LearningReport report = new LearningReport();
        report.setUserId(userId);
        report.setCourseId(courseId);
        report.setReportType(reportType);
        try {
            report.setContent(objectMapper.writeValueAsString(content));
        } catch (Exception e) {
            report.setContent("{}");
        }
        return reportRepo.save(report);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSkillTreeVisualization(Integer userId) {
        List<UserSkillsProgress> skills = skillsRepo.findByUserId(userId);

        // Define the skill tree structure
        List<Map<String, Object>> nodes = new ArrayList<>();
        String[][] skillTree = {
            {"prompt_basics", "提示词基础", "基础"},
            {"prompt_advanced", "高级提示词", "提示词"},
            {"chain_of_thought", "思维链", "提示词"},
            {"code_execution", "代码执行", "工具"},
            {"web_search", "网络搜索", "工具"},
            {"data_analysis", "数据分析", "工具"},
            {"agent_design", "Agent 设计", "Agent"},
            {"multi_agent", "多 Agent 协作", "Agent"},
            {"rag_basics", "RAG 基础", "知识"},
            {"vector_search", "向量检索", "知识"},
        };

        Map<String, Integer> skillStatusMap = new HashMap<>();
        for (UserSkillsProgress s : skills) {
            skillStatusMap.put(s.getSkillId(), s.getStatus());
        }

        for (String[] skill : skillTree) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", skill[0]);
            node.put("name", skill[1]);
            node.put("category", skill[2]);
            node.put("status", skillStatusMap.getOrDefault(skill[0], 0));
            nodes.add(node);
        }

        // Define edges (prerequisites)
        List<Map<String, String>> edges = new ArrayList<>();
        edges.add(Map.of("from", "prompt_basics", "to", "prompt_advanced"));
        edges.add(Map.of("from", "prompt_basics", "to", "chain_of_thought"));
        edges.add(Map.of("from", "prompt_advanced", "to", "agent_design"));
        edges.add(Map.of("from", "code_execution", "to", "agent_design"));
        edges.add(Map.of("from", "web_search", "to", "rag_basics"));
        edges.add(Map.of("from", "rag_basics", "to", "vector_search"));
        edges.add(Map.of("from", "agent_design", "to", "multi_agent"));
        edges.add(Map.of("from", "data_analysis", "to", "agent_design"));

        return Map.of("nodes", nodes, "edges", edges);
    }
}
