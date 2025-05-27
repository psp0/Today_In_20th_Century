package place.run.mep.century20.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class UserAuth {
    @Id
    @Column(name = "user_no")
    private Long userNo;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_no")  
    @MapsId
    private User user;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "pw_changed", nullable = true)
    private LocalDateTime pwChanged;



    // Add a default constructor for JPA
    public UserAuth() {
        // Default constructor required by JPA
    }

    public UserAuth(User user) {
        this.user = user;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
