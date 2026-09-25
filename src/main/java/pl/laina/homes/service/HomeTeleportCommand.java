package pl.laina.homes.service;

public final class HomeTeleportCommand {
    private HomeTeleportCommand() {
    }

    public static String forHome(String homeName) {
        if (homeName == null || homeName.isBlank() || homeName.chars().anyMatch(Character::isWhitespace)
                || homeName.chars().anyMatch(Character::isISOControl)
                || homeName.indexOf(':') >= 0 || homeName.indexOf('/') >= 0 || homeName.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("Unsafe home name");
        }
        return "home " + homeName;
    }
}
