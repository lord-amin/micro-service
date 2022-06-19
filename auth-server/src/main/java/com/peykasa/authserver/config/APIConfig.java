package com.peykasa.authserver.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.var;

/**
 * @author Yaser(amin) Sadeghi
 */
@NoArgsConstructor
public class APIConfig implements Comparable<APIConfig> {
    public APIConfig(String method, String url) {
        this.method = method;
        this.url = url;
    }

    @Getter
    @Setter
    private String method;
    @Getter
    @Setter
    private String url;

    public String toString() {
        return String.format("%1$-" + 30 + "s", url) + ":" + String.format("%1$-" + 10 + "s", method);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var apiConfig = (APIConfig) o;

        return method != null
                && apiConfig.method != null
                && url != null
                && apiConfig.url != null
                && method.equals(apiConfig.method) && url.equals(apiConfig.url);
    }

    @Override
    public int hashCode() {
        var result = method != null ? method.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(APIConfig o) {
        return this.toString().compareTo(o.toString());
    }
}
