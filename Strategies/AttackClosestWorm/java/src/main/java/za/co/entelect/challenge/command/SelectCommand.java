package za.co.entelect.challenge.command;

public class SelectCommand implements Command {

    private final int id;
    private final String command;

    public SelectCommand(int id, String command) {
        this.id = id;
        this.command = command;
    }

    @Override
    public String render() {
        return "select " + id + ";" + command;
    }
}