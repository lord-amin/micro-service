package com.peykasa.authserver.tools;

import com.peykasa.authserver.tools.permission.PermissionService;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author Yaser(amin) Sadeghi
 */
@SpringBootApplication(scanBasePackages = {"com.peykasa.authserver.tools"}, exclude = JmxAutoConfiguration.class)
@EntityScan("com.peykasa.authserver.model.*")
public class ImportApplication {
    private static Options options = new Options();

    public static void main(String[] args) throws ParseException, IOException {
        Command command;
        CommandLineParser parser = new RParser();
        options.addOption(Option.builder(ImportOption.ACTION.shortOpt).hasArg().longOpt(ImportOption.ACTION.longOpt)
                .argName("action").desc("action : update or delete").build());
        options.addOption(Option.builder(ImportOption.SERVICE.shortOpt).hasArg().longOpt(ImportOption.SERVICE.longOpt)
                .argName("service").desc("service name").build());
        options.addOption(Option.builder(ImportOption.FILE.shortOpt).hasArg().longOpt(ImportOption.FILE.longOpt)
                .argName("file").desc("file full path").build());
        options.addOption(Option.builder(ImportOption.LEVEL.shortOpt).hasArg().longOpt(ImportOption.LEVEL.longOpt)
                .argName("logLevel").desc("log level: error or info").build());
        CommandLine cmd = parser.parse(options, args, false);
        if (!cmd.hasOption(ImportOption.ACTION.shortOpt)) {
            System.err.println("Action not found");
            printHelp();
            return;
        }
        String optionValue = cmd.getOptionValue(ImportOption.ACTION.shortOpt);
        if (!cmd.hasOption(ImportOption.SERVICE.shortOpt)) {
            System.err.println("Service name not found");
            printHelp();
            return;
        }
        if ("update".equals(optionValue)) {
            if (!cmd.hasOption(ImportOption.FILE.shortOpt)) {
                System.err.println("File not found");
                printHelp();
                return;
            }
            File file = new File(cmd.getOptionValue(ImportOption.FILE.shortOpt));
            if (file.isDirectory() || !file.exists()) {
                throw new FileNotFoundException("The file '" + file.getAbsolutePath() + "' not found");
            }
            command = new UpdateCommand(file, cmd.getOptionValue(ImportOption.SERVICE.shortOpt));
        } else if ("delete".equals(optionValue)) {
            command = new DeleteCommand(cmd.getOptionValue(ImportOption.SERVICE.shortOpt));
        } else {
            printHelp();
            return;
        }
        Set<String> defaultArgs = new LinkedHashSet<>();
        defaultArgs.add("--spring.main.web-environment=false");
        if (cmd.hasOption(ImportOption.LEVEL.shortOpt))
            defaultArgs.add("--logging.level.root=" + cmd.getOptionValue(ImportOption.LEVEL.shortOpt));
        else
            defaultArgs.add("--logging.level.root=error");
        defaultArgs.add("--spring.jpa.show-sql=true");
        defaultArgs.add("--spring.jmx.enabled=false");
        defaultArgs.add("--logging.level.org.hibernate.type.descriptor.sql.BasicBinder=" + cmd.getOptionValue(ImportOption.LEVEL.shortOpt));
        defaultArgs.addAll(Arrays.asList(args));
        System.out.println("Default Args is --------->>> " + defaultArgs);
        ConfigurableApplicationContext run = SpringApplication.run(ImportApplication.class, defaultArgs.toArray(new String[]{}));
        command.run(run.getBean(PermissionService.class));
    }


    public enum ImportOption {
        HELP("h", "help"),
        FILE("f", "file"),
        LEVEL("l", "level"),
        SERVICE("s", "serviceName"),
        ACTION("a", "action");
        public final String shortOpt;
        public final String longOpt;

        ImportOption(String shortOpt, String longOpt) {
            this.shortOpt = shortOpt;
            this.longOpt = longOpt;
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200, ":", "", options, "");
    }

    private interface Command {
        void run(PermissionService permissionConfigUtil) throws IOException;
    }

    private static class UpdateCommand implements Command {
        private File file;
        private String sName;

        UpdateCommand(File file, String sName) {
            this.file = file;
            this.sName = sName;
        }

        @Override
        public void run(PermissionService permissionConfigUtil) throws IOException {
            Map<String, String> update = permissionConfigUtil.update(file, sName);
            for (Map.Entry<String, String> entry : update.entrySet()) {
                System.out.println(entry.getKey() + " => " + entry.getValue());
            }
        }
    }

    private static class DeleteCommand implements Command {
        private String sName;

        DeleteCommand(String sName) {
            this.sName = sName;
        }

        @Override
        public void run(PermissionService permissionConfigUtil) {
            List<String> delete = permissionConfigUtil.delete(sName);
            if (delete.isEmpty()) {
                System.out.println("Could not found any permissions with service name " + sName);
                return;
            }
            for (String entry : delete) {
                System.out.println(entry + " => deleted");
            }
        }
    }

    public static class RParser extends DefaultParser {

        private final ArrayList<String> notParsedArgs = new ArrayList<>();

        public String[] getNotParsedArgs() {
            return notParsedArgs.toArray(new String[notParsedArgs.size()]);
        }

        @Override
        public CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException {
            if (stopAtNonOption) {
                return parse(options, arguments);
            }
            List<String> knownArguments = new ArrayList<>();
            notParsedArgs.clear();
            boolean nextArgument = false;
            for (String arg : arguments) {
                if (options.hasOption(arg) || nextArgument) {
                    knownArguments.add(arg);
                } else {
                    notParsedArgs.add(arg);
                }

                nextArgument = options.hasOption(arg) && options.getOption(arg).hasArg();
            }
            return super.parse(options, knownArguments.toArray(new String[knownArguments.size()]));
        }

    }

}
