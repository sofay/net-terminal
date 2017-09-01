package cn.fay.terminal;


import cn.fay.terminal.parse.TerminalParser;
import cn.fay.terminal.request.TerminalRequest;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalApplication{
    private static TerminalRoute route = TerminalRoute.getRoute();

    private TerminalApplication(){}

    public static String run(String code) {
        try {
            String[] arr = code.split(" ");
            String terminal;
            if (arr.length <= 0 || "".equals(terminal = arr[0].trim())) {
                throw new RuntimeException(noInput());
            }
            TerminalParser parser = TerminalContext.getParser(terminal);
            if (parser == null) {
                throw new RuntimeException("not found parser for[" + terminal + "]");
            }
            TerminalRequest request = parser.parse(code);
            return route.runTerminal(request);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("msg", e.getMessage());
            return JSON.toJSONString(response);
        }
    }

    private static String noInput() {
        List<String> allTerm = TerminalContext.getAllTerminal();
        StringBuilder log = new StringBuilder("there are all terminals what you can use:[");
        for (String term : allTerm) {
            log.append(term).append(",");
        }
        log.deleteCharAt(log.length() - 1);
        log.append("]");
        return log.toString();
    }

}
