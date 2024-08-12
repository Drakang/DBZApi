package API;

import java.util.ArrayList;
import java.util.List;

public class Logs {
    private List<String> logs = new ArrayList<>();
    private boolean debugMode = false;

    public void addLog(String log) {
        if (debugMode) {
            logs.add(log);
        }
    }

    public void clearLogs() {
        logs = new ArrayList<>();
    }

    public int getLogsCount() {
        return logs.size();
    }

    public String getLog(int pos) {
        return logs.get(pos);
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
