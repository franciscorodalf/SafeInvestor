package es.franciscorodalf.safeinvestor.auth.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(Usuario usuario, String token, OffsetDateTime expiresAt) {
        this.usuario = usuario;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(OffsetDateTime now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed(OffsetDateTime when) {
        this.usedAt = when;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getToken() { return token; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public OffsetDateTime getUsedAt() { return usedAt; }
}
