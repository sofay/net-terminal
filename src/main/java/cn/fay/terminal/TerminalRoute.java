package cn.fay.terminal;


import cn.fay.terminal.request.TerminalRequest;

public class TerminalRoute {
    private static TerminalRoute route = new TerminalRoute();

    private TerminalRoute(){}

    public static TerminalRoute getRoute() {
        return route;
    }

    public String runTerminal(TerminalRequest request) {
        Command command = TerminalContext.getCommand(request.getTerminal());
        if (command == null) {
            throw new RuntimeException("command not found:" + request.getTerminal());
        }
        return command.handler(request);
    }
}
