package com.cooking.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            // H2 数据库修复表结构
            // 检查并修复 dish_id 列
            try {
                String checkSql = "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'COMMENTS' AND COLUMN_NAME = 'DISH_ID'";
                String isNullable = jdbcTemplate.queryForObject(checkSql, String.class);
                if ("NO".equalsIgnoreCase(isNullable)) {
                    // H2 数据库语法：ALTER TABLE ... ALTER COLUMN ... SET NULL
                    jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN dish_id SET NULL");
                    System.out.println("已修复 comments.dish_id 列，允许 NULL");
                }
            } catch (Exception e) {
                // 如果查询失败，可能是表不存在或列不存在，尝试直接修改
                try {
                    jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN dish_id SET NULL");
                    System.out.println("已修复 comments.dish_id 列，允许 NULL");
                } catch (Exception ex) {
                    // 忽略错误
                }
            }

            // 检查并修复 note_id 列
            try {
                String checkNoteSql = "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'COMMENTS' AND COLUMN_NAME = 'NOTE_ID'";
                String isNullable = jdbcTemplate.queryForObject(checkNoteSql, String.class);
                if ("NO".equalsIgnoreCase(isNullable)) {
                    jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN note_id SET NULL");
                    System.out.println("已修复 comments.note_id 列，允许 NULL");
                }
            } catch (Exception e) {
                // 如果查询失败，可能是列不存在，尝试直接修改
                try {
                    jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN note_id SET NULL");
                    System.out.println("已修复 comments.note_id 列，允许 NULL");
                } catch (Exception ex) {
                    // 忽略错误
                }
            }
        } catch (Exception e) {
            // 迁移失败不影响应用启动
            System.err.println("数据库迁移失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
