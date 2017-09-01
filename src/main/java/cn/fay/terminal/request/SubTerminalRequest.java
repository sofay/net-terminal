package cn.fay.terminal.request;

import lombok.Data;

@Data
public class SubTerminalRequest {
    private String subTerminal;
    private String[] params;

    @Override
    public String toString() {
        return subTerminal;
    }
}
