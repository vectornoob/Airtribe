package com.taskmanager.parser;

import com.taskmanager.model.ParsedTask;
import java.time.LocalDate;

public class RuleBasedTaskParser implements TaskParser {
    @Override
    public ParsedTask parse(String text, String today) {
        String working = text.trim();
        String lower = working.toLowerCase();
        LocalDate current = LocalDate.parse(today);
        String dueDate = null;
        String recurrence = null;

        if (lower.contains("tomorrow")) {
            dueDate = current.plusDays(1).toString();
            working = working.replaceAll("(?i)\\btomorrow\\b", " ");
        }
        if (lower.contains("every month")) {
            recurrence = "monthly";
            working = working.replaceAll("(?i)\\bevery month\\b", " ");
        }
        if (lower.contains("1st") && recurrence != null) {
            LocalDate nextFirst = current.withDayOfMonth(1);
            if (!nextFirst.isAfter(current)) nextFirst = nextFirst.plusMonths(1);
            dueDate = nextFirst.toString();
            working = working.replaceAll("(?i)\\bon\\s+(?:the\\s+)?1st(?:\\s+of)?\\b", " ")
                    .replaceAll("(?i)\\bthe\\s+1st(?:\\s+of)?\\b", " ")
                    .replaceAll("(?i)\\b1st(?:\\s+of)?\\b", " ");
        }

        working = working.replaceAll("(?i)remind me to", " ")
                .replaceAll("(?i)please", " ")
                .replaceAll("\\s+", " ").trim();
        String title = working.isBlank() ? text.trim()
                : Character.toUpperCase(working.charAt(0)) + working.substring(1);
        return new ParsedTask(title, text.trim().replaceAll("\\s+", " "), dueDate, recurrence);
    }
}
