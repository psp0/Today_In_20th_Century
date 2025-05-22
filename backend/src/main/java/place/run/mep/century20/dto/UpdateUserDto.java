package place.run.mep.century20.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class UpdateUserDto {
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Size(max = 20, message = "닉네임은 20자 이내여야 합니다.")
    private String nickname;

    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phone;

    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    private String name;

    private String birthDate;

    @Pattern(regexp = "^(MALE|FEMALE)$", message = "성별은 MALE 또는 FEMALE만 허용됩니다.")
    private String gender;

    @Builder
    public UpdateUserDto(String email, String nickname, String phone, String name, String birthDate, String gender) {
        this.email = email;
        this.nickname = nickname;
        this.phone = phone;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
