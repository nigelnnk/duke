import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoResponder {
    private final List<Task> taskList;
    private final StringBuilder toPrint;
    static Pattern pDeadline = Pattern.compile("deadline (.+) /by (.+)");
    static Pattern pEvent = Pattern.compile("event (.+) /at (.+)");
    static Pattern pTodo = Pattern.compile("todo (.+)");
    static Pattern pDone = Pattern.compile("done (\\d+)");
    static Pattern pDelete = Pattern.compile("delete (\\d+)");
    static Pattern pEmptyCommand = Pattern.compile("(todo|event|deadline)\\s*$");
    static Pattern pList = Pattern.compile("list\\s*$");
    static Pattern pSave = Pattern.compile("save\\s*$");


    private AutoResponder() {
        this.taskList = new ArrayList<>();
        this.toPrint = new StringBuilder();
    }

    private AutoResponder(List<Task> taskList, StringBuilder toPrint) {
        this.taskList = taskList;
        this.toPrint = toPrint;
    }

    public AutoResponder printToConsole() {
        System.out.print(toPrint);
        return new AutoResponder(taskList, new StringBuilder());
    }

    public AutoResponder readInput(String input) throws IOException {
        String lowerInput = input.toLowerCase();
        if (pList.matcher(lowerInput).find()) {
            return this.processList();
        } else if (pDone.matcher(lowerInput).find()) {
            Matcher m = pDone.matcher(lowerInput);
            m.find();
            int index = Integer.parseInt(m.group(1)) - 1;
            return this.markTaskDone(index);
        } else if (pDelete.matcher(lowerInput).find()) {
            Matcher m = pDelete.matcher(lowerInput);
            m.find();
            int index = Integer.parseInt(m.group(1)) - 1;
            return this.deleteTask(index);
        } else if (pSave.matcher(lowerInput).find()) {
            return this.saveList();
        } else if (pEmptyCommand.matcher(lowerInput).find()) {
            Matcher m = pEmptyCommand.matcher(lowerInput);
            m.find();
            throw new IllegalArgumentException("☹ OOPS!!! The description of a "
                    + m.group(1) + " cannot be empty.");
        } else if (pDeadline.matcher(input).find()) {
            Matcher m = pDeadline.matcher(input);
            m.find();
            return this.addDeadline(m.group(1), m.group(2));
        } else if (pEvent.matcher(input).find()) {
            Matcher m = pEvent.matcher(input);
            m.find();
            return this.addEvent(m.group(1), m.group(2));
        } else if (pTodo.matcher(input).find()) {
            Matcher m = pTodo.matcher(input);
            m.find();
            return this.addTodo(m.group(1));
        } else {
            throw new IllegalArgumentException("☹ OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
    }

    private AutoResponder processList() {
        StringBuilder sb = new StringBuilder(toPrint);
        for (int i = 1; i <= taskList.size(); ++i) {
            sb.append(i).append(". ").append(taskList.get(i - 1)).append("\n");
        }
        return new AutoResponder(taskList, sb).printToConsole();
    }

    private AutoResponder saveList() throws IOException {
        StringBuilder writeContents = new StringBuilder();
        for (Task v : taskList) {
            writeContents.append(v.writeFormat());
            writeContents.append("\n");
        }
        if (!Files.exists(Paths.get("./data"))) {
            boolean ok = new File("./data").mkdir();
            if (!ok) {
                System.err.println("Error in creating directory.\n");
            }
        }
        File f = new File("./data/duke.txt");
        String path = f.getAbsolutePath();
        FileWriter fw = new FileWriter(path);
        fw.write(writeContents.toString());
        fw.close();
        toPrint.append("Tasks saved successfully.\n");
        return this.printToConsole();
    }

    private AutoResponder loadList() {
        try {
            File f = new File("./data/duke.txt");
            Scanner sc = new Scanner(f);
            String s;
            while (sc.hasNextLine()) {
                s = sc.nextLine();
                switch (s.charAt(0)) {
                    case 'T':
                        taskList.add(Todo.readFormat(s));
                        break;
                    case 'D':
                        taskList.add(Deadline.readFormat(s));
                        break;
                    case 'E':
                        taskList.add(Event.readFormat(s));
                        break;
                    default:
                        throw new IllegalArgumentException("No corresponding command found");
                }
            }
            toPrint.append("Task file loaded successfully!\n");
        } catch (Exception e) {
            toPrint.append("Task file not loaded. Check if file exists or is corrupted.\n");
        }
        return this.printToConsole();
    }

    private AutoResponder markTaskDone(int index) {
        StringBuilder sb = new StringBuilder(toPrint);
        List<Task> tl = new ArrayList<>(taskList);
        if (index < 0 || index >= taskList.size()) {
            throw new IndexOutOfBoundsException("Index of " + (index + 1)
                    + " does not correspond to task list of size " + taskList.size());
        } else {
            tl.set(index, taskList.get(index).makeCompleted());
            sb.append("Nice! I've marked this task as done:\n\t");
            sb.append(tl.get(index)).append("\n");
        }
        return new AutoResponder(tl, sb).printToConsole();
    }

    private AutoResponder deleteTask(int index) {
        StringBuilder sb = new StringBuilder(toPrint);
        List<Task> tl = new ArrayList<>(taskList);
        if (index < 0 || index >= taskList.size()) {
            throw new IndexOutOfBoundsException("Index of " + (index + 1)
                    + " does not correspond to task list of size " + taskList.size());
        } else {
            sb.append("Noted! I've removed this task:\n\t");
            sb.append(tl.get(index)).append("\n");
            tl.remove(index);
        }
        return new AutoResponder(tl, sb).printToConsole();
    }

    private AutoResponder addDeadline(String name, String date) {
        List<Task> tl = new ArrayList<>(taskList);
        tl.add(new Deadline(name, AutoResponder.parseDateTime(date)));
        return new AutoResponder(tl, toPrint).taskAdded();
    }

    private AutoResponder addEvent(String name, String date) {
        List<Task> tl = new ArrayList<>(taskList);
        tl.add(new Event(name, AutoResponder.parseDateTime(date)));
        return new AutoResponder(tl, toPrint).taskAdded();
    }

    private AutoResponder addTodo(String name) {
        List<Task> tl = new ArrayList<>(taskList);
        tl.add(new Todo(name));
        return new AutoResponder(tl, toPrint).taskAdded();
    }

    private AutoResponder taskAdded() {
        StringBuilder sb = new StringBuilder(toPrint);
        sb.append("Got it. I've added this task:\n\t");
        sb.append(taskList.get(taskList.size() - 1));
        sb.append("\nNow you have ").append(taskList.size()).append(" tasks in the list.\n");
        return new AutoResponder(taskList, sb).printToConsole();
    }

    public static AutoResponder initialise() {
        AutoResponder ar = new AutoResponder();
        return ar.loadList();
    }

    public static LocalDateTime parseDateTime(String s) {
        Pattern patternDateTime = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s+\\d{4}");
        Pattern patternDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

        if (patternDateTime.matcher(s).find()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
            return LocalDateTime.parse(s, formatter);
        } else if (patternDate.matcher(s).find()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
            return LocalDate.parse(s, formatter).atStartOfDay();
        }
        throw new UnsupportedOperationException("Supplied string is not of proper format.");
    }

}

