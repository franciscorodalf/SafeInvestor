package es.franciscorodalf.safeinvestor.bancos.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/** Cuenta bancaria descubierta dentro de una {@link BankConnection}. */
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "connection_id", nullable = false)
    private BankConnection connection;

    /** ID de la cuenta en GoCardless. Se usa para llamar a /accounts/{id}/transactions/. */
    @Column(name = "external_id", nullable = false, unique = true, length = 64)
    private String externalId;

    @Column(length = 50)
    private String iban;

    @Column(length = 200)
    private String nombre;

    @Column(length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    protected BankAccount() {}

    public BankAccount(String externalId, String iban, String nombre, String currency) {
        this.externalId = externalId;
        this.iban = iban;
        this.nombre = nombre;
        this.currency = currency;
    }

    public Long getId() { return id; }
    public BankConnection getConnection() { return connection; }
    public void setConnection(BankConnection c) { this.connection = c; }
    public String getExternalId() { return externalId; }
    public String getIban() { return iban; }
    public String getNombre() { return nombre; }
    public String getCurrency() { return currency; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
