import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Deadline extends Task {
    private final String name;
    private boolean isCompleted;
    private final LocalDateTime deadline;
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy HHmm");

    /**
     * Creates a Deadline object with a name as its description and a LocalDateTime. Assumes
     * that Deadline is incomplete by definition.
     * @param name Name of Deadline
     * @param deadline LocalDateTime object of Deadline
     */
    public Deadline(String name, LocalDateTime deadline) {
        this.name = name;
        this.isCompleted = false;
        this.deadline = deadline;
    }

    /**
     * Constructor for Deadline with all three inputs - name, deadline and isCompleted.
     * @param name Name of Deadline
     * @param deadline LocalDateTime object of Deadline
     * @param isCompleted Boolean value if Deadline is completed or not.
     */
    public Deadline(String name, LocalDateTime deadline, boolean isCompleted) {
        this.name = name;
        this.isCompleted = isCompleted;
        this.deadline = deadline;
    }

    public void makeCompleted() {
        this.isCompleted = true;
    }

    public String getName() {
        return this.name;
    }

    public boolean getCompletion() {
        return this.isCompleted;
    }

    public LocalDateTime getDeadline() {
        return this.deadline;
    }

    /**
     * Outputs Deadline Object as a string for text files.
     * @return String to be appended to text files
     */
    public String writeFormat() {
        StringBuilder output = new StringBuilder("D|" + name + "|" + deadline + "|" + (isCompleted ? "1" : "0"));
        if (this.getTags().size() > 0) {
            output.append("|");
            this.getTags().forEach(x -> output.append("#").append(x));
        }
        return output.toString();
    }

    /**
     * Reads string formatted to store info regarding a Deadline object.
     * @param input input string to be parsed
     * @return Deadline object parsed from string information
     */
    public static Deadline readFormat(String input) {
        assert input.charAt(0) == 'D';
        List<String> list = Collections.list(new StringTokenizer(input, "|")).stream()
                .map(token -> (String) token)
                .collect(Collectors.toList());
        Deadline deadline = new Deadline(list.get(1), LocalDateTime.parse(list.get(2)),
                Boolean.parseBoolean(list.get(3)));
        if (list.size() == 5) {
            List<String> tags = Collections.list(new StringTokenizer(list.get(4), "#")).stream()
                    .map(token -> (String) token)
                    .collect(Collectors.toList());
            tags.remove(0);
            tags.forEach(deadline::addTag);
        }
        return deadline;
    }

    @Override
    public String toString() {
        return "[D][" + (isCompleted ? "\u2713" : "\u2717") + "] " + name + " (by: "  // unicode is tick and cross
                + deadline.format(formatter) + ") " + super.toString();
    }
}
