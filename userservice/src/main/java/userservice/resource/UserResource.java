package userservice.resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import userservice.domain.Response;
import userservice.dtorequest.PasswordRequest;
import userservice.dtorequest.ResetPasswordRequest;
import userservice.dtorequest.RoleRequest;
import userservice.dtorequest.UserRequest;
import userservice.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;


import static java.util.Collections.emptyMap;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static userservice.constant.Constants.PHOTO_DIRECTORY;
import static userservice.utils.RequestUtils.getResponse;
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserResource {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody UserRequest user, HttpServletRequest request) {
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getUsername(), user.getPassword());
        return created(getUri()).body(getResponse(request, emptyMap(), "Account created. Check your email to enable your account", CREATED));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("token") String token, HttpServletRequest request) {
        userService.verifyAccount(token);
        return ok(getResponse(request, emptyMap(), "Account verified. You may login now", OK));
    }

    @PatchMapping("/mfa/enable")
    public ResponseEntity<Response> enableMfa(@NotNull Authentication authentication, HttpServletRequest request) {
        var user = userService.enableMfa(authentication.getName());
        return ok(getResponse(request, of("user", user), "2FA enabled successfully", OK));
    }

    @PatchMapping("/mfa/disable")
    public ResponseEntity<Response> disableMfa(@NotNull Authentication authentication, HttpServletRequest request) {
        var user = userService.disableMfa(authentication.getName());
        return ok(getResponse(request, of("user", user), "2FA disabled successfully", OK));
    }

    @GetMapping("/profile")
    public ResponseEntity<Response> profile(@NotNull Authentication authentication, HttpServletRequest request) {
        var user = userService.getUserByUuid(authentication.getName());
        var devices = userService.getDevices(authentication.getName());
        return ok(getResponse(request, of("user", user, "devices", devices), "Profile retrieved", OK));
    }

    @GetMapping("/{userUuid}")
    public ResponseEntity<Response> getUserByUuid(@NotNull Authentication authentication, @PathVariable("userUuid") String userUuid, HttpServletRequest request) {
        var user = userService.getUserByUuid(userUuid);
        return ok(getResponse(request, of("user", user), "Profile retrieved", OK));
    }

    @GetMapping("/assignee/{ticketUuid}")
    public ResponseEntity<Response> getAssigneeByUuid(@NotNull Authentication authentication, @PathVariable("ticketUuid") String ticketUuid, HttpServletRequest request) {
        var user = userService.getAssignee(ticketUuid);
        return ok(getResponse(request, of("user", user), "Profile retrieved", OK));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<Response> getUserByEmail(@NotNull Authentication authentication, @PathVariable("email") String email, HttpServletRequest request) {
        var user = userService.getUserByEmail(email);
        return ok(getResponse(request, of("user", user), "Profile retrieved", OK));
    }

    @GetMapping("/credential/{userUuid}")
    public ResponseEntity<Response> getCredential(@NotNull Authentication authentication, @PathVariable("userUuid") String userUuid, HttpServletRequest request) {
        var credential = userService.getCredential(userUuid);
        return ok(getResponse(request, of("credential", credential), "Profile retrieved", OK));
    }

    @PatchMapping("/update")
    public ResponseEntity<Response> updateUser(@NotNull Authentication authentication, @RequestBody UserRequest user, HttpServletRequest request) {
        var updatedUser = userService.updateUser(authentication.getName(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), user.getBio(), user.getAddress());
        return ok(getResponse(request, of("user", updatedUser), "User updated successfully", OK));
    }

    @PatchMapping("/updaterole")
    public ResponseEntity<Response> updateRole(@NotNull Authentication authentication, @RequestBody RoleRequest roleRequest, HttpServletRequest request) {
        var updatedUser = userService.updateRole(authentication.getName(), roleRequest.getRole());
        return ok(getResponse(request, of("user", updatedUser), "User updated successfully", OK));
    }

    @PatchMapping("/toggleaccountexpired")
    public ResponseEntity<Response> toggleaccountexpired(@NotNull Authentication authentication, HttpServletRequest request) {
        var user = userService.toggleAccountExpired(authentication.getName());
        return ok(getResponse(request, of("user", user), "User updated successfully", OK));
    }

    @PatchMapping("/toggleaccountlocked")
    public ResponseEntity<Response> toggleaccountlocked(@NotNull Authentication authentication, HttpServletRequest request) {
        var user = userService.toggleAccountLocked(authentication.getName());
        return ok(getResponse(request, of("user", user), "User updated successfully", OK));
    }

    @PatchMapping("/toggleaccountenabled")
    public ResponseEntity<Response> toggleaccountenabled(@NotNull Authentication authentication, HttpServletRequest request) {
        var user = userService.toggleAccountEnabled(authentication.getName());
        return ok(getResponse(request, of("user", user), "User updated successfully", OK));
    }

    //When user IS logged in
    @PatchMapping("/updatepassword")
    public ResponseEntity<Response> updatePassword(@NotNull Authentication authentication, @RequestBody PasswordRequest passwordRequest, HttpServletRequest request) {
        userService.updatePassword(authentication.getName(), passwordRequest.getCurrentPassword(), passwordRequest.getNewPassword(), passwordRequest.getConfirmPassword());
        return ok(getResponse(request, emptyMap(), "Password updated successfully", OK));
    }

    //When user is NOT logged in
    @PostMapping("/resetpassword")
    public ResponseEntity<Response> resetPassword(@RequestParam("email") String email, HttpServletRequest request) {
        userService.resetPassword(email);
        return ok(getResponse(request, emptyMap(), "We sent you an email for you to reset your password", OK));
    }

    //When user is NOT logged in
    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyPassword(@RequestParam("token") String token, HttpServletRequest request) {
        var user = userService.verifyPasswordToken(token);
        return ok(getResponse(request, of("user", user), "Enter your new password", OK));
    }

    //When user is NOT logged in
    @PostMapping("/resetpassword/reset")
    public ResponseEntity<Response> doResetPassword(@RequestBody ResetPasswordRequest passwordRequest, HttpServletRequest request) {
        userService.doResetPassword(passwordRequest.getUserUuid(), passwordRequest.getToken(), passwordRequest.getPassword(), passwordRequest.getConfirmPassword());
        return ok(getResponse(request, emptyMap(), "Password reset successfully. You may log in now", OK));
    }

    @GetMapping("/list")
    public ResponseEntity<Response> getUsers(@NotNull Authentication authentication, HttpServletRequest request) {
        return ok(getResponse(request, of("users", userService.getUsers()), "Users retrieved", OK));
    }

    @PatchMapping("/photo")
    public ResponseEntity<Response> uploadPhoto(@NotNull Authentication authentication, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        var user = userService.uploadPhoto(authentication.getName(), file);
        return ok(getResponse(request, of("users", user), "Photo updated successfully", OK));
    }

    @GetMapping("/image/{filename}")
    public byte [] getPhoto(@PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(PHOTO_DIRECTORY + filename));
    }


    private URI getUri() {
        return URI.create("/user/profile/userId");
    }

}