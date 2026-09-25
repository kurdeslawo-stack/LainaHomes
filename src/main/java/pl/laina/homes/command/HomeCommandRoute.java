package pl.laina.homes.command;

public final class HomeCommandRoute {
    private HomeCommandRoute() {
    }

    public static boolean opensGui(String rawMessage) {
        return rawMessage != null && rawMessage.strip().equalsIgnoreCase("/home");
    }
}
