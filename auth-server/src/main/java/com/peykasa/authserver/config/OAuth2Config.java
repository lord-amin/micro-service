package com.peykasa.authserver.config;

import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

/**
 * @author Yaser(amin) Sadeghi
 */
@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2Config.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userService;
    private final ClientService clientService;
    private final ApplicationContext context;
    private final TokenStore tokenStore;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager).userDetailsService(userService).setClientDetailsService(clientService);
        endpoints.tokenStore(tokenStore);
    }


    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.checkTokenAccess("isAuthenticated()").passwordEncoder(context.getBean(PasswordEncoder.class));

    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientService);
    }


    public static class AESEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            try {
                LOGGER.debug("Encoding by AESEncoder {}",rawPassword);
                return SimpleProtector.encrypt(rawPassword.toString());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return "could not encode";
            }
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            try {
                LOGGER.warn("Checking by AESEncoder {}={}",rawPassword,SimpleProtector.decrypt(encodedPassword));
                return rawPassword.equals(SimpleProtector.decrypt(encodedPassword));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return false;
        }
    }

    public static class HashEncoder extends BCryptPasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            LOGGER.info("Encoding by HashEncoder ");
            return super.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            LOGGER.info("Checking by HashEncoder");
            return super.matches(rawPassword, encodedPassword);
        }
    }

    public static class SimpleProtector {
        private static byte[] SALT;
        private static Key SECRET_KEY_SPEC;

        public static final String SECRET_KEY = "B6mrFx77brJauUR58Z9RoheZTNLZgPOjqzSttYhY8ps=";
        public static final String INITIAL_VALUE = "gS0P4mJbFO00gyJI";
        public static final String ALGORITHM = "AES";


        static {
            SECRET_KEY_SPEC = new SecretKeySpec(decode(SECRET_KEY), ALGORITHM);
            SALT = INITIAL_VALUE.getBytes(StandardCharsets.UTF_8);
        }

        private SimpleProtector() {
        }

        @SneakyThrows
        static String encrypt(String valueToEnc) {
            final byte[] valueToEncInByte = valueToEnc.getBytes();
            Cipher encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            final IvParameterSpec ivSpec = new IvParameterSpec(SALT);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY_SPEC, ivSpec);
            byte[] encryptedBytes = encryptionCipher.doFinal(valueToEncInByte);
            return encode(encryptedBytes);
        }

        @SneakyThrows
        public static String decrypt(String encryptedValue) {
            final byte[] encryptedValueInByte = decode(encryptedValue);
            final Cipher decryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final IvParameterSpec ivSpec = new IvParameterSpec(SALT);
            decryptionCipher.init(Cipher.DECRYPT_MODE, SECRET_KEY_SPEC, ivSpec);
            byte[] decryptedBytes = decryptionCipher.doFinal(encryptedValueInByte);
            return new String(decryptedBytes);
        }

        /**
         * convert Byte[] to Base64
         */
        private static String encode(byte[] data) {
            return Base64.getEncoder().encodeToString(data);
        }

        /**
         * convert Byte[] to Base64
         */
        private static byte[] decode(String data) {
            return Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8));
        }
    }
}
