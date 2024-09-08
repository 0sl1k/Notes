package ua.glek.notes.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    public Token(String token, String username, Boolean active, Date createdDate, Date expiryDate) {
        this.token = token;
        this.username = username;
        this.isActive = active;
        this.createdDate = createdDate;
        this.expiryDate = expiryDate;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;
    private String username;
    private boolean isActive;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;


}
