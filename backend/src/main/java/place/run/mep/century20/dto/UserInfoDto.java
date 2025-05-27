package place.run.mep.century20.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserInfoDto {
    private String userId;
    private String email;
    private String phone;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String gender;
}
