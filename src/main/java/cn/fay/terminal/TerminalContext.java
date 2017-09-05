package cn.fay.terminal;


import cn.fay.terminal.annotation.Parser;
import cn.fay.terminal.annotation.SubTerminal;
import cn.fay.terminal.annotation.Terminal;
import cn.fay.terminal.parse.TerminalParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TerminalContext {
    private static final Logger LOGGER = LoggerFactory.getLogger("cn/fay/terminal");
    private static volatile Map<String, Map<String, Command>> terminal2Sub = null;//一个命令对应的子命令
    private static volatile Map<String, Command> name2command = null;//all super cn.fay.terminal name 2 parser
    private static volatile Map<String, TerminalParser> name2Parsers = new HashMap<>();//all parsers name 2 parser
    private static volatile Map<String, String> terminal2parser = null;//cn.fay.terminal name 2 parser name    //record the relation between cn.fay.terminal and parser
    private static AtomicBoolean load = new AtomicBoolean(false);
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    static {
        String defaultPath = TerminalContext.class.getResource("").getPath();
        load(defaultPath);
    }

    private TerminalContext() {
    }

    public static void load(String location) {
        if (load.compareAndSet(false, true)) {
            LOGGER.info("load at " + new Date());
            Map<SubTerminal, Class> subTerminals = null;
            for (File file : getResources(location)) {
                if (file.getName().endsWith(".class")) {
                    String className = file.getAbsolutePath().split(FILE_SEPARATOR + "classes" + FILE_SEPARATOR)[1].split(".class")[0].replace(FILE_SEPARATOR, ".");
                    try {
                        if (className.contains("$")) {//filter inner class
                            continue;
                        }
                        Class clazz = Class.forName(className);
                        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {//filter interface or abstract class
                            if (Command.class.isAssignableFrom(clazz)) {
                                if (clazz.isAnnotationPresent(Terminal.class)) {
                                    Terminal terminal = ((Terminal) clazz.getAnnotation(Terminal.class));
                                    String value = terminal.value();
                                    String parser = terminal.parser();
                                    Command command = (Command) clazz.newInstance();
                                    String lowerCaseClassName = clazz.getSimpleName().toLowerCase();
                                    if (Arrays.asList(clazz.getDeclaredAnnotations()).contains(terminal)) {
                                        terminal2parser = putGrace(terminal2parser, value, parser);
                                        name2command = putGrace(name2command, value, command);
                                    } else if (!clazz.isAnnotationPresent(SubTerminal.class)) {//sub class and no SubTerminal annotation present so it's a use default 'name config' subTerminal
                                        int index = lowerCaseClassName.indexOf(value);
                                        String subTerminal = lowerCaseClassName.substring(index + value.length());
                                        terminal2Sub = putGrace(terminal2Sub, value, subTerminal, command);
                                    } else {
                                        SubTerminal subTerminal = (SubTerminal) clazz.getAnnotation(SubTerminal.class);
                                        subTerminals = putGrace(subTerminals, subTerminal, clazz);//handler later
                                    }
                                }
                            } else if (TerminalParser.class.isAssignableFrom(clazz)) {//subclass of TerminalParser
                                if (clazz.isAnnotationPresent(Parser.class)) {
                                    String value = ((Parser) clazz.getAnnotation(Parser.class)).value();
                                    name2Parsers.put(value, (TerminalParser) clazz.newInstance());
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("class not found:" + className);
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (subTerminals != null) {//now handler SubTerminal annotation
                for (SubTerminal subTerminal : subTerminals.keySet()) {
                    String base = subTerminal.base();
                    String value = subTerminal.value();
                    if (name2command == null || !name2command.containsKey(base)) {//no that super cn.fay.terminal//ignore
                        LOGGER.warn("{} be filtered because not found the base {}", value, base);
                        continue;
                    }
                    Command command = null;
                    try {
                        command = (Command) subTerminals.get(subTerminal).newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    terminal2Sub = putGrace(terminal2Sub, base, value, command);
                }
            }
        } else {
            LOGGER.info("command is already loaded.");
        }
    }

    private static <K, V> Map<K, V> putGrace(Map<K, V> map, K key, V value) {
        if (map == null) {
            map = new HashMap<K, V>();
        }
        map.put(key, value);
        return map;
    }

    private static <K, V extends Collection<T>, T> Map<K, V> putGrace(Map<K, V> map, K key, T itemOfCollection, V initVal) {
        if (map == null) {
            map = new HashMap<K, V>();
        }
        if (!map.containsKey(key)) {
            map.put(key, initVal);
        }
        map.get(key).add(itemOfCollection);
        return map;
    }

    private static <K, T> Map<K, Map<K, T>> putGrace(Map<K, Map<K, T>> map, K outMapKey, K innerMapKey, T innerMapVal) {
        if (map == null) {
            map = new HashMap<K, Map<K, T>>();
        }
        if (!map.containsKey(outMapKey)) {
            map.put(outMapKey, new HashMap<K, T>());
        }
        map.get(outMapKey).put(innerMapKey, innerMapVal);
        return map;
    }

    public static List<String> getAllTerminal() {
        if (name2command == null) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<String>(name2command.keySet());
        Collections.sort(list);
        return list;
    }

    public static Map<String, Command> getSubTerminal(String superTerminal) {
        if (terminal2Sub == null || !terminal2Sub.containsKey(superTerminal)) {
            return Collections.emptyMap();
        }
        return terminal2Sub.get(superTerminal);
    }

    public static Command getCommand(String terminal) {
        return name2command == null ? null : name2command.get(terminal);
    }

    private static File[] getResources(String location) {
        File[] result = null;
        try {
            File rootDir = new File(location);
            if (!rootDir.isDirectory()) {
                throw new RuntimeException(String.format("%s is not a directory.", location));
            }
            File[] files = rootDir.listFiles();
            result = files;
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] temp = getResources(file.getAbsolutePath());
                    File[] trans = new File[result.length + temp.length];
                    System.arraycopy(result, 0, trans, 0, result.length);
                    System.arraycopy(temp, 0, trans, result.length, temp.length);
                    result = trans;
                }
            }

        } catch (Exception e) {
        }
        return result;
    }

    public static void addParser(String name, TerminalParser parser) {
        name2Parsers.put(name, parser);
    }

    public static TerminalParser getParser(String terminal) {
        if (terminal2parser == null || !terminal2parser.containsKey(terminal) || !name2Parsers.containsKey(terminal2parser.get(terminal))) {
            return null;
        }
        return name2Parsers.get(terminal2parser.get(terminal));
    }

    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println(terminal2Sub);
        System.out.println(name2command);
        System.out.println(name2Parsers);
        System.out.println(terminal2parser);
    }
}
