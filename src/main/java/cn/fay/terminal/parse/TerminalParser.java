package cn.fay.terminal.parse;


import cn.fay.terminal.request.TerminalRequest;

public interface TerminalParser{
    TerminalRequest parse(String code);
}
