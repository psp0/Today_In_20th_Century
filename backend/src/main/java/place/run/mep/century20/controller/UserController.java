package place.run.mep.century20;

import place.run.mep.century20.dto.UpdateUserDto;
import place.run.mep.century20.dto.UserInfoDto;
import place.run.mep.century20.dto.PasswordChangeDto;
import place.run.mep.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserInfo(userDetails.getUsername()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserInfoDto> updateUser(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody UpdateUserDto updateUserDto) {
        return ResponseEntity.ok(userService.updateUser(userDetails.getUsername(), updateUserDto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody PasswordChangeDto dto) {
        userService.updatePassword(userDetails.getUsername(), dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }
}
