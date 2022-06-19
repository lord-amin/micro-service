package com.peykasa.silo.central.filter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
public class SecConfig {
    @Getter
    @Value("${rs.security.ignore.expressions:}")
    private String securityIgnoreExpressions;

    public List<Pattern> getExcludePathPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        if (!securityIgnoreExpressions.isEmpty()) {
            String[] split = securityIgnoreExpressions.trim().split("^\\[|\\]$|\\[\\s*\\]|\\]\\s*\\[");
            for (String s : split) {
                if (s.isEmpty())
                    continue;
                try {
                    patterns.add(Pattern.compile(s.trim()));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException("Invalid regex='" + s + "' in security ignore ", e);
                }
            }
        }
        return patterns;
    }

}
