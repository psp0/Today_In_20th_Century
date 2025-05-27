package place.run.mep.century20.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserDto {
    @Size(max = 20, message = "닉네임은 20자 이내여야 합니다.")
    private String nickname;

    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phone;

    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private String birthDate;

    @Pattern(regexp = "^(M|F)$", message = "성별은 M 또는 F만 허용됩니다.")
    private String gender;



    @Builder
    public UpdateUserDto(String nickname, String phone, String name, String email, String birthDate, String gender) {
        this.nickname = nickname;
        this.phone = phone;
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
