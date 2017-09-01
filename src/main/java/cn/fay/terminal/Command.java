package cn.fay.terminal;


import cn.fay.terminal.request.SubTerminalRequest;
import cn.fay.terminal.request.TerminalRequest;

public interface Command{
    String handler(TerminalRequest request);

    String execute(TerminalRequest request);

    String subExecute(String unDecorate, SubTerminalRequest subRequest);
}
