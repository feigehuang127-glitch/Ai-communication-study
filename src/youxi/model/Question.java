package youxi.model;

import java.util.StringJoiner;

public class Question {
    private int id;
    private String category;
    private String type;        // "单选" / "多选" / "判断"
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;      // "A" / "BC" / "ABC" 字母连写
    private String explanation;
    private int difficulty;     // 1-10

    public Question() {}

    /** 合并四个选项为逗号分隔字符串，供 UI 展示 */
    public String getOptions() {
        StringJoiner sj = new StringJoiner(",");
        if (optionA != null) sj.add("A." + optionA);
        if (optionB != null) sj.add("B." + optionB);
        if (optionC != null && !optionC.isEmpty()) sj.add("C." + optionC);
        if (optionD != null && !optionD.isEmpty()) sj.add("D." + optionD);
        return sj.toString();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}
