package healthy.lifestyle.backend.activities.repository;

import healthy.lifestyle.backend.users.repository.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "http_refs", unique = false, nullable = true)
    private String httpRefs;

    @Column(name = "is_active", unique = false, nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user; // FOREIGN KEY(user_id) REFERENCES users(id)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private ActivityType type; // FOREIGN KEY(type_id) REFERENCES activity_types(id)
}
