package com.peykasa.authserver;


import com.mysql.cj.jdbc.MysqlDataSource;
import com.peykasa.authserver.config.OAuth2Config;
import com.peykasa.authserver.model.entity.User;
import org.apache.commons.cli.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Yaser(amin) Sadeghi
 */

public class EncoderApplication {
    private static Properties properties = new Properties();
    private static Options options = new Options();

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();

        Option encode = Option.builder(EncodeOptions.ENCODE_UPDATE.shortOpt).hasArg().optionalArg(true).longOpt(EncodeOptions.ENCODE_UPDATE.longOpt).argName("encode").desc("encode and update database password , ex : -e {c}").build();
        encode.setArgName("commit");
        options.addOption(encode);
        Option decode = Option.builder(EncodeOptions.DECODE_UPDATE.shortOpt).hasArg().optionalArg(true).longOpt(EncodeOptions.DECODE_UPDATE.longOpt).argName("decode").desc("decode and update database password , ex : -d {c}").build();
        decode.setArgName("commit");
        options.addOption(decode);

        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(EncodeOptions.ENCODE_UPDATE.shortOpt)) {
            String optionValue = cmd.getOptionValue(EncodeOptions.ENCODE_UPDATE.shortOpt);
            if (optionValue != null && "c".equals(optionValue)) {
                encodeAndUpdate();
            } else {
                JdbcTemplate jdbcTemplate = jdbcTemplate();
                encode(getUsers(jdbcTemplate));
                encode(getClients(jdbcTemplate));
            }
            return;
        }
        if (cmd.hasOption(EncodeOptions.DECODE_UPDATE.shortOpt)) {
            String optionValue = cmd.getOptionValue(EncodeOptions.DECODE_UPDATE.shortOpt);
            if (optionValue != null && "c".equals(optionValue)) {
                decodeAndUpdate();
            } else {
                JdbcTemplate jdbcTemplate = jdbcTemplate();
                decode(getUsers(jdbcTemplate));
                decode(getClients(jdbcTemplate));
            }
            return;
        }
        printHelp();
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200, ":", "", options, "");
    }

    public enum EncodeOptions {
        ENCODE_UPDATE("e", "encode"),
        DECODE_UPDATE("d", "decode");

        public final String shortOpt;
        public final String longOpt;

        EncodeOptions(String shortOpt, String longOpt) {
            this.shortOpt = shortOpt;
            this.longOpt = longOpt;
        }
    }

    private static void encodeAndUpdate() throws IOException {
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        List<User> print = encode(getUsers(jdbcTemplate));
        for (User user : print) {
            System.out.println("Updating user '" + user.getUsername() + "' ");
            jdbcTemplate.execute("update tbl_user set password = '" + user.getPassword() + "' where username='" + user.getUsername() + "'");
        }
        List<User> clients = encode(getClients(jdbcTemplate));
        for (User user : clients) {
            System.out.println("Updating client '" + user.getUsername() + "' ");
            jdbcTemplate.execute("update tbl_client set client_secret = '" + user.getPassword() + "' where client_id='" + user.getUsername() + "'");
        }
    }

    private static void decodeAndUpdate() throws Exception {
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        List<User> print = decode(getUsers(jdbcTemplate));
        for (User user : print) {
            System.out.println("Updating user '" + user.getUsername() + "' ");
            jdbcTemplate.execute("update tbl_user set password = '" + user.getPassword() + "' where username='" + user.getUsername() + "'");
        }
        List<User> clients = decode(getClients(jdbcTemplate));
        for (User user : clients) {
            System.out.println("Updating client '" + user.getUsername() + "' ");
            jdbcTemplate.execute("update tbl_client set client_secret= '" + user.getPassword() + "' where client_id='" + user.getUsername() + "'");
        }
    }

    private static JdbcTemplate jdbcTemplate() throws IOException {
        properties.load(EncoderApplication.class.getResourceAsStream("/application.properties"));
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(properties.getProperty("spring.datasource.url"));
        dataSource.setUser(properties.getProperty("spring.datasource.username"));
        dataSource.setPassword(properties.getProperty("spring.datasource.password"));
        return new JdbcTemplate(dataSource);
    }

    private static List<User> encode(List<User> users) throws IOException {
        String property = properties.getProperty("app.password.encoder");
        PasswordEncoder hashEncoder = NoOpPasswordEncoder.getInstance();
        if (property != null && "hash".equalsIgnoreCase(property)) {
            hashEncoder = new OAuth2Config.HashEncoder();
        }
        if (property != null && "encrypt".equalsIgnoreCase(property)) {
            hashEncoder = new OAuth2Config.AESEncoder();
        }
        for (User user : users) {
            user.setPassword(hashEncoder.encode(user.getPassword()));
            System.out.println("'" + user.getUsername() + "' ==> '" + user.getPassword() + "'");
        }
        return users;
    }

    private static List<User> decode(List<User> users) throws Exception {
        String property = properties.getProperty("app.password.encoder");
        if (property != null && "hash".equalsIgnoreCase(property)) {
            System.err.println("Could not decode from hash");
            throw new IllegalStateException();
        } else if (property != null && "encrypt".equalsIgnoreCase(property)) {
            System.out.println("Decrypt to encrypt type ");
            for (User user : users) {
                user.setPassword(OAuth2Config.SimpleProtector.decrypt(user.getPassword()));
                System.out.println("'" + user.getUsername() + "' ==> '" + user.getPassword() + "'");
            }
        } else {
            System.out.println("Decrypt to plain type");
            for (User user : users) {
                System.out.println("'" + user.getUsername() + "' ==> '" + user.getPassword() + "'");
            }
        }
        return users;
    }

    private static List<User> getUsers(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.query("select * from tbl_user", (resultSet, i) -> {
            User user = new User();
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            return user;
        });

    }

    private static List<User> getClients(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.query("select * from tbl_client", (resultSet, i) -> {
            User user = new User();
            user.setUsername(resultSet.getString("client_id"));
            user.setPassword(resultSet.getString("client_secret"));
            return user;
        });

    }
}
