package com.blitz.ai.infrastructure.rag;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KeywordSearchService {

    private final JdbcTemplate pgJdbcTemplate;

    public KeywordSearchService(@Qualifier("pgJdbcTemplate") JdbcTemplate pgJdbcTemplate) {
        this.pgJdbcTemplate = pgJdbcTemplate;
    }

    public List<Map<String, Object>> search(String query, int topK) {
        String[] keywords = query.split("\\s+");
        StringBuilder sql = new StringBuilder(
            "SELECT id, content, metadata, (CASE ");

        for (int i = 0; i < keywords.length; i++) {
            sql.append("WHEN content ILIKE '%").append(escapeLike(keywords[i]))
               .append("%' THEN 1 ELSE 0 END + ");
        }
        sql.append("0) AS score ");
        sql.append("FROM vector_store ");
        sql.append("WHERE ");
        for (int i = 0; i < keywords.length; i++) {
            if (i > 0) sql.append("OR ");
            sql.append("content ILIKE '%").append(escapeLike(keywords[i])).append("%' ");
        }
        sql.append("ORDER BY score DESC LIMIT ?");

        return pgJdbcTemplate.queryForList(sql.toString(), topK);
    }

    private String escapeLike(String s) {
        return s.replace("'", "''").replace("%", "\\%").replace("_", "\\_");
    }
}
