package com.splitms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private int transactionId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "settled", nullable = false)
    private boolean settled;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_txn_group"))
    private GroupsEntity group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_txn_from_user"))
    private UserEntity fromUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_txn_to_user"))
    private UserEntity toUser;

    protected TransactionEntity() {
    }

    public TransactionEntity(GroupsEntity group, UserEntity fromUser, UserEntity toUser,
            BigDecimal amount, LocalDate transactionDate) {
        this.group = group;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.settled = false;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public boolean isSettled() {
        return settled;
    }

    public GroupsEntity getGroup() {
        return group;
    }

    public UserEntity getFromUser() {
        return fromUser;
    }

    public UserEntity getToUser() {
        return toUser;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }
}
