package cn.fay.terminal;


import cn.fay.terminal.request.SubTerminalRequest;
import cn.fay.terminal.request.TerminalRequest;
import com.alibaba.fastjson.JSON;

import java.util.Map;

public abstract class AbstractCommand implements Command {
    private boolean allowNoSub = false;//allow no sub cn.fay.terminal

    public final String handler(TerminalRequest request) {
        Map<String, Command> subTerminals = TerminalContext.getSubTerminal(request.getTerminal());
        if (!getAllowNoSub()) {//need sub cn.fay.terminal
            if (request.getSubTerminals() == null || request.getSubTerminals().size() == 0) {//does not input sub
                throw new RuntimeException(String.format("{}: illegal operator with no sub cn.fay.terminal", request.getTerminal()));
            } else {
                for (SubTerminalRequest subRequest : request.getSubTerminals()) {
                    if (subTerminals != null && !subTerminals.containsKey(subRequest.getSubTerminal())) {//sub not suit
                        throw new RuntimeException(notFoundSubTermLog(subTerminals, request.getTerminal(), subRequest.getSubTerminal()));
                    }
                }
            }
        }
        if (subTerminals != null && subTerminals.size() > 0) {
            StringBuilder result = new StringBuilder();
            for (SubTerminalRequest subRequest : request.getSubTerminals()) {
                result.append(subTerminals.get(subRequest.getSubTerminal()).subExecute(result.toString(), subRequest));
            }
            return result.toString();
        }
        return execute(request);
    }

    @Override
    public String execute(TerminalRequest request) {
        throw new RuntimeException("please set allowNoSub [yes] or override those execute function:" + request.getTerminal());
    }

    @Override
    public String subExecute(String unDecorate, SubTerminalRequest subRequest) {
        throw new RuntimeException("please override the sub terminals:" + subRequest.getSubTerminal());
    }

    public boolean getAllowNoSub() {
        return allowNoSub;
    }

    public void setAllowNoSub(boolean allNoSub) {
        this.allowNoSub = allNoSub;
    }

    protected String notFoundSubTermLog(Map<String, Command> subCommands, String superTerm, String subTerm) {
        Object[] params = new Object[2 + subCommands.size()];
        params[0] = superTerm;
        params[1] = subTerm;
        StringBuilder log = new StringBuilder("{}: illegal option {}.===>>> usage:[");
        int index = 2;
        String sepa = " | ";
        for (String term : subCommands.keySet()) {
            log.append("-").append(term).append(sepa);
            params[index++] = term;
        }
        log.delete(log.length() - sepa.length(), log.length());
        log.append("]");
        return String.format(log.toString(), params);
    }

    /**
     * return the only parameter
     * @param subRequest
     * @return
     */
    protected String ensureOneParam(SubTerminalRequest subRequest) {
        if (subRequest.getParams() == null || subRequest.getParams().length != 1) {
            throw new RuntimeException("error param:" + JSON.toJSONString(subRequest));
        }
        return subRequest.getParams()[0];
    }

}
