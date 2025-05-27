package place.run.mep.century20.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDto {
    @NotBlank(message = "현재 비밀번호는 필수 입력값입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수 입력값입니다.")
    private String confirmPassword;
}
