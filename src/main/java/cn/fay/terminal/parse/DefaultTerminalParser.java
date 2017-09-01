package cn.fay.terminal.parse;


import cn.fay.terminal.annotation.Parser;
import cn.fay.terminal.request.SubTerminalRequest;
import cn.fay.terminal.request.TerminalRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * default parser
 * can only parser one sub cn.fay.terminal once.
 */
@Parser("default")
public class DefaultTerminalParser extends AbstractTerminalParser {
    private static final String SEP = "-";

    @Override
    public TerminalRequest parse(String code) {
        String[] arr = code.split(" ");
        String terminal = arr[0];
        TerminalRequest request = new TerminalRequest();
        request.setTerminal(terminal);
        List<SubTerminalRequest> subRequests = null;
        if (arr.length > 1) {
            String subTerminal = null;
            String[] param;
            subRequests = new ArrayList<>();
            boolean haveSub = false;
            if (arr[1].contains(SEP)) {//sub cn.fay.terminal
                haveSub = true;
                subTerminal = arr[1].trim().substring(SEP.length()).toLowerCase();
            }
            param = new String[arr.length - (haveSub ? 2 : 1)];
            if (param.length > 0) {
                System.arraycopy(arr, (haveSub ? 2 : 1), param, 0, param.length);
            }
            SubTerminalRequest subRequest = new SubTerminalRequest();
            subRequest.setSubTerminal(subTerminal);
            subRequest.setParams(param);
            subRequests.add(subRequest);
        }
        request.setSubTerminals(subRequests);
        return request;
    }
}
