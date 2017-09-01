package cn.fay.terminal.request;

import lombok.Data;

import java.util.List;

@Data
public class TerminalRequest {
    private String terminal;
    private String[] param;
    private List<SubTerminalRequest> subTerminals;
}
