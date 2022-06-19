package com.peykasa.authserver.config;

import com.peykasa.authserver.model.entity.DeletedUser;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.model.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;

/**
 * @author Taher Khorshidi, Yaser(amin) Sadeghi
 */
@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(User.class, Role.class, Permission.class, DeletedUser.class);
        config.setDefaultMediaType(MediaType.APPLICATION_JSON);
    }

    @Bean
    public HateoasPageableHandlerMethodArgumentResolver customResolver(
            HateoasPageableHandlerMethodArgumentResolver pageableResolver) {
        pageableResolver.setFallbackPageable(null);
        return pageableResolver;
    }

}