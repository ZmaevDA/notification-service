package ru.zmaev.notification.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "subscription")
data class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscription_id_seq")
    @SequenceGenerator(
        name = "subscription_id_seq",
        sequenceName = "subscription_id_seq",
        allocationSize = 1
    )
    val id: Long? = null,
    @Column(name = "subscriber_id")
    val subscriberId: String? = null,
    @Column(name = "subscribed_at_id")
    val subscribedAtId: String? = null,
    @Column(name = "subscriber_email")
    var subscriberEmail: String? = null,
    @Column(name = "subscribed_at_username")
    var subscribedAtUsername: String? = null,
    @OneToMany(mappedBy = "subscription", cascade = [CascadeType.REMOVE])
    val notifications: Set<Notification>? = null
)
