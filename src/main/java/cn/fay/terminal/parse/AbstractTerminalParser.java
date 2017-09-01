package cn.fay.terminal.parse;


import cn.fay.terminal.request.TerminalRequest;

public abstract class AbstractTerminalParser implements TerminalParser {
    public abstract TerminalRequest parse(String code);
}
