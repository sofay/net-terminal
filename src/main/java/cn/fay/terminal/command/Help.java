package cn.fay.terminal.command;

import cn.fay.terminal.AbstractCommand;
import cn.fay.terminal.annotation.Terminal;
import cn.fay.terminal.request.SubTerminalRequest;
import cn.fay.terminal.request.TerminalRequest;

@Terminal("help")
public class Help extends AbstractCommand {

    @Override
    public boolean getAllowNoSub() {
        return true;
    }

    @Override
    public String execute(TerminalRequest request) {
        return "help command run.";
    }

    @Override
    public String subExecute(String unDecorate, SubTerminalRequest subRequest) {
        return "help sub command run [" + subRequest.getSubTerminal() + "]";
    }
}
