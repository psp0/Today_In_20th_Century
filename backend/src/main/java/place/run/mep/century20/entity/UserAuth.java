package place.run.mep.century20;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_auth")
public class UserAuth {
    @Id
    @Column(name = "user_no")
    private Long userNo;

    @OneToOne
    @MapsId // This ensures that user_no is taken from the User entity
    @JoinColumn(name = "user_no")
    private User user;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "pw_changed", nullable = false)
    private LocalDateTime pwChanged;

    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_no"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
