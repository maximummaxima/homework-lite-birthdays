package academy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class ApplicationTest {

    @Test
    @DisplayName("CLI: --help должен завершаться успешно")
    void helpShouldExitWithZero() {
        int exitCode = new CommandLine(new Application()).execute("--help");
        assertEquals(0, exitCode);
    }
}
