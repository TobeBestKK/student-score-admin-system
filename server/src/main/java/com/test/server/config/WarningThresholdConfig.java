package com.test.server.config;

public final class WarningThresholdConfig {

    private WarningThresholdConfig() {}

    // 普通提醒
    public static final double LOW_SCORE_MIN = 60;
    public static final double LOW_SCORE_MAX = 65;
    public static final double SEMESTER_AVG_THRESHOLD = 65;
    public static final double RANK_PERCENTILE = 0.30;

    // 重点关注
    public static final double FAIL_SCORE = 60;
    public static final int FAIL_COUNT_MODERATE_MAX = 2;

    // 严重预警
    public static final int FAIL_COUNT_SEVERE_MIN = 3;

    // 预警级别枚举
    public enum WarningLevel {
        NORMAL("普通提醒", 1),
        MODERATE("重点关注", 2),
        SEVERE("严重预警", 3);

        private final String label;
        private final int priority;

        WarningLevel(String label, int priority) {
            this.label = label;
            this.priority = priority;
        }

        public String getLabel() { return label; }
        public int getPriority() { return priority; }

        public static WarningLevel max(WarningLevel a, WarningLevel b) {
            return a.priority >= b.priority ? a : b;
        }
    }

    // 预警类型枚举
    public enum WarningType {
        LOW_SCORE("单科低分"),
        SEMESTER_AVG("学期平均分偏低"),
        RANK_BOTTOM("总分排名靠后"),
        CUMULATIVE_FAIL("累计不及格");

        private final String label;

        WarningType(String label) {
            this.label = label;
        }

        public String getLabel() { return label; }
    }
}
