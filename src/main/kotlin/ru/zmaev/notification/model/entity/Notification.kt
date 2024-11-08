package ru.zmaev.notification.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "notification")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_id_seq")
    @SequenceGenerator(
        name = "notification_id_seq",
        sequenceName = "notification_id_seq",
        allocationSize = 1
    )
    val id: Long? = null,
    @Column(name = "build_id")
    val buildId: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_id", referencedColumnName = "id")
    val subscription: Subscription? = null
)