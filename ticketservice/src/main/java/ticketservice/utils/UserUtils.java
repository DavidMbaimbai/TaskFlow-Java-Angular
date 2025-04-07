package ticketservice.utils;
import ticketservice.constant.Roles;
import ticketservice.model.User;

import java.util.Objects;
import java.util.function.Function;
public class UserUtils {
    public static Function<User, Boolean> hasElevatedPermissions = user -> Objects.equals(user.getRole(), Roles.TECH_SUPPORT) || Objects.equals(user.getRole(), Roles.MANAGER) || Objects.equals(user.getRole(), Roles.ADMIN) || Objects.equals(user.getRole(), Roles.SUPER_ADMIN);
}